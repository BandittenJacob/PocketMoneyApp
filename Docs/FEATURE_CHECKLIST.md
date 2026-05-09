# Feature Development Checklist

**Use this checklist for EVERY new feature, screen, or UI component!**

---

## ✅ **Before You Code**

- [ ] List all user-facing text needed (buttons, labels, messages, etc.)
- [ ] Choose descriptive string resource names
- [ ] Add strings to **both** language files:
  - [ ] `app/src/main/res/values/strings.xml` (English)
  - [ ] `app/src/main/res/values-da/strings.xml` (Danish)

---

## ✅ **While Coding**

- [ ] Use `stringResource(R.string.your_string)` for ALL text
- [ ] Use `AppText.Category()` for text styling (see APPTEXT_STYLE_GUIDE.md)
- [ ] Use reusable components when available (see COMPONENT_GUIDE.md)
- [ ] **NEVER** use `Text("hardcoded text")` or manual `fontSize`
- [ ] Import: `import androidx.compose.ui.res.stringResource`
- [ ] Import: `import com.jmp.pocketmoneyapp.R`
- [ ] Import: `import com.jmp.pocketmoneyapp.ui.theme.AppText`

**Text Styling:**
- Page titles → `AppText.PageTitle()`
- Section headers → `AppText.SectionHeader()` or `SectionHeader()` component
- Card/item titles → `AppText.CardTitle()`
- Descriptions → `AppText.Body()` or `AppText.BodySecondary()`
- Buttons → `AppText.ButtonPrimary()` or `AppText.ButtonSecondary()`
- Empty states → `EmptyState()` component (from `ui/components/LayoutComponents.kt`)

**Reusable Components:**
- Top bar → `AppTopBar()` (from `ui/components/AppTopBar.kt`) — **use this on every new screen**
- Chore card → `ChoreCard()` (from `ui/components/ChoreCard.kt`)
- Status chips → `StatusChip()` or `ChoreTypeBadge()` (from `ui/components/StatusComponents.kt`)
- Empty lists → `EmptyState()` (from `ui/components/LayoutComponents.kt`)
- Section dividers → `SectionHeader()` (from `ui/components/LayoutComponents.kt`)

---

## ✅ **Before Committing**

- [ ] Search your files for `Text("` to find hardcoded strings
- [ ] Build the app successfully
- [ ] Test in **English** (device language = English)
- [ ] Test in **Danish** (device language = Dansk)
- [ ] Check for text overflow/truncation
- [ ] Verify all buttons, labels, and messages are translated

---

## 🔍 **Quick Self-Check Command**

Run in PowerShell from project root:

```powershell
# Find hardcoded strings in your changed files
git diff --name-only | Select-String "\.kt$" | ForEach-Object {
    Select-String 'Text\s*\(\s*"' $_
}

# Or use the automated script:
.\scripts\check-translations.ps1
```

**If this returns results → You have untranslated strings to fix!**

---

## 📝 **Quick Reference**

### **Adding a String:**

```xml
<!-- English: app/src/main/res/values/strings.xml -->
<string name="feature_action_label">Do Something</string>

<!-- Danish: app/src/main/res/values-da/strings.xml -->
<string name="feature_action_label">Gør Noget</string>
```

### **Using in Compose:**

```kotlin
import androidx.compose.ui.res.stringResource
import com.jmp.pocketmoneyapp.R

Text(stringResource(R.string.feature_action_label))
Button(onClick = { }) {
    Text(stringResource(R.string.feature_action_label))
}
```

### **With Parameters:**

```xml
<string name="welcome_user">Welcome, %s!</string>
<string name="item_count">You have %d items</string>
```

```kotlin
Text(stringResource(R.string.welcome_user, userName))
Text(stringResource(R.string.item_count, count))
```

---

## ⚠️ **Common Mistakes**

| ❌ WRONG | ✅ CORRECT |
|----------|-----------|
| `Text("Save")` | `Text(stringResource(R.string.button_save))` |
| `title = { Text("Settings") }` | `title = { Text(stringResource(R.string.settings_title)) }` |
| `Icon(Icons.Default.Add, "Add")` | `Icon(Icons.Default.Add, stringResource(R.string.add))` |
| Hardcoding dropdown values | Using `stringResource()` in dropdown lists |

---

## 📚 **Full Documentation**

See [TRANSLATION_GUIDE.md](TRANSLATION_GUIDE.md) for complete details.

---

**Last Updated:** April 23, 2026
