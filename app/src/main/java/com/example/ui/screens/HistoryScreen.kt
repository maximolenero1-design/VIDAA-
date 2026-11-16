package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.TransactionEntity
import com.example.ui.components.TransactionRow
import com.example.ui.components.formatCurrencyCents
import com.example.ui.components.formatDateGroupHeader
import com.example.ui.theme.LocalBankColors
import com.example.ui.theme.MonoAmountMedium
import com.example.ui.viewmodel.BankUiState
import com.example.ui.viewmodel.HistoryFilter

@Composable
fun HistoryScreen(
  uiState: BankUiState,
  onSearchChange: (String) -> Unit,
  onSelectFilter: (HistoryFilter) -> Unit,
  onSimulateLiveEvent: () -> Unit,
  onOpenReceipt: (TransactionEntity) -> Unit,
  modifier: Modifier = Modifier,
) {
  val bankColors = LocalBankColors.current
  val income = uiState.totalIncomeCents
  val expense = uiState.totalExpenseCents
  val totalFlow = (income + expense).coerceAtLeast(1L)
  val incomeFraction = (income.toFloat() / totalFlow.toFloat()).coerceIn(0.05f, 0.95f)

  val grouped = uiState.filteredTransactions.groupBy { tx ->
    formatDateGroupHeader(tx.timestampMillis, uiState.nowMillis)
  }

  LazyColumn(
    modifier = modifier.fillMaxSize(),
    contentPadding = PaddingValues(horizontal = 18.dp, vertical = 12.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp),
  ) {
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Column {
          Text(
            text = "Historial en vivo",
            style = MaterialTheme.typography.headlineMedium,
          )
          Text(
            text = if (uiState.processingCount > 0) {
              "${uiState.processingCount} operación(es) procesándose ahora"
            } else {
              "${uiState.transactions.size} movimientos sincronizados"
            },
            style = MaterialTheme.typography.bodyMedium,
            color = if (uiState.processingCount > 0) {
              bankColors.pending
            } else {
              MaterialTheme.colorScheme.onSurfaceVariant
            },
          )
        }
        FilledTonalButton(
          onClick = onSimulateLiveEvent,
          modifier = Modifier.testTag("simulate_live_event_button"),
          shape = RoundedCornerShape(12.dp),
        ) {
          Icon(
            imageVector = Icons.Rounded.AutoAwesome,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
          )
          Spacer(modifier = Modifier.width(4.dp))
          Text("Simular")
        }
      }
    }

    // Real-time Income vs Expense Flow Card
    item {
      Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
        modifier = Modifier
          .fillMaxWidth()
          .border(0.6.dp, bankColors.subtleBorder, RoundedCornerShape(20.dp)),
      ) {
        Column(
          modifier = Modifier.padding(16.dp),
          verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
          ) {
            Column {
              Text(
                text = "Ingresos",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
              )
              Text(
                text = "+${formatCurrencyCents(income)}",
                style = MonoAmountMedium,
                color = bankColors.income,
                fontWeight = FontWeight.Bold,
              )
            }
            Column(horizontalAlignment = Alignment.End) {
              Text(
                text = "Pagos y egresos",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
              )
              Text(
                text = "-${formatCurrencyCents(expense)}",
                style = MonoAmountMedium,
                color = bankColors.expense,
                fontWeight = FontWeight.Bold,
              )
            }
          }

          // Ratio Bar
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .height(10.dp)
              .clip(RoundedCornerShape(999.dp))
              .background(bankColors.expense.copy(alpha = 0.25f)),
          ) {
            Box(
              modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(incomeFraction)
                .clip(RoundedCornerShape(999.dp))
                .background(bankColors.income),
            )
          }
        }
      }
    }

    // Search
    item {
      OutlinedTextField(
        value = uiState.historySearchQuery,
        onValueChange = onSearchChange,
        placeholder = { Text("Buscar concepto o folio…") },
        leadingIcon = {
          Icon(
            imageVector = Icons.Rounded.Search,
            contentDescription = "Buscar transacción",
          )
        },
        singleLine = true,
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
          .fillMaxWidth()
          .testTag("history_search_input"),
      )
    }

    // Filter Chips
    item {
      LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(HistoryFilter.entries) { filter ->
          FilterChip(
            selected = uiState.selectedHistoryFilter == filter,
            onClick = { onSelectFilter(filter) },
            label = { Text(filter.labelEs) },
            modifier = Modifier.testTag("history_filter_${filter.name.lowercase()}"),
          )
        }
      }
    }

    if (grouped.isEmpty()) {
      item {
        Surface(
          shape = RoundedCornerShape(18.dp),
          color = MaterialTheme.colorScheme.surface,
          modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp),
        ) {
          Column(
            modifier = Modifier.padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
          ) {
            Icon(
              imageVector = Icons.Rounded.History,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.onSurfaceVariant,
              modifier = Modifier.size(36.dp),
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
              text = "Sin movimientos para este filtro",
              style = MaterialTheme.typography.titleMedium,
            )
            Text(
              text = "Realiza un pago de servicio, transferencia o depósito para verlo aquí al instante.",
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }
        }
      }
    } else {
      grouped.forEach { (dateLabel, txs) ->
        item(key = "header_$dateLabel") {
          Text(
            text = dateLabel,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp, bottom = 2.dp),
          )
        }
        items(txs, key = { it.id }) { tx ->
          TransactionRow(
            transaction = tx,
            nowMillis = uiState.nowMillis,
            onClick = { onOpenReceipt(tx) },
          )
        }
      }
    }

    item { Spacer(modifier = Modifier.height(12.dp)) }
  }
}
