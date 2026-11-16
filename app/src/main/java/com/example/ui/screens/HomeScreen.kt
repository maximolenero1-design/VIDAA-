package com.example.ui.screens

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.AcUnit
import androidx.compose.material.icons.rounded.AddCard
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.LockOpen
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.ReceiptLong
import androidx.compose.material.icons.rounded.RestartAlt
import androidx.compose.material.icons.rounded.Sensors
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.ServiceBillerEntity
import com.example.data.model.TransactionEntity
import com.example.ui.components.TransactionRow
import com.example.ui.components.VirtualDebitCard
import com.example.ui.components.formatCurrencyCents
import com.example.ui.components.serviceAccentColor
import com.example.ui.components.serviceIconFor
import com.example.ui.theme.LocalBankColors
import com.example.ui.theme.MonoAmountMedium
import com.example.ui.viewmodel.BankUiState

@Composable
fun HomeScreen(
  uiState: BankUiState,
  onToggleBalanceHidden: () -> Unit,
  onToggleCardDetails: () -> Unit,
  onToggleCardFrozen: () -> Unit,
  onToggleAutoStream: () -> Unit,
  onResetSimulator: () -> Unit,
  onOpenServicesTab: () -> Unit,
  onOpenHistoryTab: () -> Unit,
  onOpenDeposit: () -> Unit,
  onOpenTransfer: () -> Unit,
  onPayService: (ServiceBillerEntity) -> Unit,
  onOpenReceipt: (TransactionEntity) -> Unit,
  modifier: Modifier = Modifier,
) {
  val bankColors = LocalBankColors.current
  val account = uiState.account ?: return

  val infiniteTransition = rememberInfiniteTransition(label = "live_dot")
  val liveAlpha by infiniteTransition.animateFloat(
    initialValue = 0.35f,
    targetValue = 1f,
    animationSpec = infiniteRepeatable(tween(900), RepeatMode.Reverse),
    label = "live_dot_alpha",
  )

  LazyColumn(
    modifier = modifier.fillMaxSize(),
    contentPadding = PaddingValues(horizontal = 18.dp, vertical = 12.dp),
    verticalArrangement = Arrangement.spacedBy(18.dp),
  ) {
    // Top Header
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = "Hola, ${account.holderName.substringBefore(" ")}",
            style = MaterialTheme.typography.headlineMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
          )
          Spacer(modifier = Modifier.height(2.dp))
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(8.dp)
                .alpha(liveAlpha)
                .clip(CircleShape)
                .background(bankColors.income),
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "Banca conectada en tiempo real",
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
          Surface(
            onClick = onToggleAutoStream,
            shape = RoundedCornerShape(999.dp),
            color = if (uiState.isAutoStreamEnabled) {
              bankColors.income.copy(alpha = 0.16f)
            } else {
              MaterialTheme.colorScheme.surfaceVariant
            },
            modifier = Modifier
              .minimumInteractiveComponentSize()
              .testTag("toggle_auto_stream_button"),
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
              verticalAlignment = Alignment.CenterVertically,
            ) {
              Icon(
                imageVector = if (uiState.isAutoStreamEnabled) Icons.Rounded.Stop else Icons.Rounded.PlayArrow,
                contentDescription = "Flujo en vivo",
                tint = if (uiState.isAutoStreamEnabled) bankColors.income else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(16.dp),
              )
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = if (uiState.isAutoStreamEnabled) "En vivo" else "Auto-flujo",
                style = MaterialTheme.typography.labelMedium,
                color = if (uiState.isAutoStreamEnabled) bankColors.income else MaterialTheme.colorScheme.onSurfaceVariant,
              )
            }
          }

          IconButton(
            onClick = onResetSimulator,
            modifier = Modifier
              .minimumInteractiveComponentSize()
              .testTag("reset_simulator_button"),
          ) {
            Icon(
              imageVector = Icons.Rounded.RestartAlt,
              contentDescription = "Reiniciar simulador",
              tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }
        }
      }
    }

    // Virtual Debit Card
    item {
      VirtualDebitCard(
        account = account,
        isBalanceHidden = uiState.isBalanceHidden,
        isCardDetailsRevealed = uiState.isCardDetailsRevealed,
        onToggleBalanceHidden = onToggleBalanceHidden,
        onToggleCardDetails = onToggleCardDetails,
      )
    }

    // Quick Actions Row
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
      ) {
        QuickActionTile(
          icon = Icons.Rounded.ReceiptLong,
          label = "Pagar",
          tint = MaterialTheme.colorScheme.tertiary,
          onClick = onOpenServicesTab,
          testTag = "quick_action_pay",
          modifier = Modifier.weight(1f),
        )
        QuickActionTile(
          icon = Icons.Rounded.AddCard,
          label = "Depositar",
          tint = bankColors.income,
          onClick = onOpenDeposit,
          testTag = "quick_action_deposit",
          modifier = Modifier.weight(1f),
        )
        QuickActionTile(
          icon = Icons.AutoMirrored.Rounded.Send,
          label = "Transferir",
          tint = Color(0xFF6366F1),
          onClick = onOpenTransfer,
          testTag = "quick_action_transfer",
          modifier = Modifier.weight(1f),
        )
        QuickActionTile(
          icon = if (account.isCardFrozen) Icons.Rounded.LockOpen else Icons.Rounded.AcUnit,
          label = if (account.isCardFrozen) "Activar" else "Congelar",
          tint = if (account.isCardFrozen) bankColors.income else bankColors.frozen,
          onClick = onToggleCardFrozen,
          testTag = "quick_action_freeze",
          modifier = Modifier.weight(1f),
        )
      }
    }

    // Pending Services Carousel
    item {
      Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Text(
            text = "Servicios por pagar (${uiState.pendingServices.size})",
            style = MaterialTheme.typography.titleMedium,
          )
          TextButton(
            onClick = onOpenServicesTab,
            modifier = Modifier.testTag("home_see_all_services"),
          ) {
            Text("Ver todos")
          }
        }

        if (uiState.pendingServices.isEmpty()) {
          Surface(
            shape = RoundedCornerShape(16.dp),
            color = bankColors.income.copy(alpha = 0.10f),
            modifier = Modifier.fillMaxWidth(),
          ) {
            Row(
              modifier = Modifier.padding(16.dp),
              verticalAlignment = Alignment.CenterVertically,
            ) {
              Icon(
                imageVector = Icons.Rounded.CheckCircle,
                contentDescription = null,
                tint = bankColors.income,
              )
              Spacer(modifier = Modifier.width(12.dp))
              Text(
                text = "¡Todos tus servicios están al día!",
                style = MaterialTheme.typography.titleSmall,
                color = bankColors.income,
              )
            }
          }
        } else {
          LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
          ) {
            items(uiState.pendingServices, key = { it.id }) { service ->
              PendingServiceMiniCard(
                service = service,
                onPayClick = { onPayService(service) },
              )
            }
          }
        }
      }
    }

    // Live Real-time Activity Section Header
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.Rounded.Sensors,
            contentDescription = null,
            tint = bankColors.income,
            modifier = Modifier.size(18.dp),
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = "Movimientos en tiempo real",
            style = MaterialTheme.typography.titleMedium,
          )
        }
        TextButton(
          onClick = onOpenHistoryTab,
          modifier = Modifier.testTag("home_see_all_history"),
        ) {
          Text("Historial")
        }
      }
    }

    // Recent Transactions (top 5)
    val recent = uiState.transactions.take(5)
    if (recent.isEmpty()) {
      item {
        Surface(
          shape = RoundedCornerShape(16.dp),
          color = MaterialTheme.colorScheme.surface,
          modifier = Modifier.fillMaxWidth(),
        ) {
          Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
          ) {
            Icon(
              imageVector = Icons.Rounded.Bolt,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
              text = "Aún no hay movimientos registrados",
              style = MaterialTheme.typography.titleSmall,
            )
          }
        }
      }
    } else {
      items(recent, key = { it.id }) { tx ->
        TransactionRow(
          transaction = tx,
          nowMillis = uiState.nowMillis,
          onClick = { onOpenReceipt(tx) },
        )
      }
    }

    item { Spacer(modifier = Modifier.height(12.dp)) }
  }
}

