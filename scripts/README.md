# Build & Deployment Scripts

Automated scripts for building and deploying PocketMoneyApp.

---

## 📋 Available Scripts

### Build Scripts

#### `build-debug.ps1`
Build a debug APK for testing.

```powershell
.\scripts\build-debug.ps1
```

**Output:** `app/build/outputs/apk/debug/app-debug.apk`

**Use When:**
- Testing new features
- Debugging issues
- Development iterations

**Notes:**
- Debug builds are NOT signed with release keystore
- Debug builds use package name `com.jmp.pocketmoneyapp.debug`
- Debug and release builds can be installed side-by-side
- Debug builds are NOT tracked in deployment log
- Faster build time (no optimization)
- Debug builds show "-DEBUG" in version name

---

#### `build-release.ps1`
Build a signed release APK for production.

```powershell
.\scripts\build-release.ps1
```

**Output:** `app/build/outputs/apk/release/app-release.apk`

**Use When:**
- Deploying to family devices
- Creating version releases
- Production distribution

**Requirements:**
- Release keystore must exist: `app/release.keystore`
- Version must be set in `app/build.gradle.kts`

**Notes:**
- Fully optimized and signed
- Ready for production use
- Tracked in deployment log when deployed

---

### Deployment Scripts

#### `deploy-debug.ps1`
Install debug APK on all connected devices via USB.

```powershell
.\scripts\deploy-debug.ps1
```

**Process:**
1. Detects all connected ADB devices
2. Shows list of devices
3. Asks for confirmation
4. Installs APK on each device
5. Handles signature conflicts automatically

**Use When:**
- Testing on real devices
- Quick development iterations
- Pre-release testing

**Notes:**
- Does NOT update deployment log
- Uses package name `com.jmp.pocketmoneyapp.debug`
- Can coexist with release build (different package name)
- Automatically handles reinstallation if needed
- Works with multiple devices simultaneously

---

#### `deploy-release.ps1`
Install release APK on all connected devices and update deployment log.

```powershell
.\scripts\deploy-release.ps1
```

**Process:**
1. Detects all connected ADB devices
2. Shows version and device list
3. Asks for confirmation
4. Installs APK on each device
5. Updates `Docs/DEPLOYMENT_LOG.md` with:
   - New version number
   - Installation timestamp
   - Device status

**Use When:**
- Deploying new version to family
- Official version releases
- Production updates

**Requirements:**
- Release APK must be built first
- Devices must be connected via USB
- USB debugging enabled on devices

**Notes:**
- ONLY for release builds
- Automatically updates deployment log
- Handles signature conflicts
- Updates version for existing devices in log

---

## 🚀 Typical Workflow

### Development/Testing

```powershell
# Build debug APK
.\scripts\build-debug.ps1

# Deploy to connected test device
.\scripts\deploy-debug.ps1
```

---

### Release to Family

```powershell
# 1. Update version in app/build.gradle.kts
# 2. Build signed release
.\scripts\build-release.ps1

# 3. Connect all family devices via USB
# 4. Deploy to all devices
.\scripts\deploy-release.ps1

# 5. Verify Docs/DEPLOYMENT_LOG.md was updated
```

---

## 📱 Device Setup (First Time)

For each device:

1. **Enable Developer Options:**
   - Settings → About Phone
   - Tap "Build Number" 7 times

2. **Enable USB Debugging:**
   - Settings → Developer Options
   - Enable "USB Debugging"

3. **Connect to PC:**
   - Use USB cable
   - Allow debugging prompt on device

4. **Verify Connection:**
   ```powershell
   adb devices
   ```

---

## 🔀 Debug vs Release Side-by-Side

Debug and release builds use different package names and can be installed simultaneously:

- **Release:** `com.jmp.pocketmoneyapp`
- **Debug:** `com.jmp.pocketmoneyapp.debug`

**Why is this useful?**
- Test new features (debug) without affecting production version (release)
- Compare behavior between debug and release builds
- Keep stable version for family while developing
- Each app has separate data/settings in Firebase

**On your device:**
- Two separate app icons will appear
- Debug version shows "PocketMoneyApp-DEBUG" in the app list
- Each connects to Firebase independently with same credentials

---

## ⚠️ Troubleshooting

### "ADB not found"
- Install Android SDK Platform Tools
- Add to PATH: `C:\Users\[YourUser]\AppData\Local\Android\Sdk\platform-tools`

### "No devices connected"
- Check USB cable connection
- Enable USB debugging on device
- Try different USB port
- Unlock device screen

### "Signature mismatch"
- Scripts handle this automatically
- Old version is uninstalled, then new version installed
- **Warning:** This will clear app data (users need to log in again)

### "Installation failed"
- Check device storage space
- Ensure USB debugging is allowed
- Try revoking USB debugging authorizations and reconnecting

---

## 📝 Notes

- **Debug builds** are for development only - not optimized, not signed for release
- **Release builds** are production-ready - optimized, signed, tracked
- **Deployment log** is only updated for release deployments
- **Multiple devices** can be deployed simultaneously
- **Version tracking** happens automatically via deployment log

---

**Last Updated:** April 23, 2026
