# Known Issues & Future Enhancements

This document tracks known limitations and planned improvements for the PocketMoneyApp.

---

## 🔴 Known Issues

### Firebase Auth Account Deletion (Family Deletion)

**Status:** ✅ Fixed — Cloud Function deployed (April 27, 2026)  
**Priority:** Medium (affects production deployment)  
**Affects:** Family deletion feature

#### Issue Description

When a family is deleted, all Firestore data is successfully removed:
- ✅ Family document deleted
- ✅ User documents deleted
- ✅ Chores, templates, transactions deleted
- ✅ Family members deleted
- ✅ Current user's Firebase Auth account deleted

**However:**
- ❌ **Other family members' Firebase Auth accounts remain**

This happens because Firebase security prevents deleting Auth accounts from a different user session. Only the currently authenticated user can delete their own Auth account.

#### Current Workaround (Testing)

For testing/development, manually delete orphaned Auth accounts:
1. Open **Firebase Console** → **Authentication**
2. Find users with UIDs that match deleted user documents
3. Manually delete them from the Firebase Console

#### Impact

**In Production:**
- Users from deleted families can still attempt to sign in
- They'll see an error because their Firestore user document doesn't exist
- Their Auth account remains in the system (orphaned account)

**Not Critical Because:**
- Orphaned accounts can't access any data (no Firestore document)
- Real-time listener will sign them out if they try to use the app
- Accounts don't consume significant resources

---

## 💡 Planned Solution: Firebase Cloud Functions

### Recommended Implementation (Future)

**When to implement:** Before public release or when approaching 50+ families

**Solution:** Firebase Cloud Function with Admin SDK

#### How It Works

1. **Trigger:** Cloud Function runs when a family document is deleted
2. **Process:**
   - Function queries all users with the deleted `familyId`
   - Uses Firebase Admin SDK to delete all Auth accounts
   - Logs success/failure for each deletion
3. **Result:** Complete cleanup of all user data AND Auth accounts

#### Requirements

- **Firebase Plan:** Blaze (Pay-as-you-go) - **FREE if under limits**
- **Free Tier Limits:**
  - 2 million function invocations/month
  - 400,000 GB-seconds compute time
  - 200,000 GHz-seconds CPU time
- **Estimated Cost for This App:** $0/month (well within free tier)

#### Implementation Steps

1. **Install Firebase CLI:**
   ```powershell
   npm install -g firebase-tools
   ```

2. **Initialize Cloud Functions:**
   ```powershell
   cd C:\prj\Div kode opkast\PocketMoneyApp
   firebase init functions
   # Select TypeScript
   # Select ESLint
   ```

3. **Ask Firebase Gemini (or use code below):**

   **Prompt for Firebase Gemini:**
   ```
   I need help creating a Firebase Cloud Function to handle family deletion in my Android app.

   CURRENT SETUP:
   - Android app using Firebase Authentication and Firestore
   - Kotlin/Jetpack Compose app
   - Users belong to families stored in Firestore collection "families"
   - User documents in collection "users" have a "familyId" field
   - When a family is deleted, all Firestore data is cleaned up, but Firebase Auth accounts remain

   WHAT I NEED:
   Create a Cloud Function that:
   1. Triggers when a "families" document is deleted from Firestore
   2. Finds all users with that familyId in the "users" collection
   3. Deletes all their Firebase Auth accounts using Admin SDK
   4. Include proper error handling and logging

   SPECIFIC REQUIREMENTS:
   - Use Firebase Admin SDK to delete Auth accounts
   - Function should be triggered by Firestore onDelete event
   - Should handle cases where user Auth account might already be deleted
   - Should log success/failure for each user deletion
   - Use TypeScript (preferred) or JavaScript

   SECURITY:
   - Ensure only authenticated users can trigger family deletion
   - Function should validate that the family was actually deleted before proceeding

   Please provide:
   1. Complete Cloud Function code
   2. Deployment commands
   3. Any required configuration (firebase.json, etc.)
   4. Instructions for enabling Cloud Functions in my Firebase project
   5. How to test the function locally if possible

   My Firebase project is already set up with Authentication and Firestore.
   ```

4. **Deploy the function:**
   ```powershell
   firebase deploy --only functions
   ```

5. **Test:**
   - Delete a test family from the app
   - Check Firebase Console → Functions logs
   - Verify Auth accounts were deleted

#### Manual Implementation Code (Backup)

If Firebase Gemini is unavailable, here's the function code:

**File:** `functions/src/index.ts`

```typescript
import * as functions from 'firebase-functions';
import * as admin from 'firebase-admin';

admin.initializeApp();

/**
 * Cloud Function to delete all Firebase Auth accounts when a family is deleted
 * Triggers when a document in the "families" collection is deleted
 */
export const deleteUserAccountsOnFamilyDeletion = functions.firestore
  .document('families/{familyId}')
  .onDelete(async (snapshot, context) => {
    const familyId = context.params.familyId;
    
    console.log(`Family ${familyId} deleted. Starting user account cleanup...`);
    
    try {
      // Find all users who belonged to this family
      const usersSnapshot = await admin.firestore()
        .collection('users')
        .where('familyId', '==', familyId)
        .get();
      
      if (usersSnapshot.empty) {
        console.log(`No users found for family ${familyId}`);
        return;
      }
      
      console.log(`Found ${usersSnapshot.size} users to delete`);
      
      // Delete each user's Firebase Auth account
      const deletePromises = usersSnapshot.docs.map(async (doc) => {
        const userId = doc.id;
        
        try {
          // Delete the Auth account
          await admin.auth().deleteUser(userId);
          console.log(`✓ Deleted Auth account for user ${userId}`);
          return { userId, success: true };
        } catch (error) {
          // User might already be deleted or never had an Auth account
          console.warn(`⚠ Failed to delete Auth account for user ${userId}:`, error);
          return { userId, success: false, error };
        }
      });
      
      const results = await Promise.all(deletePromises);
      
      const successCount = results.filter(r => r.success).length;
      const failCount = results.filter(r => !r.success).length;
      
      console.log(`✓ Family ${familyId} cleanup complete: ${successCount} accounts deleted, ${failCount} failed`);
      
    } catch (error) {
      console.error(`✗ Error cleaning up family ${familyId}:`, error);
      throw error;
    }
  });
```

**File:** `functions/package.json` (add dependencies):

```json
{
  "dependencies": {
    "firebase-admin": "^12.0.0",
    "firebase-functions": "^5.0.0"
  }
}
```

#### Security Considerations

- ✅ Function runs with Admin SDK privileges (can delete any Auth account)
- ✅ Only triggers when family is actually deleted from Firestore
- ✅ Client app already has proper authorization for family deletion
- ✅ Function validates family was deleted before proceeding
- ⚠️ Requires Blaze plan (free tier is sufficient)

#### Testing

**Local Testing:**
```powershell
firebase emulators:start --only functions,firestore,auth
```

**Production Testing:**
1. Deploy the function
2. Delete a test family from the app
3. Check Firebase Console → Functions → Logs
4. Verify Auth accounts were deleted

---

## 📝 Notes

- This limitation doesn't affect app functionality (users are signed out automatically)
- Orphaned Auth accounts don't cost money or consume resources
- Cloud Functions solution is recommended before public release
- For small-scale testing, manual deletion from Firebase Console is acceptable

---

**Last Updated:** April 23, 2026  
**Next Review:** Before public release
