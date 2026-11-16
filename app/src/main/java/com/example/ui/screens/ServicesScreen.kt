package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.ReceiptLong
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.ServiceBillerEntity
import com.example.data.model.ServiceCategory
import com.example.ui.components.formatCurrencyCents
import com.example.ui.components.serviceAccentColor
import com.example.ui.components.serviceIconFor
import com.example.ui.theme.LocalBankColors
import com.example.ui.theme.MonoAmountMedium
import com.example.ui.theme.MonoCodeStyle
import com.example.ui.viewmodel.BankUiState

@Composable
fun ServicesScreen(
  uiState: BankUiState,
  onSearchChange: (String) -> Unit,
  onSelectCategory: (ServiceCategory?) -> Unit,
  onPayService: (ServiceBillerEntity) -> Unit,
  onRenewService: (ServiceBillerEntity) -> Unit,
  onOpenAddService: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val bankColors = LocalBankColors.current
  val pendingCount = uiState.pendingServices.size
  val pendingTotal = uiState.pendingServices.sumOf { it.amountDueCents }

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
            text = "Pago de servicios",
            style = MaterialTheme.typography.headlineMedium,
          )
          Text(
            text = "Liquidación instantánea sin comisiones",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
        FilledTonalButton(
          onClick = onOpenAddService,
          modifier = Modifier.testTag("add_service_button"),
          shape = RoundedCornerShape(12.dp),
        ) {
          Icon(
            imageVector = Icons.Rounded.Add,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
          )
          Spacer(modifier = Modifier.width(4.dp))
          Text("Nuevo")
        }
      }
    }

    // Pending Summary Banner
    item {
      Surface(
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
        modifier = Modifier
          .fillMaxWidth()
          .border(0.6.dp, bankColors.subtleBorder, RoundedCornerShape(18.dp)),
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Column {
            Text(
              text = if (pendingCount > 0) "$pendingCount recibos pendientes" else "Todos tus recibos pagados",
              style = MaterialTheme.typography.titleSmall,
              fontWeight = FontWeight.SemiBold,
            )
            Text(
              text = "Gana 1% de cashback en cada pago",
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }
          Text(
            text = formatCurrencyCents(pendingTotal),
            style = MonoAmountMedium,
            fontWeight = FontWeight.Bold,
          )
        }
      }
    }

    // Search bar
    item {
      OutlinedTextField(
        value = uiState.serviceSearchQuery,
        onValueChange = onSearchChange,
        placeholder = { Text("Buscar servicio, empresa o referencia…") },
        leadingIcon = {
          Icon(
            imageVector = Icons.Rounded.Search,
            contentDescription = "Buscar servicio",
          )
        },
        singleLine = true,
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
          .fillMaxWidth()
          .testTag("services_search_input"),
      )
    }

    // Category Chips
    item {
      LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        item {
          FilterChip(
            selected = uiState.selectedServiceCategory == null,
            onClick = { onSelectCategory(null) },
            label = { Text("Todos") },
          )
        }
        items(ServiceCategory.entries) { category ->
          FilterChip(
            selected = uiState.selectedServiceCategory == category,
            onClick = {
              onSelectCategory(if (uiState.selectedServiceCategory == category) null else category)
            },
            label = { Text(category.labelEs) },
          )
        }
      }
    }

    val services = uiState.filteredServices
    if (services.isEmpty()) {
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
              imageVector = Icons.Rounded.ReceiptLong,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.onSurfaceVariant,
              modifier = Modifier.size(36.dp),
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
              text = "No se encontraron servicios",
              style = MaterialTheme.typography.titleMedium,
            )
            Text(
              text = "Prueba con otro filtro o agrega un nuevo servicio personalizado.",
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }
        }
      }
    } else {
      items(services, key = { it.id }) { service ->
        ServiceBillerCard(
          service = service,
          onPayClick = { onPayService(service) },
          onRenewClick = { onRenewService(service) },
        )
      }
    }

    item { Spacer(modifier = Modifier.height(12.dp)) }
  }
}

@Composable
private fun ServiceBillerCard(
  service: ServiceBillerEntity,
  onPayClick: () -> Unit,
  onRenewClick: () -> Unit,
) {
  val bankColors = LocalBankColors.current
  val accent = serviceAccentColor(service.iconKey)

  Surface(
    shape = RoundedCornerShape(18.dp),
    color = MaterialTheme.colorScheme.surface,
    tonalElevation = 1.dp,
    modifier = Modifier
      .fillMaxWidth()
      .border(0.6.dp, bankColors.subtleBorder, RoundedCornerShape(18.dp))
      .testTag("service_card_${service.id}"),
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Box(
          modifier = Modifier
            .size(46.dp)
            .clip(RoundedCornerShape(13.dp))
            .background(accent.copy(alpha = 0.14f)),
          contentAlignment = Alignment.Center,
        ) {
          Icon(
            imageVector = serviceIconFor(service.iconKey),
            contentDescription = service.name,
            tint = accent,
            modifier = Modifier.size(24.dp),
          )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = service.name,
            style = MaterialTheme.typography.titleMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
          )
          Text(
            text = service.provider,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
          )
        }

        Column(horizontalAlignment = Alignment.End) {
          Text(
            text = formatCurrencyCents(service.amountDueCents),
            style = MonoAmountMedium,
            fontWeight = FontWeight.Bold,
          )
          Surface(
            shape = RoundedCornerShape(999.dp),
            color = if (service.isPaid) {
              bankColors.income.copy(alpha = 0.14f)
            } else {
              bankColors.pending.copy(alpha = 0.14f)
            },
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
              verticalAlignment = Alignment.CenterVertically,
            ) {
              if (service.isPaid) {
                Icon(
                  imageVector = Icons.Rounded.CheckCircle,
                  contentDescription = null,
                  tint = bankColors.income,
                  modifier = Modifier.size(12.dp),
                )
                Spacer(modifier = Modifier.width(4.dp))
              }
              Text(
                text = if (service.isPaid) "Pagado" else service.dueDateLabel,
                style = MaterialTheme.typography.labelSmall,
                color = if (service.isPaid) bankColors.income else bankColors.pending,
              )
            }
          }
        }
      }

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Text(
          text = "Ref: ${service.referenceNumber}",
          style = MonoCodeStyle,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        if (service.isPaid) {
          Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(
              onClick = onRenewClick,
              shape = RoundedCornerShape(10.dp),
              contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
              modifier = Modifier.testTag("renew_service_${service.id}"),
            ) {
              Icon(
                imageVector = Icons.Rounded.Refresh,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
              )
              Spacer(modifier = Modifier.width(4.dp))
              Text("Nuevo ciclo", style = MaterialTheme.typography.labelMedium)
            }
            FilledTonalButton(
              onClick = onPayClick,
              shape = RoundedCornerShape(10.dp),
              contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
              modifier = Modifier.testTag("pay_service_again_${service.id}"),
            ) {
              Text("Pagar otro", style = MaterialTheme.typography.labelMedium)
            }
          }
        } else {
          Button(
            onClick = onPayClick,
            shape = RoundedCornerShape(10.dp),
            contentPadding = PaddingValues(horizontal = 18.dp, vertical = 8.dp),
            modifier = Modifier.testTag("pay_service_button_${service.id}"),
          ) {
            Text("Pagar servicio", style = MaterialTheme.typography.labelLarge)
          }
        }
      }
    }
  }
}
