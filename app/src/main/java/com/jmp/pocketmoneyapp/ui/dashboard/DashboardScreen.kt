package com.jmp.pocketmoneyapp.ui.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.canopas.lib.showcase.IntroShowcase
import com.canopas.lib.showcase.component.ShowcaseStyle
import com.jmp.pocketmoneyapp.R
import com.jmp.pocketmoneyapp.data.repository.PreferencesManager
import com.jmp.pocketmoneyapp.ui.components.AppTopBar
import com.jmp.pocketmoneyapp.data.model.UserRole
import com.jmp.pocketmoneyapp.ui.family.FamilyDeletionApprovalScreen
import com.jmp.pocketmoneyapp.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: AuthViewModel,
    onChoresClick: () -> Unit,
    onFamilyMembersClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onSignOut: () -> Unit
) {
    val authState by viewModel.authState.collectAsState()
    val context = LocalContext.current
    val versionName = remember {
        try { context.packageManager.getPackageInfo(context.packageName, 0).versionName }
        catch (e: Exception) { "Unknown" }
    }
    
    val isParent = authState.user?.role == UserRole.PARENT
    val hasPendingDeletion = authState.family?.deletionRequest?.active == true
    
    val prefs = remember { PreferencesManager(context) }
    var showTour by remember { mutableStateOf(!prefs.hasSeenDashboardTour) }
    
    // Show deletion approval screen INSTEAD of normal dashboard for parents
    if (isParent && hasPendingDeletion && authState.family != null && authState.user != null) {
        FamilyDeletionApprovalScreen(
            deletionRequest = authState.family!!.deletionRequest!!,
            familyName = authState.family!!.name,
            currentUserId = authState.user!!.id,
            viewModel = viewModel,
            onDismiss = {
                // After dismissal (cancel or complete), user may be signed out
                if (!authState.isAuthenticated) {
                    onSignOut()
                }
            }
        )
        return  // Don't show normal dashboard content
    }
    
    IntroShowcase(
        showIntroShowCase = showTour,
        dismissOnClickOutside = true,
        onShowCaseCompleted = {
            prefs.hasSeenDashboardTour = true
            showTour = false
        }
    ) {
    Scaffold(
        topBar = {
            AppTopBar(
                title = { Text(authState.family?.name ?: stringResource(R.string.dashboard_title)) },
                actions = {
                    IconButton(onClick = {
                        viewModel.signOut()
                        onSignOut()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ExitToApp, stringResource(R.string.sign_out))
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = "👋",
                fontSize = 48.sp
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = stringResource(R.string.dashboard_welcome, authState.user?.name ?: ""),
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = stringResource(R.string.dashboard_family, authState.family?.name ?: ""),
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(48.dp))
            
            // Chores Card
            Card(
                onClick = onChoresClick,
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = stringResource(R.string.dashboard_chores_title),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = stringResource(R.string.dashboard_chores_description),
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = stringResource(R.string.dashboard_go_chores),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.introShowCaseTarget(
                            index = 0,
                            style = ShowcaseStyle.Default.copy(
                                backgroundColor = Color(0xFF1A237E),
                                backgroundAlpha = 0.95f,
                                targetCircleColor = Color.White
                            ),
                            content = {
                                Column {
                                    Text(
                                        text = stringResource(R.string.tour_dashboard_chores_title),
                                        color = Color.White,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = if (isParent)
                                            stringResource(R.string.tour_dashboard_chores_body_parent)
                                        else
                                            stringResource(R.string.tour_dashboard_chores_body_child),
                                        color = Color.White,
                                        fontSize = 14.sp
                                    )
                                }
                            }
                        )
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Family Members Card
            Card(
                onClick = onFamilyMembersClick,
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = stringResource(R.string.dashboard_members_title),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = stringResource(R.string.dashboard_members_description),
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    }
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = stringResource(R.string.dashboard_go_members),
                        tint = MaterialTheme.colorScheme.onTertiaryContainer,
                        modifier = Modifier.introShowCaseTarget(
                            index = 1,
                            style = ShowcaseStyle.Default.copy(
                                backgroundColor = Color(0xFF1A237E),
                                backgroundAlpha = 0.95f,
                                targetCircleColor = Color.White
                            ),
                            content = {
                                Column {
                                    Text(
                                        text = stringResource(R.string.tour_dashboard_members_title),
                                        color = Color.White,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = if (isParent)
                                            stringResource(R.string.tour_dashboard_members_body_parent)
                                        else
                                            stringResource(R.string.tour_dashboard_members_body_child),
                                        color = Color.White,
                                        fontSize = 14.sp
                                    )
                                }
                            }
                        )
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Settings Card
            Card(
                onClick = onSettingsClick,
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = stringResource(R.string.dashboard_settings_title),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = stringResource(R.string.dashboard_settings_description),
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = stringResource(R.string.dashboard_go_settings),
                        tint = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.introShowCaseTarget(
                            index = 2,
                            style = ShowcaseStyle.Default.copy(
                                backgroundColor = Color(0xFF1A237E),
                                backgroundAlpha = 0.95f,
                                targetCircleColor = Color.White
                            ),
                            content = {
                                Column {
                                    Text(
                                        text = stringResource(R.string.tour_dashboard_settings_title),
                                        color = Color.White,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = stringResource(R.string.tour_dashboard_settings_body),
                                        color = Color.White,
                                        fontSize = 14.sp
                                    )
                                }
                            }
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = "App version: $versionName",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
    } // end IntroShowcase
}
