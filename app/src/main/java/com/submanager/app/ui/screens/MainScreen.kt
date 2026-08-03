package com.submanager.app.ui.screens

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.submanager.app.data.local.SubscriptionEntity
import com.submanager.app.ui.viewmodel.SubscriptionViewModel

enum class Screen(val title: String) {
    DASHBOARD("Dashboard"),
    SUBSCRIPTIONS("Subscriptions"),
    DETECTOR("Auto-Detector"),
    ANALYTICS("Analytics"),
    SETTINGS("Settings")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: SubscriptionViewModel) {
    var currentScreen by remember { mutableStateOf(Screen.DASHBOARD) }
    var showEditSheet by remember { mutableStateOf(false) }
    var showHistorySheet by remember { mutableStateOf(false) }
    var editingSubscription by remember { mutableStateOf<SubscriptionEntity?>(null) }
    var selectedHistorySubscription by remember { mutableStateOf<SubscriptionEntity?>(null) }

    val editSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val historySheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                NavigationBarItem(
                    selected = currentScreen == Screen.DASHBOARD,
                    onClick = { currentScreen = Screen.DASHBOARD },
                    icon = { Icon(Icons.Default.Dashboard, contentDescription = "Dashboard") },
                    label = { Text("Dashboard") }
                )
                NavigationBarItem(
                    selected = currentScreen == Screen.SUBSCRIPTIONS,
                    onClick = { currentScreen = Screen.SUBSCRIPTIONS },
                    icon = { Icon(Icons.Default.ListAlt, contentDescription = "Subs") },
                    label = { Text("Subs") }
                )
                NavigationBarItem(
                    selected = currentScreen == Screen.DETECTOR,
                    onClick = { currentScreen = Screen.DETECTOR },
                    icon = { Icon(Icons.Default.AutoAwesome, contentDescription = "Detector") },
                    label = { Text("Detector") }
                )
                NavigationBarItem(
                    selected = currentScreen == Screen.ANALYTICS,
                    onClick = { currentScreen = Screen.ANALYTICS },
                    icon = { Icon(Icons.Default.Analytics, contentDescription = "Analytics") },
                    label = { Text("Analytics") }
                )
                NavigationBarItem(
                    selected = currentScreen == Screen.SETTINGS,
                    onClick = { currentScreen = Screen.SETTINGS },
                    icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                    label = { Text("Settings") }
                )
            }
        },
        floatingActionButton = {
            if (currentScreen == Screen.DASHBOARD || currentScreen == Screen.SUBSCRIPTIONS) {
                FloatingActionButton(
                    onClick = {
                        editingSubscription = null
                        showEditSheet = true
                    },
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Subscription")
                }
            }
        }
    ) { innerPadding ->
        androidx.compose.foundation.layout.Box(modifier = Modifier.padding(innerPadding)) {
            when (currentScreen) {
                Screen.DASHBOARD -> DashboardScreen(
                    viewModel = viewModel,
                    onAddNewClick = {
                        editingSubscription = null
                        showEditSheet = true
                    },
                    onNavigateToDetector = { currentScreen = Screen.DETECTOR },
                    onSubscriptionClick = { sub ->
                        selectedHistorySubscription = sub
                        showHistorySheet = true
                    }
                )
                Screen.SUBSCRIPTIONS -> SubscriptionsScreen(
                    viewModel = viewModel,
                    onSubscriptionClick = { sub ->
                        selectedHistorySubscription = sub
                        showHistorySheet = true
                    },
                    onEditClick = { sub ->
                        editingSubscription = sub
                        showEditSheet = true
                    }
                )
                Screen.DETECTOR -> DetectorScreen(viewModel = viewModel)
                Screen.ANALYTICS -> AnalyticsScreen(viewModel = viewModel)
                Screen.SETTINGS -> SettingsScreen(viewModel = viewModel)
            }
        }

        // Add / Edit Sheet
        if (showEditSheet) {
            AddEditSubscriptionSheet(
                sheetState = editSheetState,
                existingSubscription = editingSubscription,
                onDismiss = { showEditSheet = false },
                onSave = { name, amount, currency, cycle, category, paymentMethod, notes ->
                    if (editingSubscription == null) {
                        viewModel.addSubscription(
                            name = name,
                            amount = amount,
                            currency = currency,
                            billingCycle = cycle,
                            category = category,
                            notes = notes,
                            paymentMethod = paymentMethod
                        )
                    } else {
                        val updated = editingSubscription!!.copy(
                            name = name,
                            amount = amount,
                            currency = currency,
                            billingCycle = cycle.name,
                            category = category.name,
                            paymentMethod = paymentMethod,
                            notes = notes
                        )
                        viewModel.updateSubscription(updated)
                    }
                }
            )
        }

        // History & Timeline Sheet
        if (showHistorySheet && selectedHistorySubscription != null) {
            SubscriptionHistorySheet(
                subscription = selectedHistorySubscription!!,
                viewModel = viewModel,
                sheetState = historySheetState,
                onDismiss = {
                    showHistorySheet = false
                    selectedHistorySubscription = null
                }
            )
        }
    }
}
