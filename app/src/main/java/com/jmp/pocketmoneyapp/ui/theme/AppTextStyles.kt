package com.jmp.pocketmoneyapp.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp

/**
 * Centralized text style management for the PocketMoneyApp.
 * 
 * This file defines all text categories and their styling to ensure
 * consistent typography throughout the app.
 * 
 * Usage:
 * - AppText.PageTitle("My Page")
 * - AppText.SectionHeader("My Section")
 * - AppText.Body("Regular text content")
 * 
 * Categories:
 * - Page-level: PageTitle, PageSubtitle
 * - Section-level: SectionHeader, SectionSubheader
 * - Card/Item-level: CardTitle, CardValue, CardLabel
 * - Body text: Body, BodySecondary, BodyEmphasis
 * - Labels: Label, LabelSecondary, Caption
 * - Icons/Emojis: LargeIcon, MediumIcon, SmallIcon
 */
object AppText {
    
    // ==================== PAGE LEVEL ====================
    
    /**
     * Large page title (e.g., "Chores", "Settings")
     * Usage: Main screen titles
     */
    @Composable
    fun PageTitle(
        text: String,
        modifier: Modifier = Modifier,
        color: Color = MaterialTheme.colorScheme.onSurface
    ) {
        Text(
            text = text,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = color,
            modifier = modifier
        )
    }
    
    /**
     * Page subtitle or welcome message
     * Usage: "Welcome, John!", secondary page info
     */
    @Composable
    fun PageSubtitle(
        text: String,
        modifier: Modifier = Modifier,
        color: Color = MaterialTheme.colorScheme.onSurfaceVariant
    ) {
        Text(
            text = text,
            fontSize = 14.sp,
            fontWeight = FontWeight.Normal,
            color = color,
            modifier = modifier
        )
    }
    
    // ==================== SECTION LEVEL ====================
    
    /**
     * Section header within a page
     * Usage: "To Do (5)", "Pending Approval (2)"
     */
    @Composable
    fun SectionHeader(
        text: String,
        modifier: Modifier = Modifier,
        color: Color = MaterialTheme.colorScheme.primary
    ) {
        Text(
            text = text,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = color,
            modifier = modifier
        )
    }
    
    /**
     * Secondary section header
     * Usage: Less prominent section dividers
     */
    @Composable
    fun SectionSubheader(
        text: String,
        modifier: Modifier = Modifier,
        color: Color = MaterialTheme.colorScheme.onSurfaceVariant
    ) {
        Text(
            text = text,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = color,
            modifier = modifier
        )
    }
    
    // ==================== CARD/ITEM LEVEL ====================
    
    /**
     * Title for cards and list items
     * Usage: Chore name, family member name, transaction title
     */
    @Composable
    fun CardTitle(
        text: String,
        modifier: Modifier = Modifier,
        color: Color = MaterialTheme.colorScheme.onSurface,
        maxLines: Int = Int.MAX_VALUE,
        overflow: TextOverflow = TextOverflow.Clip
    ) {
        Text(
            text = text,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = color,
            modifier = modifier,
            maxLines = maxLines,
            overflow = overflow
        )
    }
    
    /**
     * Prominent value display in cards
     * Usage: Money amounts, point totals
     */
    @Composable
    fun CardValue(
        text: String,
        modifier: Modifier = Modifier,
        color: Color = MaterialTheme.colorScheme.primary
    ) {
        Text(
            text = text,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = color,
            modifier = modifier
        )
    }
    
    /**
     * Small label in cards
     * Usage: Status badges, tags, metadata
     */
    @Composable
    fun CardLabel(
        text: String,
        modifier: Modifier = Modifier,
        color: Color = MaterialTheme.colorScheme.onSurfaceVariant
    ) {
        Text(
            text = text,
            fontSize = 11.sp,
            fontWeight = FontWeight.Normal,
            color = color,
            modifier = modifier
        )
    }
    
    // ==================== BODY TEXT ====================
    
    /**
     * Primary body text
     * Usage: Descriptions, main content, paragraphs
     */
    @Composable
    fun Body(
        text: String,
        modifier: Modifier = Modifier,
        color: Color = MaterialTheme.colorScheme.onSurface,
        textAlign: TextAlign? = null
    ) {
        Text(
            text = text,
            fontSize = 12.sp,
            fontWeight = FontWeight.Normal,
            color = color,
            modifier = modifier,
            textAlign = textAlign
        )
    }
    
    /**
     * Secondary/muted body text
     * Usage: Helper text, less important information
     */
    @Composable
    fun BodySecondary(
        text: String,
        modifier: Modifier = Modifier,
        color: Color = MaterialTheme.colorScheme.onSurfaceVariant
    ) {
        Text(
            text = text,
            fontSize = 12.sp,
            fontWeight = FontWeight.Normal,
            color = color,
            modifier = modifier
        )
    }
    
