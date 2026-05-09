package com.jmp.pocketmoneyapp.navigation

sealed class Screen(val route: String) {
    object Welcome : Screen("welcome")
    object SignIn : Screen("signin")
    object SignUp : Screen("signup")
    object CreateFamily : Screen("create_family")
    object Dashboard : Screen("dashboard")
    object Chores : Screen("chores")
    object AddChore : Screen("add_chore")
    object ChoreLibrary : Screen("chore_library")
    object AddChoreLibraryItem : Screen("add_chore_library_item")
    object FamilyMembers : Screen("family_members")
    object AddFamilyMember : Screen("add_family_member")
    object AddTransaction : Screen("add_transaction")
    object TransactionHistory : Screen("transaction_history/{memberId}") {
        fun createRoute(memberId: String) = "transaction_history/$memberId"
    }
    object Settings : Screen("settings")
    object ChoreHistory : Screen("chore_history")
    object AddProposal : Screen("add_proposal")
}
