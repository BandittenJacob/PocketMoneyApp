# Pocket Money & Chores App - Project Plan

## 📱 Project Overview
An Android app to help manage kids' pocket money and track their chores with multi-device family synchronization.

**Platform**: Android  
**Language**: Kotlin  
**Status**: Core Features Complete - Production Ready  
**Created**: March 8, 2026  
**Last Updated**: April 27, 2026

---

## 🎯 Core Objectives
1. Track children's chores and completion status ✅
2. Manage pocket money allocation and spending ✅  
3. Provide visibility for both parents and children ✅
4. Teach kids financial responsibility ✅
5. Multi-device real-time synchronization ✅
6. Secure authentication with PIN + optional biometrics ✅
7. Multi-language support (Danish & English) ✅
8. Chore Library (template-based on-demand chore deployment) ✅

---

## 🗄️ Database & Backend Strategy

### Implemented Solution: Firebase Firestore
**Why Firebase?**
- ✅ Free tier sufficient for family use
- ✅ Real-time synchronization across devices
- ✅ Built-in authentication (parent vs child accounts)
- ✅ No server maintenance required
- ✅ Excellent Android/Kotlin integration
- ✅ Cloud-based - accessible from multiple devices
- ✅ Offline support with automatic sync

---

### Frontend / Backend Responsibility Split

The app uses **Firebase Cloud Functions** for backend logic and the **Android app** for everything user-facing.

**The guiding principle:** If logic can be triggered or bypassed by the user, or needs to run on a schedule, it belongs in the backend. If it's purely display or immediate user feedback, keep it on the phone.

#### Run in Cloud Functions (backend)

| Responsibility | Reason |
|---|---|
| Recurring allowance disbursement | Scheduled job — must run whether anyone has the app open or not |
| Chore auction deadline enforcement | Close bidding and pick winner at a fixed time |
| Balance updates on chore approval | Atomic transaction — prevents race conditions if two parents approve simultaneously |
| Push notifications via FCM | Triggered by Firestore events, reliable regardless of device state |
| Monthly reports / data exports | CPU-heavy, no reason to burden the phone |
| Cleanup jobs (e.g. delete old chores) | Scheduled maintenance with no UI dependency |

#### Run on the phone (frontend)

| Responsibility | Reason |
|---|---|
| All UI and navigation | Obviously |
| Real-time Firestore listeners | Already works great with near-zero latency |
| Form validation | Immediate feedback to user |
| QR code scanning | Device hardware |
| Chore completion photo | Device camera |
| Savings goal progress display | Pure calculation from data already on device |

#### Distribution
- App is distributed via **Firebase App Distribution** to family testers
- No Google Play Store account required
- Testers receive email notifications when a new build is uploaded

---

## 👥 User Roles & Family System

### Family-Based Architecture ✅ IMPLEMENTED
The app is organized around **families**:
- One person creates a family (becomes the first parent)
- Parents can add children as family members
- All family members share the same family data (chores, money tracking)
- Parent/child role separation with appropriate permissions

### Parent Role ✅ IMPLEMENTED
- Create and manage family
- Add/edit/delete family member profiles
- Create, edit, and delete chores
- Create and manage chore library templates
- Assign chores to children or leave unassigned
- View and manage all family member balances
- Create manual transactions (adjustments, allowances)
- View transaction history
- Configure app settings (language, automation)
- Access restricted features (Chore Library management, add chore button)

### Child Role ✅ IMPLEMENTED
- View assigned chores
- Claim unassigned chores
- Mark chores as completed
- View personal balance and earnings breakdown
- View personal transaction history
- Cannot access parent-only features (Chore Library management, creating chores)

---

## 🔐 Authentication & Security

### Mandatory PIN Authentication ✅ IMPLEMENTED
- **4-6 digit PIN** required for all new users during signup
- **SHA-256 hashed** - never stored in plain text
- **Mandatory** - cannot be disabled for security
- PIN verification on app launch
- PIN change functionality in settings

### Optional Biometric Authentication ✅ IMPLEMENTED
- **Fingerprint/Face unlock** available after PIN setup
- **Optional enhancement** - can be enabled/disabled in settings
- **Falls back to PIN** if biometric fails or is unavailable
- Device capability detection
- Per-device biometric configuration

### Security Features
- Firebase Authentication for user accounts
- Secure PIN storage with SHA-256 hashing
- Biometric authentication via AndroidX Biometric library
- Automatic session management
- Google Password Manager integration with smart credential tracking

---

## 🎨 Core Features - Implementation Status

