# Translation Guide

This guide explains the internationalization (i18n) requirements for the PocketMoneyApp.

---

## 🌍 **Supported Languages**

Currently supported:
- **English (en)** - Default language (`res/values/strings.xml`)
- **Danish (da)** - Danish translation (`res/values-da/strings.xml`)

---

## 📋 **Translation Requirements**

### ✅ **MANDATORY RULE: All User-Facing Text MUST Use String Resources**

**Every single piece of text that a user can see in the app MUST be defined in `strings.xml` and translated to all supported languages.**

This includes:
- ✅ Button labels
- ✅ Screen titles
- ✅ Dialog titles and messages
- ✅ Error messages
- ✅ Empty state messages
- ✅ Placeholders and hints
- ✅ Section headers
- ✅ Descriptions and help text
- ✅ Status labels
- ✅ Toast messages
- ✅ Confirmation prompts
- ✅ Navigation labels
- ✅ Content descriptions (for accessibility)

### ❌ **What Should NOT Be Translated**

- User-generated content (names, family names, chore descriptions, etc.)
- Dates and times (use system locale formatting instead)
- Currency amounts (format according to locale)
- Emojis (they're universal)
- Technical identifiers and keys

---

## � **Development Workflow: Ensuring Translations Are Always Done**

### **BEFORE You Write Code**

When planning a new feature or UI element, **think about text first**:

1. **List all user-facing text** that will be needed
2. **Create string resources immediately** in both `values/strings.xml` AND `values-da/strings.xml`
3. **Then** start writing your UI code using `stringResource()`

**✅ This prevents forgetting translations later!**

---

### **Feature Development Checklist**

Use this checklist for every new screen, dialog, or UI component:

#### **Planning Phase:**
- [ ] List all buttons, labels, titles, and messages needed
- [ ] Choose descriptive string resource names (e.g., `feature_action_element`)

#### **Implementation Phase:**
- [ ] Add English strings to `res/values/strings.xml`
- [ ] Add Danish translations to `res/values-da/strings.xml`
- [ ] Use `stringResource(R.string.your_string)` in all Compose code
- [ ] **NEVER** use hardcoded strings like `Text("Hello")`

#### **Code Review/Testing Phase:**
- [ ] Search your changed files for `Text("` to catch hardcoded strings
- [ ] Test app in **both English and Danish** languages
- [ ] Verify all text displays correctly in both languages
- [ ] Check for text overflow/truncation in Danish (often longer than English)

---

### **Quick Self-Check Before Committing**

Run this command to find hardcoded strings in your files:

```powershell
# Search for hardcoded Text() calls in your modified files
git diff --name-only | Select-String "\.kt$" | ForEach-Object {
    Select-String 'Text\s*\(\s*"[^"]+"\s*\)' $_ -CaseSensitive
}
```

**If you see matches, those are untranslated strings that need to be fixed!**

---

### **Common Development Scenarios**

#### **Scenario 1: Adding a New Button**

❌ **WRONG:**
```kotlin
Button(onClick = { /* ... */ }) {
    Text("Save Changes")
}
```

✅ **CORRECT:**
```kotlin
// 1. First add to strings.xml (both languages):
// <string name="button_save_changes">Save Changes</string>  // English
// <string name="button_save_changes">Gem ændringer</string>  // Danish

// 2. Then use in code:
Button(onClick = { /* ... */ }) {
    Text(stringResource(R.string.button_save_changes))
}
```

#### **Scenario 2: Adding a New Screen**

✅ **Best Practice Workflow:**

1. **Create all strings first** (add to both language files):
```xml
<!-- English (values/strings.xml) -->
<string name="my_screen_title">My New Screen</string>
<string name="my_screen_subtitle">This is a description</string>
<string name="my_screen_button_action">Do Something</string>
<string name="my_screen_empty_state">No items yet</string>
<string name="my_screen_empty_hint">Tap + to add</string>

<!-- Danish (values-da/strings.xml) -->
<string name="my_screen_title">Min Nye Skærm</string>
<string name="my_screen_subtitle">Dette er en beskrivelse</string>
<string name="my_screen_button_action">Gør Noget</string>
<string name="my_screen_empty_state">Ingen elementer endnu</string>
<string name="my_screen_empty_hint">Tryk + for at tilføje</string>
```

2. **Then build your screen** using the resources:
```kotlin
@Composable
fun MyNewScreen() {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.my_screen_title)) }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            Text(stringResource(R.string.my_screen_subtitle))
            Button(onClick = { /* ... */ }) {
                Text(stringResource(R.string.my_screen_button_action))
            }
        }
    }
}
```

#### **Scenario 3: Dynamic Text with Parameters**

When text includes variables:

```xml
<!-- English -->
<string name="welcome_message">Welcome, %s!</string>
<string name="items_count">You have %d items</string>

<!-- Danish -->
<string name="welcome_message">Velkommen, %s!</string>
<string name="items_count">Du har %d elementer</string>
```

```kotlin
// In code:
Text(stringResource(R.string.welcome_message, userName))
Text(stringResource(R.string.items_count, itemCount))
```

---

### **Automated Checks (Future Enhancement)**

**To make this even more foolproof, consider:**

1. **Git Pre-commit Hook:**
   - Automatically scan staged `.kt` files for `Text("` patterns
   - Block commit if hardcoded strings are found

2. **CI/CD Check:**
   - Add a build step that fails if hardcoded strings are detected
   - Compare string counts between `values/strings.xml` and `values-da/strings.xml`

3. **IDE Configuration:**
   - Set up Android Studio inspections to warn about hardcoded strings
   - Add custom lint rules for translation enforcement

---

### **Translation Maintenance Tips**

**Monthly Review:**
- Test app in all supported languages
- Look for UI overflow issues (Danish text is often longer)
- Verify new features are translated

**When Adding a New Language:**
1. Copy `values/strings.xml` to new folder (e.g., `values-de/strings.xml`)
2. Translate all strings
3. Test app with new language
4. Check for text truncation/overflow

**Keep Translations Organized:**
- Group related strings together with comments
- Use consistent naming conventions
- Document why certain strings are formatted a specific way

---

## �🛠️ **How to Add New Translatable Text**

### Step 1: Add to English strings.xml

**File:** `app/src/main/res/values/strings.xml`

```xml
<string name="your_string_name">Your English Text</string>
```

**Naming Convention:**
- Use snake_case: `screen_action_element`
- Examples:
  - `chores_add_button` - "Add Chore" button on chores screen
  - `settings_delete_family` - "Delete Family" label in settings
  - `error_network_failed` - Network error message

### Step 2: Add to Danish strings.xml

**File:** `app/src/main/res/values-da/strings.xml`

```xml
<string name="your_string_name">Din Danske Tekst</string>
```

**Important:** The `name` attribute MUST be identical in all language files!

### Step 3: Use in Compose UI

**In Kotlin Compose code:**

```kotlin
import androidx.compose.ui.res.stringResource
import com.jmp.pocketmoneyapp.R

@Composable
fun MyScreen() {
    Text(
        text = stringResource(R.string.your_string_name)
    )
}
```

**With String Formatting:**

If your string has placeholders:

```xml
<!-- strings.xml -->
<string name="welcome_message">Welcome, %s!</string>
<string name="items_count">You have %d items</string>
```

```kotlin
// In Compose
Text(
    text = stringResource(R.string.welcome_message, userName)
)

Text(
    text = stringResource(R.string.items_count, count)
)
```

---

## 🚫 **Common Mistakes to Avoid**

### ❌ **WRONG: Hardcoded Strings**

```kotlin
// DON'T DO THIS!
Text("Delete Chore")
Button(onClick = { /* ... */ }) {
    Text("Save Changes")
}
```

### ✅ **CORRECT: String Resources**

```kotlin
// DO THIS INSTEAD!
Text(stringResource(R.string.delete_chore))
Button(onClick = { /* ... */ }) {
    Text(stringResource(R.string.save_changes))
}
```

---

## 📝 **Special Cases**

### Plurals

For text that changes based on quantity:

**File:** `res/values/strings.xml`

```xml
<plurals name="chores_count">
    <item quantity="one">%d chore</item>
    <item quantity="other">%d chores</item>
</plurals>
```

**Usage:**

```kotlin
val count = 5
Text(
    text = pluralStringResource(R.plurals.chores_count, count, count)
)
```

### String Arrays

For lists of options (e.g., dropdowns):

```xml
<string-array name="recurrence_types">
    <item>None</item>
    <item>Daily</item>
    <item>Weekly</item>
    <item>Monthly</item>
</string-array>
```

**Usage:**

```kotlin
val options = stringArrayResource(R.array.recurrence_types)
```

### HTML Formatting

For text with formatting:

```xml
<string name="terms_and_conditions">
    By clicking Continue, you agree to our
    <b>Terms of Service</b> and <i>Privacy Policy</i>.
</string>
```

**Usage:**

```kotlin
import androidx.compose.ui.text.buildAnnotatedString
import androidx.core.text.HtmlCompat

val htmlText = stringResource(R.string.terms_and_conditions)
val styledText = remember(htmlText) {
    HtmlCompat.fromHtml(htmlText, HtmlCompat.FROM_HTML_MODE_COMPACT)
}
```

---

## ✅ **Verification Checklist**

Before submitting code with new UI text:

- [ ] All user-facing text is in `strings.xml`
- [ ] English translation exists in `res/values/strings.xml`
- [ ] Danish translation exists in `res/values-da/strings.xml`
- [ ] String names match exactly across all language files
- [ ] Formatting placeholders (`%s`, `%d`) are identical in all translations
- [ ] UI uses `stringResource()` to load text
- [ ] No hardcoded English strings in `Text()` composables
- [ ] Content descriptions for accessibility are translated

---

## 🔍 **Finding Hardcoded Strings**

To find hardcoded strings in your code:

**Search Pattern (Regex):**
```
Text\s*\(\s*"[^"]+"|Text\s*\(\s*text\s*=\s*"[^"]+"
```

**In VS Code / Android Studio:**
1. Open Find in Files (Ctrl+Shift+F)
2. Enable Regex
3. Search for: `Text\s*\(\s*"` in `app/src/main/java/**/*.kt`
4. Review all matches and convert to `stringResource()`

---

## 🌐 **Adding a New Language**

To add support for a new language (e.g., German):

1. **Create new values folder:**
   ```
   app/src/main/res/values-de/
   ```

2. **Copy strings.xml:**
   ```
   app/src/main/res/values-de/strings.xml
   ```

3. **Translate all strings** to the target language

4. **Test** by changing device language

**Language Codes:**
- `values` - Default (English)
- `values-da` - Danish
- `values-de` - German
- `values-fr` - French
- `values-es` - Spanish
- `values-sv` - Swedish
- `values-no` - Norwegian

---

## 🤖 **Translation Automation & Enforcement**

### **Automated Checks**

To ensure translations are never forgotten, use the automated check script before committing code.

#### **Quick Check Command**

Run this PowerShell command before committing:

```powershell
.\scripts\check-translations.ps1
```

The script will:
- ✅ Scan all staged `.kt` files for hardcoded `Text("...")` patterns
- ✅ Block the commit if untranslated strings are found
- ✅ Provide a list of files and line numbers with issues

---

### **Setting Up Git Pre-Commit Hook (Optional)**

**Option 1: Manual Check Before Each Commit**

Simply run the check script before every commit:

```powershell
# Check for untranslated strings
.\scripts\check-translations.ps1

# If it passes, commit
git commit -m "Add new feature"
```

**Option 2: Automatic Git Hook (Advanced)**

Set up an automatic pre-commit hook:

1. **Create the hook file:**
   ```powershell
   # Navigate to your project root
   cd "C:\prj\Div kode opkast\PocketMoneyApp"
   
   # Create hooks directory if it doesn't exist
   New-Item -ItemType Directory -Force -Path ".git\hooks"
   
   # Copy the hook
   Copy-Item "scripts\check-translations.ps1" ".git\hooks\pre-commit.ps1"
   ```

2. **Configure Git to run PowerShell scripts:**
   - Edit `.git\hooks\pre-commit` (no extension)
   - Add these lines:
     ```sh
     #!/bin/sh
     powershell.exe -ExecutionPolicy Bypass -File "$0.ps1"
     ```

**Note:** Git hooks are local and not committed to the repository. Each developer needs to set this up individually.

---

### **The Check Script**

The `check-translations.ps1` script is in the `scripts/` folder:

```powershell
#!/usr/bin/env pwsh
# Check for hardcoded strings in Kotlin files

Write-Host "🔍 Checking for untranslated strings..." -ForegroundColor Cyan

# Get all staged .kt files
$stagedFiles = git diff --cached --name-only --diff-filter=ACM | Where-Object { $_ -match '\.kt$' }

if ($stagedFiles.Count -eq 0) {
    Write-Host "✅ No Kotlin files to check" -ForegroundColor Green
    exit 0
}

$foundIssues = $false

foreach ($file in $stagedFiles) {
    if (-not (Test-Path $file)) { continue }
    
    # Search for hardcoded Text() calls
    $matches = Select-String -Path $file -Pattern 'Text\s*\(\s*"[^"]+"\s*\)' -CaseSensitive
    
    if ($matches) {
        $foundIssues = $true
        Write-Host "`n❌ Found hardcoded strings in: $file" -ForegroundColor Red
        foreach ($match in $matches) {
            Write-Host "   Line $($match.LineNumber): $($match.Line.Trim())" -ForegroundColor Yellow
        }
    }
}

