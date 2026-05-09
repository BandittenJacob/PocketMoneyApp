package com.jmp.pocketmoneyapp.ui.chores

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.canopas.lib.showcase.IntroShowcase
import com.canopas.lib.showcase.component.ShowcaseStyle
import com.jmp.pocketmoneyapp.R
import com.jmp.pocketmoneyapp.data.repository.PreferencesManager
import com.jmp.pocketmoneyapp.ui.components.AppTopBar
import com.jmp.pocketmoneyapp.data.model.ChoreLibraryTemplate
import com.jmp.pocketmoneyapp.viewmodel.AuthViewModel
import com.jmp.pocketmoneyapp.viewmodel.ChoreLibraryViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChoreLibraryScreen(
    authViewModel: AuthViewModel,
    recurringViewModel: ChoreLibraryViewModel,
    onNavigateBack: () -> Unit,
    onAddTemplate: () -> Unit,
    onEditTemplate: (ChoreLibraryTemplate) -> Unit
) {
    val authState by authViewModel.authState.collectAsState()
    val templateState by recurringViewModel.templateState.collectAsState()

    val isParent = authState.user?.role == com.jmp.pocketmoneyapp.data.model.UserRole.PARENT
    LaunchedEffect(isParent) {
        if (!isParent) onNavigateBack()
    }

    val context = LocalContext.current
    val prefs = remember { PreferencesManager(context) }
    var showTour by remember { mutableStateOf(!prefs.hasSeenLibraryTour) }

    LaunchedEffect(authState.family?.id) {
        authState.family?.id?.let { familyId ->
            recurringViewModel.loadFamilyTemplates(familyId)
        }
    }

    LaunchedEffect(templateState.successMessage) {
        templateState.successMessage?.let {
            kotlinx.coroutines.delay(2000)
            recurringViewModel.clearSuccessMessage()
        }
    }

    IntroShowcase(
        showIntroShowCase = showTour,
        dismissOnClickOutside = false,
        onShowCaseCompleted = {
            prefs.hasSeenLibraryTour = true
            showTour = false
        }
    ) {
    Scaffold(
        topBar = {
            AppTopBar(
                title = { Text(stringResource(R.string.recurring_chores_title)) },
                onNavigateBack = onNavigateBack
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddTemplate,
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
                                text = stringResource(R.string.tour_library_fab_title),
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = stringResource(R.string.tour_library_fab_body),
                                color = Color.White,
                                fontSize = 14.sp
                            )
                        }
                    }
                )
            ) {
                Icon(Icons.Default.Add, stringResource(R.string.recurring_chores_add))
            }
        }
    ) { padding ->
        PullToRefreshBox(
            isRefreshing = templateState.isLoading,
            onRefresh = {
                authState.family?.id?.let { familyId ->
                    recurringViewModel.loadFamilyTemplates(familyId)
                }
            },
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                templateState.successMessage?.let { message ->
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                    ) {
                        Text(text = message, modifier = Modifier.padding(16.dp), color = MaterialTheme.colorScheme.onPrimaryContainer)
                    }
                }

                templateState.error?.let { error ->
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                    ) {
                        Text(text = error, modifier = Modifier.padding(16.dp), color = MaterialTheme.colorScheme.onErrorContainer)
                    }
                }

                if (templateState.isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else if (templateState.templates.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(text = "📋", fontSize = 48.sp)
                            Text(text = stringResource(R.string.recurring_chores_empty), fontSize = 15.sp, fontWeight = FontWeight.Medium)
                            Text(text = stringResource(R.string.recurring_chores_empty_hint), fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        itemsIndexed(items = templateState.templates, key = { _, t -> t.id }) { index, template ->
                            ChoreTemplateCard(
                                template = template,
                                modifier = if (index == 0) Modifier.introShowCaseTarget(
                                    index = 0,
                                    style = ShowcaseStyle.Default.copy(
                                        backgroundColor = Color(0xFF1A237E),
                                        backgroundAlpha = 0.95f,
                                        targetCircleColor = Color.White
                                    ),
                                    content = {
                                        Column {
                                            Text(
                                                text = stringResource(R.string.tour_library_card_title),
                                                color = Color.White,
                                                fontSize = 18.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Text(
                                                text = stringResource(R.string.tour_library_card_body),
                                                color = Color.White,
                                                fontSize = 14.sp
                                            )
                                        }
                                    }
                                ) else Modifier,
                                onDeploy = { recurringViewModel.generateInstance(template) },
                                onEdit = { onEditTemplate(template) }
                            )
                        }
                    }
                }
            }
        }
    }
    } // end IntroShowcase
}

@Composable
fun ChoreTemplateCard(
    template: ChoreLibraryTemplate,
    modifier: Modifier = Modifier,
    onDeploy: () -> Unit,
    onEdit: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()) }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = template.name,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    if (template.description.isNotEmpty()) {
                        Text(
                            text = template.description,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }

                    if (template.defaultAssignedTo.isNotEmpty()) {
                        Text(
                            text = "👤 ${template.defaultAssignedTo}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }

                    template.lastInstanceCreated?.let { timestamp ->
                        Text(
                            text = stringResource(R.string.recurring_chores_last_deployed, dateFormat.format(timestamp.toDate())),
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }

                Text(
                    text = String.format("%.2f kr.", template.value),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onDeploy,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(stringResource(R.string.recurring_chores_create))
                }

                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, stringResource(R.string.recurring_chores_edit))
                }
            }
        }
    }
}
