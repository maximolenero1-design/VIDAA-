package com.example.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.ui.components.AddServiceSheet
import com.example.ui.components.DepositSheet
import com.example.ui.components.PayServiceSheet
import com.example.ui.components.TransactionReceiptDialog
import com.example.ui.components.TransferSheet
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.ServicesScreen
import com.example.ui.viewmodel.BankViewModel
import kotlinx.coroutines.launch

const val ROUTE_HOME = "home"
const val ROUTE_SERVICES = "services"
const val ROUTE_HISTORY = "history"

private data class NavDestinationItem(
  val route: String,
  val label: String,
  val selectedIcon: ImageVector,
  val unselectedIcon: ImageVector,
  val testTag: String,
)

private val navItems = listOf(
  NavDestinationItem(
    route = ROUTE_HOME,
    label = "Inicio",
    selectedIcon = Icons.Filled.AccountBalance,
    unselectedIcon = Icons.Outlined.AccountBalance,
    testTag = "nav_home",
  ),
  NavDestinationItem(
    route = ROUTE_SERVICES,
    label = "Servicios",
    selectedIcon = Icons.Filled.ReceiptLong,
    unselectedIcon = Icons.Outlined.ReceiptLong,
    testTag = "nav_services",
  ),
  NavDestinationItem(
    route = ROUTE_HISTORY,
    label = "Historial",
    selectedIcon = Icons.Filled.History,
    unselectedIcon = Icons.Outlined.History,
    testTag = "nav_history",
  ),
)

