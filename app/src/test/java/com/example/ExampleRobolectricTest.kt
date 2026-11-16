package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.BankDatabase
import com.example.data.model.TransactionStatus
import com.example.data.repository.BankRepository
import com.example.data.repository.OperationResult
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  private lateinit var db: BankDatabase
  private lateinit var repository: BankRepository

  @Before
  fun setUp() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    db = Room.inMemoryDatabaseBuilder(context, BankDatabase::class.java)
      .allowMainThreadQueries()
      .build()
    repository = BankRepository(db.bankDao())
  }

  @After
  fun tearDown() {
    db.close()
  }

  @Test
  fun `app name matches NovaBanca`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("NovaBanca", appName)
  }

  @Test
  fun `paying a service deducts balance and records completed transaction`() = runBlocking {
    repository.ensureSeeded()
    val initialAccount = repository.accountFlow.first()!!
    val service = repository.servicesFlow.first().first()

    var queuedCalled = false
    val result = repository.payService(
      service = service,
      amountCents = service.amountDueCents,
      reference = service.referenceNumber,
      note = "Prueba unitaria",
      onQueued = { queuedCalled = true },
    )

    assertTrue(queuedCalled)
    assertTrue(result is OperationResult.Success)

    val updatedAccount = repository.accountFlow.first()!!
    assertEquals(initialAccount.balanceCents - service.amountDueCents, updatedAccount.balanceCents)

    val latestTx = repository.transactionsFlow.first().first()
    assertEquals(TransactionStatus.COMPLETED, latestTx.status)
    assertEquals(-service.amountDueCents, latestTx.amountCents)
  }
}
