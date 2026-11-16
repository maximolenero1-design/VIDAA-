package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.HourglassTop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.data.model.ServiceBillerEntity
import com.example.data.model.ServiceCategory
import com.example.data.model.TransactionEntity
import com.example.data.model.TransactionStatus
import com.example.ui.theme.LocalBankColors
import com.example.ui.theme.MonoAmountLarge
import com.example.ui.theme.MonoAmountMedium
import com.example.ui.theme.MonoCodeStyle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PayServiceSheet(
  service: ServiceBillerEntity,
  availableBalanceCents: Long,
  isCardFrozen: Boolean,
  onDismiss: () -> Unit,
  onConfirm: (amountCents: Long, reference: String, note: String) -> Unit,
) {
  val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
  var amountText by remember(service.id) {
    mutableStateOf(String.format(java.util.Locale.US, "%.2f", service.amountDueCents / 100.0))
  }
  var referenceText by remember(service.id) { mutableStateOf(service.referenceNumber) }
  var noteText by remember(service.id) { mutableStateOf("") }

  val parsedCents = parseAmountToCents(amountText) ?: 0L
  val hasEnoughBalance = parsedCents in 1L..availableBalanceCents
  val accentColor = serviceAccentColor(service.iconKey)

  ModalBottomSheet(
    onDismissRequest = onDismiss,
    sheetState = sheetState,
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .navigationBarsPadding()
        .padding(horizontal = 20.dp)
        .padding(bottom = 24.dp),
      verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
          modifier = Modifier
            .size(48.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(accentColor.copy(alpha = 0.14f)),
          contentAlignment = Alignment.Center,
        ) {
          Icon(
            imageVector = serviceIconFor(service.iconKey),
            contentDescription = service.name,
            tint = accentColor,
            modifier = Modifier.size(26.dp),
          )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
          Text(
            text = "Pagar ${service.name}",
            style = MaterialTheme.typography.titleLarge,
          )
          Text(
            text = service.provider,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
      }

      Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
        modifier = Modifier.fillMaxWidth(),
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 10.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Text(
            text = "Saldo disponible",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
          Text(
            text = formatCurrencyCents(availableBalanceCents),
            style = MonoAmountMedium,
            color = if (hasEnoughBalance || parsedCents == 0L) {
              MaterialTheme.colorScheme.onSurface
            } else {
              MaterialTheme.colorScheme.error
            },
          )
        }
      }

      OutlinedTextField(
        value = amountText,
        onValueChange = { amountText = it },
        label = { Text("Monto a pagar (MXN)") },
        prefix = { Text("$") },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        singleLine = true,
        isError = parsedCents > availableBalanceCents,
        supportingText = {
          if (parsedCents > availableBalanceCents) {
            Text("Saldo insuficiente para cubrir este pago")
          } else if (isCardFrozen) {
            Text("Tu tarjeta está congelada. Actívala antes de pagar.")
          }
        },
        modifier = Modifier
          .fillMaxWidth()
          .testTag("pay_service_amount_input"),
      )

      OutlinedTextField(
        value = referenceText,
        onValueChange = { referenceText = it },
        label = { Text("Número de referencia / contrato") },
        singleLine = true,
        modifier = Modifier
          .fillMaxWidth()
          .testTag("pay_service_reference_input"),
      )

      OutlinedTextField(
        value = noteText,
        onValueChange = { noteText = it },
        label = { Text("Nota opcional") },
        placeholder = { Text("Ej. Recibo de noviembre") },
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
      )

      Button(
        onClick = { onConfirm(parsedCents, referenceText, noteText) },
        enabled = parsedCents > 0L && hasEnoughBalance && !isCardFrozen && referenceText.isNotBlank(),
        modifier = Modifier
          .fillMaxWidth()
          .height(52.dp)
          .testTag("confirm_pay_service_button"),
        shape = RoundedCornerShape(14.dp),
      ) {
        Text(
          text = if (parsedCents > 0L) {
            "Confirmar pago • ${formatCurrencyCents(parsedCents)}"
          } else {
            "Confirmar pago"
          },
          style = MaterialTheme.typography.labelLarge,
        )
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun DepositSheet(
  onDismiss: () -> Unit,
  onConfirm: (amountCents: Long, sourceLabel: String) -> Unit,
) {
  val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
  var amountText by remember { mutableStateOf("1500.00") }
  val sources = listOf("Transferencia SPEI", "Depósito en efectivo", "Abono de nómina")
  var selectedSource by remember { mutableStateOf(sources.first()) }
  val presets = listOf(50000L, 150000L, 500000L, 1000000L)
  val parsedCents = parseAmountToCents(amountText) ?: 0L

  ModalBottomSheet(
    onDismissRequest = onDismiss,
    sheetState = sheetState,
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .navigationBarsPadding()
        .padding(horizontal = 20.dp)
        .padding(bottom = 24.dp),
      verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
      Text(
        text = "Simular depósito a cuenta",
        style = MaterialTheme.typography.titleLarge,
      )
      Text(
        text = "Recarga tu saldo de prueba para seguir pagando servicios y viendo el historial en vivo.",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )

      FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        presets.forEach { presetCents ->
          FilterChip(
            selected = parsedCents == presetCents,
            onClick = {
              amountText = String.format(java.util.Locale.US, "%.2f", presetCents / 100.0)
            },
            label = { Text("+${formatCurrencyCents(presetCents)}") },
          )
        }
      }

      OutlinedTextField(
        value = amountText,
        onValueChange = { amountText = it },
        label = { Text("Monto a depositar (MXN)") },
        prefix = { Text("$") },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        singleLine = true,
        modifier = Modifier
          .fillMaxWidth()
          .testTag("deposit_amount_input"),
      )

      FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        sources.forEach { source ->
          FilterChip(
            selected = selectedSource == source,
            onClick = { selectedSource = source },
            label = { Text(source) },
          )
        }
      }

      Button(
        onClick = { onConfirm(parsedCents, selectedSource) },
        enabled = parsedCents > 0L,
        modifier = Modifier
          .fillMaxWidth()
          .height(52.dp)
          .testTag("confirm_deposit_button"),
        shape = RoundedCornerShape(14.dp),
      ) {
        Text("Acreditar ${if (parsedCents > 0) formatCurrencyCents(parsedCents) else ""}")
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransferSheet(
  availableBalanceCents: Long,
  isCardFrozen: Boolean,
  onDismiss: () -> Unit,
  onConfirm: (recipient: String, clabe: String, amountCents: Long, concept: String) -> Unit,
) {
  val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
  var recipient by remember { mutableStateOf("") }
  var clabe by remember { mutableStateOf("") }
  var amountText by remember { mutableStateOf("") }
  var concept by remember { mutableStateOf("") }
  val parsedCents = parseAmountToCents(amountText) ?: 0L
  val hasFunds = parsedCents in 1L..availableBalanceCents

  ModalBottomSheet(
    onDismissRequest = onDismiss,
    sheetState = sheetState,
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .navigationBarsPadding()
        .padding(horizontal = 20.dp)
        .padding(bottom = 24.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      Text(
        text = "Transferencia rápida SPEI",
        style = MaterialTheme.typography.titleLarge,
      )
      OutlinedTextField(
        value = recipient,
        onValueChange = { recipient = it },
        label = { Text("Nombre del destinatario") },
        placeholder = { Text("Ej. Andrea Gómez") },
        singleLine = true,
        modifier = Modifier
          .fillMaxWidth()
          .testTag("transfer_recipient_input"),
      )
      OutlinedTextField(
        value = clabe,
        onValueChange = { clabe = it.filter { ch -> ch.isDigit() }.take(18) },
        label = { Text("CLABE o últimos 4 dígitos") },
        placeholder = { Text("18 dígitos o terminación") },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
      )
      OutlinedTextField(
        value = amountText,
        onValueChange = { amountText = it },
        label = { Text("Monto a transferir (MXN)") },
        prefix = { Text("$") },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        singleLine = true,
        isError = parsedCents > availableBalanceCents,
        modifier = Modifier
          .fillMaxWidth()
          .testTag("transfer_amount_input"),
      )
      OutlinedTextField(
        value = concept,
        onValueChange = { concept = it },
        label = { Text("Concepto") },
        placeholder = { Text("Ej. Renta o pago compartido") },
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
      )
      Button(
        onClick = { onConfirm(recipient, clabe, parsedCents, concept) },
        enabled = recipient.isNotBlank() && hasFunds && !isCardFrozen,
        modifier = Modifier
          .fillMaxWidth()
          .height(52.dp)
          .testTag("confirm_transfer_button"),
        shape = RoundedCornerShape(14.dp),
      ) {
        Text("Enviar transferencia")
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddServiceSheet(
  onDismiss: () -> Unit,
  onConfirm: (name: String, provider: String, category: ServiceCategory, reference: String, amountCents: Long) -> Unit,
) {
  val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
  var name by remember { mutableStateOf("") }
  var provider by remember { mutableStateOf("") }
  var reference by remember { mutableStateOf("") }
  var amountText by remember { mutableStateOf("") }
  var selectedCategory by remember { mutableStateOf(ServiceCategory.HOME) }
  val parsedCents = parseAmountToCents(amountText) ?: 0L

  ModalBottomSheet(
    onDismissRequest = onDismiss,
    sheetState = sheetState,
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .navigationBarsPadding()
        .padding(horizontal = 20.dp)
        .padding(bottom = 24.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      Text(
        text = "Agregar nuevo servicio",
        style = MaterialTheme.typography.titleLarge,
      )
      OutlinedTextField(
        value = name,
        onValueChange = { name = it },
        label = { Text("Nombre del servicio") },
        placeholder = { Text("Ej. Colegiatura o Gimnasio") },
        singleLine = true,
        modifier = Modifier
          .fillMaxWidth()
          .testTag("add_service_name_input"),
      )
      OutlinedTextField(
        value = provider,
        onValueChange = { provider = it },
        label = { Text("Empresa / Proveedor") },
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
      )
      FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        ServiceCategory.entries.forEach { cat ->
          FilterChip(
            selected = selectedCategory == cat,
            onClick = { selectedCategory = cat },
            label = { Text(cat.labelEs) },
          )
        }
      }
      OutlinedTextField(
        value = reference,
        onValueChange = { reference = it },
        label = { Text("Número de referencia") },
        singleLine = true,
        modifier = Modifier
          .fillMaxWidth()
          .testTag("add_service_reference_input"),
      )
      OutlinedTextField(
        value = amountText,
        onValueChange = { amountText = it },
        label = { Text("Monto del recibo (MXN)") },
        prefix = { Text("$") },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        singleLine = true,
        modifier = Modifier
          .fillMaxWidth()
          .testTag("add_service_amount_input"),
      )
      Button(
        onClick = { onConfirm(name, provider, selectedCategory, reference, parsedCents) },
        enabled = name.isNotBlank() && reference.isNotBlank() && parsedCents > 0L,
        modifier = Modifier
          .fillMaxWidth()
          .height(52.dp)
          .testTag("confirm_add_service_button"),
        shape = RoundedCornerShape(14.dp),
      ) {
        Text("Guardar servicio")
      }
    }
  }
}

@Composable
fun TransactionReceiptDialog(
  transaction: TransactionEntity,
  onDismiss: () -> Unit,
  onCopiedReference: (String) -> Unit,
) {
  val bankColors = LocalBankColors.current
  val clipboard = LocalClipboardManager.current
  val isIncome = transaction.amountCents >= 0L

  val statusColor = when (transaction.status) {
    TransactionStatus.COMPLETED -> bankColors.income
    TransactionStatus.PROCESSING -> bankColors.pending
    TransactionStatus.FAILED -> MaterialTheme.colorScheme.error
  }
  val statusIcon = when (transaction.status) {
    TransactionStatus.COMPLETED -> Icons.Rounded.CheckCircle
    TransactionStatus.PROCESSING -> Icons.Rounded.HourglassTop
    TransactionStatus.FAILED -> Icons.Rounded.ErrorOutline
  }
  val statusLabel = when (transaction.status) {
    TransactionStatus.COMPLETED -> "Operación liquidada"
    TransactionStatus.PROCESSING -> "Procesando en tiempo real"
    TransactionStatus.FAILED -> "Operación rechazada"
  }

  Dialog(onDismissRequest = onDismiss) {
    Surface(
      shape = RoundedCornerShape(24.dp),
      color = MaterialTheme.colorScheme.surface,
      tonalElevation = 6.dp,
      modifier = Modifier
        .fillMaxWidth()
        .border(1.dp, bankColors.subtleBorder, RoundedCornerShape(24.dp)),
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(22.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
      ) {
        Box(
          modifier = Modifier
            .size(56.dp)
            .clip(CircleShape)
            .background(statusColor.copy(alpha = 0.14f)),
          contentAlignment = Alignment.Center,
        ) {
          Icon(
            imageVector = statusIcon,
            contentDescription = statusLabel,
            tint = statusColor,
            modifier = Modifier.size(30.dp),
          )
        }
        Spacer(modifier = Modifier.height(10.dp))
        Text(
          text = "Comprobante digital",
          style = MaterialTheme.typography.titleMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
          text = formatSignedCurrencyCents(transaction.amountCents),
          style = MonoAmountLarge,
          color = if (isIncome) bankColors.income else MaterialTheme.colorScheme.onSurface,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Surface(
          shape = RoundedCornerShape(999.dp),
          color = statusColor.copy(alpha = 0.14f),
        ) {
          Text(
            text = statusLabel,
            style = MaterialTheme.typography.labelMedium,
            color = statusColor,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
          )
        }

        Spacer(modifier = Modifier.height(18.dp))
        HorizontalDivider(color = bankColors.subtleBorder)
        Spacer(modifier = Modifier.height(14.dp))

        ReceiptDetailRow(label = "Concepto", value = transaction.title)
        ReceiptDetailRow(label = "Detalle", value = transaction.subtitle)
        ReceiptDetailRow(label = "Categoría", value = transaction.categoryLabel)
        ReceiptDetailRow(label = "Fecha y hora", value = formatFullTimestampEs(transaction.timestampMillis))
        if (transaction.note.isNotBlank()) {
          ReceiptDetailRow(label = "Nota", value = transaction.note)
        }
        ReceiptDetailRow(
          label = "Saldo posterior",
          value = formatCurrencyCents(transaction.balanceAfterCents),
          isMono = true,
        )

        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Text(
            text = "Folio",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
          Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
              text = transaction.referenceCode,
              style = MonoCodeStyle,
              fontWeight = FontWeight.SemiBold,
            )
            IconButton(
              onClick = {
                clipboard.setText(AnnotatedString(transaction.referenceCode))
                onCopiedReference("Folio ${transaction.referenceCode} copiado")
              },
              modifier = Modifier.size(36.dp),
            ) {
              Icon(
                imageVector = Icons.Rounded.ContentCopy,
                contentDescription = "Copiar folio",
                modifier = Modifier.size(16.dp),
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(16.dp))
        TextButton(
          onClick = onDismiss,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("close_receipt_button"),
          colors = ButtonDefaults.textButtonColors(),
        ) {
          Text("Cerrar comprobante")
        }
      }
    }
  }
}

@Composable
private fun ReceiptDetailRow(
  label: String,
  value: String,
  isMono: Boolean = false,
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(vertical = 5.dp),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.Top,
  ) {
    Text(
      text = label,
      style = MaterialTheme.typography.bodyMedium,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
      modifier = Modifier.weight(0.38f),
    )
    Text(
      text = value,
      style = if (isMono) MonoAmountMedium else MaterialTheme.typography.bodyMedium,
      fontWeight = FontWeight.Medium,
      modifier = Modifier.weight(0.62f),
      textAlign = androidx.compose.ui.text.style.TextAlign.End,
    )
  }
}
