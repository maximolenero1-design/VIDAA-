package com.example.data.repository

import com.example.data.local.BankDao
import com.example.data.model.AccountEntity
import com.example.data.model.ServiceBillerEntity
import com.example.data.model.ServiceCategory
import com.example.data.model.TransactionEntity
import com.example.data.model.TransactionStatus
import com.example.data.model.TransactionType
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.random.Random

sealed interface OperationResult {
  data class Success(val message: String, val cashbackEarnedCents: Long = 0L) : OperationResult
  data class Error(val message: String) : OperationResult
}

class BankRepository(private val dao: BankDao) {
  private val transactionMutex = Mutex()

  val accountFlow: Flow<AccountEntity?> = dao.observeAccount()
  val servicesFlow: Flow<List<ServiceBillerEntity>> = dao.observeServices()
  val transactionsFlow: Flow<List<TransactionEntity>> = dao.observeTransactions()

  suspend fun ensureSeeded() {
    transactionMutex.withLock {
      if (dao.getAccountOnce() == null) {
        seedInitialState()
      }
    }
  }

  suspend fun resetSimulator() {
    transactionMutex.withLock {
      dao.clearTransactions()
      dao.clearServices()
      seedInitialState()
    }
  }

  private suspend fun seedInitialState() {
    val now = System.currentTimeMillis()
    val account = AccountEntity(
      id = 1,
      holderName = "Máximo Leñero",
      accountLabel = "Cuenta Nova Digital",
      cardNumberMasked = "••••  ••••  ••••  4829",
      cardNumberFull = "4532  8910  3321  4829",
      cardExpiry = "09/29",
      cardCvv = "742",
      clabe = "6461 8011 0492 8831 05",
      balanceCents = 2485000L, // $24,850.00
      cashbackCents = 34250L, // $342.50
      isCardFrozen = false,
    )
    dao.upsertAccount(account)

    val services = listOf(
      ServiceBillerEntity(
        name = "Luz (CFE)",
        provider = "Comisión Federal de Electricidad",
        category = ServiceCategory.HOME,
        referenceNumber = "882140921104",
        amountDueCents = 68450L,
        dueDateLabel = "Vence en 3 días",
        iconKey = "electricity",
      ),
      ServiceBillerEntity(
        name = "Agua Potable",
        provider = "SACMEX Ciudad",
        category = ServiceCategory.HOME,
        referenceNumber = "4409128405",
        amountDueCents = 31200L,
        dueDateLabel = "Vence en 5 días",
        iconKey = "water",
      ),
      ServiceBillerEntity(
        name = "Fibra Óptica 300M",
        provider = "TotalNet Telecom",
        category = ServiceCategory.CONNECTIVITY,
        referenceNumber = "TN-992841",
        amountDueCents = 59900L,
        dueDateLabel = "Vence mañana",
        iconKey = "wifi",
      ),
      ServiceBillerEntity(
        name = "Plan Móvil 5G",
        provider = "NovaTel Celular",
        category = ServiceCategory.CONNECTIVITY,
        referenceNumber = "55-4829-1034",
        amountDueCents = 34900L,
        dueDateLabel = "Vence en 8 días",
        iconKey = "phone",
      ),
      ServiceBillerEntity(
        name = "Gas Natural",
        provider = "Naturgy Hogar",
        category = ServiceCategory.HOME,
        referenceNumber = "GN-301948",
        amountDueCents = 42875L,
        dueDateLabel = "Vence en 6 días",
        iconKey = "gas",
      ),
      ServiceBillerEntity(
        name = "Streaming Ultra",
        provider = "CineStream Plus",
        category = ServiceCategory.SUBSCRIPTIONS,
        referenceNumber = "SP-774102",
        amountDueCents = 21900L,
        dueDateLabel = "Cargo mensual",
        iconKey = "streaming",
      ),
      ServiceBillerEntity(
        name = "Tarjeta Platino",
        provider = "Crédito Departamental",
        category = ServiceCategory.CREDIT,
        referenceNumber = "TC-6610-8812",
        amountDueCents = 145000L,
        dueDateLabel = "Pago mínimo",
        iconKey = "credit",
      ),
    )
    dao.insertServices(services)

    val initialTransactions = listOf(
      TransactionEntity(
        title = "Depósito Nómina",
        subtitle = "Transferencia SPEI recibida",
        categoryLabel = "Ingreso",
        referenceCode = "SPEI-904821",
        amountCents = 1850000L,
        balanceAfterCents = 2594900L,
        type = TransactionType.DEPOSIT,
        status = TransactionStatus.COMPLETED,
        timestampMillis = now - 3 * 3600_000L,
        iconKey = "deposit",
        note = "Abono quincenal",
      ),
      TransactionEntity(
        title = "Fibra Óptica (Mes anterior)",
        subtitle = "TotalNet Telecom • TN-992841",
        categoryLabel = "Conectividad",
        referenceCode = "NV-772104",
        amountCents = -59900L,
        balanceAfterCents = 2535000L,
        type = TransactionType.SERVICE_PAYMENT,
        status = TransactionStatus.COMPLETED,
        timestampMillis = now - 26 * 3600_000L,
        iconKey = "wifi",
      ),
      TransactionEntity(
        title = "Transferencia a Sofía M.",
        subtitle = "SPEI •••• 3190",
        categoryLabel = "Transferencia",
        referenceCode = "SPEI-661209",
        amountCents = -50000L,
        balanceAfterCents = 2485000L,
        type = TransactionType.TRANSFER,
        status = TransactionStatus.COMPLETED,
        timestampMillis = now - 45 * 60_000L,
        iconKey = "transfer",
        note = "Cena compartida",
      ),
    )
    initialTransactions.forEach { dao.insertTransaction(it) }
  }

