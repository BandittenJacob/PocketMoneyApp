package com.jmp.pocketmoneyapp.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.NavType
import com.jmp.pocketmoneyapp.ui.auth.CompleteProfileScreen
import com.jmp.pocketmoneyapp.ui.auth.CreateFamilyChoiceScreen
import com.jmp.pocketmoneyapp.ui.auth.CreateFamilyScreen
import com.jmp.pocketmoneyapp.ui.auth.ForgotPasswordScreen
import com.jmp.pocketmoneyapp.ui.auth.JoinFamilyChoiceScreen
import com.jmp.pocketmoneyapp.ui.auth.QRScannerScreen
import com.jmp.pocketmoneyapp.ui.auth.SignInContext
import com.jmp.pocketmoneyapp.ui.auth.SignInScreen
import com.jmp.pocketmoneyapp.ui.auth.SignUpScreen
import com.jmp.pocketmoneyapp.ui.auth.WelcomeScreen
import com.jmp.pocketmoneyapp.ui.balance.AddTransactionScreen
import com.jmp.pocketmoneyapp.ui.balance.TransactionHistoryScreen
import com.jmp.pocketmoneyapp.ui.chores.AddChoreScreen
import com.jmp.pocketmoneyapp.ui.chores.AddProposalScreen
import com.jmp.pocketmoneyapp.ui.chores.AddChoreLibraryItemScreen
import com.jmp.pocketmoneyapp.ui.chores.ChoreHistoryScreen
import com.jmp.pocketmoneyapp.ui.chores.ChoresScreen
import com.jmp.pocketmoneyapp.ui.chores.ChoreLibraryScreen
import com.jmp.pocketmoneyapp.ui.dashboard.DashboardScreen
import com.jmp.pocketmoneyapp.ui.family.InvitationQRScreen
import com.jmp.pocketmoneyapp.ui.familymembers.AddFamilyMemberScreen
import com.jmp.pocketmoneyapp.ui.familymembers.FamilyMembersScreen
import com.jmp.pocketmoneyapp.ui.settings.SettingsScreen
import com.jmp.pocketmoneyapp.viewmodel.AuthViewModel
import com.jmp.pocketmoneyapp.viewmodel.BalanceViewModel
import com.jmp.pocketmoneyapp.viewmodel.ChoreProposalViewModel
import com.jmp.pocketmoneyapp.viewmodel.ChoreViewModel
import com.jmp.pocketmoneyapp.viewmodel.FamilyMemberViewModel
import com.jmp.pocketmoneyapp.viewmodel.ChoreLibraryViewModel

