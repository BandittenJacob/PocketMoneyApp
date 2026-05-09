# Quick Reference - Pocket Money App

## 📋 Project Summary

**Purpose**: Help manage pocket money and chores for kids  
**Platform**: Android (Kotlin + Jetpack Compose)  
**Backend**: Firebase Firestore + Authentication  
**Status**: Core features implemented and working ✅  
**Languages**: Danish (default) & English  
**Key Features**: Multi-device sync, biometric login, chore claiming system, recurring schedules

---

## 🎯 Core Functionality (Implemented)

### Authentication ✅
- Email/password signup and login
- Family creation (first user becomes parent)
- Firebase user documents
- Google Password Manager integration
- Biometric authentication (fingerprint/face unlock)
- Settings toggle for biometric
- Quick app unlock without password

### Chore System ✅
- **Create chores** with name, description, value (kr), due date
- **Assignment options**:
  - Unassigned (default)
  - Assign to specific child (currently "Boy" or "Girl")
- **Chore claiming**: Kids can claim unassigned chores they want to do
- **Reassignment**: Move chores between children or back to unassigned
- **Status tracking**: PENDING → IN_PROGRESS → COMPLETED → APPROVED → PAID
- **Completion flow**:
  - Mark chore as "Complete"
  - Revert with "Not Done"
  - Collapsible completed section

### Chore Library ✅
- **Templates**: Create reusable chore templates in the Chore Library
- **Deploy on demand**: Tap a template to deploy it as a real chore with one tap
- **Edit/delete**: Manage templates from the Chore Library screen (parents only)

### Internationalization ✅
- Danish language (default)
- English language
- 140+ translated strings
- Language selector in Settings
- Dynamic switching with app restart

### UI Features ✅
- Material3 design with Jetpack Compose
- Shared `AppTopBar` component across all 22 screens (back arrow, per-screen actions, ✏️ feedback button)
- Dashboard with quick access cards
- Chores list with collapsible sections
- Add/Edit chore screen with all options
- Settings screen (family name, biometric, language, PIN, danger zone)
- Calendar date picker
- Dropdown selectors

---

## 👥 Users (Current Implementation)

### Parents ✅
- Full administrative control
- Create/edit/delete chores
- Mark chores complete/incomplete
- Claim and reassign chores
- Access from multiple devices
- Biometric login option

### Test Children (Hardcoded)
- Currently: "Boy" and "Girl" (placeholders)
- **TODO**: Implement real child profiles with:
  - Names, ages, photos
  - Balance tracking
  - Own device accounts
  - Child-specific dashboard view

---

## 🗄️ Firebase Collections (Implemented)

```
users/{userId} ✅
├── id, email, name
├── role: UserRole (ADMIN/PARENT/CHILD)
├── familyId
├── biometricEnabled
└── createdAt

families/{familyId} ✅
├── id, name
├── createdBy
├── members: List<String>
└── createdAt

chores/{choreId} ✅
├── id, name, description, value
├── assignedTo (child name or empty)
├── createdBy, familyId
├── status: ChoreStatus (5 states)
├── dueDate, completedDate
├── isRecurring, recurrenceType
├── timeOfDay, dayOfWeek, dayOfMonth
└── createdAt
```

**Planned Collections**:
- `children/{childId}` - Real child profiles
- `familyInvites/{inviteId}` - QR code invitations
- `transactions/{transactionId}` - Money tracking
- `allowances/{allowanceId}` - Fixed allowances

---

## 📱 Key Screens (Implemented)

### WelcomeScreen ✅
- "Create a Family" button
- "Sign In" button
- App entry point

### SignInScreen ✅
- Email and password fields
- Password manager auto-fill
- Credential tracking
- Biometric fallback option

### SignUpScreen ✅
- Name, email, password, confirm password
- Password manager "Use Strong Password"
- Auto-fill support

### CreateFamilyScreen ✅
- Family name input
- Creates family in Firebase
- Links user to family