  suspend fun toggleCardFrozen(): OperationResult {
    return transactionMutex.withLock {
      val account = dao.getAccountOnce() ?: return@withLock OperationResult.Error("Cuenta no encontrada.")
      val nextFrozen = !account.isCardFrozen
      dao.upsertAccount(account.copy(isCardFrozen = nextFrozen))
      val msg = if (nextFrozen) {
        "Tarjeta congelada por seguridad."
      } else {
        "Tarjeta activada para pagos."
      }
      OperationResult.Success(msg)
    }
  }

  suspend fun payService(
    service: ServiceBillerEntity,
    amountCents: Long,
    reference: String,
    note: String,
    onQueued: () -> Unit = {},
  ): OperationResult {
    if (amountCents <= 0L) {
      return OperationResult.Error("Ingresa un monto válido mayor a $0.00.")
    }

    val preCheck = transactionMutex.withLock {
      val account = dao.getAccountOnce() ?: return@withLock "Cuenta no disponible." to null
      if (account.isCardFrozen) {
        return@withLock "Tu tarjeta está congelada. Actívala para pagar servicios." to null
      }
      if (account.balanceCents < amountCents) {
        return@withLock "Saldo insuficiente. Deposita fondos para completar el pago." to null
      }
      val pendingTx = TransactionEntity(
        title = "Pago ${service.name}",
        subtitle = "${service.provider} • $reference",
        categoryLabel = service.category.labelEs,
        referenceCode = generateReference("SRV"),
        amountCents = -amountCents,
        balanceAfterCents = account.balanceCents,
        type = TransactionType.SERVICE_PAYMENT,
        status = TransactionStatus.PROCESSING,
        timestampMillis = System.currentTimeMillis(),
        iconKey = service.iconKey,
        note = note.trim(),
      )
      val insertedId = dao.insertTransaction(pendingTx).toInt()
      null to pendingTx.copy(id = insertedId)
    }

    val errorMsg = preCheck.first
    val queuedTx = preCheck.second
    if (errorMsg != null || queuedTx == null) {
      return OperationResult.Error(errorMsg ?: "No se pudo iniciar el pago.")
    }

    onQueued()
    delay(1150L)

    return transactionMutex.withLock {
      val account = dao.getAccountOnce() ?: return@withLock OperationResult.Error("Cuenta no disponible.")
      if (account.balanceCents < amountCents) {
        dao.updateTransaction(queuedTx.copy(status = TransactionStatus.FAILED))
        return@withLock OperationResult.Error("Saldo insuficiente al confirmar el pago.")
      }
      val newBalance = account.balanceCents - amountCents
      val cashback = (amountCents / 100L).coerceAtLeast(100L) // 1% cashback simulado (min $1.00)
      dao.upsertAccount(
        account.copy(
          balanceCents = newBalance,
          cashbackCents = account.cashbackCents + cashback,
        ),
      )
      dao.updateTransaction(
        queuedTx.copy(
          status = TransactionStatus.COMPLETED,
          balanceAfterCents = newBalance,
        ),
      )
      val latestService = dao.getServiceById(service.id) ?: service
      dao.updateService(
        latestService.copy(
          referenceNumber = reference,
          amountDueCents = amountCents,
          isPaid = true,
          lastPaidTimestamp = System.currentTimeMillis(),
          dueDateLabel = "Pagado hoy",
        ),
      )
      OperationResult.Success(
        message = "Pago de ${service.name} completado en tiempo real.",
        cashbackEarnedCents = cashback,
      )
    }
  }

