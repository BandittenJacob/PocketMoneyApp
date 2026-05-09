package com.jmp.pocketmoneyapp.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.jmp.pocketmoneyapp.ui.theme.AppText

/**
 * Standardized empty state component.
 * 
 * Displays a large icon, title, and optional hint text when a list or screen is empty.
 * 
 * @param icon Large emoji or icon to display (e.g., "📝", "🏠")
 * @param title Main message (e.g., "No chores yet")
 * @param hint Optional secondary message (e.g., "Tap + to add your first chore")
 * @param modifier Optional modifier for the container
 */
@Composable
fun EmptyState(
    icon: String,
    title: String,
    hint: String? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        AppText.LargeIcon(text = icon)
        
        Spacer(modifier = Modifier.height(16.dp))
        
        AppText.EmptyStateTitle(text = title)
        
        if (hint != null) {
            Spacer(modifier = Modifier.height(8.dp))
            AppText.EmptyStateHint(text = hint)
        }
    }
}

/**
 * Standardized section header component.
 * 
 * Used to divide content into labeled sections within a screen.
 * 
 * @param text The section header text (e.g., "To Do (5)", "⏳ Pending Approval (2)")
 * @param modifier Optional modifier for the text
 * @param color Optional color override
 */
@Composable
fun SectionHeader(
    text: String,
    modifier: Modifier = Modifier,
    color: Color? = null
) {
    AppText.SectionHeader(
        text = text,
        modifier = modifier.padding(vertical = 8.dp),
        color = color ?: androidx.compose.material3.MaterialTheme.colorScheme.primary
    )
}
