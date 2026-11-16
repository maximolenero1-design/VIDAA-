package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Immutable
data class BankExtendedColors(
  val income: Color,
  val expense: Color,
  val pending: Color,
  val frozen: Color,
  val cardSurface: Color,
  val subtleBorder: Color,
)

val LocalBankColors = staticCompositionLocalOf {
  BankExtendedColors(
    income = IncomeGreenLight,
    expense = ExpenseCoralLight,
    pending = PendingAmberLight,
    frozen = FrozenIceBlue,
    cardSurface = PureSurfaceLight,
    subtleBorder = OutlineLight,
  )
}

private val LightColorScheme = lightColorScheme(
  primary = ObsidianNavy,
  onPrimary = Color.White,
  primaryContainer = MintContainerLight,
  onPrimaryContainer = OnMintContainerLight,
  secondary = EmeraldMint,
  onSecondary = ObsidianNavy,
  secondaryContainer = SoftSurfaceVariantLight,
  onSecondaryContainer = InkPrimaryLight,
  tertiary = ElectricCobalt,
  onTertiary = Color.White,
  tertiaryContainer = CobaltContainerLight,
  onTertiaryContainer = OnCobaltContainerLight,
  background = IceBackgroundLight,
  onBackground = InkPrimaryLight,
  surface = PureSurfaceLight,
  onSurface = InkPrimaryLight,
  surfaceVariant = SoftSurfaceVariantLight,
  onSurfaceVariant = InkSecondaryLight,
  outline = OutlineLight,
  error = ExpenseCoralLight,
  onError = Color.White,
)

private val DarkColorScheme = darkColorScheme(
  primary = BrightMint,
  onPrimary = ObsidianNavy,
  primaryContainer = MintContainerDark,
  onPrimaryContainer = OnMintContainerDark,
  secondary = EmeraldMint,
  onSecondary = ObsidianNavy,
  secondaryContainer = SoftSurfaceVariantDark,
  onSecondaryContainer = InkPrimaryDark,
  tertiary = ElectricCobalt,
  onTertiary = Color.White,
  tertiaryContainer = CobaltContainerDark,
  onTertiaryContainer = OnCobaltContainerDark,
  background = ObsidianBackgroundDark,
  onBackground = InkPrimaryDark,
  surface = ElevatedSurfaceDark,
  onSurface = InkPrimaryDark,
  surfaceVariant = SoftSurfaceVariantDark,
  onSurfaceVariant = InkSecondaryDark,
  outline = OutlineDark,
  error = ExpenseCoralDark,
  onError = ObsidianNavy,
)

private val BankShapes = Shapes(
  small = RoundedCornerShape(10.dp),
  medium = RoundedCornerShape(16.dp),
  large = RoundedCornerShape(22.dp),
  extraLarge = RoundedCornerShape(28.dp),
)

@Composable
fun NovaBancaTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  content: @Composable () -> Unit,
) {
  val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
  val extendedColors = if (darkTheme) {
    BankExtendedColors(
      income = IncomeGreenDark,
      expense = ExpenseCoralDark,
      pending = PendingAmberDark,
      frozen = FrozenIceBlue,
      cardSurface = ElevatedSurfaceDark,
      subtleBorder = OutlineDark,
    )
  } else {
    BankExtendedColors(
      income = IncomeGreenLight,
      expense = ExpenseCoralLight,
      pending = PendingAmberLight,
      frozen = FrozenIceBlue,
      cardSurface = PureSurfaceLight,
      subtleBorder = OutlineLight,
    )
  }

  CompositionLocalProvider(LocalBankColors provides extendedColors) {
    MaterialTheme(
      colorScheme = colorScheme,
      typography = Typography,
      shapes = BankShapes,
      content = content,
    )
  }
}
