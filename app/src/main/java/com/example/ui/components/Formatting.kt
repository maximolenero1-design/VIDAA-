package com.example.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.CreditCard
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.Movie
import androidx.compose.material.icons.rounded.PhoneAndroid
import androidx.compose.material.icons.rounded.ReceiptLong
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.material.icons.rounded.Wifi
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.abs

private val currencyFormatter: NumberFormat =
  NumberFormat.getCurrencyInstance(Locale("es", "MX")).apply {
    minimumFractionDigits = 2
    maximumFractionDigits = 2
  }

fun formatCurrencyCents(cents: Long): String {
  return currencyFormatter.format(cents / 100.0)
}

fun formatSignedCurrencyCents(cents: Long): String {
  val formatted = currencyFormatter.format(abs(cents) / 100.0)
  return if (cents >= 0L) "+$formatted" else "-$formatted"
}

fun parseAmountToCents(rawInput: String): Long? {
  val normalized = rawInput.trim().replace(",", "").replace("$", "")
  if (normalized.isEmpty()) return null
  val value = normalized.toDoubleOrNull() ?: return null
  if (value <= 0.0) return null
  return Math.round(value * 100.0)
}

fun formatRelativeTimeEs(timestampMillis: Long, nowMillis: Long): String {
  val diffMillis = (nowMillis - timestampMillis).coerceAtLeast(0L)
  val seconds = diffMillis / 1000L
  return when {
    seconds < 8L -> "Justo ahora"
    seconds < 60L -> "Hace ${seconds} s"
    seconds < 3600L -> "Hace ${seconds / 60L} min"
    else -> {
      val calTx = Calendar.getInstance().apply { timeInMillis = timestampMillis }
      val calNow = Calendar.getInstance().apply { timeInMillis = nowMillis }
      val timeStr = SimpleDateFormat("HH:mm", Locale("es", "MX")).format(Date(timestampMillis))
      if (calTx.get(Calendar.YEAR) == calNow.get(Calendar.YEAR) &&
        calTx.get(Calendar.DAY_OF_YEAR) == calNow.get(Calendar.DAY_OF_YEAR)
      ) {
        "Hoy, $timeStr"
      } else {
        SimpleDateFormat("d MMM, HH:mm", Locale("es", "MX")).format(Date(timestampMillis))
      }
    }
  }
}

fun formatFullTimestampEs(timestampMillis: Long): String {
  return SimpleDateFormat("dd MMM yyyy • HH:mm:ss", Locale("es", "MX")).format(Date(timestampMillis))
}

fun formatDateGroupHeader(timestampMillis: Long, nowMillis: Long): String {
  val calTx = Calendar.getInstance().apply { timeInMillis = timestampMillis }
  val calNow = Calendar.getInstance().apply { timeInMillis = nowMillis }
  val sameYear = calTx.get(Calendar.YEAR) == calNow.get(Calendar.YEAR)
  val dayDiff = calNow.get(Calendar.DAY_OF_YEAR) - calTx.get(Calendar.DAY_OF_YEAR)
  return when {
    sameYear && dayDiff == 0 -> "Hoy"
    sameYear && dayDiff == 1 -> "Ayer"
    else -> SimpleDateFormat("EEEE d 'de' MMMM", Locale("es", "MX"))
      .format(Date(timestampMillis))
      .replaceFirstChar { it.uppercase() }
  }
}

fun serviceIconFor(iconKey: String): ImageVector {
  return when (iconKey) {
    "electricity" -> Icons.Rounded.Bolt
    "water" -> Icons.Rounded.WaterDrop
    "wifi" -> Icons.Rounded.Wifi
    "phone" -> Icons.Rounded.PhoneAndroid
    "gas" -> Icons.Rounded.LocalFireDepartment
    "streaming" -> Icons.Rounded.Movie
    "credit" -> Icons.Rounded.CreditCard
    "deposit" -> Icons.Rounded.AccountBalanceWallet
    "transfer" -> Icons.AutoMirrored.Rounded.Send
    "cashback" -> Icons.Rounded.AutoAwesome
    else -> Icons.Rounded.ReceiptLong
  }
}

fun serviceAccentColor(iconKey: String): Color {
  return when (iconKey) {
    "electricity" -> Color(0xFFF59E0B)
    "water" -> Color(0xFF0EA5E9)
    "wifi" -> Color(0xFF6366F1)
    "phone" -> Color(0xFF8B5CF6)
    "gas" -> Color(0xFFF97316)
    "streaming" -> Color(0xFFEC4899)
    "credit" -> Color(0xFF14B8A6)
    "deposit" -> Color(0xFF059669)
    "transfer" -> Color(0xFF2563EB)
    "cashback" -> Color(0xFF00C896)
    else -> Color(0xFF64748B)
  }
}
