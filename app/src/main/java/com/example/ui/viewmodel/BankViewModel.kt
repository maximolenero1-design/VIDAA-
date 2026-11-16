package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.BankDatabase
import com.example.data.model.AccountEntity
import com.example.data.model.ServiceBillerEntity
import com.example.data.model.ServiceCategory
import com.example.data.model.TransactionEntity
import com.example.data.model.TransactionStatus
import com.example.data.model.TransactionType
import com.example.data.repository.BankRepository
import com.example.data.repository.OperationResult
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class HistoryFilter(val labelEs: String) {
  ALL("Todos"),
  SERVICES("Servicios"),
  DEPOSITS("Depósitos"),
  TRANSFERS("Transferencias"),
  PROCESSING("En proceso"),
}

data class BankUiState(
  val isLoading: Boolean = true,
  val account: AccountEntity? = null,
  val services: List<ServiceBillerEntity> = emptyList(),
  val transactions: List<TransactionEntity> = emptyList(),
  val nowMillis: Long = System.currentTimeMillis(),
  val isBalanceHidden: Boolean = false,
  val isCardDetailsRevealed: Boolean = false,
  val isAutoStreamEnabled: Boolean = false,
  val serviceSearchQuery: String = "",
  val selectedServiceCategory: ServiceCategory? = null,
  val historySearchQuery: String = "",
  val selectedHistoryFilter: HistoryFilter = HistoryFilter.ALL,
  val activePayService: ServiceBillerEntity? = null,
  val activeReceiptTransaction: TransactionEntity? = null,
  val showDepositSheet: Boolean = false,
  val showTransferSheet: Boolean = false,
  val showAddServiceSheet: Boolean = false,
) {
  val pendingServices: List<ServiceBillerEntity>
    get() = services.filter { !it.isPaid }

  val filteredServices: List<ServiceBillerEntity>
    get() = services.filter { service ->
      val matchesCat = selectedServiceCategory == null || service.category == selectedServiceCategory
      val q = serviceSearchQuery.trim()
      val matchesQuery = q.isEmpty() ||
        service.name.contains(q, ignoreCase = true) ||
        service.provider.contains(q, ignoreCase = true) ||
        service.referenceNumber.contains(q, ignoreCase = true)
      matchesCat && matchesQuery
    }

  val filteredTransactions: List<TransactionEntity>
    get() = transactions.filter { tx ->
      val matchesFilter = when (selectedHistoryFilter) {
        HistoryFilter.ALL -> true
        HistoryFilter.SERVICES -> tx.type == TransactionType.SERVICE_PAYMENT
        HistoryFilter.DEPOSITS -> tx.type == TransactionType.DEPOSIT || tx.type == TransactionType.CASHBACK
        HistoryFilter.TRANSFERS -> tx.type == TransactionType.TRANSFER
        HistoryFilter.PROCESSING -> tx.status == TransactionStatus.PROCESSING
      }
      val q = historySearchQuery.trim()
      val matchesQuery = q.isEmpty() ||
        tx.title.contains(q, ignoreCase = true) ||
        tx.subtitle.contains(q, ignoreCase = true) ||
        tx.referenceCode.contains(q, ignoreCase = true)
      matchesFilter && matchesQuery
    }

  val totalIncomeCents: Long
    get() = transactions
      .filter { it.status == TransactionStatus.COMPLETED && it.amountCents > 0L }
      .sumOf { it.amountCents }

  val totalExpenseCents: Long
    get() = transactions
      .filter { it.status == TransactionStatus.COMPLETED && it.amountCents < 0L }
      .sumOf { -it.amountCents }

  val processingCount: Int
    get() = transactions.count { it.status == TransactionStatus.PROCESSING }
}

