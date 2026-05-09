# Release Notes

## [Unreleased] - In Development

### Added
- Intro tours on the Dashboard, Chores, Chore Library, and Family Members screens — on first visit, a guided spotlight walkthrough highlights the key controls on each screen
- Settings: "Reset intro tours" button opens a dialog to reset individual screen tours (by checkbox) or all at once

### Changed
- Top bar back arrow and feedback pencil icons now use the primary theme colour, making them more visible
- Chore screen top bar icons replaced with emoji: 💡 (suggest chore), 📚 (chore library), 🕐 (chore history) — full colour, consistent across all devices

### Removed

### Fixed

### Developer
- `PreferencesManager` extended with per-screen tour flags (`hasSeenDashboardTour`, `hasSeenChoresTour`, `hasSeenLibraryTour`, `hasSeenFamilyMembersTour`) and `resetAllTours()`
- Tour implementation uses `canopas/intro-showcase-view` 2.0.2 — `IntroShowcase` wraps each Scaffold; `introShowCaseTarget` is a receiver-scope method on `IntroShowcaseScope`, not a top-level import
- Dashboard tour targets the small arrow/settings icons inside each card rather than the full-width cards, keeping spotlight circles tight
- `ShowcaseStyle` is in `com.canopas.lib.showcase.component` (not the root package)
- Emoji top bar icons implemented via `AppText.TopBarIcon()` (new 20sp entry in `AppTextStyles`) and dedicated string resources (`proposals_suggest_icon`, `chores_recurring_icon`, `chore_history_icon` in both EN and DA), following the project's no-hardcoded-strings convention
- Feedback pencil button (✏️) in `AppTopBar` is now hidden in release builds via `BuildConfig.DEBUG` guard — button was always a no-op in release (SDK is `debugImplementation` only), now it is not shown at all
- `BuildConfig` generation enabled explicitly in `build.gradle.kts` (`buildFeatures { buildConfig = true }`) — required since AGP 8+ disables it by default
- Debug build version names now include an incremental daily counter (e.g. `2.02.000-DEBUG-20260429-3`) — counter stored in `debug-build-counter.txt` (gitignored), resets each day
- New VS Code task and script `release-firebase-debug-all.ps1`: builds the debug APK and distributes it to both the **Familien** and **Nørregaard** tester groups via Firebase CLI

---

## [2.02.000] - 2026-04-28

### Changed
- Settings screen redesigned — all settings are now grouped into one **General** card (language, family name, proposals toggle, biometric, PIN) and one **Danger Zone** card (delete family). Removed the four separate section headings that each contained only one setting.

### Fixed
- **Accept proposal** now actually creates a chore — previously the proposal was marked accepted in Firestore but no chore was created
- **Accept with edits** now opens the Add Chore screen pre-filled with the proposal's name, description, and suggested reward — previously it opened an empty form; saving the edited chore now correctly marks the proposal as `ACCEPTED_WITH_EDITS`

### Developer
- `RecurringChore*` → `ChoreLibrary*` rename across the entire codebase: `ChoreLibraryTemplate`, `ChoreLibraryRepository`, `ChoreLibraryViewModel`, `ChoreLibraryScreen`, `AddChoreLibraryItemScreen`; nav routes changed to `chore_library` / `add_chore_library_item`; parameter `onRecurringChoresClick` → `onChoreLibraryClick`
- Firestore collection transparently migrated from `recurring_chore_templates` to `chore_library_templates` — first load copies and deletes legacy documents automatically
- `AddChoreScreen` now accepts optional `proposalToAccept: ChoreProposal?` and `onProposalAccepted` callback

---

## [2.01.000] - 2026-04-27

### Added
- Feedback button (pencil icon ✏️) in the top bar on every screen — tap it to send a screenshot and comment directly to the developer
- Parents can now rename the family in Settings

### Fixed
- Deleting a family now fully removes all member accounts — previously, other family members' login accounts were left behind after a family was deleted
- "Create Family" and "Join Family" screens now correctly show Danish text when the device language is set to Danish
- Keyboard no longer covers the password fields on the sign-up screen — the page now scrolls when the keyboard is open

### Developer
- `AppTopBar` shared component introduced — replaces per-screen `TopAppBar` calls across all 22 screens, ensuring a consistent layout and a single place to extend the top bar in future
- Firebase App Distribution in-app feedback SDK added (`firebase-appdistribution-api-ktx` + `debugImplementation` of `firebase-appdistribution`); `FirebaseAppDistribution.getInstance().startFeedback()` called from `AppTopBar`; no-op in release builds
- `onFamilyDeleted` Cloud Function added: deletes all member Firebase Auth accounts via Admin SDK when a family document is deleted, resolving the orphaned Auth accounts issue
- Family deletion now stamps all member Auth UIDs onto the family document (`memberAuthUids`) before the deletion cascade begins, so the Cloud Function has the data it needs even after sub-collections are gone
- Cloud Functions runtime upgraded from Node.js 20 to Node.js 22
- Release notes sent to Firebase App Distribution are now automatically extracted from this file, with the Developer section stripped out
- Firebase App Distribution tester groups discovered: **Familien** and **Nørregaard** — separate release scripts and VS Code tasks added for each

---

## [2.00.000] - 2026-04-27

### Added
- Push notifications via Firebase Cloud Functions + FCM:
  - Child submits a chore proposal → parents are notified
  - Parent accepts or rejects a proposal → the child is notified
  - Parent creates a chore: assigned child is notified; if unassigned, all children are notified
  - Child marks a chore complete → parents are notified
  - Parent approves a completed chore → the child is notified
  - Parent pays a chore → the child is notified
