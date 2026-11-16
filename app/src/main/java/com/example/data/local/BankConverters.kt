package com.example.data.local

import androidx.room.TypeConverter
import com.example.data.model.ServiceCategory
import com.example.data.model.TransactionStatus
import com.example.data.model.TransactionType

class BankConverters {
  @TypeConverter
  fun fromTransactionType(value: TransactionType): String = value.name

  @TypeConverter
  fun toTransactionType(value: String): TransactionType = TransactionType.valueOf(value)

  @TypeConverter
  fun fromTransactionStatus(value: TransactionStatus): String = value.name

  @TypeConverter
  fun toTransactionStatus(value: String): TransactionStatus = TransactionStatus.valueOf(value)

  @TypeConverter
  fun fromServiceCategory(value: ServiceCategory): String = value.name

  @TypeConverter
  fun toServiceCategory(value: String): ServiceCategory = ServiceCategory.valueOf(value)
}