    /**
     * Emphasized body text
     * Usage: Important information that needs to stand out
     */
    @Composable
    fun BodyEmphasis(
        text: String,
        modifier: Modifier = Modifier,
        color: Color = MaterialTheme.colorScheme.onSurface
    ) {
        Text(
            text = text,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = color,
            modifier = modifier
        )
    }
    
    // ==================== LABELS & CAPTIONS ====================
    
    /**
     * Standard label
     * Usage: Form labels, field names, button text
     */
    @Composable
    fun Label(
        text: String,
        modifier: Modifier = Modifier,
        color: Color = MaterialTheme.colorScheme.onSurface
    ) {
        Text(
            text = text,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = color,
            modifier = modifier
        )
    }
    
    /**
     * Secondary/smaller label
     * Usage: Subtle labels, hints
     */
    @Composable
    fun LabelSecondary(
        text: String,
        modifier: Modifier = Modifier,
        color: Color = MaterialTheme.colorScheme.onSurfaceVariant
    ) {
        Text(
            text = text,
            fontSize = 11.sp,
            fontWeight = FontWeight.Normal,
            color = color,
            modifier = modifier
        )
    }
    
    /**
     * Small caption text
     * Usage: Timestamps, metadata, fine print
     */
    @Composable
    fun Caption(
        text: String,
        modifier: Modifier = Modifier,
        color: Color = MaterialTheme.colorScheme.onSurfaceVariant
    ) {
        Text(
            text = text,
            fontSize = 10.sp,
            fontWeight = FontWeight.Normal,
            color = color,
            modifier = modifier
        )
    }
    
    // ==================== BUTTONS ====================
    
    /**
     * Primary button text
     * Usage: Main action buttons
     */
    @Composable
    fun ButtonPrimary(
        text: String,
        modifier: Modifier = Modifier
    ) {
        Text(
            text = text,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            modifier = modifier
        )
    }
    
    /**
     * Secondary button text
     * Usage: Outlined buttons, text buttons
     */
    @Composable
    fun ButtonSecondary(
        text: String,
        modifier: Modifier = Modifier
    ) {
        Text(
            text = text,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            modifier = modifier
        )
    }
    
    // ==================== ICONS & EMOJIS ====================
    
    /**
     * Large decorative emoji/icon
     * Usage: Empty states, welcome screens, large illustrations
     */
    @Composable
    fun LargeIcon(
        text: String,
        modifier: Modifier = Modifier
    ) {
        Text(
            text = text,
            fontSize = 48.sp,
            modifier = modifier
        )
    }
    
    /**
     * Medium emoji/icon
     * Usage: Page headers, prominent decorations
     */
    @Composable
    fun MediumIcon(
        text: String,
        modifier: Modifier = Modifier
    ) {
        Text(
            text = text,
            fontSize = 32.sp,
            modifier = modifier
        )
    }
    
    /**
     * Small inline emoji/icon
     * Usage: Inline with text, small indicators
     */
    @Composable
    fun SmallIcon(
        text: String,
        modifier: Modifier = Modifier
    ) {
        Text(
            text = text,
            fontSize = 16.sp,
            modifier = modifier
        )
    }

    /**
     * Top bar action emoji/icon
     * Usage: IconButton content in AppTopBar action slots
     */
    @Composable
    fun TopBarIcon(
        text: String,
        modifier: Modifier = Modifier
    ) {
        Text(
            text = text,
            fontSize = 20.sp,
            modifier = modifier
        )
    }
    
    // ==================== SPECIAL CASES ====================
    
    /**
     * Error message text
     * Usage: Error states, validation messages
     */
    @Composable
    fun Error(
        text: String,
        modifier: Modifier = Modifier
    ) {
        Text(
            text = text,
            fontSize = 12.sp,
            fontWeight = FontWeight.Normal,
            color = MaterialTheme.colorScheme.error,
            modifier = modifier
        )
    }
    
    /**
     * Success message text
     * Usage: Success states, confirmations
     */
    @Composable
    fun Success(
        text: String,
        modifier: Modifier = Modifier
    ) {
        Text(
            text = text,
            fontSize = 12.sp,
            fontWeight = FontWeight.Normal,
            color = MaterialTheme.colorScheme.primary,
            modifier = modifier
        )
    }
    
    /**
     * Empty state message
     * Usage: Main text in empty state screens
     */
    @Composable
    fun EmptyStateTitle(
        text: String,
        modifier: Modifier = Modifier,
        color: Color = MaterialTheme.colorScheme.onSurface
    ) {
        Text(
            text = text,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = color,
            modifier = modifier,
            textAlign = TextAlign.Center
        )
    }
    
    /**
     * Empty state hint
     * Usage: Secondary text in empty states
     */
    @Composable
    fun EmptyStateHint(
        text: String,
        modifier: Modifier = Modifier,
        color: Color = MaterialTheme.colorScheme.onSurfaceVariant
    ) {
        Text(
            text = text,
            fontSize = 12.sp,
            fontWeight = FontWeight.Normal,
            color = color,
            modifier = modifier,
            textAlign = TextAlign.Center
        )
    }
}
