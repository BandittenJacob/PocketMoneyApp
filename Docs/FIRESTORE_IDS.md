# PocketMoneyApp — Firestore Reference IDs

## Project
- **Firebase project:** `pocketmoneyapp-47283`
- **API key:** `AIzaSyDJu93IJx_XUcJZ2DFocf7L2KSQndcMNhU`
- **Base URL:** `https://firestore.googleapis.com/v1/projects/pocketmoneyapp-47283/databases/(default)/documents`

## Firebase App Distribution
- **Script:** `scripts/release-firebase.ps1` — builds and distributes in one command
- **Tester group:** `Familien` (Jacob + Maria)
- **Auth token:** stored in Windows user environment variable `FIREBASE_TOKEN`
  - View: `[System.Environment]::GetEnvironmentVariable('FIREBASE_TOKEN', 'User')`
  - Update: `[System.Environment]::SetEnvironmentVariable('FIREBASE_TOKEN', '<new_token>', 'User')`
  - The token expires periodically. Refresh with: `firebase.cmd login:ci`

## Active Family
- **Family ID:** `HuVXFQHZRygoLTh0FaZd`
- **Family name:** Pries Møller
- **choreProposalsEnabled:** `true`

## Users (Auth UID → name)
| Auth UID | Name | Role |
|---|---|---|
| `dkRnLmTLHfaIDVo5saPeV91fz9f2` | Jacob | PARENT |
| `r9Lf02S0VjRuhUpDuCMwOLDuN8E2` | Maria | PARENT |
| `4uypREBhjCUVvVq4qPMNbmXDan52` | Isabella | CHILD |
| `Cm59p9CZqnOTISbhiExqyXreTof1` | William | CHILD |

## Family Members (`family_members` collection)
> All members correctly reference family `HuVXFQHZRygoLTh0FaZd`.

| Member doc ID | Name | Role | Emoji | userId (Auth UID) |
|---|---|---|---|---|
| `plSneydhAoQcmftnKVfK` | Jacob | PARENT | 🐻 | `dkRnLmTLHfaIDVo5saPeV91fz9f2` |
| `M3tbwIXPuHphuRjSM8aQ` | Maria | PARENT | 👩 | `r9Lf02S0VjRuhUpDuCMwOLDuN8E2` |
| `wcYaw10fGzMWnEzHJLxA` | Isabella | CHILD | 🐭 | `4uypREBhjCUVvVq4qPMNbmXDan52` |
| `ytviDrco4naGj4lg19CF` | William | CHILD | 🎮 | `Cm59p9CZqnOTISbhiExqyXreTof1` |

## Collections
- `families/{familyId}` — family doc
- `chores/{choreId}` — chores (field `familyId`)
- `choreProposals/{proposalId}` — proposals (fields: `familyId`, `proposedByMemberId` = **auth UID**)
- `family_members/{memberId}` — member profiles (fields: `familyId`, `userId` = auth UID)
- `users/{authUid}` — user profiles

## ADB Device IDs
| Name | ADB Device ID | Phone model |
|---|---|---|
| Jacob | `3B15AS0072B00000` | CPH2747 (OPPO/OnePlus) |
| Isabella | `49121JEKB08094` | — |
| William | `8c1fef8c` | CPH2653 (OPPO/OnePlus) |
| Maria | — | not yet connected via ADB |

> William's ID `8c1fef8c` is a short USB serial and may change. Run `adb devices` to confirm before deploying.

## Isabella — quick reference
- **Auth UID:** `4uypREBhjCUVvVq4qPMNbmXDan52`
- **Member doc ID:** `wcYaw10fGzMWnEzHJLxA`
- **Display name for proposals:** `🐭 Isabella`

## Jacob (you) — quick reference
- **Auth UID:** `dkRnLmTLHfaIDVo5saPeV91fz9f2`
- **Member doc ID:** `plSneydhAoQcmftnKVfK`