### Phase 1 - MVP ✅ COMPLETE

#### ✅ Authentication & Family Setup
- Firebase Email/Password authentication
- Mandatory PIN creation (4-6 digits, SHA-256 hashed)
- Optional biometric authentication setup
- Create family flow
- Google Password Manager integration
- Role assignment (PARENT/CHILD)
- Multi-device support per user

#### ✅ Biometric Authentication
- Enable/disable in settings
- Secure credential storage
- Quick unlock with fingerprint/face
- Fallback to PIN authentication
- Device capability detection
- Per-device biometric setup

#### ✅ Internationalization (i18n)
- **Danish language** (default)
- **English language**
- Language selector in Settings
- 160+ translated strings
- Dynamic language switching with app restart
- All screens fully translated

#### ✅ Family Member Management
- Create family member profiles (name, avatar emoji, role)
- **Role-based separation**: Parent vs Child
- Edit existing members
- Delete members (with confirmation)
- Parent/child role filtering in UI
- Balance tracking per child
- Transaction history per child
- Parents excluded from balance and chore assignments

#### ✅ Chore Management System
- Create chores with:
  - Name/description
  - Monetary value (kr)
  - Assignment to children or "Unassigned"
  - Optional due date with calendar picker
  - Status tracking (PENDING/IN_PROGRESS/COMPLETED/APPROVED/PAID)
- Edit existing chores (long press card → edit, parents only)
- Delete chores (parent-only, in edit screen)
- **Collapsible chore cards**:
  - All cards start collapsed (shows name, value, status chip, assignee)
  - Tap to expand; only one card expanded at a time
  - Long press (parents) goes directly to edit from any state
- **Drag-to-reorder**:
  - Reorder mode toggled via ≡ button in TopAppBar
  - Drag handles appear per card in reorder mode
  - Order stored locally per user in Android DataStore
  - Reconciled against Firestore on every load (new chores appended to bottom)
- **Chore Claiming System**:
  - Default to "Unassigned"
  - Children can claim chores
  - Reassign between children
  - Promotes child autonomy

#### ✅ Chore Library (Template System)
- **Template-based architecture** for on-demand chore deployment
- Create chore library templates with:
  - Name, description, value
  - Default assignment (optional)
  - Active/Paused status
- **Template management**:
  - Edit templates
  - Delete templates
  - Toggle active/paused status
  - View last deployed timestamp
- **Deploy on demand**: tap a template to instantly create a chore from it
- Prevents deploying a template that already has an active pending instance
- **Parent-only access** to Chore Library management
- Children only see deployed chore instances

#### ✅ Balance & Transaction System
- **Virtual money ledger** (no real transactions)
- Balance tracking per child (parents excluded)
- **Automatic transactions**:
  - Created when chore marked complete
  - Deleted when chore reverted to pending
  - Prevents duplicate transactions
- **Manual transactions**:
  - Create adjustments (add/subtract money)
  - Record allowances
  - Add descriptions
- **Transaction history**:
  - View all transactions per member
  - Shows date, description, amount
  - Color-coded (green for earnings, red for spending)
  - Real-time updates
- **Earnings breakdown**:
  - Shows chore earnings
  - Shows manual adjustments
  - Calculates total balance

#### ✅ Chore Completion Flow  
- Mark chore as "Complete"
- Revert completed chore to "Pending"
- Transaction automatically created/deleted
- Collapsible completed section
- Complete button only shows when assigned
- Real-time status updates

#### ✅ User Interface
- Material3 design with Jetpack Compose
- Responsive layouts for all screen sizes
- Dark/light theme support (system default)
- Collapsible sections for organization
- Calendar date picker for due dates
- Dropdown selectors for assignments
- **Dashboard** with role-based navigation cards
- **Chores screen** with incomplete/completed sections
- **Chore Library screen** (parent-only)
- **Family members screen** with role grouping
- **Balance screen** (children only)
- **Transaction history screen**
- **Settings screen** with automation toggle

#### ✅ Real-time Sync
- All devices see updates instantly via Firebase
- Firestore real-time listeners
- Offline support with automatic sync when online
- Optimistic UI updates for better responsiveness

---

### Phase 2 - Enhanced Features ⏳ PLANNED

#### ⏳ Family Invitation System
- Generate QR code for family invite
- Generate shareable invitation link  
- Scan QR code to join family
- Click link to join family
- Deep linking support (`pocketmoneyapp://invite/{code}`)
- Invite expiration (7 days)
- Revoke invites

