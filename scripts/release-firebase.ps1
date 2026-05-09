# Build and distribute PocketMoneyApp via Firebase App Distribution
# Usage: powershell -ExecutionPolicy Bypass -File ".\scripts\release-firebase.ps1"

Write-Host "=====================================" -ForegroundColor Cyan
Write-Host " PocketMoneyApp - Firebase Release" -ForegroundColor Cyan
Write-Host "=====================================" -ForegroundColor Cyan
Write-Host ""

# Navigate to project root
$scriptPath = Split-Path -Parent $MyInvocation.MyCommand.Path
$projectRoot = Split-Path -Parent $scriptPath
Set-Location $projectRoot

# Read version from build.gradle.kts
$buildGradle = Get-Content "app\build.gradle.kts" -Raw
if ($buildGradle -match 'versionName\s*=\s*"([^"]+)"') {
    $version = $matches[1]
} else {
    Write-Host "ERROR: Could not detect version from build.gradle.kts" -ForegroundColor Red
    exit 1
}
Write-Host "Version : $version" -ForegroundColor White

# Check keystore
if (!(Test-Path "app\release.keystore")) {
    Write-Host "ERROR: Release keystore not found at app\release.keystore" -ForegroundColor Red
    exit 1
}

# Uses Application Default Credentials (firebase login on this machine).
# No token needed - the Gradle plugin picks up your local Firebase login automatically.

# Extract release notes from RELEASE_NOTES.md (section matching current version, excluding Developer)
$releaseNotesFile = "app\release-notes.txt"
$releaseNotesSrc  = "Docs\RELEASE_NOTES.md"

if (Test-Path $releaseNotesSrc) {
    $content = Get-Content $releaseNotesSrc -Raw
    if ($content -match "(?s)## \[$([regex]::Escape($version))\][^\n]*\n(.*?)(?=\n## \[|\z)") {
        $versionContent = $matches[1]
        $sections = $versionContent -split '(?=### )'
        $outputLines = [System.Collections.Generic.List[string]]::new()

        foreach ($section in $sections) {
            $section = $section.Trim()
            if ($section -eq '') { continue }
            if ($section -match '^### Developer') { continue }   # skip developer section

            $lines  = $section -split '\r?\n'
            $header = $lines[0] -replace '^### ', ''
            $items  = $lines[1..($lines.Length - 1)] | Where-Object { $_.Trim() -ne '' }

            if ($items.Count -gt 0) {
                $outputLines.Add("${header}:")
                foreach ($item in $items) { $outputLines.Add($item) }
                $outputLines.Add('')
            }
        }

        if ($outputLines.Count -gt 0) {
            $notesBody = ($outputLines -join "`n").Trim()
            "v${version}`n`n${notesBody}" | Set-Content $releaseNotesFile -Encoding UTF8
            Write-Host "Release notes: extracted from RELEASE_NOTES.md (v$version)" -ForegroundColor White
        } else {
            'No release notes for this version.' | Set-Content $releaseNotesFile -Encoding UTF8
            Write-Host "Release notes: (no user-facing changes listed)" -ForegroundColor Yellow
        }
    } else {
        "No release notes found for v${version}." | Set-Content $releaseNotesFile -Encoding UTF8
        Write-Host "WARNING: No section found for v$version in RELEASE_NOTES.md" -ForegroundColor Yellow
    }
} else {
    'Release notes not available.' | Set-Content $releaseNotesFile -Encoding UTF8
}

Write-Host ""

# Step 1: Build
Write-Host "[1/2] Building signed release APK..." -ForegroundColor Yellow
$buildOutput = .\gradlew.bat assembleRelease 2>&1
if ($LASTEXITCODE -ne 0) {
    Write-Host ""
    Write-Host "BUILD FAILED!" -ForegroundColor Red
    Write-Host $buildOutput
    exit 1
}

$apkPath = "app\build\outputs\apk\release\app-release.apk"
if (!(Test-Path $apkPath)) {
    Write-Host "ERROR: APK not found after build. Check Gradle output above." -ForegroundColor Red
    exit 1
}

$sizeMB = [math]::Round((Get-Item $apkPath).Length / 1MB, 2)
Write-Host "Build OK - $apkPath ($sizeMB MB)" -ForegroundColor Green
Write-Host ""

# Step 2: Upload to Firebase App Distribution (Familien group)
Write-Host "[2/2] Uploading to Firebase App Distribution (Familien)..." -ForegroundColor Yellow
$releaseNotesArg = (Resolve-Path $releaseNotesFile).Path
$apkFullPath = (Resolve-Path $apkPath).Path
$firebaseOutput = powershell -ExecutionPolicy Bypass -Command "firebase appdistribution:distribute '$apkFullPath' --app '1:790017941760:android:16b72dcd008493bc9cd3e5' --groups 'familien' --release-notes-file '$releaseNotesArg' 2>&1"
if ($LASTEXITCODE -ne 0) {
    Write-Host ""
    Write-Host "FIREBASE UPLOAD FAILED!" -ForegroundColor Red
    Write-Host $firebaseOutput
    exit 1
}

# Extract the Firebase console URL from output
$consoleUrl = ($firebaseOutput | Select-String "console.firebase.google.com").Line
$shareUrl   = ($firebaseOutput | Select-String "appdistribution.firebase.google.com").Line

Write-Host ""
Write-Host "=====================================" -ForegroundColor Green
Write-Host " Release v$version Distributed!" -ForegroundColor Green
Write-Host " Group: Familien" -ForegroundColor Green
Write-Host "=====================================" -ForegroundColor Green
Write-Host ""
if ($consoleUrl) {
    Write-Host "Firebase console :" -ForegroundColor Gray
    Write-Host "  $($consoleUrl.Trim())" -ForegroundColor Cyan
}
if ($shareUrl) {
    Write-Host "Tester share link:" -ForegroundColor Gray
    Write-Host "  $($shareUrl.Trim())" -ForegroundColor Cyan
}
Write-Host ""
Write-Host "Testers in the Familien group will receive an email notification." -ForegroundColor White
Write-Host ""
