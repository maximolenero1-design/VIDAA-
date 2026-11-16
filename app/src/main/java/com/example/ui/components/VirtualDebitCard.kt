package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AcUnit
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Contactless
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.model.AccountEntity
import com.example.ui.theme.BrightMint
import com.example.ui.theme.FrozenIceBlue
import com.example.ui.theme.MonoAmountLarge
import com.example.ui.theme.MonoCodeStyle

@Composable
fun VirtualDebitCard(
  account: AccountEntity,
  isBalanceHidden: Boolean,
  isCardDetailsRevealed: Boolean,
  onToggleBalanceHidden: () -> Unit,
  onToggleCardDetails: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val animatedBalance by animateFloatAsState(
    targetValue = account.balanceCents.toFloat(),
    animationSpec = tween(durationMillis = 550),
    label = "balance_anim",
  )

  Surface(
    modifier = modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(24.dp)),
    shape = RoundedCornerShape(24.dp),
    shadowElevation = 8.dp,
    color = Color(0xFF0B132B),
  ) {
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .height(228.dp),
    ) {
      Image(
        painter = painterResource(id = R.drawable.img_card_mesh),
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = Modifier.fillMaxSize(),
      )

      Box(
        modifier = Modifier
          .fillMaxSize()
          .background(
            Brush.linearGradient(
              colors = listOf(
                Color(0xD90B132B),
                Color(0x8C101E42),
                Color(0xCC060C1C),
              ),
            ),
          ),
      )

      if (account.isCardFrozen) {
        Box(
          modifier = Modifier
            .fillMaxSize()
            .background(Color(0x660F2942)),
        )
      }

      Column(
        modifier = Modifier
          .fillMaxSize()
          .padding(horizontal = 20.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.SpaceBetween,
      ) {
        // Top row: Brand & Cashback / Contactless
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(BrightMint),
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = account.accountLabel,
              style = MaterialTheme.typography.labelLarge,
              color = Color.White.copy(alpha = 0.92f),
            )
          }

          Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(
              shape = RoundedCornerShape(999.dp),
              color = Color.White.copy(alpha = 0.12f),
              modifier = Modifier.border(
                width = 0.8.dp,
                color = BrightMint.copy(alpha = 0.45f),
                shape = RoundedCornerShape(999.dp),
              ),
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
              ) {
                Icon(
                  imageVector = Icons.Rounded.AutoAwesome,
                  contentDescription = null,
                  tint = BrightMint,
                  modifier = Modifier.size(14.dp),
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                  text = "Cashback ${formatCurrencyCents(account.cashbackCents)}",
                  style = MaterialTheme.typography.labelSmall,
                  color = BrightMint,
                )
              }
            }
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
              imageVector = Icons.Rounded.Contactless,
              contentDescription = "Pago sin contacto",
              tint = Color.White.copy(alpha = 0.8f),
              modifier = Modifier.size(22.dp),
            )
          }
        }

        // Balance block
        Column {
          Text(
            text = "SALDO DISPONIBLE",
            style = MaterialTheme.typography.labelSmall,
            color = Color.White.copy(alpha = 0.65f),
          )
          Spacer(modifier = Modifier.height(2.dp))
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth(),
          ) {
            Text(
              text = if (isBalanceHidden) "••••••••" else formatCurrencyCents(animatedBalance.toLong()),
              style = MonoAmountLarge,
              color = Color.White,
              modifier = Modifier.testTag("balance_display_text"),
            )
            IconButton(
              onClick = onToggleBalanceHidden,
              modifier = Modifier
                .minimumInteractiveComponentSize()
                .testTag("toggle_balance_visibility_button"),
            ) {
              Icon(
                imageVector = if (isBalanceHidden) Icons.Rounded.Visibility else Icons.Rounded.VisibilityOff,
                contentDescription = if (isBalanceHidden) "Mostrar saldo" else "Ocultar saldo",
                tint = Color.White.copy(alpha = 0.82f),
              )
            }
          }
        }

        // Bottom row: Card digits + Reveal toggle + Frozen pill
        Column {
          AnimatedVisibility(visible = account.isCardFrozen) {
            Surface(
              shape = RoundedCornerShape(8.dp),
              color = FrozenIceBlue.copy(alpha = 0.22f),
              modifier = Modifier.padding(bottom = 8.dp),
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
              ) {
                Icon(
                  imageVector = Icons.Rounded.AcUnit,
                  contentDescription = null,
                  tint = FrozenIceBlue,
                  modifier = Modifier.size(14.dp),
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = "Tarjeta congelada • Pagos bloqueados",
                  style = MaterialTheme.typography.labelSmall,
                  color = FrozenIceBlue,
                  fontWeight = FontWeight.SemiBold,
                )
              }
            }
          }

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Column {
              Text(
                text = if (isCardDetailsRevealed) account.cardNumberFull else account.cardNumberMasked,
                style = MonoCodeStyle,
                color = Color.White.copy(alpha = 0.92f),
              )
              Spacer(modifier = Modifier.height(2.dp))
              Text(
                text = if (isCardDetailsRevealed) {
                  "VENCE ${account.cardExpiry}  •  CVV ${account.cardCvv}"
                } else {
                  "${account.holderName.uppercase()}  •  DÉBITO"
                },
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.65f),
              )
            }

            Surface(
              onClick = onToggleCardDetails,
              shape = RoundedCornerShape(999.dp),
              color = Color.White.copy(alpha = 0.14f),
              modifier = Modifier
                .minimumInteractiveComponentSize()
                .testTag("toggle_card_details_button"),
            ) {
              Text(
                text = if (isCardDetailsRevealed) "Ocultar datos" else "Ver tarjeta",
                style = MaterialTheme.typography.labelMedium,
                color = Color.White,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
              )
            }
          }
        }
      }
    }
  }
}