private data class LocalControlsState(
  val isBalanceHidden: Boolean = false,
  val isCardDetailsRevealed: Boolean = false,
  val isAutoStreamEnabled: Boolean = false,
  val serviceSearchQuery: String = "",
  val selectedServiceCategory: ServiceCategory? = null,
  val historySearchQuery: String = "",
  val selectedHistoryFilter: HistoryFilter = HistoryFilter.ALL,
  val activePayService: ServiceBillerEntity? = null,
  val activeReceiptTransaction: TransactionEntity? = null,
  val showDepositSheet: Boolean = false,
  val showTransferSheet: Boolean = false,
  val showAddServiceSheet: Boolean = false,
)

class BankViewModel(application: Application) : AndroidViewModel(application) {
  private val repository: BankRepository = BankRepository(
    BankDatabase.getInstance(application).bankDao(),
  )

  private val controls = MutableStateFlow(LocalControlsState())
  private val _events = MutableSharedFlow<String>(extraBufferCapacity = 8)
  val events: SharedFlow<String> = _events.asSharedFlow()

  private var autoStreamJob: Job? = null

  private val tickerFlow = flow {
    while (true) {
      emit(System.currentTimeMillis())
      delay(1000L)
    }
  }

  val uiState: StateFlow<BankUiState> = combine(
    repository.accountFlow,
    repository.servicesFlow,
    repository.transactionsFlow,
    controls,
    tickerFlow,
  ) { account, services, transactions, ctrl, now ->
    BankUiState(
      isLoading = account == null,
      account = account,
      services = services,
      transactions = transactions,
      nowMillis = now,
      isBalanceHidden = ctrl.isBalanceHidden,
      isCardDetailsRevealed = ctrl.isCardDetailsRevealed,
      isAutoStreamEnabled = ctrl.isAutoStreamEnabled,
      serviceSearchQuery = ctrl.serviceSearchQuery,
      selectedServiceCategory = ctrl.selectedServiceCategory,
      historySearchQuery = ctrl.historySearchQuery,
      selectedHistoryFilter = ctrl.selectedHistoryFilter,
      activePayService = ctrl.activePayService,
      activeReceiptTransaction = ctrl.activeReceiptTransaction,
      showDepositSheet = ctrl.showDepositSheet,
      showTransferSheet = ctrl.showTransferSheet,
      showAddServiceSheet = ctrl.showAddServiceSheet,
    )
  }.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(5000L),
    initialValue = BankUiState(),
  )

  init {
    viewModelScope.launch {
      repository.ensureSeeded()
    }
  }

  fun toggleBalanceHidden() {
    controls.update { it.copy(isBalanceHidden = !it.isBalanceHidden) }
  }

  fun toggleCardDetailsRevealed() {
    controls.update { it.copy(isCardDetailsRevealed = !it.isCardDetailsRevealed) }
  }

  fun toggleCardFrozen() {
    viewModelScope.launch {
      when (val result = repository.toggleCardFrozen()) {
        is OperationResult.Success -> _events.tryEmit(result.message)
        is OperationResult.Error -> _events.tryEmit(result.message)
      }
    }
  }

  fun toggleAutoStream() {
    val next = !controls.value.isAutoStreamEnabled
    controls.update { it.copy(isAutoStreamEnabled = next) }
    autoStreamJob?.cancel()
    if (next) {
      _events.tryEmit("Flujo en vivo activado: recibirás movimientos automáticos.")
      autoStreamJob = viewModelScope.launch {
        delay(2200L)
        while (true) {
          repository.simulateLiveActivity()
          delay(9000L)
        }
      }
    } else {
      _events.tryEmit("Flujo automático pausado.")
    }
  }

  fun triggerManualLiveEvent() {
    viewModelScope.launch {
      when (val result = repository.simulateLiveActivity()) {
        is OperationResult.Success -> _events.tryEmit(result.message)
        is OperationResult.Error -> _events.tryEmit(result.message)
      }
    }
  }

  fun updateServiceSearch(query: String) {
    controls.update { it.copy(serviceSearchQuery = query) }
  }

  fun selectServiceCategory(category: ServiceCategory?) {
    controls.update { it.copy(selectedServiceCategory = category) }
  }

  fun updateHistorySearch(query: String) {
    controls.update { it.copy(historySearchQuery = query) }
  }

  fun selectHistoryFilter(filter: HistoryFilter) {
    controls.update { it.copy(selectedHistoryFilter = filter) }
  }

  fun openPayServiceSheet(service: ServiceBillerEntity) {
    controls.update { it.copy(activePayService = service) }
  }

  fun dismissPayServiceSheet() {
    controls.update { it.copy(activePayService = null) }
  }

  fun openReceipt(tx: TransactionEntity) {
    controls.update { it.copy(activeReceiptTransaction = tx) }
  }

  fun dismissReceipt() {
    controls.update { it.copy(activeReceiptTransaction = null) }
  }

  fun openDepositSheet() {
    controls.update { it.copy(showDepositSheet = true) }
  }

  fun dismissDepositSheet() {
    controls.update { it.copy(showDepositSheet = false) }
  }

  fun openTransferSheet() {
    controls.update { it.copy(showTransferSheet = true) }
  }

  fun dismissTransferSheet() {
    controls.update { it.copy(showTransferSheet = false) }
  }

  fun openAddServiceSheet() {
    controls.update { it.copy(showAddServiceSheet = true) }
  }

  fun dismissAddServiceSheet() {
    controls.update { it.copy(showAddServiceSheet = false) }
  }

  fun confirmServicePayment(
    service: ServiceBillerEntity,
    amountCents: Long,
    reference: String,
    note: String,
  ) {
    viewModelScope.launch {
      val result = repository.payService(
        service = service,
        amountCents = amountCents,
        reference = reference,
        note = note,
        onQueued = {
          dismissPayServiceSheet()
          _events.tryEmit("Procesando pago de ${service.name} en tiempo real…")
        },
      )
      when (result) {
        is OperationResult.Success -> _events.tryEmit(result.message)
        is OperationResult.Error -> _events.tryEmit(result.message)
      }
    }
  }

  fun confirmDeposit(amountCents: Long, sourceLabel: String) {
    viewModelScope.launch {
      val result = repository.depositFunds(
        amountCents = amountCents,
        sourceLabel = sourceLabel,
        onQueued = {
          dismissDepositSheet()
          _events.tryEmit("Acreditando depósito en tiempo real…")
        },
      )
      when (result) {
        is OperationResult.Success -> _events.tryEmit(result.message)
        is OperationResult.Error -> _events.tryEmit(result.message)
      }
    }
  }

  fun confirmTransfer(
    recipientName: String,
    clabeOrCard: String,
    amountCents: Long,
    concept: String,
  ) {
    viewModelScope.launch {
      val result = repository.sendTransfer(
        recipientName = recipientName,
        clabeOrCard = clabeOrCard,
        amountCents = amountCents,
        concept = concept,
        onQueued = {
          dismissTransferSheet()
          _events.tryEmit("Enviando transferencia SPEI en tiempo real…")
        },
      )
      when (result) {
        is OperationResult.Success -> _events.tryEmit(result.message)
        is OperationResult.Error -> _events.tryEmit(result.message)
      }
    }
  }

  fun addCustomService(
    name: String,
    provider: String,
    category: ServiceCategory,
    referenceNumber: String,
    amountCents: Long,
  ) {
    viewModelScope.launch {
      when (val result = repository.addCustomService(name, provider, category, referenceNumber, amountCents)) {
        is OperationResult.Success -> {
          dismissAddServiceSheet()
          _events.tryEmit(result.message)
        }
        is OperationResult.Error -> _events.tryEmit(result.message)
      }
    }
  }

  fun renewServiceBill(service: ServiceBillerEntity) {
    viewModelScope.launch {
      repository.renewServiceBill(service)
      _events.tryEmit("Nuevo recibo generado para ${service.name}.")
    }
  }

  fun resetSimulator() {
    viewModelScope.launch {
      autoStreamJob?.cancel()
      controls.update { it.copy(isAutoStreamEnabled = false) }
      repository.resetSimulator()
      _events.tryEmit("Simulador reiniciado con saldo inicial.")
    }
  }

  override fun onCleared() {
    autoStreamJob?.cancel()
    super.onCleared()
  }
}
