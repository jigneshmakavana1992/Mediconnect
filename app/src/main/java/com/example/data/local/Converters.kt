package com.example.data.local

import androidx.room.TypeConverter
import com.example.data.model.AppointmentStatus
import com.example.data.model.PayoutBatchStatus
import com.example.data.model.UserRole

class Converters {
    @TypeConverter
    fun fromUserRole(value: UserRole): String = value.name

    @TypeConverter
    fun toUserRole(value: String): UserRole = try {
        UserRole.valueOf(value)
    } catch (e: Exception) {
        UserRole.PATIENT
    }

    @TypeConverter
    fun fromAppointmentStatus(value: AppointmentStatus): String = value.name

    @TypeConverter
    fun toAppointmentStatus(value: String): AppointmentStatus = try {
        AppointmentStatus.valueOf(value)
    } catch (e: Exception) {
        AppointmentStatus.SCHEDULED
    }

    @TypeConverter
    fun fromPayoutBatchStatus(value: PayoutBatchStatus): String = value.name

    @TypeConverter
    fun toPayoutBatchStatus(value: String): PayoutBatchStatus = try {
        PayoutBatchStatus.valueOf(value)
    } catch (e: Exception) {
        PayoutBatchStatus.MATURED_QUEUED
    }
}
