# AppText Style Guide

**Centralized text styling system for PocketMoneyApp**

---

## 📋 **Overview**

The `AppText` object provides a consistent, categorized approach to text styling throughout the app. Instead of manually setting `fontSize`, `fontWeight`, and `color` on every `Text()` composable, use the pre-defined categories.

---

## 🎯 **Benefits**

1. ✅ **Consistency**: All text of the same type looks identical
2. ✅ **Maintainability**: Change one place, update everywhere
3. ✅ **Readability**: Code is more semantic (`AppText.SectionHeader()` vs `Text(fontSize = 15.sp, fontWeight = FontWeight.Bold)`)
4. ✅ **Standards**: Forces developers to use standard text sizes

---

## 📚 **Text Categories**

### **Page Level**

Used for main screen titles and subtitles.

```kotlin
// Large page title
AppText.PageTitle("Chores")

// Page subtitle
AppText.PageSubtitle("Welcome, John!")
```

**When to use:**
- Main screen titles in TopAppBar
- Welcome messages
- Page-level headers

---

### **Section Level**

Used for dividing content into sections within a page.

```kotlin
// Primary section header
AppText.SectionHeader("To Do (5)")

// Secondary section header
AppText.SectionSubheader("Optional Tasks")
```

**When to use:**
- Section dividers in lists
- Category headers
- Grouped content labels

---

### **Card/Item Level**

Used within cards and list items.

```kotlin
// Card title
AppText.CardTitle("Clean your room")

// Prominent value
AppText.CardValue("25.00 kr.")

// Small metadata label
AppText.CardLabel("Due: May 1, 2026")
```

**When to use:**
- Chore names in cards
- Transaction titles
- Family member names
- Money amounts
- Status labels
- Dates and metadata

---

### **Body Text**

Used for descriptions and content.

```kotlin
// Primary body text
AppText.Body("This is a description of the chore.")

// Secondary/muted text
AppText.BodySecondary("Optional additional information")

// Emphasized body text
AppText.BodyEmphasis("Important note!")
```

**When to use:**
- Chore descriptions
- Dialog messages
- Explanatory text
- Instructions

---

### **Labels & Captions**

Used for form fields and small text.

```kotlin
// Standard label
AppText.Label("Name")

// Secondary label
AppText.LabelSecondary("Optional")

// Small caption
AppText.Caption("Last updated: 2 hours ago")
```

**When to use:**
- Form field labels
- Timestamps
- Metadata
- Fine print

---

### **Buttons**

Used for button text.

```kotlin
// Primary button
Button(onClick = { }) {
    AppText.ButtonPrimary("Save Changes")
}

// Secondary button
OutlinedButton(onClick = { }) {
    AppText.ButtonSecondary("Cancel")
}
```

**When to use:**
- All button text
- FAB labels

---

### **Icons & Emojis**

Used for decorative emojis and icons.

```kotlin
// Large empty state icon
AppText.LargeIcon("🏠")

// Medium page header icon
AppText.MediumIcon("💰")

// Small inline icon
AppText.SmallIcon("👤")
```

**When to use:**
- Empty state illustrations
- Page header decorations
- Inline emoji indicators

---

### **Special Cases**

Used for specific UI states.

```kotlin
// Error message
AppText.Error("Invalid PIN")

// Success message
AppText.Success("Family created!")

// Empty state
AppText.EmptyStateTitle("No chores yet")
AppText.EmptyStateHint("Tap + to add your first chore")
```

**When to use:**
- Error messages
- Success confirmations
- Empty state screens

---

## 🔧 **Usage Examples**

### **Before (Old Way):**

```kotlin
Text(
    text = "Clean your room",
    fontSize = 15.sp,
    fontWeight = FontWeight.Bold,
    color = MaterialTheme.colorScheme.onSurface
)
Text(
    text = "This is a description",
    fontSize = 12.sp,
    fontWeight = FontWeight.Normal,
    color = MaterialTheme.colorScheme.onSurfaceVariant
)
Text(
    text = "25.00 kr.",
    fontSize = 16.sp,
    fontWeight = FontWeight.Bold,
    color = MaterialTheme.colorScheme.primary
)
```

### **After (New Way):**

```kotlin
AppText.CardTitle("Clean your room")
AppText.BodySecondary("This is a description")
AppText.CardValue("25.00 kr.")
```

---

## 📐 **Font Size Reference**

