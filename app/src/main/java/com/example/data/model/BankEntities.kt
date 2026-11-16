package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class TransactionType {
  SERVICE_PAYMENT,
  DEPOSIT,
  TRANSFER,
  CASHBACK,
}

enum class TransactionStatus {
  PROCESSING,
  COMPLETED,
  FAILED,
}

enum class ServiceCategory(val labelEs: String) {
  HOME("Hogar"),
  CONNECTIVITY("Conectividad"),
  SUBSCRIPTIONS("Suscripciones"),
  CREDIT("Crédito"),
  OTHER("Otros"),
}

@Entity(tableName = "account")
data class AccountEntity(
  @PrimaryKey val id: Int = 1,
  val holderName: String,
  val accountLabel: String,
  val cardNumberMasked: String,
  val cardNumberFull: String,
  val cardExpiry: String,
  val cardCvv: String,
  val clabe: String,
  val balanceCents: Long,
  val cashbackCents: Long,
  val isCardFrozen: Boolean,
)

@Entity(tableName = "service_billers")
data class ServiceBillerEntity(
  @PrimaryKey(autoGenerate = true) val id: Int = 0,
  val name: String,
  val provider: String,
  val category: ServiceCategory,
  val referenceNumber: String,
  val amountDueCents: Long,
  val dueDateLabel: String,
  val iconKey: String,
  val isPaid: Boolean = false,
  val lastPaidTimestamp: Long? = null,
)

@Entity(tableName = "transactions")
data class TransactionEntity(
  @PrimaryKey(autoGenerate = true) val id: Int = 0,
  val title: String,
  val subtitle: String,
  val categoryLabel: String,
  val referenceCode: String,
  val amountCents: Long,
  val balanceAfterCents: Long,
  val type: TransactionType,
  val status: TransactionStatus,
  val timestampMillis: Long,
  val iconKey: String,
  val note: String = "",
)
