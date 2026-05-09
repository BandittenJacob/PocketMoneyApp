# Component System Guide

**Reusable UI Components for PocketMoneyApp**

---

## 📋 **Overview**

The component system provides reusable, standardized UI elements that ensure consistency across the app. All components follow DRY principles and use `AppText` for text styling.

**Component Location:** `app/src/main/java/com/jmp/pocketmoneyapp/ui/components/`

---

## 🏗️ **Architecture**

```
ui/
├── theme/                    # Design system (colors, typography, text styles)
│   ├── AppText.kt           # Text styling categories
│   ├── Type.kt              # Typography definitions
│   ├── Color.kt             # Color scheme
│   └── Theme.kt             # Material3 theme
│
├── components/               # Reusable UI components (NEW)
│   ├── ChoreCard.kt         # Standardized chore card
│   ├── StatusComponents.kt  # Status chip & type badge
│   └── LayoutComponents.kt  # Empty state & section headers
│
└── [screens]/                # Screen-specific UI
    ├── chores/
    ├── auth/
    └── ...
```

**Layer Relationship:**
- **Theme Layer** (`ui/theme/`) = Design decisions (colors, text sizes)
- **Component Layer** (`ui/components/`) = Reusable UI blocks (uses theme)
- **Screen Layer** (`ui/[screens]/`) = Complete screens (uses components)

---

## 🎨 **Available Components**

### **1. ChoreCard**

Standardized card for displaying chore information.

**File:** `ui/components/ChoreCard.kt`

**Usage:**
```kotlin
import com.jmp.pocketmoneyapp.ui.components.ChoreCard

ChoreCard(
    chore = chore,
    members = familyMembers,
    onMarkComplete = { /* ... */ },
    onEdit = { /* ... */ },
    onApprove = { /* ... */ },
    onAssign = { assignTo -> /* ... */ },
    isParent = true
)
```

**Parameters:**
- `chore: Chore` - The chore data to display
- `members: List<FamilyMember>` - Family members for assignment dropdown
- `onMarkComplete: (() -> Unit)?` - Callback when marked complete (optional)
- `onEdit: (() -> Unit)?` - Callback when edited (optional, parents only)
- `onRevert: (() -> Unit)?` - Callback when reverted (optional)
- `onApprove: (() -> Unit)?` - Callback when approved (optional, parents only)
- `onAssign: ((String) -> Unit)?` - Callback for assign/claim/reassign (optional)
- `isParent: Boolean` - Whether current user is a parent
- `modifier: Modifier` - Optional modifier (default = Modifier)

**Features:**
- ✅ Automatic status-based card background color
- ✅ Type badge (🔁 recurring or 1x one-time)
- ✅ Status chip with proper contrast
- ✅ Role-based button display (parent vs child)
- ✅ Smart assignment buttons (Claim/Assign/Reassign)
- ✅ Uses AppText for all text styling

---

### **2. StatusChip**

Displays chore status with accessibility-compliant colors.

**File:** `ui/components/StatusComponents.kt`

**Usage:**
```kotlin
import com.jmp.pocketmoneyapp.ui.components.StatusChip

StatusChip(status = ChoreStatus.COMPLETED)
```

**Parameters:**
- `status: ChoreStatus` - The chore status (PENDING/COMPLETED/APPROVED/PAID)
- `modifier: Modifier` - Optional modifier

**Colors:**
| Status | Background | Text Color |
|--------|------------|------------|
| PENDING | surfaceVariant | onSurfaceVariant |
| COMPLETED | tertiaryContainer | onTertiaryContainer |
| APPROVED | primaryContainer | onPrimaryContainer |
| PAID | secondaryContainer | onSecondaryContainer |

**Benefits:**
- ✅ WCAG-compliant contrast ratios
- ✅ Colorblind-friendly
- ✅ Consistent across app

---

### **3. ChoreTypeBadge**

Badge showing if chore is recurring (🔁) or one-time (1x).

**File:** `ui/components/StatusComponents.kt`

**Usage:**
```kotlin
import com.jmp.pocketmoneyapp.ui.components.ChoreTypeBadge

ChoreTypeBadge(isRecurring = chore.templateId != null)
```

**Parameters:**
- `isRecurring: Boolean` - True for recurring, false for one-time
- `modifier: Modifier` - Optional modifier

---

### **4. EmptyState**

Standardized empty state component for lists and screens.

**File:** `ui/components/LayoutComponents.kt`

**Usage:**
```kotlin
import com.jmp.pocketmoneyapp.ui.components.EmptyState

EmptyState(
    icon = "📝",
    title = "No chores yet",
    hint = "Tap + to add your first chore"
)
```

**Parameters:**
- `icon: String` - Large emoji or icon (e.g., "📝", "🏠")
- `title: String` - Main message
- `hint: String?` - Optional secondary message
- `modifier: Modifier` - Optional modifier

**Where to Use:**
- Empty chore lists
- Empty family member lists
- No search results
- Any empty collection

---

### **5. SectionHeader**

Standardized section divider for lists.