@Composable
private fun QuickActionTile(
  icon: ImageVector,
  label: String,
  tint: Color,
  onClick: () -> Unit,
  testTag: String,
  modifier: Modifier = Modifier,
) {
  val bankColors = LocalBankColors.current
  Surface(
    modifier = modifier
      .clip(RoundedCornerShape(18.dp))
      .clickable(onClick = onClick)
      .minimumInteractiveComponentSize()
      .testTag(testTag),
    shape = RoundedCornerShape(18.dp),
    color = MaterialTheme.colorScheme.surface,
    tonalElevation = 1.dp,
  ) {
    Column(
      modifier = Modifier
        .border(0.6.dp, bankColors.subtleBorder, RoundedCornerShape(18.dp))
        .padding(vertical = 14.dp, horizontal = 6.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      Box(
        modifier = Modifier
          .size(40.dp)
          .clip(CircleShape)
          .background(tint.copy(alpha = 0.14f)),
        contentAlignment = Alignment.Center,
      ) {
        Icon(
          imageVector = icon,
          contentDescription = label,
          tint = tint,
          modifier = Modifier.size(20.dp),
        )
      }
      Text(
        text = label,
        style = MaterialTheme.typography.labelMedium,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
      )
    }
  }
}

@Composable
private fun PendingServiceMiniCard(
  service: ServiceBillerEntity,
  onPayClick: () -> Unit,
) {
  val bankColors = LocalBankColors.current
  val accent = serviceAccentColor(service.iconKey)

  Surface(
    shape = RoundedCornerShape(18.dp),
    color = MaterialTheme.colorScheme.surface,
    tonalElevation = 1.dp,
    modifier = Modifier
      .width(172.dp)
      .border(0.6.dp, bankColors.subtleBorder, RoundedCornerShape(18.dp)),
  ) {
    Column(
      modifier = Modifier.padding(14.dp),
      verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Box(
          modifier = Modifier
            .size(36.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(accent.copy(alpha = 0.14f)),
          contentAlignment = Alignment.Center,
        ) {
          Icon(
            imageVector = serviceIconFor(service.iconKey),
            contentDescription = service.name,
            tint = accent,
            modifier = Modifier.size(18.dp),
          )
        }
        Text(
          text = service.dueDateLabel,
          style = MaterialTheme.typography.labelSmall,
          color = bankColors.pending,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
        )
      }

      Column {
        Text(
          text = service.name,
          style = MaterialTheme.typography.titleSmall,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
        )
        Text(
          text = formatCurrencyCents(service.amountDueCents),
          style = MonoAmountMedium,
          fontWeight = FontWeight.Bold,
        )
      }

      Button(
        onClick = onPayClick,
        modifier = Modifier
          .fillMaxWidth()
          .height(38.dp)
          .testTag("home_quick_pay_${service.id}"),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
        shape = RoundedCornerShape(10.dp),
        colors = ButtonDefaults.buttonColors(
          containerColor = MaterialTheme.colorScheme.primary,
        ),
      ) {
        Text(
          text = "Pagar ahora",
          style = MaterialTheme.typography.labelMedium,
        )
      }
    }
  }
}
