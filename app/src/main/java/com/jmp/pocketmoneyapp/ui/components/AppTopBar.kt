package com.jmp.pocketmoneyapp.ui.components

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.google.firebase.appdistribution.FirebaseAppDistribution
import com.jmp.pocketmoneyapp.BuildConfig
import com.jmp.pocketmoneyapp.R

/**
 * Shared top app bar used across all screens.
 *
 * Provides a consistent layout with:
 * - Optional back arrow (shown when [onNavigateBack] is not null)
 * - Screen-specific action icons via the [actions] slot
 * - A feedback icon (✏️) that triggers Firebase App Distribution in-app feedback
 *
 * The feedback icon is only shown in debug builds. It is hidden entirely in release builds.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(
    title: @Composable () -> Unit,
    onNavigateBack: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {}
) {
    TopAppBar(
        title = title,
        navigationIcon = {
            if (onNavigateBack != null) {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.back),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        },
        actions = {
            actions()
            if (BuildConfig.DEBUG) {
                IconButton(onClick = {
                    FirebaseAppDistribution.getInstance().startFeedback(R.string.feedback_notice_text)
                }) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = stringResource(R.string.feedback_button_description),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    )
}