**File:** `ui/components/LayoutComponents.kt`

**Usage:**
```kotlin
import com.jmp.pocketmoneyapp.ui.components.SectionHeader

SectionHeader(text = "To Do (5)")

// With custom color
SectionHeader(
    text = "⏳ Pending Approval (2)",
    color = MaterialTheme.colorScheme.tertiary
)
```

**Parameters:**
- `text: String` - Section header text
- `modifier: Modifier` - Optional modifier
- `color: Color?` - Optional color (default = primary)

---

## 💡 **Usage Guidelines**

### **When to Create a Component**

Create a new component when:
- ✅ UI element is used in multiple screens
- ✅ UI element is complex (>30 lines)
- ✅ UI element has consistent styling/behavior
- ✅ UI element will be reused in future features

**Don't** create a component for:
- ❌ Screen-specific, one-off UI
- ❌ Simple single-line elements
- ❌ Highly variable layouts

---

### **Component Design Principles**

1. **DRY (Don't Repeat Yourself)**
   - Extract repeated UI patterns
   - Single source of truth

2. **Composability**
   - Accept modifiers for flexibility
   - Use optional parameters for variants
   - Provide sensible defaults

3. **Accessibility**
   - Use proper color contrast
   - Include content descriptions
   - Support text scaling

4. **Consistency**
   - Use `AppText` for all text
   - Follow Material3 guidelines
   - Match existing component patterns

---

## 🚀 **Creating a New Component**

### **Step 1: Identify the Pattern**

Find UI code that's repeated across screens:
```kotlin
// Before: Repeated in multiple screens
Card {
    Column {
        AppText.CardTitle(title)
        AppText.BodySecondary(description)
        Button(onClick = { }) { ... }
    }
}
```

### **Step 2: Extract to Component**

Create a new file in `ui/components/`:
```kotlin
// ui/components/MyComponent.kt
package com.jmp.pocketmoneyapp.ui.components

@Composable
fun MyComponent(
    title: String,
    description: String,
    onAction: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier) {
        Column {
            AppText.CardTitle(title)
            AppText.BodySecondary(description)
            Button(onClick = onAction) { 
                AppText.ButtonPrimary("Action")
            }
        }
    }
}
```

### **Step 3: Document It**

Add KDoc comments:
```kotlin
/**
 * Brief description of the component.
 * 
 * Longer description explaining use cases and behavior.
 * 
 * @param title Main title text
 * @param description Secondary description
 * @param onAction Callback when action button is clicked
 * @param modifier Optional modifier for the card
 */
@Composable
fun MyComponent( ... )
```

### **Step 4: Update Screens**

Replace old code with component:
```kotlin
// Before
Card { ... lots of code ... }

// After
import com.jmp.pocketmoneyapp.ui.components.MyComponent

MyComponent(
    title = "Title",
    description = "Description",
    onAction = { /* ... */ }
)
```

### **Step 5: Update Documentation**

Add your component to this guide!

---

## 📝 **Component Checklist**

When creating a new component:

- [ ] Named clearly (describes what it is)
- [ ] Located in `ui/components/`
- [ ] Uses `AppText` for text styling
- [ ] Accepts `modifier: Modifier` parameter
- [ ] Has default parameter values where appropriate
- [ ] Includes KDoc comments
- [ ] Follows Material3 guidelines
- [ ] Tested in both light and dark themes
- [ ] Tested with different text sizes
- [ ] Added to this documentation

---

## 🎯 **Benefits of Component System**

### **Before Components:**
```kotlin
// ChoresScreen.kt - 700 lines
// ChoreLibraryScreen.kt - 600 lines
// DashboardScreen.kt - 500 lines
// Total: 1800 lines, lots of duplication
```

### **After Components:**
```kotlin
// ChoreCard.kt - 250 lines (reusable)
// ChoresScreen.kt - 450 lines (uses ChoreCard)
// ChoreLibraryScreen.kt - 350 lines (uses ChoreCard)
// DashboardScreen.kt - 300 lines (uses EmptyState, SectionHeader)
// Total: 1350 lines, DRY principles followed
```

**Savings:** ~450 lines of code, easier maintenance, guaranteed consistency!

---

## 🔍 **Finding Opportunities**

To find code that should be componentized:

```powershell
# Find duplicate UI patterns
Select-String -Path "app\src\main\java\**\*.kt" -Pattern "Card\s*\{" -Recurse

# Find repeated button patterns
Select-String -Path "app\src\main\java\**\*.kt" -Pattern "Button\s*\(" -Recurse
```

Look for:
- 🔍 Similar Card layouts across screens
- 🔍 Repeated dialog patterns
- 🔍 Common list item layouts
- 🔍 Shared empty states

---

## 📚 **Related Documentation**

- [APPTEXT_STYLE_GUIDE.md](APPTEXT_STYLE_GUIDE.md) - Text styling standards
- [FEATURE_CHECKLIST.md](FEATURE_CHECKLIST.md) - Development checklist
- [Material3 Components](https://m3.material.io/components) - Official guide

---

**Last Updated:** April 23, 2026
