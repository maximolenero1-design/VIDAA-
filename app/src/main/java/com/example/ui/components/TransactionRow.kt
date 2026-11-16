package com.example.ui.components

import androidx.compose.animation.animateColorAsState
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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.TransactionEntity
import com.example.data.model.TransactionStatus
import com.example.ui.theme.LocalBankColors
import com.example.ui.theme.MonoAmountMedium

@Composable
fun TransactionRow(
  transaction: TransactionEntity,
  nowMillis: Long,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val bankColors = LocalBankColors.current
  val accentColor = serviceAccentColor(transaction.iconKey)
  val isProcessing = transaction.status == TransactionStatus.PROCESSING
  val isFailed = transaction.status == TransactionStatus.FAILED
  val isPositive = transaction.amountCents >= 0L

  val amountColor by animateColorAsState(
    targetValue = when {
      isFailed -> MaterialTheme.colorScheme.error
      isProcessing -> bankColors.pending
      isPositive -> bankColors.income
      else -> MaterialTheme.colorScheme.onSurface
    },
    label = "tx_amount_color",
  )

  val infiniteTransition = rememberInfiniteTransition(label = "processing_pulse")
  val pulseAlpha by infiniteTransition.animateFloat(
    initialValue = 0.35f,
    targetValue = 1f,
    animationSpec = infiniteRepeatable(
      animation = tween(650),
      repeatMode = RepeatMode.Reverse,
    ),
    label = "pulse_alpha",
  )

  Surface(
    modifier = modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(16.dp))
      .clickable(onClick = onClick)
      .testTag("transaction_row_${transaction.id}"),
    shape = RoundedCornerShape(16.dp),
    color = MaterialTheme.colorScheme.surface,
    tonalElevation = if (isProcessing) 3.dp else 1.dp,
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .border(
          width = if (isProcessing) 1.dp else 0.5.dp,
          color = if (isProcessing) bankColors.pending.copy(alpha = 0.5f) else bankColors.subtleBorder,
          shape = RoundedCornerShape(16.dp),
        )
        .padding(horizontal = 14.dp, vertical = 12.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Box(
        modifier = Modifier
          .size(44.dp)
          .clip(RoundedCornerShape(12.dp))
          .background(accentColor.copy(alpha = 0.14f)),
        contentAlignment = Alignment.Center,
      ) {
        Icon(
          imageVector = serviceIconFor(transaction.iconKey),
          contentDescription = transaction.categoryLabel,
          tint = accentColor,
          modifier = Modifier.size(22.dp),
        )
      }

      Spacer(modifier = Modifier.width(12.dp))

      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = transaction.title,
          style = MaterialTheme.typography.titleSmall,
          fontWeight = FontWeight.SemiBold,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
        )
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
          if (isProcessing) {
            Box(
              modifier = Modifier
                .size(7.dp)
                .alpha(pulseAlpha)
                .clip(CircleShape)
                .background(bankColors.pending),
            )
            Text(
              text = "Procesando en vivo…",
              style = MaterialTheme.typography.bodyMedium,
              color = bankColors.pending,
              fontWeight = FontWeight.Medium,
            )
          } else {
            Text(
              text = "${transaction.subtitle} • ${formatRelativeTimeEs(transaction.timestampMillis, nowMillis)}",
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
            )
          }
        }
      }

      Spacer(modifier = Modifier.width(10.dp))

      Column(horizontalAlignment = Alignment.End) {
        Text(
          text = formatSignedCurrencyCents(transaction.amountCents),
          style = MonoAmountMedium,
          color = amountColor,
        )
        Text(
          text = when (transaction.status) {
            TransactionStatus.PROCESSING -> "En curso"
            TransactionStatus.COMPLETED -> transaction.referenceCode
            TransactionStatus.FAILED -> "Rechazada"
          },
          style = MaterialTheme.typography.labelSmall,
          color = if (isFailed) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
        )
      }
    }
  }
}