  suspend fun depositFunds(
    amountCents: Long,
    sourceLabel: String,
    onQueued: () -> Unit = {},
  ): OperationResult {
    if (amountCents <= 0L) {
      return OperationResult.Error("Ingresa un monto válido mayor a $0.00.")
    }

    val queuedTx = transactionMutex.withLock {
      val account = dao.getAccountOnce() ?: return@withLock null
      val pending = TransactionEntity(
        title = "Depósito recibido",
        subtitle = sourceLabel,
        categoryLabel = "Ingreso",
        referenceCode = generateReference("DEP"),
        amountCents = amountCents,
        balanceAfterCents = account.balanceCents,
        type = TransactionType.DEPOSIT,
        status = TransactionStatus.PROCESSING,
        timestampMillis = System.currentTimeMillis(),
        iconKey = "deposit",
        note = "Abono en tiempo real",
      )
      val id = dao.insertTransaction(pending).toInt()
      pending.copy(id = id)
    } ?: return OperationResult.Error("Cuenta no disponible.")

    onQueued()
    delay(900L)

    return transactionMutex.withLock {
      val account = dao.getAccountOnce() ?: return@withLock OperationResult.Error("Cuenta no disponible.")
      val newBalance = account.balanceCents + amountCents
      dao.upsertAccount(account.copy(balanceCents = newBalance))
      dao.updateTransaction(
        queuedTx.copy(
          status = TransactionStatus.COMPLETED,
          balanceAfterCents = newBalance,
        ),
      )
      OperationResult.Success("Depósito acreditado en tu cuenta.")
    }
  }

  suspend fun sendTransfer(
    recipientName: String,
    clabeOrCard: String,
    amountCents: Long,
    concept: String,
    onQueued: () -> Unit = {},
  ): OperationResult {
    if (recipientName.isBlank()) {
      return OperationResult.Error("Ingresa el nombre del destinatario.")
    }
    if (amountCents <= 0L) {
      return OperationResult.Error("Ingresa un monto válido mayor a $0.00.")
    }

    val preCheck = transactionMutex.withLock {
      val account = dao.getAccountOnce() ?: return@withLock "Cuenta no disponible." to null
      if (account.isCardFrozen) {
        return@withLock "Tu tarjeta está congelada. Actívala para transferir." to null
      }
      if (account.balanceCents < amountCents) {
        return@withLock "Saldo insuficiente para realizar la transferencia." to null
      }
      val tail = clabeOrCard.filter { it.isDigit() }.takeLast(4).ifEmpty { "0000" }
      val pending = TransactionEntity(
        title = "Transferencia a ${recipientName.trim()}",
        subtitle = "SPEI •••• $tail",
        categoryLabel = "Transferencia",
        referenceCode = generateReference("SPEI"),
        amountCents = -amountCents,
        balanceAfterCents = account.balanceCents,
        type = TransactionType.TRANSFER,
        status = TransactionStatus.PROCESSING,
        timestampMillis = System.currentTimeMillis(),
        iconKey = "transfer",
        note = concept.trim().ifEmpty { "Transferencia inmediata" },
      )
      val id = dao.insertTransaction(pending).toInt()
      null to pending.copy(id = id)
    }

    val errorMsg = preCheck.first
    val queuedTx = preCheck.second
    if (errorMsg != null || queuedTx == null) {
      return OperationResult.Error(errorMsg ?: "No se pudo enviar la transferencia.")
    }

    onQueued()
    delay(1100L)

    return transactionMutex.withLock {
      val account = dao.getAccountOnce() ?: return@withLock OperationResult.Error("Cuenta no disponible.")
      if (account.balanceCents < amountCents) {
        dao.updateTransaction(queuedTx.copy(status = TransactionStatus.FAILED))
        return@withLock OperationResult.Error("Saldo insuficiente al liquidar SPEI.")
      }
      val newBalance = account.balanceCents - amountCents
      dao.upsertAccount(account.copy(balanceCents = newBalance))
      dao.updateTransaction(
        queuedTx.copy(
          status = TransactionStatus.COMPLETED,
          balanceAfterCents = newBalance,
        ),
      )
      OperationResult.Success("Transferencia SPEI enviada a ${recipientName.trim()}.")
    }
  }