@Composable
fun NovaBancaApp(
  viewModel: BankViewModel = viewModel(),
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  val navController = rememberNavController()
  val backStackEntry by navController.currentBackStackEntryAsState()
  val currentRoute = backStackEntry?.destination?.route ?: ROUTE_HOME

  val snackbarHostState = remember { SnackbarHostState() }
  val scope = rememberCoroutineScope()

  LaunchedEffect(viewModel) {
    viewModel.events.collect { message ->
      snackbarHostState.currentSnackbarData?.dismiss()
      snackbarHostState.showSnackbar(message)
    }
  }

  val navigateToTab: (String) -> Unit = { route ->
    navController.navigate(route) {
      popUpTo(navController.graph.findStartDestination().id) {
        saveState = true
      }
      launchSingleTop = true
      restoreState = true
    }
  }

  BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
    val useRail = maxWidth >= 600.dp

    Scaffold(
      modifier = Modifier.fillMaxSize(),
      contentWindowInsets = WindowInsets.safeDrawing,
      snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
      bottomBar = {
        if (!useRail) {
          NavigationBar {
            navItems.forEach { item ->
              val selected = currentRoute == item.route
              val badgeCount = when (item.route) {
                ROUTE_SERVICES -> uiState.pendingServices.size
                ROUTE_HISTORY -> uiState.processingCount
                else -> 0
              }
              NavigationBarItem(
                selected = selected,
                onClick = { navigateToTab(item.route) },
                icon = {
                  BadgedBox(
                    badge = {
                      if (badgeCount > 0) {
                        Badge { Text("$badgeCount") }
                      }
                    },
                  ) {
                    Icon(
                      imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                      contentDescription = item.label,
                    )
                  }
                },
                label = { Text(item.label) },
                modifier = Modifier.testTag(item.testTag),
              )
            }
          }
        }
      },
    ) { innerPadding ->
      Row(
        modifier = Modifier
          .fillMaxSize()
          .padding(innerPadding),
      ) {
        if (useRail) {
          NavigationRail {
            navItems.forEach { item ->
              val selected = currentRoute == item.route
              val badgeCount = when (item.route) {
                ROUTE_SERVICES -> uiState.pendingServices.size
                ROUTE_HISTORY -> uiState.processingCount
                else -> 0
              }
              NavigationRailItem(
                selected = selected,
                onClick = { navigateToTab(item.route) },
                icon = {
                  BadgedBox(
                    badge = {
                      if (badgeCount > 0) {
                        Badge { Text("$badgeCount") }
                      }
                    },
                  ) {
                    Icon(
                      imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                      contentDescription = item.label,
                    )
                  }
                },
                label = { Text(item.label) },
                modifier = Modifier.testTag(item.testTag),
              )
            }
          }
        }

        Box(
          modifier = Modifier.fillMaxSize(),
          contentAlignment = Alignment.TopCenter,
        ) {
          if (uiState.isLoading) {
            CircularProgressIndicator(
              modifier = Modifier.align(Alignment.Center),
              color = MaterialTheme.colorScheme.secondary,
            )
          } else {
            NavHost(
              navController = navController,
              startDestination = ROUTE_HOME,
              modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 680.dp),
            ) {
              composable(ROUTE_HOME) {
                HomeScreen(
                  uiState = uiState,
                  onToggleBalanceHidden = viewModel::toggleBalanceHidden,
                  onToggleCardDetails = viewModel::toggleCardDetailsRevealed,
                  onToggleCardFrozen = viewModel::toggleCardFrozen,
                  onToggleAutoStream = viewModel::toggleAutoStream,
                  onResetSimulator = viewModel::resetSimulator,
                  onOpenServicesTab = { navigateToTab(ROUTE_SERVICES) },
                  onOpenHistoryTab = { navigateToTab(ROUTE_HISTORY) },
                  onOpenDeposit = viewModel::openDepositSheet,
                  onOpenTransfer = viewModel::openTransferSheet,
                  onPayService = viewModel::openPayServiceSheet,
                  onOpenReceipt = viewModel::openReceipt,
                )
              }
              composable(ROUTE_SERVICES) {
                ServicesScreen(
                  uiState = uiState,
                  onSearchChange = viewModel::updateServiceSearch,
                  onSelectCategory = viewModel::selectServiceCategory,
                  onPayService = viewModel::openPayServiceSheet,
                  onRenewService = viewModel::renewServiceBill,
                  onOpenAddService = viewModel::openAddServiceSheet,
                )
              }
              composable(ROUTE_HISTORY) {
                HistoryScreen(
                  uiState = uiState,
                  onSearchChange = viewModel::updateHistorySearch,
                  onSelectFilter = viewModel::selectHistoryFilter,
                  onSimulateLiveEvent = viewModel::triggerManualLiveEvent,
                  onOpenReceipt = viewModel::openReceipt,
                )
              }
            }
          }
        }
      }
    }
  }

  // Modal Sheets & Dialogs
  val account = uiState.account
  uiState.activePayService?.let { service ->
    PayServiceSheet(
      service = service,
      availableBalanceCents = account?.balanceCents ?: 0L,
      isCardFrozen = account?.isCardFrozen ?: false,
      onDismiss = viewModel::dismissPayServiceSheet,
      onConfirm = { amountCents, reference, note ->
        viewModel.confirmServicePayment(service, amountCents, reference, note)
      },
    )
  }

  if (uiState.showDepositSheet) {
    DepositSheet(
      onDismiss = viewModel::dismissDepositSheet,
      onConfirm = viewModel::confirmDeposit,
    )
  }

  if (uiState.showTransferSheet) {
    TransferSheet(
      availableBalanceCents = account?.balanceCents ?: 0L,
      isCardFrozen = account?.isCardFrozen ?: false,
      onDismiss = viewModel::dismissTransferSheet,
      onConfirm = viewModel::confirmTransfer,
    )
  }

  if (uiState.showAddServiceSheet) {
    AddServiceSheet(
      onDismiss = viewModel::dismissAddServiceSheet,
      onConfirm = viewModel::addCustomService,
    )
  }

  uiState.activeReceiptTransaction?.let { tx ->
    TransactionReceiptDialog(
      transaction = tx,
      onDismiss = viewModel::dismissReceipt,
      onCopiedReference = { msg ->
        scope.launch {
          snackbarHostState.showSnackbar(msg)
        }
      },
    )
  }
}