@Composable
fun NavGraph(
    navController: NavHostController,
    authViewModel: AuthViewModel = viewModel(),
    choreViewModel: ChoreViewModel = viewModel(),
    memberViewModel: FamilyMemberViewModel = viewModel(),
    balanceViewModel: BalanceViewModel = viewModel(),
    libraryViewModel: ChoreLibraryViewModel = viewModel(),
    proposalViewModel: ChoreProposalViewModel = viewModel()
) {
    val authState by authViewModel.authState.collectAsState()
    
    val startDestination = when {
        authState.isAuthenticated && authState.needsProfileCompletion -> "complete_profile"
        authState.isAuthenticated && authState.family != null -> Screen.Dashboard.route
        authState.isAuthenticated && authState.needsFamilySetup -> Screen.CreateFamily.route
        else -> Screen.Welcome.route
    }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Welcome.route) {
            WelcomeScreen(
                onSignInClick = { navController.navigate(Screen.SignIn.route) },
                onSignUpClick = { navController.navigate("create_family_choice") },
                onJoinFamilyClick = { navController.navigate("qr_scanner") }
            )
        }

        composable(Screen.SignIn.route) {
            SignInScreen(
                viewModel = authViewModel,
                onSignInSuccess = {
                    if (authState.needsProfileCompletion) {
                        navController.navigate("complete_profile") {
                            popUpTo(Screen.Welcome.route) { inclusive = true }
                        }
                    } else if (authState.needsFamilySetup) {
                        navController.navigate(Screen.CreateFamily.route) {
                            popUpTo(Screen.Welcome.route) { inclusive = true }
                        }
                    } else {
                        navController.navigate(Screen.Dashboard.route) {
                            popUpTo(Screen.Welcome.route) { inclusive = true }
                        }
                    }
                },
                onNavigateBack = { navController.popBackStack() },
                onForgotPasswordClick = { navController.navigate("forgot_password") }
            )
        }
        
        // Forgot password screen
        composable("forgot_password") {
            ForgotPasswordScreen(
                viewModel = authViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.SignUp.route) {
            SignUpScreen(
                viewModel = authViewModel,
                onSignUpSuccess = {
                    navController.navigate(Screen.CreateFamily.route) {
                        popUpTo(Screen.Welcome.route) { inclusive = true }
                    }
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.CreateFamily.route) {
            CreateFamilyScreen(
                viewModel = authViewModel,
                onFamilyCreated = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Welcome.route) { inclusive = true }
                    }
                }
            )
        }
        
        // Complete profile for users with Firebase Auth but no Firestore document
        composable("complete_profile") {
            CompleteProfileScreen(
                viewModel = authViewModel,
                onProfileCompleted = {
                    if (authState.needsFamilySetup) {
                        navController.navigate(Screen.CreateFamily.route) {
                            popUpTo("complete_profile") { inclusive = true }
                        }
                    } else {
                        navController.navigate(Screen.Dashboard.route) {
                            popUpTo("complete_profile") { inclusive = true }
                        }
                    }
                }
            )
        }
        
        // Create family choice screen (create account or sign in)
        composable("create_family_choice") {
            CreateFamilyChoiceScreen(
                onCreateAccountClick = {
                    navController.navigate(Screen.SignUp.route)
                },
                onSignInClick = {
                    navController.navigate("sign_in_to_create_family")
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        // Sign in to create family
        composable("sign_in_to_create_family") {
            SignInScreen(
                viewModel = authViewModel,
                signInContext = SignInContext.CREATE_FAMILY,
                onSignInSuccess = {
                    if (authState.needsProfileCompletion) {
                        navController.navigate("complete_profile") {
                            popUpTo("create_family_choice") { inclusive = true }
                        }
                    } else {
                        navController.navigate(Screen.CreateFamily.route) {
                            popUpTo("create_family_choice") { inclusive = true }
                        }
                    }
                },
                onNavigateBack = { navController.popBackStack() },
                onForgotPasswordClick = { navController.navigate("forgot_password") }
            )
        }

        composable(Screen.Dashboard.route) {
            DashboardScreen(
                viewModel = authViewModel,
                onChoresClick = {
                    navController.navigate(Screen.Chores.route)
                },
                onFamilyMembersClick = {
                    navController.navigate(Screen.FamilyMembers.route)
                },
                onSettingsClick = {
                    navController.navigate(Screen.Settings.route)
                },
                onSignOut = {
                    navController.navigate(Screen.Welcome.route) {
                        popUpTo(Screen.Dashboard.route) { inclusive = true }
                    }
                }
            )
        }
        
        composable(Screen.Settings.route) {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        composable(Screen.Chores.route) {
            ChoresScreen(
                authViewModel = authViewModel,
                choreViewModel = choreViewModel,
                memberViewModel = memberViewModel,
                proposalViewModel = proposalViewModel,
                onAddChoreClick = {
                    choreViewModel.setChoreToEdit(null)
                    navController.navigate(Screen.AddChore.route)
                },
                onEditChore = { chore ->
                    choreViewModel.setChoreToEdit(chore)
                    navController.navigate(Screen.AddChore.route)
                },
                onChoreLibraryClick = {
                    navController.navigate(Screen.ChoreLibrary.route)
                },
                onChoreHistoryClick = {
                    navController.navigate(Screen.ChoreHistory.route)
                },
                onSuggestChoreClick = {
                    navController.navigate(Screen.AddProposal.route)
                },
                onAcceptWithEdits = {
                    choreViewModel.setChoreToEdit(null)
                    navController.navigate(Screen.AddChore.route)
                },
                onSignOut = {
                    navController.navigate(Screen.Welcome.route) {
                        popUpTo(Screen.Dashboard.route) { inclusive = true }
                    }
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        composable(Screen.ChoreHistory.route) {
            ChoreHistoryScreen(
                authViewModel = authViewModel,
                choreViewModel = choreViewModel,
                memberViewModel = memberViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.AddChore.route) {
            AddChoreScreen(
                authViewModel = authViewModel,
                choreViewModel = choreViewModel,
                memberViewModel = memberViewModel,
                choreToEdit = choreViewModel.choreToEdit,
                proposalToAccept = proposalViewModel.proposalToAccept,
                onChoreCreated = {
                    choreViewModel.setChoreToEdit(null)
                    navController.popBackStack()
                },
                onProposalAccepted = { proposalId ->
                    proposalViewModel.setProposalToAccept(null)
                    authState.family?.id?.let { familyId ->
                        proposalViewModel.markAcceptedWithEdits(proposalId, familyId)
                    }
                },
                onNavigateBack = {
                    choreViewModel.setChoreToEdit(null)
                    proposalViewModel.setProposalToAccept(null)
                    navController.popBackStack()
                }
            )
        }
        
        composable(Screen.ChoreLibrary.route) {
            ChoreLibraryScreen(
                authViewModel = authViewModel,
                recurringViewModel = libraryViewModel,
                onNavigateBack = { navController.popBackStack() },
                onAddTemplate = {
                    libraryViewModel.setTemplateToEdit(null)
                    navController.navigate(Screen.AddChoreLibraryItem.route)
                },
                onEditTemplate = { template ->
                    libraryViewModel.setTemplateToEdit(template)
                    navController.navigate(Screen.AddChoreLibraryItem.route)
                }
            )
        }
        
        composable(Screen.AddChoreLibraryItem.route) {
            AddChoreLibraryItemScreen(
                authViewModel = authViewModel,
                recurringViewModel = libraryViewModel,
                memberViewModel = memberViewModel,
                templateToEdit = libraryViewModel.templateToEdit,
                onTemplateCreated = {
                    libraryViewModel.setTemplateToEdit(null)
                    navController.popBackStack()
                },
                onNavigateBack = {
                    libraryViewModel.setTemplateToEdit(null)
                    navController.popBackStack()
                }
            )
        }
        
        composable(Screen.FamilyMembers.route) {
            FamilyMembersScreen(
                authViewModel = authViewModel,
                memberViewModel = memberViewModel,
                balanceViewModel = balanceViewModel,
                onAddMemberClick = {
                    navController.navigate(Screen.AddFamilyMember.route)
                },
                onAddTransactionClick = {
                    navController.navigate(Screen.AddTransaction.route)
                },
                onEditMember = { member ->
                    memberViewModel.setMemberToEdit(member)
                    navController.navigate(Screen.AddFamilyMember.route)
                },
                onViewMemberTransactions = { member ->
                    navController.navigate(Screen.TransactionHistory.createRoute(member.id))
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        composable(Screen.AddFamilyMember.route) {
            AddFamilyMemberScreen(
                authViewModel = authViewModel,
                memberViewModel = memberViewModel,
                memberToEdit = memberViewModel.memberToEdit,
                onNavigateBack = {
                    memberViewModel.setMemberToEdit(null)
                    navController.popBackStack()
                },
                onInviteClick = { member ->
                    navController.navigate("show_invitation/${member.id}")
                }
            )
        }
        
        composable(Screen.AddTransaction.route) {
            AddTransactionScreen(
                authViewModel = authViewModel,
                memberViewModel = memberViewModel,
                balanceViewModel = balanceViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        composable(
            route = Screen.TransactionHistory.route,
            arguments = listOf(navArgument("memberId") { type = NavType.StringType })
        ) { backStackEntry ->
            val memberId = backStackEntry.arguments?.getString("memberId") ?: ""
            val memberState by memberViewModel.memberState.collectAsState()
            val member = memberState.members.find { it.id == memberId }
            
            if (member != null) {
                TransactionHistoryScreen(
                    member = member,
                    balanceViewModel = balanceViewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
        
        // QR Scanner for joining family
        composable("qr_scanner") {
            QRScannerScreen(
                onQRCodeScanned = { qrData ->
                    android.util.Log.d("NavGraph", "QR Data scanned: $qrData")
                    val invitationData = com.jmp.pocketmoneyapp.data.model.FamilyInvitation.fromQrData(qrData)
                    if (invitationData != null) {
                        val (invitationCode, familyId) = invitationData
                        android.util.Log.d("NavGraph", "Valid invitation - code: $invitationCode, familyId: $familyId")
                        navController.navigate("join_family_choice/$invitationCode") {
                            popUpTo("qr_scanner") { inclusive = true }
                        }
                    } else {
                        android.util.Log.e("NavGraph", "Invalid QR code format: $qrData")
                        // Show error and go back
                        navController.popBackStack()
                    }
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        // Join family choice screen (create account or sign in)
        composable(
            route = "join_family_choice/{invitationCode}",
            arguments = listOf(navArgument("invitationCode") { type = NavType.StringType })
        ) { backStackEntry ->
            val invitationCode = backStackEntry.arguments?.getString("invitationCode") ?: ""
            var invitation by remember { mutableStateOf<com.jmp.pocketmoneyapp.data.model.FamilyInvitation?>(null) }
            
            LaunchedEffect(invitationCode) {
                val memberRepository = com.jmp.pocketmoneyapp.data.repository.FamilyMemberRepository()
                val result = memberRepository.getInvitation(invitationCode)
                result.fold(
                    onSuccess = { inv -> invitation = inv },
                    onFailure = { /* Handle error */ }
                )
            }
            
            if (invitation != null) {
                JoinFamilyChoiceScreen(
                    familyName = invitation!!.familyName,
                    memberName = invitation!!.memberName,
                    onCreateAccountClick = {
                        navController.navigate("sign_up_with_invite/$invitationCode")
                    },
                    onSignInClick = {
                        navController.navigate("sign_in_with_invite/$invitationCode")
                    },
                    onNavigateBack = { navController.popBackStack() }
                )
            } else {
                // Show loading
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }
        
        // Sign in with invitation
        composable(
            route = "sign_in_with_invite/{invitationCode}",
            arguments = listOf(navArgument("invitationCode") { type = NavType.StringType })
        ) { backStackEntry ->
            val invitationCode = backStackEntry.arguments?.getString("invitationCode") ?: ""
            var invitation by remember { mutableStateOf<com.jmp.pocketmoneyapp.data.model.FamilyInvitation?>(null) }
            
            LaunchedEffect(invitationCode) {
                val memberRepository = com.jmp.pocketmoneyapp.data.repository.FamilyMemberRepository()
                val result = memberRepository.getInvitation(invitationCode)
                result.fold(
                    onSuccess = { inv -> invitation = inv },
                    onFailure = { /* Handle error */ }
                )
            }
            
            if (invitation != null) {
                SignInScreen(
                    viewModel = authViewModel,
                    signInContext = SignInContext.JOIN_INVITATION,
                    invitationCode = invitationCode,
                    familyName = invitation!!.familyName,
                    onSignInSuccess = {
                        if (authState.needsProfileCompletion) {
                            navController.navigate("complete_profile") {
                                popUpTo(Screen.Welcome.route) { inclusive = true }
                            }
                        } else {
                            navController.navigate(Screen.Dashboard.route) {
                                popUpTo(Screen.Welcome.route) { inclusive = true }
                            }
                        }
                    },
                    onNavigateBack = { navController.popBackStack() },
                    onForgotPasswordClick = { navController.navigate("forgot_password") }
                )
            } else {
                // Show loading
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }
        
        // Sign up with invitation
        composable(
            route = "sign_up_with_invite/{invitationCode}",
            arguments = listOf(navArgument("invitationCode") { type = NavType.StringType })
        ) { backStackEntry ->
            val invitationCode = backStackEntry.arguments?.getString("invitationCode") ?: ""
            var invitation by remember { mutableStateOf<com.jmp.pocketmoneyapp.data.model.FamilyInvitation?>(null) }
            
            LaunchedEffect(invitationCode) {
                val memberRepository = com.jmp.pocketmoneyapp.data.repository.FamilyMemberRepository()
                val result = memberRepository.getInvitation(invitationCode)
                result.fold(
                    onSuccess = { inv -> invitation = inv },
                    onFailure = { /* Handle error */ }
                )
            }
            
            if (invitation != null) {
                SignUpScreen(
                    viewModel = authViewModel,
                    invitationCode = invitationCode,
                    memberName = invitation!!.memberName,
                    onSignUpSuccess = {
                        navController.navigate(Screen.Dashboard.route) {
                            popUpTo(Screen.Welcome.route) { inclusive = true }
                        }
                    },
                    onNavigateBack = { navController.popBackStack() }
                )
            } else {
                // Show loading
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }
        
        // Show invitation QR code
        composable(
            route = "show_invitation/{memberId}",
            arguments = listOf(navArgument("memberId") { type = NavType.StringType })
        ) { backStackEntry ->
            val memberId = backStackEntry.arguments?.getString("memberId") ?: ""
            val memberState by memberViewModel.memberState.collectAsState()
            val member = memberState.members.find { it.id == memberId }
            var invitation by remember { mutableStateOf<com.jmp.pocketmoneyapp.data.model.FamilyInvitation?>(null) }
            
            LaunchedEffect(member) {
                if (member != null && authState.family != null) {
                    val memberRepository = com.jmp.pocketmoneyapp.data.repository.FamilyMemberRepository()
                    val result = memberRepository.createInvitation(member, authState.family!!.name)
                    result.fold(
                        onSuccess = { inv -> invitation = inv },
                        onFailure = { /* Handle error */ }
                    )
                }
            }
            
            if (invitation != null) {
                InvitationQRScreen(
                    invitation = invitation!!,
                    onNavigateBack = { navController.popBackStack() }
                )
            } else {
                // Show loading
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }

        composable(Screen.AddProposal.route) {
            AddProposalScreen(
                authViewModel = authViewModel,
                memberViewModel = memberViewModel,
                proposalViewModel = proposalViewModel,
                onSubmitted = { navController.popBackStack() },
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