  suspend fun addCustomService(
    name: String,
    provider: String,
    category: ServiceCategory,
    referenceNumber: String,
    amountCents: Long,
  ): OperationResult {
    if (name.isBlank() || referenceNumber.isBlank() || amountCents <= 0L) {
      return OperationResult.Error("Completa nombre, referencia y monto válido.")
    }
    val iconKey = when (category) {
      ServiceCategory.HOME -> "electricity"
      ServiceCategory.CONNECTIVITY -> "wifi"
      ServiceCategory.SUBSCRIPTIONS -> "streaming"
      ServiceCategory.CREDIT -> "credit"
      ServiceCategory.OTHER -> "receipt"
    }
    val entity = ServiceBillerEntity(
      name = name.trim(),
      provider = provider.trim().ifEmpty { name.trim() },
      category = category,
      referenceNumber = referenceNumber.trim(),
      amountDueCents = amountCents,
      dueDateLabel = "Vence este mes",
      iconKey = iconKey,
    )
    dao.insertService(entity)
    return OperationResult.Success("Servicio ${name.trim()} agregado.")
  }

  suspend fun renewServiceBill(service: ServiceBillerEntity) {
    val updated = service.copy(
      isPaid = false,
      dueDateLabel = "Nuevo recibo disponible",
    )
    dao.updateService(updated)
  }

  suspend fun simulateLiveActivity(): OperationResult {
    val scenarios = listOf(
      Triple("Reembolso Cashback", "Bonificación NovaBanca 2%", 4500L to TransactionType.CASHBACK),
      Triple("SPEI de Carlos R.", "Transferencia entrante •••• 7120", 32000L to TransactionType.DEPOSIT),
      Triple("Rendimiento Diario", "Interés cuenta digital", 1850L to TransactionType.DEPOSIT),
      Triple("SPEI de Laura V.", "Pago compartido •••• 9042", 25000L to TransactionType.DEPOSIT),
    )
    val chosen = scenarios[Random.nextInt(scenarios.size)]
    val amountCents = chosen.third.first
    val txType = chosen.third.second

    val queued = transactionMutex.withLock {
      val account = dao.getAccountOnce() ?: return@withLock null
      val pending = TransactionEntity(
        title = chosen.first,
        subtitle = chosen.second,
        categoryLabel = if (txType == TransactionType.CASHBACK) "Cashback" else "Ingreso en vivo",
        referenceCode = generateReference("LIVE"),
        amountCents = amountCents,
        balanceAfterCents = account.balanceCents,
        type = txType,
        status = TransactionStatus.PROCESSING,
        timestampMillis = System.currentTimeMillis(),
        iconKey = if (txType == TransactionType.CASHBACK) "cashback" else "deposit",
        note = "Movimiento automático en tiempo real",
      )
      val id = dao.insertTransaction(pending).toInt()
      pending.copy(id = id)
    } ?: return OperationResult.Error("Cuenta no disponible.")

    delay(850L)

    return transactionMutex.withLock {
      val account = dao.getAccountOnce() ?: return@withLock OperationResult.Error("Cuenta no disponible.")
      val newBalance = account.balanceCents + amountCents
      dao.upsertAccount(account.copy(balanceCents = newBalance))
      dao.updateTransaction(
        queued.copy(
          status = TransactionStatus.COMPLETED,
          balanceAfterCents = newBalance,
        ),
      )
      OperationResult.Success("Movimiento en vivo: ${chosen.first}")
    }
  }

  private fun generateReference(prefix: String): String {
    val num = Random.nextInt(100000, 999999)
    return "$prefix-$num"
  }
}
