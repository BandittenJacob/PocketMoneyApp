import * as functions from "firebase-functions";
import * as admin from "firebase-admin";

admin.initializeApp();
const db = admin.firestore();
const CHANNEL_ID = "pocketmoney_notifications";

// ─── Helpers ────────────────────────────────────────────────────────────────

/** Get the stored FCM token for a Firebase Auth user. */
async function getToken(userId: string): Promise<string | null> {
  const doc = await db.collection("users").doc(userId).get();
  const token = doc.data()?.fcmToken;
  return typeof token === "string" && token.length > 0 ? token : null;
}

/** Get FCM tokens for all PARENT members of a family. */
async function getParentTokens(familyId: string): Promise<string[]> {
  const snap = await db
    .collection("family_members")
    .where("familyId", "==", familyId)
    .where("role", "==", "PARENT")
    .get();
  const tokens: string[] = [];
  for (const doc of snap.docs) {
    const token = await getToken(doc.data().userId);
    if (token) tokens.push(token);
  }
  return tokens;
}

/** Get FCM tokens for all CHILD members of a family. */
async function getChildTokens(familyId: string): Promise<string[]> {
  const snap = await db
    .collection("family_members")
    .where("familyId", "==", familyId)
    .where("role", "==", "CHILD")
    .get();
  const tokens: string[] = [];
  for (const doc of snap.docs) {
    const token = await getToken(doc.data().userId);
    if (token) tokens.push(token);
  }
  return tokens;
}

/** Send a notification to one or more FCM tokens. */
async function sendNotification(
  tokens: string[],
  title: string,
  body: string
): Promise<void> {
  if (tokens.length === 0) return;
  await admin.messaging().sendEachForMulticast({
    tokens,
    notification: { title, body },
    android: {
      priority: "high",
      notification: { channelId: CHANNEL_ID },
    },
  });
}

// ─── Trigger 1: Child submits a proposal → notify all parents ───────────────

export const onProposalCreated = functions.firestore
  .document("choreProposals/{proposalId}")
  .onCreate(async (snap) => {
    const proposal = snap.data();
    if (!proposal) return;

    const tokens = await getParentTokens(proposal.familyId);
    await sendNotification(
      tokens,
      "New Chore Suggestion 💡",
      `${proposal.proposedByName} suggested: "${proposal.name}"`
    );
  });

// ─── Trigger 2: Parent resolves a proposal → notify the child ───────────────

export const onProposalResolved = functions.firestore
  .document("choreProposals/{proposalId}")
  .onUpdate(async (change) => {
    const before = change.before.data();
    const after = change.after.data();
    if (!before || !after) return;
    // Only act when transitioning away from PENDING
    if (before.status !== "PENDING" || after.status === "PENDING") return;

    const token = await getToken(after.proposedByMemberId);
    if (!token) return;

    let title: string;
    let body: string;

    if (after.status === "ACCEPTED") {
      title = "Chore Suggestion Accepted! ✅";
      body = `Your suggestion "${after.name}" was accepted — it's now on the chores list.`;
    } else if (after.status === "ACCEPTED_WITH_EDITS") {
      title = "Chore Suggestion Accepted ✏️";
      body = `Your suggestion "${after.name}" was accepted with some changes.`;
    } else if (after.status === "REJECTED") {
      title = "Chore Suggestion Declined";
      body =
        after.parentNote && after.parentNote.length > 0
          ? `"${after.name}" was declined: ${after.parentNote}`
          : `Your suggestion "${after.name}" was not accepted this time.`;
    } else {
      return;
    }

    await sendNotification([token], title, body);
  });

// ─── Trigger 3: Parent creates a chore → notify child(ren) ──────────────────

export const onChoreCreated = functions.firestore
  .document("chores/{choreId}")
  .onCreate(async (snap) => {
    const chore = snap.data();
    if (!chore) return;

    const valueFmt = `${chore.value.toFixed(2)} kr.`;

    if (chore.assignedToUserId && chore.assignedToUserId.length > 0) {
      // Assigned to a specific child
      const token = await getToken(chore.assignedToUserId);
      if (!token) return;
      await sendNotification(
        [token],
        "New Chore Assigned 📋",
        `You have a new chore: "${chore.name}" (${valueFmt})`
      );
    } else {
      // Unassigned — notify all children
      const tokens = await getChildTokens(chore.familyId);
      await sendNotification(
        tokens,
        "New Chore Available 🎯",
        `A new chore is available: "${chore.name}" (${valueFmt})`
      );
    }
  });

// ─── Trigger 4: Chore status changed ────────────────────────────────────────

export const onChoreStatusChanged = functions.firestore
  .document("chores/{choreId}")
  .onUpdate(async (change) => {
    const before = change.before.data();
    const after = change.after.data();
    if (!before || !after) return;
    if (before.status === after.status) return;

    const valueFmt = `${after.value.toFixed(2)} kr.`;

    if (after.status === "COMPLETED") {
      // Child marked a chore complete → notify parents
      const tokens = await getParentTokens(after.familyId);
      const who = after.assignedTo || "Someone";
      await sendNotification(
        tokens,
        "Chore Completed ⏳",
        `${who} completed "${after.name}" — tap to approve`
      );
    } else if (after.status === "APPROVED") {
      // Parent approved → notify the child
      if (!after.assignedToUserId) return;
      const token = await getToken(after.assignedToUserId);
      if (!token) return;
      await sendNotification(
        [token],
        "Chore Approved! 🎉",
        `"${after.name}" was approved — ${valueFmt} added to your balance`
      );
    } else if (after.status === "PAID") {
      // Parent paid → notify the child
      if (!after.assignedToUserId) return;
      const token = await getToken(after.assignedToUserId);
      if (!token) return;
      await sendNotification(
        [token],
        "Money Paid! 💰",
        `${valueFmt} for "${after.name}" has been paid`
      );
    }
  });

// ─── Trigger 5: Family deleted → delete all member Auth accounts ─────────────

export const onFamilyDeleted = functions.firestore
  .document("families/{familyId}")
  .onDelete(async (snap) => {
    const data = snap.data();
    if (!data) return;

    const memberAuthUids: string[] = data.memberAuthUids ?? [];
    if (memberAuthUids.length === 0) return;

    for (const uid of memberAuthUids) {
      try {
        await admin.auth().deleteUser(uid);
        functions.logger.info(`Deleted Auth account for uid: ${uid}`);
      } catch (err: any) {
        if (err?.code === "auth/user-not-found") {
          // Already deleted (e.g. the user who initiated deletion deleted their own account on-device)
          functions.logger.info(`Auth account already deleted for uid: ${uid}`);
        } else {
          functions.logger.error(`Failed to delete Auth account for uid: ${uid}`, err);
        }
      }
    }
  });