#### ⏳ Approval Workflow
- Parent approval of completed chores
- Status progression: COMPLETED → APPROVED → PAID
- Approval required before transaction creation
- Notification when approval needed

#### ⏳ Scheduled Allowances
- Set fixed weekly/monthly allowance per child
- Automatic crediting on schedule
- Separate from chore earnings
- Allowance history tracking

#### ⏳ Notifications
- Remind children of pending chores
- Notify parents when chore completed
- Notify children when chore approved
- Push notification support

#### ⏳ Spending Tracking
- Record what money was spent on
- Categories (toys, candy, savings, etc.)
- Spending history per child
- Category-based reports

#### ⏳ Reports & Analytics
- Chores completion rate
- Money earned vs spent
- Weekly/monthly summaries
- Charts and visualizations
- Export data to CSV

---

## 🏗️ Technical Architecture

### Current Implementation
```
PocketMoneyApp/
├── app/
│   ├── src/main/java/com/jmp/pocketmoneyapp/
│   │   ├── data/
│   │   │   ├── model/              # Data classes
│   │   │   │   ├── User.kt         # User with role, PIN hash
│   │   │   │   ├── Family.kt       # Family entity
│   │   │   │   ├── FamilyMember.kt # Member profiles with role
│   │   │   │   ├── Chore.kt        # Chore entity (cleaned up)
│   │   │   │   ├── ChoreLibraryTemplate.kt  # Template for chore library
│   │   │   │   ├── Transaction.kt  # Money transactions
│   │   │   │   └── Enums           # Status, roles, recurrence types
│   │   │   └── repository/         # Data access layer
│   │   │       ├── AuthRepository.kt
│   │   │       ├── ChoreRepository.kt
│   │   │       ├── ChoreLibraryRepository.kt
│   │   │       ├── FamilyMemberRepository.kt
│   │   │       ├── TransactionRepository.kt
│   │   │       ├── BiometricAuthManager.kt
│   │   │       ├── PreferencesManager.kt
│   │   │       └── LocaleManager.kt
│   │   ├── viewmodel/              # MVVM ViewModels
│   │   │   ├── AuthViewModel.kt
│   │   │   ├── ChoreViewModel.kt
│   │   │   ├── ChoreLibraryViewModel.kt
│   │   │   ├── FamilyMemberViewModel.kt
│   │   │   └── BalanceViewModel.kt
│   │   ├── ui/                     # Compose UI screens
│   │   │   ├── auth/              # Authentication screens
│   │   │   ├── chores/            # Chore management
│   │   │   ├── balance/           # Balance & transactions
│   │   │   ├── family/            # Family member management
│   │   │   ├── settings/          # Settings
│   │   │   └── dashboard/         # Main dashboard
│   │   ├── worker/                 # Background workers
│   │   │   └── ChoreGenerationWorker.kt
│   │   ├── navigation/             # Navigation graphs
│   │   └── ui/theme/              # Material3 theming
│   └── res/
│       ├── values/                # English strings
│       └── values-da/             # Danish strings
└── Docs/
    └── PROJECT_PLAN.md
```

### Tech Stack ✅ IMPLEMENTED
- **Language**: Kotlin
- **Min SDK**: Android 8.0 (API 26)
- **Target SDK**: API 34 (Android 14)
- **Architecture**: MVVM (Model-View-ViewModel)
- **Database**: Firebase Firestore
- **Authentication**: Firebase Authentication
- **UI Framework**: Jetpack Compose with Material3
- **Build System**: Gradle 8.7 with Kotlin DSL
- **JDK**: Temurin 17.0.17
- **Package**: com.jmp.pocketmoneyapp

### Dependencies ✅ IMPLEMENTED
- Firebase BOM 33.1.0
  - Firebase Firestore
  - Firebase Authentication
  - Firebase Analytics
- Jetpack Compose BOM 2024.06.00
  - Compose UI, Material3, Icons Extended
- Navigation Compose 2.7.7
- Lifecycle & ViewModel Compose
- Kotlin Coroutines & StateFlow
- AndroidX Biometric 1.1.0
- AndroidX Credentials 1.3.0 (Password Manager)
- AndroidX Fragment 1.8.0
- AndroidX WorkManager 2.9.0

---

## 📊 Data Model (Firebase Firestore)

### Collections ✅ IMPLEMENTED

#### users/
```kotlin
{
  id: String
  email: String
  name: String
  role: String                 // "PARENT" or "CHILD"
  pinHash: String              // SHA-256 hashed PIN
  familyId: String
  createdAt: Timestamp
}
```