if ($foundIssues) {
    Write-Host "`n⚠️  COMMIT BLOCKED: Untranslated strings detected!" -ForegroundColor Red
    Write-Host "See Docs/TRANSLATION_GUIDE.md for help" -ForegroundColor Cyan
    exit 1
} else {
    Write-Host "✅ All Kotlin files use string resources correctly!" -ForegroundColor Green
    exit 0
}
```

---

### **Bypass Hook (Emergency Only)**

In rare emergencies, you can bypass the check:

```powershell
# Skip the hook (NOT RECOMMENDED)
git commit --no-verify -m "Emergency fix"
```

---

### **Customizing the Check**

To adjust what patterns get flagged, edit the regex in `scripts/check-translations.ps1`:

```powershell
# Current pattern (strict):
'Text\s*\(\s*"[^"]+"\s*\)'

# More lenient (allows empty strings):
'Text\s*\(\s*"[^"]+[a-zA-Z][^"]*"\s*\)'

# Also check Icon content descriptions:
'(Text|Icon)\s*\([^,]*,\s*"[^"]+"\s*\)'
```

---

## 🎯 **Current Status**

✅ **Completed:**
- English translations (227 strings)
- Danish translations (227 strings)
- Translation infrastructure set up
- Automated check script created

⏳ **TODO:**
- Convert remaining hardcoded strings to use `stringResource()`
- Add plurals for dynamic counts
- Test language switching throughout the app

---

## 📚 **Resources**

- [Android String Resources Documentation](https://developer.android.com/guide/topics/resources/string-resource)
- [Android Localization Guide](https://developer.android.com/guide/topics/resources/localization)
- [Jetpack Compose String Resources](https://developer.android.com/jetpack/compose/resources)

---

**Last Updated:** April 23, 2026  
**Maintained by:** Development Team
