# PocketMoneyApp Roadmap

Track upcoming features, improvements, and planned work.

**Current Version:** 2.02.000  
**Last Updated:** April 29, 2026

---

## âœ… Recently Shipped

### 2.02.000 (April 29, 2026)
- **Settings screen redesigned**: All settings grouped into a General card and a Danger Zone card â€” removed four single-item section headers
- **Proposal bugs fixed**: Accept now actually creates a chore; Accept with Edits now opens the Add Chore form pre-filled with the proposal data
- **Chore Library rename**: `RecurringChore*` â†’ `ChoreLibrary*` across the entire codebase; Firestore collection migrated transparently
- **Top bar icons coloured**: Back arrow and action icons use the primary theme colour; chore screen icons replaced with emoji (ðŸ’¡ðŸ“šðŸ•) via `AppText.TopBarIcon()` and string resources
- **Feedback button hidden in release**: `BuildConfig.DEBUG` guard prevents the pencil icon from showing in production builds
- **Debug build versioning**: Version names include an incremental daily counter (e.g. `2.02.000-DEBUG-20260429-3`)
- **Debug â†’ All Testers release script**: New `release-firebase-debug-all.ps1` + VS Code task distributes debug builds to the full Familien group

### 2.01.000 (April 27, 2026)
- **Shared `AppTopBar` component**: Single composable used across all 22 screens
- **In-app tester feedback**: Pencil icon âœï¸ triggers Firebase App Distribution feedback in debug builds
- **Edit family name**: Parents can rename the family from Settings
- **Orphaned Auth account fix**: Deleting a family removes all member Firebase Auth accounts via Cloud Function
- **Multi-group Firebase distribution**: Separate release tasks for Familien and NÃ¸rregaard tester groups
- **Danish translation fixes**: Create/Join Family screens now correctly show Danish text

### 2.00.000 (April 27, 2026)
- **Push notifications**: Proposals, chore creation, completion, approval and payment events trigger FCM notifications via Cloud Functions

---

## ðŸŽ¯ Planned Features

### High Priority

#### 1. Due Dates Enforcement
**Status:** ðŸ“‹ Planned  
**Priority:** High

**Description:**  
Currently, due dates are displayed but have no functional effect. Need to add:
- Automatic status indicators (overdue, due soon, etc.)
- Visual highlighting for overdue chores
- Notifications/reminders before due date
- Optional auto-completion or penalties for missed chores
- Dashboard view showing upcoming/overdue chores

**Impact:**
- Improves accountability for children
- Helps parents track which chores need attention
- Adds urgency to incomplete tasks

---

#### 2. Savings Goals
**Status:** ðŸ“‹ Planned  
**Priority:** High

**Description:**  
Children set a savings target (e.g. "Save 150kr for Lego") and track progress toward it.

**Features:**
- Child creates a goal with a name, target amount, and optional image/emoji
- Progress bar shown on the child's dashboard
- Goal marked as reached when balance milestone is hit
- Multiple active goals supported
- Parent can see all children's goals

**Database Changes:**
- New `SavingsGoal` collection: `{ memberId, name, targetAmount, emoji, createdAt, completedAt }`

---

#### 3. Recurring Allowance
**Status:** ðŸ“‹ Planned  
**Priority:** High

**Description:**  
Auto-add a fixed allowance amount to a child's balance on a weekly or monthly schedule, without manual intervention.

**Features:**
- Parent sets a recurring allowance per child (amount + interval: weekly/monthly)
- Allowance is applied automatically and appears as a transaction in history
- Can be paused or adjusted at any time
- Runs server-side via Firebase Cloud Function (no on-device background worker)

**Database Changes:**
- `allowance` field on `FamilyMember`: `{ amount, interval, nextDueDate, enabled }`
- Allowance transactions recorded as `TransactionType.ALLOWANCE`

---

#### 4. Chore Auction System
**Status:** ðŸ“‹ Planned  
**Priority:** Medium

**Description:**  
Allow children to bid on unassigned chores with a maximum price set by parents.

**Features:**
- Parent sets max price when creating chore
- Children submit bids (lower than max)
- Lowest bid wins the chore (encourages competitive pricing)
- Optional: timer/deadline for bidding period
- Bidding history visible to family

**UI Requirements:**
- "Auction" badge on eligible chores
- Bidding interface for children
- Bid management screen for parents
- Winner notification

**Database Changes:**
- Add `maxPrice` and `isAuction` fields to Chore model
- New `ChoreAuction` collection for bid tracking: `{ choreId, memberId, bidAmount, timestamp }`

---

#### 5. Push Notification on Chore Assignment/Reassignment
**Status:** ðŸ“‹ Planned  
**Priority:** Low

When a parent assigns or reassigns an existing chore, no push notification is currently sent.

**What to add:**
- Detect when `assignedToUserId` changes on an existing chore (extend `onChoreStatusChanged` or add new `onChoreAssigned` Cloud Function)
- Notify the newly assigned child: "Chore Assigned ðŸ“‹ â€” "{name}" has been assigned to you (X kr.)"
- Optionally notify the previous assignee if reassigned away from them

---

## ðŸ”® Future Ideas

- **Reports & Analytics**: Monthly earning reports, chore completion rates, CSV/PDF export, charts
- **Scheduled chore automation**: Firebase Cloud Function that auto-deploys library templates on a schedule (server-side, not on-device WorkManager)

---

## ðŸ“ Notes

### Power Consumption

- Avoid background workers, polling, or wake locks where possible
- Prefer event-driven updates (Firestore real-time listeners) over periodic fetching
- Any scheduled or background task should run server-side (Firebase Cloud Function), not on-device

### Version Strategy

- **Patch (x.xx.XXX)**: Bug fixes, UI tweaks, small improvements
- **Minor (x.XX.000)**: New features
- **Major (X.00.000)**: Breaking changes, major redesigns

---

## âœ… Completed Features

### 2.02.000 â€” See Recently Shipped above

### 2.01.000 â€” See Recently Shipped above

### 2.00.000 â€” See Recently Shipped above

### 1.02.000 / 1.03.000
- âœ… Chore Library (replaces recurring chores) â€” `ChoreLibraryScreen`, `AddChoreLibraryItemScreen`, `chore_library_templates` Firestore collection
- âœ… Child chore proposals â€” `ChoreProposal` model, `AddProposalScreen`, parent accept/reject/edit flow, Settings toggle
- âœ… Money Correction screen rework â€” Bonus, Correction (+/âˆ’), Bank Transfer transaction types
- âœ… Custom app icon â€” adaptive white piggy bank on amber gradient, monochrome variant

### 1.00.000 â€” 1.01.000 (April 23, 2026)
- âœ… Component system (ChoreCard, StatusChip, EmptyState, SectionHeader)
- âœ… Full translation support (English/Danish)
- âœ… Centralized text styling (AppText)
- âœ… Family member management
- âœ… Chore status workflow (Pending â†’ Completed â†’ Approved â†’ Paid)
- âœ… Assignment system (Claim/Assign/Reassign)
- âœ… Firebase Authentication & Firestore
- âœ… Release build & signing
- âœ… Pull-to-refresh on main screens

---

**End of Roadmap**
