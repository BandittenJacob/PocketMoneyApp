# 💰 PocketMoneyApp

**Family pocket money and chores management for Android**

A multi-device Android app to help parents manage pocket money, assign chores, and track completion — all synced in real-time across the family.

---

## ✨ Features

- 👨‍👩‍👧‍👦 **Multi-device family sync** — Parents and kids access the same data
- ✅ **Chore management** — Create, assign, and track chores
- 🔁 **Recurring schedules** — Daily, weekly, or monthly chores
- 💵 **Pocket money tracking** — Reward values and payment management
- 🔐 **Biometric authentication** — Fingerprint/face unlock
- 🌍 **Multi-language** — Danish & English (full i18n support)
- 🎨 **Material3 Design** — Modern, clean interface
- 📱 **Optimized UX** — Parent vs. child role-specific views

---

## 🚀 Quick Start

### Prerequisites

- JDK 17 (Temurin)
- Android SDK (API 26+, target 34)
- Gradle 8.7 (included via wrapper)
- Firebase account (for Firestore + Auth)

### Build & Run

```powershell
# Build debug APK (for testing)
.\scripts\build-debug.ps1

# Build release APK (for production)
.\scripts\build-release.ps1

# Deploy to connected devices
.\scripts\deploy-debug.ps1      # Debug build
.\scripts\deploy-release.ps1    # Release build

# Or use Gradle directly
.\gradlew.bat assembleDebug
.\gradlew.bat assembleRelease
```

See [scripts/README.md](../scripts/README.md) for full build & deployment documentation.

### Firebase Distribution

```powershell
# Distribute release build to family testers
.\scripts\release-firebase.ps1

# Distribute debug build to yourself only
.\scripts\release-firebase-debug.ps1
```

### Speed Up Builds (Windows)

Run as Administrator:

```powershell
.\scripts\speed-up-builds.ps1
```

This adds Windows Defender exclusions for build directories (~30 second builds).

---

## 🛠️ Tech Stack

| Area | Technology |
|---|---|
| Language | Kotlin |
| UI Framework | Jetpack Compose with Material3 |
| Architecture | MVVM with StateFlow |
| Backend | Firebase Firestore + Authentication |
| Distribution | Firebase App Distribution |
| Build System | Gradle 8.7 with Kotlin DSL |
| Min SDK | API 26 (Android 8.0) |
| Target SDK | API 34 (Android 14) |

---

## 📚 Documentation

### 📋 Core Documentation

1. **[PROJECT_PLAN.md](PROJECT_PLAN.md)** — Master plan
   - Complete feature list, user requirements, data model design, architecture decisions, development roadmap

2. **[QUICK_REFERENCE.md](QUICK_REFERENCE.md)** — One-page summary
   - Core functionality at a glance, user roles & key screens, tech stack reference

3. **[KNOWN_ISSUES.md](KNOWN_ISSUES.md)** — Known limitations & workarounds

4. **[ROADMAP.md](ROADMAP.md)** ⭐ **UPCOMING FEATURES & TODO LIST**
   - Active todo list, future enhancements, version targets, completed features history

### 🎨 Development Standards

5. **[FEATURE_CHECKLIST.md](FEATURE_CHECKLIST.md)** ⭐ **USE FOR EVERY NEW FEATURE**
   - Pre-coding checklist, translation requirements, testing requirements

6. **[TRANSLATION_GUIDE.md](TRANSLATION_GUIDE.md)** — Internationalization
   - How to add translations (English & Danish), automated translation checks, pre-commit hook setup

7. **[APPTEXT_STYLE_GUIDE.md](APPTEXT_STYLE_GUIDE.md)** ⭐ **TEXT STYLING STANDARD**
   - Centralized text category system (22 categories), usage examples, font size reference, migration guide

8. **[COMPONENT_GUIDE.md](COMPONENT_GUIDE.md)** ⭐ **REUSABLE UI COMPONENTS**
   - Component architecture, all available components, DRY principles and best practices

### 🔧 Technical Guides

9. **[AUTHENTICATION_GUIDE.md](AUTHENTICATION_GUIDE.md)** — Auth implementation
   - Setup flow diagrams, biometric authentication, family-based system, multi-device sync

---

## 🎯 Quick Navigation

### For New Developers
1. Read [PROJECT_PLAN.md](PROJECT_PLAN.md) — Understand the app vision
2. Read [QUICK_REFERENCE.md](QUICK_REFERENCE.md) — Get familiar with features
3. Read [AUTHENTICATION_GUIDE.md](AUTHENTICATION_GUIDE.md) — Understand auth flow