- `assignedToUserId` field added to the Chore model (Firebase Auth UID of the assigned member, used by Cloud Functions for targeted notifications)

### Fixed
- Completed chores on children's phones no longer appear expanded in the "Pending Approval" section — marking a chore complete now clears the expanded state
- Pull-to-refresh now reloads chore proposals (both parent pending proposals and child "My Suggestions")

---

## [1.03.001] - 2026-04-27 (Hotfix)

### Fixed
- Child chore proposals not loading on the chores screen — proposals were queried before family members had finished loading, so the member ID lookup always returned empty. Fixed by separating the proposals load into its own effect that waits for members to be available

---

## [1.03.000] - 2026-04-27

### Added
- Chore cards are now collapsible — all cards start collapsed (showing name, value, status, assignee). Tap any card to expand it; only one card can be expanded at a time
- Long press a chore card (parents only) to go directly to edit, whether the card is collapsed or expanded
- Drag-to-reorder chore cards — tap the reorder button (≡) in the top bar to enter reorder mode. Drag handles appear on each card; drag to rearrange. Tap the checkmark to exit reorder mode. Order is saved per user on-device
- Child Chore Proposals: children can suggest a chore (name, description, suggested reward) via a 💡 button in the top bar (visible when feature is enabled). Parents see pending proposals at the top of the chores screen and can Accept, Edit & Accept, or Reject with a reason. Children see the outcome of their proposals inline
- Chore Library accessible via a 📚 icon button in the chores top bar (parents only) — the card in the list has been removed

### Changed
- Chores top bar reorganised: navigation buttons (📚 Library, 🕐 History) are grouped together, separated from the reorder toggle (≡/✓) by a vertical divider

### Removed
- Recurring chores auto-generate toggle removed from Settings (feature was already replaced by Chore Library)

### Fixed

---

## [1.02.000] - 2026-04-24

### Added
- Keyboard Next/Done navigation between form fields now works correctly — fields are explicitly linked so the Next arrow always moves to the right field (Sign In, Sign Up, Add Chore, Add Transaction)
- Add Transaction redesigned: types are now Bonus, Correction, and Transferred to bank. A colored indicator clearly shows whether the transaction adds to or removes from the balance. Correction type has a +/− toggle chip
- Chore Library replaces Recurring Chores — templates are now deployed on-demand with one tap instead of auto-generated on a schedule. Background chore generation worker removed
- App icon: custom piggy bank adaptive icon with amber/orange gradient background

### Removed
- Notifications removed entirely: `NotificationHelper`, chore-completed and chore-approved notifications, and `POST_NOTIFICATIONS` permission all deleted — the app is now fully passive

### Fixed
- Keyboard no longer covers input fields — all screens with text input now push content above the keyboard when it appears
- All deprecated API calls updated: `autoCorrect` → `autoCorrectEnabled`, `Divider` → `HorizontalDivider`, `menuAnchor()` → `menuAnchor(MenuAnchorType.PrimaryNotEditable)`, non-mirrored icons → `AutoMirrored` equivalents
- Hardcoded UI strings replaced with `stringResource()` across all screens — full Danish/English translation coverage
- Chore status labels now localized ("Pending", "Completed", "Approved", "Paid") instead of showing raw enum names
- Debug log statements removed from production code

---

## [1.01.000] - 2026-04-23

### Added
- **Version Display on Dashboard**: App version shown as a single line of text at the bottom of the dashboard (e.g., "App version: 1.00.000-DEBUG")
- **Pull-to-Refresh**: Added swipe-down refresh gesture to chore and family screens
  - ChoresScreen: Refresh chores and family members
  - ChoreLibraryScreen: Refresh chore templates
  - FamilyMembersScreen: Refresh family members and balances
  - Visual loading indicator disappears only when data load is complete
- **Screen Rotation Lock**: App is now locked to portrait orientation on all screens
- **Chore History Screen**: Approved and paid chores moved to a dedicated history screen
  - Access via the history icon (🕘) in the chores screen top bar
  - Parents can see all completed chores; children see only their own
  - Parents can revert any chore back to Pending from the history screen
  - Pull-to-refresh supported
  - Main chores list is now cleaner, showing only active (Pending, In Progress, Completed) chores

### Changed
- **Compose BOM Upgrade**: Updated from 2024.06.00 to 2024.09.00
  - Enables PullToRefreshBox API for pull-to-refresh functionality
  - Includes latest Material3 improvements and bug fixes

### Fixed
- BuildConfig import issue resolved by using PackageManager.getPackageInfo() instead
- Lint checks temporarily disabled (abortOnError = false) to bypass notification permission warnings
- Duplicate calendar icon (📅) removed from due date on chore cards
- Logout button on Chores screen showed a checkmark icon — removed; sign-out is now only available from the Dashboard

### Technical Notes
- Modified: `DashboardScreen.kt`, `AndroidManifest.xml`, `ChoresScreen.kt`, `ChoreLibraryScreen.kt`

---

## [1.00.000] - 2026-03-08

### Initial Production Release
- Complete translation system (English/Danish) with 227+ strings
- Role-based UI (Parent/Child modes)
- Chore management with one-time and recurring tasks
- Allowance tracking and balance management
- Transaction history
- Family member management
- Firebase authentication and Firestore database
- Biometric authentication option
- Material3 design with custom typography
- Component architecture (ChoreCard, StatusChip, EmptyState, SectionHeader)
- AppText centralized styling system with 22 text categories
- Accessibility-compliant status colors (colorblind friendly)
- QR code family joining

### Deployed To
- 4 family devices (JMP, Maria, William, Isabella)