### DashboardScreen ✅
- Welcome message with user name
- Family name display
- Chores card → ChoresScreen
- Settings card → SettingsScreen
- Sign out button

### ChoresScreen ✅
- Collapsible "To Do" section
- Collapsible "Completed" section (collapsed by default)
- Each chore card shows:
  - Name, value (kr), assigned person
  - Due date and schedule
  - Claim/Reassign dropdown
  - Complete/Not Done buttons
- Floating action button → Add chore
- Edit button per chore

### AddChoreScreen ✅
- Create new or edit existing chore
- Fields: name, description, value, assign to, due date
- Recurring toggle with frequency selector
- Daily: time picker
- Weekly: day selector
- Monthly: day number input
- Create/Save button

### SettingsScreen ✅
- Language selector (Danish/English)
- Family name editor (parents only — renames the family across all devices)
- Automation section: chore proposals toggle (parents only)
- Biometric authentication toggle
- Change PIN
- Device capability detection
- Danger zone: request/cancel/approve family deletion

---

## ⚙️ Tech Stack (Implemented)

- **Kotlin** - Primary language ✅
- **Jetpack Compose** - UI framework (Material3) ✅
- **MVVM** - Architecture pattern ✅
- **Firebase Firestore** - Database ✅
- **Firebase Authentication** - User management ✅
- **Navigation Compose** - Screen navigation ✅
- **StateFlow** - Reactive state management ✅
- **Coroutines** - Async operations ✅
- **Biometric API** - Fingerprint/face unlock ✅
- **Credential Manager** - Password manager integration ✅
- **SharedPreferences** - Local settings storage ✅
- **Gradle 8.7** - Build system ✅
- **JDK 17** - Java development kit ✅

---

## 💡 Key Design Decisions

1. **Firebase over local database**: Real-time multi-device sync ✅
2. **Family-based system**: All family members share data ✅
3. **Chore claiming mechanic**: Kids choose chores (promotes autonomy) ✅
4. **Unassigned by default**: Flexible assignment workflow ✅
5. **Collapsible sections**: Better UI organization ✅
6. **Biometric authentication**: Quick, secure access ✅
7. **Danish default**: Primary language for target users ✅
8. **Material3**: Modern, consistent design ✅
9. **MVVM architecture**: Separation of concerns ✅
10. **Repository pattern**: Clean data access layer ✅

---

## 🚀 Next Development Steps

1. **Family Member Management** (High Priority)
   - Replace "Boy"/"Girl" with real family members
   - Add/edit/remove members UI
   - Store in Firebase `children/` collection

2. **Invitation System** (High Priority)
   - QR code generation
   - Deep link handling
   - Family join workflow

3. **Approval & Payment** (Medium Priority)
   - Parent approval workflow
   - Balance tracking per child
   - Transaction history

4. **Allowance System** (Medium Priority)
   - Weekly/monthly automatic payments
   - Separate from chore earnings

5. **Dashboard Cleanup** (Low Priority)
   - Remove debug cards
   - Add meaningful stats
- **Firebase Auth** - Authentication
- **Hilt** - Dependency injection
- **Coroutines** - Async operations
- **Navigation Component** - Screen navigation

---

## 📊 Balance Calculation Logic

```kotlin
// Child's balance breakdown
totalBalance = 
    allowanceEarned +    // Sum of all allowance credits
    choreEarnings +      // Sum of approved chores
    manualAdjustments -  // Parent added/deducted
    spending             // Money spent (future feature)
```

---

## 🔔 Notifications (Phase 2)

- Kid marks chore complete → Notify parent
- Parent approves → Notify kid ("You earned $5!")
- Chore due soon → Remind kid
- Chore overdue → Notify kid & parent
- Allowance paid → Notify kid

---

## 🚀 Development Order

1. ✅ Planning complete
2. Project setup + Firebase config
3. Authentication (parent + child)
4. Data models & repository layer
5. Parent: Create chores
6. Child: View & mark chores
7. Parent: Approve chores
8. Balance calculation
9. Allowance system
10. UI polish & testing

---

**Last Updated**: April 22, 2026