| Category | Size | Weight | Use Case |
|----------|------|--------|----------|
| **PageTitle** | 22sp | Bold | Screen titles |
| **PageSubtitle** | 14sp | Normal | Welcome messages |
| **SectionHeader** | 15sp | Bold | Section dividers |
| **SectionSubheader** | 13sp | Medium | Minor sections |
| **CardTitle** | 15sp | Bold | Item titles |
| **CardValue** | 16sp | Bold | Money/numbers |
| **CardLabel** | 11sp | Normal | Metadata |
| **Body** | 12sp | Normal | Descriptions |
| **BodySecondary** | 12sp | Normal | Helper text |
| **BodyEmphasis** | 12sp | Medium | Important text |
| **Label** | 12sp | Medium | Form labels |
| **LabelSecondary** | 11sp | Normal | Subtle labels |
| **Caption** | 10sp | Normal | Fine print |
| **ButtonPrimary** | 14sp | Medium | Main buttons |
| **ButtonSecondary** | 13sp | Medium | Alt buttons |
| **LargeIcon** | 48sp | - | Big emojis |
| **MediumIcon** | 32sp | - | Page icons |
| **SmallIcon** | 16sp | - | Inline icons |

---

## ⚙️ **Advanced Usage**

### **Override Colors**

All composables accept a `color` parameter:

```kotlin
AppText.SectionHeader(
    text = "Warning Section",
    color = MaterialTheme.colorScheme.error
)
```

### **Add Modifiers**

All composables accept a `modifier` parameter:

```kotlin
AppText.Body(
    text = "Description",
    modifier = Modifier
        .padding(16.dp)
        .fillMaxWidth()
)
```

### **Text Overflow**

CardTitle supports overflow handling:

```kotlin
AppText.CardTitle(
    text = "Very long title that might overflow",
    maxLines = 1,
    overflow = TextOverflow.Ellipsis
)
```

---

## 🚀 **Migration Guide**

To migrate existing screens:

1. **Import AppText:**
   ```kotlin
   import com.jmp.pocketmoneyapp.ui.theme.AppText
   ```

2. **Replace Text() with AppText.Category():**
   ```kotlin
   // OLD:
   Text("Section Header", fontSize = 15.sp, fontWeight = FontWeight.Bold)
   
   // NEW:
   AppText.SectionHeader("Section Header")
   ```

3. **Remove manual fontSize and fontWeight:**
   - Let AppText handle styling
   - Only override when absolutely necessary

4. **Use stringResource() with AppText:**
   ```kotlin
   AppText.CardTitle(stringResource(R.string.chore_name))
   ```

---

## ✅ **Checklist for New Screens**

When creating a new screen:

- [ ] Import `AppText`
- [ ] Use `AppText.PageTitle()` for screen title
- [ ] Use `AppText.SectionHeader()` for section dividers
- [ ] Use `AppText.CardTitle()` for list item titles
- [ ] Use `AppText.Body()` for descriptions
- [ ] Use `AppText.ButtonPrimary()` for button text
- [ ] Use `AppText.LargeIcon()` for empty state emojis
- [ ] Avoid manual `fontSize` specifications

---

## 📝 **Adding New Categories**

If you need a new text style that doesn't fit existing categories:

1. Add it to `AppTextStyles.kt`
2. Document it in this guide
3. Update the Font Size Reference table
4. Consider if it can replace an existing category

---

## 🔍 **Finding Non-Standard Text**

To find text that doesn't use AppText:

```powershell
# Search for manual fontSize in Kotlin files
Select-String -Path "app\src\main\java\**\*.kt" -Pattern "fontSize = \d+\.sp" -Recurse
```

---

## 📋 **Migration Example: Complete Screen**

### **Before (Manual Styling):**

```kotlin
import androidx.compose.material3.Text
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Family name header
Text(
    text = authState.family?.name ?: "Loading...",
    fontSize = 16.sp,
    fontWeight = FontWeight.Bold,
    color = MaterialTheme.colorScheme.onPrimaryContainer
)

// Welcome message
Text(
    text = "Welcome, ${authState.user?.name}!",
    fontSize = 12.sp,
    color = MaterialTheme.colorScheme.onPrimaryContainer
)

// Empty state icon
Text(
    text = stringResource(R.string.chores_empty_icon),
    fontSize = 48.sp
)

// Section header
Text(
    text = "📝 My Chores (${myChores.size})",
    fontSize = 15.sp,
    fontWeight = FontWeight.Bold,
    color = MaterialTheme.colorScheme.primary,
    modifier = Modifier.padding(vertical = 8.dp)
)

// Chore title in card
Text(
    text = chore.name,
    fontSize = 15.sp,
    fontWeight = FontWeight.Bold
)

// Chore description
Text(
    text = chore.description,
    fontSize = 12.sp,
    color = MaterialTheme.colorScheme.onSurfaceVariant
)

// Chore value
Text(
    text = String.format("%.2f kr.", chore.value),
    fontSize = 16.sp,
    fontWeight = FontWeight.Bold,
    color = MaterialTheme.colorScheme.primary
)
```