#### families/
```kotlin
{
  id: String
  name: String                  // e.g., "The Smith Family"
  createdBy: String            // userId of creator
  createdAt: Timestamp
  members: List<String>        // userIds of all family members
}
```

#### family_members/
```kotlin
{
  id: String
  familyId: String
  name: String
  avatarEmoji: String          // 🎮, 👧, etc.
  role: String                 // "PARENT" or "CHILD"
  balance: Double              // Current balance (children only, parents always 0)
  createdAt: Timestamp
}
```

#### chores/
```kotlin
{
  id: String
  name: String
  description: String
  value: Double                // Monetary value in kr
  assignedTo: String           // Family member name or empty
  createdBy: String            // userId who created it
  familyId: String
  status: String               // "PENDING", "IN_PROGRESS", "COMPLETED", "APPROVED", "PAID"
  dueDate: Timestamp?          // Optional due date
  completedDate: Timestamp?    // When marked complete
  templateId: String?          // ID of template if generated from one
  createdAt: Timestamp
}
```

#### chore_library_templates/
```kotlin
{
  id: String
  name: String
  description: String
  value: Double
  familyId: String
  createdBy: String
  defaultAssignedTo: String    // Default assignment (can be empty)
  recurrenceType: String       // "NONE" (schedule fields legacy, kept for compatibility)
  isActive: Boolean            // Can pause without deleting
  createdAt: Timestamp
  lastInstanceCreated: Timestamp?
}
```

#### transactions/
```kotlin
{
  id: String
  familyMemberId: String
  familyId: String
  type: String                 // "EARNING" or "SPENDING"
  amount: Double               // Positive for earnings, negative for spending
  description: String
  choreId: String?             // If from chore completion
  createdBy: String            // userId who created transaction
  createdAt: Timestamp
}
```

---

## 🚀 Build & Deployment

### Build Configuration
- **Build time**: ~30-40 seconds (optimized)
- **APK size**: ~8-10 MB
- **Test device**: CPH2747 (physical Android phone)
- **Gradle**: 8.7 with build optimizations

### Build Commands
```bash
# Debug build
.\gradlew.bat assembleDebug

# Install on device
.\gradlew.bat installDebug

# Combined
.\gradlew.bat assembleDebug installDebug
```

### Performance Optimizations
- Gradle daemon enabled
- Parallel builds
- Configuration caching
- Build cache optimization
- Optimized dex merging

---

## 📝 Known Issues & Limitations

### Resolved Issues
- ✅ Firestore deserialization issue with `isActive` field (manual extraction fix)
- ✅ Duplicate `active`/`isActive` field conflict (cleanup implemented)
- ✅ Transaction duplication bug on chore reversion (fixed with deleteTransactionsByChoreId)
- ✅ Recurring chore toggle not persisting (optimistic updates + Firestore cache bypass)
- ✅ Biometric-only security hole (made PIN mandatory)
- ✅ Password manager duplicate saves (credential tracking flag)
- ✅ Balance calculation race condition (independent Flow listeners)

### Current Limitations
- Single family per user account
- No family switching mechanism
- No real payment processing (virtual ledger only)
- No data export functionality
- No notification system
- No image uploads for family members

---

## 🔮 Future Enhancements

### Short-term (Next Release)
1. **Family Invitation System** - QR code and link-based invites
2. **Approval Workflow** - Parent approval before transaction creation
3. **Scheduled Allowances** - Automatic recurring allowances
4. **Notifications** - Remind kids of chores, notify on completion
5. **Chore Archive/History** - Move approved/paid chores to archive to prevent clutter in main view

### Medium-term
1. **Spending Tracking** - Categories and spending history
2. **Reports & Analytics** - Charts, completion rates, trends
3. **Data Export** - CSV export for records
4. **Photo Proof** - Attach photos to completed chores
5. **Savings Goals** - Track progress toward goals

### Long-term
1. **Multiple Families** - Support for blended families
2. **Achievements/Badges** - Gamification elements
3. **Parental Controls** - Advanced permission management
4. **API Integration** - Potential bank account integration (future)

---

## 📄 License & Credits

**Status**: Private family app (not for public distribution)  
**Created by**: JMP  
**Framework**: Android with Kotlin  
**Backend**: Firebase (Google Cloud)  
**UI**: Jetpack Compose Material3  

---

## 📞 Support & Documentation

For technical documentation, see:
- `/app/src/main/java/` - Source code with inline documentation
- `/Docs/` - Project documentation
- `README.md` - Setup and build instructions

---

*Last Updated: April 22, 2026*
