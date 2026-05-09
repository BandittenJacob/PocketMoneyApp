package com.jmp.pocketmoneyapp.ui.chores

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jmp.pocketmoneyapp.R
import com.jmp.pocketmoneyapp.ui.components.AppTopBar
import com.jmp.pocketmoneyapp.data.model.ChoreProposal
import com.jmp.pocketmoneyapp.data.model.MemberRole
import com.jmp.pocketmoneyapp.viewmodel.AuthViewModel
import com.jmp.pocketmoneyapp.viewmodel.ChoreProposalViewModel
import com.jmp.pocketmoneyapp.viewmodel.FamilyMemberViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddProposalScreen(
    authViewModel: AuthViewModel,
    memberViewModel: FamilyMemberViewModel,
    proposalViewModel: ChoreProposalViewModel,
    onSubmitted: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val authState by authViewModel.authState.collectAsState()
    val memberState by memberViewModel.memberState.collectAsState()

    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var rewardText by remember { mutableStateOf("") }

    val focusManager = LocalFocusManager.current
    val descriptionFocus = remember { FocusRequester() }
    val rewardFocus = remember { FocusRequester() }

    // Find the current user's family member record
    val currentMember = memberState.members.find { it.userId == authState.user?.id }

    Scaffold(
        modifier = Modifier.imePadding(),
        topBar = {
            AppTopBar(
                title = { Text(stringResource(R.string.proposals_add_title)) },
                onNavigateBack = onNavigateBack
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text(stringResource(R.string.proposals_name_label)) },
                placeholder = { Text(stringResource(R.string.proposals_name_placeholder)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = { descriptionFocus.requestFocus() })
            )

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text(stringResource(R.string.proposals_description_label)) },
                placeholder = { Text(stringResource(R.string.proposals_description_placeholder)) },
                modifier = Modifier.fillMaxWidth().focusRequester(descriptionFocus),
                minLines = 3,
                maxLines = 5,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = { rewardFocus.requestFocus() })
            )

            OutlinedTextField(
                value = rewardText,
                onValueChange = { rewardText = it.filter { c -> c.isDigit() || c == '.' } },
                label = { Text(stringResource(R.string.proposals_reward_label)) },
                modifier = Modifier.fillMaxWidth().focusRequester(rewardFocus),
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() })
            )

            Button(
                onClick = {
                    val familyId = authState.family?.id ?: return@Button
                    val member = currentMember ?: return@Button
                    val reward = rewardText.toDoubleOrNull() ?: 0.0
                    val proposal = ChoreProposal(
                        familyId = familyId,
                        proposedByMemberId = member.id,
                        proposedByName = "${member.avatarEmoji} ${member.name}",
                        name = name.trim(),
                        description = description.trim(),
                        suggestedReward = reward
                    )
                    proposalViewModel.submitProposal(proposal, familyId)
                    onSubmitted()
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = name.isNotBlank()
            ) {
                Text(stringResource(R.string.proposals_submit))
            }
        }
    }
}