### **After (Using AppText):**

```kotlin
import com.jmp.pocketmoneyapp.ui.theme.AppText

// Family name header
AppText.PageSubtitle(
    text = authState.family?.name ?: "Loading...",
    color = MaterialTheme.colorScheme.onPrimaryContainer
)

// Welcome message
AppText.BodySecondary(
    text = "Welcome, ${authState.user?.name}!",
    color = MaterialTheme.colorScheme.onPrimaryContainer
)

// Empty state icon
AppText.LargeIcon(
    text = stringResource(R.string.chores_empty_icon)
)

// Section header
AppText.SectionHeader(
    text = "📝 My Chores (${myChores.size})",
    modifier = Modifier.padding(vertical = 8.dp)
)

// Chore title in card
AppText.CardTitle(text = chore.name)

// Chore description
AppText.BodySecondary(text = chore.description)

// Chore value
AppText.CardValue(text = String.format("%.2f kr.", chore.value))
```

**Result:** 50% less code, automatic consistency, better readability!

---

## 🚀 **Step-by-Step Migration Process**

### **Step 1: Add Import**
```kotlin
import com.jmp.pocketmoneyapp.ui.theme.AppText
```

### **Step 2: Identify Text Categories**

Go through your screen and categorize each `Text()`:
- Page title? → `AppText.PageTitle()`
- Section header? → `AppText.SectionHeader()`
- Card title? → `AppText.CardTitle()`
- Description? → `AppText.Body()` or `AppText.BodySecondary()`
- Label? → `AppText.CardLabel()` or `AppText.LabelSecondary()`
- Emoji? → `AppText.LargeIcon()`, `AppText.MediumIcon()`, or `AppText.SmallIcon()`

### **Step 3: Replace Text() Calls**

```kotlin
// BEFORE
Text(
    text = "My Title",
    fontSize = 15.sp,
    fontWeight = FontWeight.Bold,
    color = someColor,
    modifier = Modifier.padding(8.dp)
)

// AFTER
AppText.CardTitle(
    text = "My Title",
    color = someColor,  // Only if custom color needed
    modifier = Modifier.padding(8.dp)
)
```

### **Step 4: Remove fontSize and fontWeight**

AppText handles these automatically.

### **Step 5: Test**

Build and visually verify the screen looks correct.

---

## ⚠️ **Common Migration Pitfalls**

### **1. Using Wrong Category**

```kotlin
// ❌ WRONG: Using Body for a title
AppText.Body("Section Header")

// ✅ CORRECT: Using SectionHeader
AppText.SectionHeader("Section Header")
```

### **2. Forcing Custom Sizes**

```kotlin
// ❌ WRONG: Overriding AppText with custom size
Text("Title", fontSize = 18.sp)  // Don't do this!

// ✅ CORRECT: Use appropriate AppText category
AppText.SectionHeader("Title")
```

### **3. Not Using AppText for Buttons**

```kotlin
// ❌ WRONG: Manual button text
Button(onClick = {}) {
    Text("Save", fontSize = 14.sp)
}

// ✅ CORRECT: Use AppText.ButtonPrimary
Button(onClick = {}) {
    AppText.ButtonPrimary("Save")
}
```

---

## 📝 **Migration Checklist**

When migrating a screen:

- [ ] Import `AppText`
- [ ] Replace all page titles with `AppText.PageTitle()`
- [ ] Replace all section headers with `AppText.SectionHeader()`
- [ ] Replace all card titles with `AppText.CardTitle()`
- [ ] Replace all descriptions with `AppText.Body()` or `AppText.BodySecondary()`
- [ ] Replace all labels with `AppText.CardLabel()` or `AppText.LabelSecondary()`
- [ ] Replace all button text with `AppText.ButtonPrimary()` or `AppText.ButtonSecondary()`
- [ ] Replace all emojis with `AppText.LargeIcon()`, `AppText.MediumIcon()`, or `AppText.SmallIcon()`
- [ ] Remove manual `fontSize` and `fontWeight` parameters
- [ ] Build and test
- [ ] Verify visual appearance is correct

---

**Last Updated:** April 23, 2026