### For Daily Development
1. **Before coding:** Check [FEATURE_CHECKLIST.md](FEATURE_CHECKLIST.md)
2. **While coding:** Use [APPTEXT_STYLE_GUIDE.md](APPTEXT_STYLE_GUIDE.md) for text styling
3. **Before committing:** Run `.\scripts\check-translations.ps1`
4. **For reference:** [QUICK_REFERENCE.md](QUICK_REFERENCE.md) & [TRANSLATION_GUIDE.md](TRANSLATION_GUIDE.md)

### For Troubleshooting
- Check [KNOWN_ISSUES.md](KNOWN_ISSUES.md) for known limitations
- Check [AUTHENTICATION_GUIDE.md](AUTHENTICATION_GUIDE.md) for auth issues

---

## 👨‍💻 Development

### Before Coding

1. Read [FEATURE_CHECKLIST.md](FEATURE_CHECKLIST.md)
2. Plan translations (English + Danish)
3. Use `AppText` for all text styling

### Before Committing

```powershell
# Check for untranslated strings
.\scripts\check-translations.ps1

# Verify build succeeds
.\gradlew.bat assembleDebug
```

### Code Standards

- ✅ All UI text uses `stringResource(R.string.*)`
- ✅ All text styling uses `AppText.Category()`
- ✅ No hardcoded `fontSize` or strings
- ✅ Test in both English and Danish

---

## ✅ Current Status (April 27, 2026)

### Completed Features ✅

- ✅ Environment setup (JDK 17, Android SDK, Gradle 8.7)
- ✅ Firebase configured (Firestore + Authentication + App Distribution)
- ✅ Authentication system:
  - Email/password signup and login
  - Family creation workflow
  - Google Password Manager integration & credential auto-fill
- ✅ Biometric authentication (fingerprint/face unlock, settings toggle, secure credential storage)
- ✅ Full internationalization (i18n):
  - 227 string resources in English & Danish
  - All UI text uses `stringResource()`
  - Automated translation check script
- ✅ Centralized text styling system (AppText, 22 categories, 50% code reduction)
- ✅ Data models (User, Family, Chore with full enums)
- ✅ Repository pattern (AuthRepository, ChoreRepository, BiometricAuthManager, PreferencesManager, LocaleManager)
- ✅ ViewModels (AuthViewModel, ChoreViewModel with StateFlow)
- ✅ Navigation system (6 screens with deep linking ready)
- ✅ Complete UI with Material3 & Jetpack Compose:
  - WelcomeScreen, SignInScreen, SignUpScreen, CreateFamilyScreen
  - DashboardScreen, ChoresScreen (collapsible sections)
  - AddChoreScreen (create & edit), SettingsScreen (biometric & language)
- ✅ Chore management system:
  - Create/edit/delete, claiming mechanic, reassignment, due date picker
  - Status tracking (5 states), Chore Library with templates (deploy on demand)
- ✅ Real-time Firebase synchronization
- ✅ Build optimizations (Windows Defender exclusions, ~30 second builds)

### In Progress ⏳

- ⏳ Balance tracking & transactions
- ⏳ Payment management
- ⏳ Family member profiles
- ⏳ Advanced reporting

### Planned Features 📋

- 📋 Family invitation system (QR code & deep links)
- 📋 Approval & payment workflow (parent approval → PAID status)
- 📋 Allowance system (weekly/monthly automatic payments)
- 📋 Push notifications
- 📋 Child-specific dashboard view

---

## 📁 Project Structure

```
PocketMoneyApp/
├── Docs/                        # Full documentation (you are here)
├── scripts/                     # Utility scripts
│   ├── build-debug.ps1
│   ├── build-release.ps1
│   ├── deploy-debug.ps1
│   ├── deploy-release.ps1
│   ├── release-firebase.ps1     # Firebase dist → Familien group
│   ├── release-firebase-debug.ps1  # Firebase dist → developer only
│   ├── check-translations.ps1
│   └── speed-up-builds.ps1
├── app/
│   ├── src/main/
│   │   ├── java/com/jmp/pocketmoneyapp/
│   │   │   ├── data/            # Models & repositories
│   │   │   ├── ui/              # Compose screens
│   │   │   │   ├── theme/       # AppText & Typography
│   │   │   │   ├── auth/        # Authentication screens
│   │   │   │   ├── chores/      # Chore management
│   │   │   │   └── ...
│   │   │   └── viewmodel/       # ViewModels
│   │   └── res/
│   │       ├── values/          # English strings
│   │       └── values-da/       # Danish strings
│   └── build.gradle.kts
├── build.gradle.kts
└── settings.gradle.kts
```

---

## 📦 Project Details

**App Name**: Pocket Money & Chores App  
**Platform**: Android  
**Package**: `com.jmp.pocketmoneyapp`  
**Users**: 2 children (ages 10–14) + multiple parent devices

---

## 📝 License

Private project — All rights reserved
5. Build data models
6. Create UI screens
7. Test with real devices

---

**Last Updated**: April 22, 2026
