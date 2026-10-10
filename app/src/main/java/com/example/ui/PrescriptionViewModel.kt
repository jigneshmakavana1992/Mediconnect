package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.MedConnectDatabase
import com.example.data.model.MedicationItem
import com.example.data.model.Prescription
import com.example.data.model.PrescriptionEntity
import com.example.data.repository.PrescriptionRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * Predefined date range filtering options.
 */
enum class DateFilterOption(val label: String) {
    ALL("All Dates"),
    TODAY("Today"),
    LAST_7_DAYS("Last 7 Days"),
    LAST_30_DAYS("Last 30 Days"),
    SPECIFIC_DATE("Specific Date")
}

/**
 * Configuration for date filtering criteria.
 */
data class DateFilterConfig(
    val option: DateFilterOption = DateFilterOption.ALL,
    val specificDateEpochMillis: Long? = null
)

/**
 * UI State representing the clinical prescription generator and viewer.
 */
data class PrescriptionUiState(
    val isLoading: Boolean = false,
    val searchQuery: String = "",
    val filterPatientId: String? = null,
    val filterPatientName: String? = null,
    val filterDoctorId: String? = null,
    val selectedDateFilter: DateFilterOption = DateFilterOption.ALL,
    val selectedDateEpochMillis: Long? = null,
    val selectedPrescription: PrescriptionEntity? = null,
    val lastGeneratedPrescription: PrescriptionEntity? = null,
    val isSignatureValid: Boolean? = null,
    val errorMessage: String? = null,
    val successMessage: String? = null
)

/**
 * ViewModel that utilizes [PrescriptionRepository] to expose prescription data to the UI via StateFlow.
 * Handles all asynchronous Room database interactions, reactive search queries, filtering,
 * and clinical prescription generation lifecycle.
 */
class PrescriptionViewModel(
    application: Application,
    repository: PrescriptionRepository? = null
) : AndroidViewModel(application) {

    val repository: PrescriptionRepository = repository ?: PrescriptionRepository(
        MedConnectDatabase.getDatabase(application, CoroutineScope(Dispatchers.IO)).prescriptionDao()
    )

    private val _uiState = MutableStateFlow(PrescriptionUiState())
    val uiState: StateFlow<PrescriptionUiState> = _uiState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _filterPatientId = MutableStateFlow<String?>(null)
    private val _filterPatientName = MutableStateFlow<String?>(null)
    private val _filterDoctorId = MutableStateFlow<String?>(null)
    private val _dateFilterConfig = MutableStateFlow(DateFilterConfig())

    /**
     * Reactive StateFlow of all prescriptions in the local Room database,
     * ordered by creation date descending.
     */
    val allPrescriptions: StateFlow<List<PrescriptionEntity>> = this.repository.allPrescriptions
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = emptyList()
        )

    /**
     * Reactive StateFlow of filtered prescriptions based on active search query, patient/doctor, and date filters.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    val filteredPrescriptions: StateFlow<List<PrescriptionEntity>> = combine(
        allPrescriptions,
        _searchQuery,
        _filterPatientId,
        _filterDoctorId,
        _dateFilterConfig
    ) { all, query, patientId, doctorId, dateConfig ->
        val now = System.currentTimeMillis()
        val dateFiltered = when (dateConfig.option) {
            DateFilterOption.ALL -> all
            DateFilterOption.TODAY -> {
                val startOfDay = now - (now % 86400000L)
                all.filter { it.createdAt >= startOfDay }
            }
            DateFilterOption.LAST_7_DAYS -> {
                val sevenDaysAgo = now - (7L * 24 * 3600 * 1000L)
                all.filter { it.createdAt >= sevenDaysAgo }
            }
            DateFilterOption.LAST_30_DAYS -> {
                val thirtyDaysAgo = now - (30L * 24 * 3600 * 1000L)
                all.filter { it.createdAt >= thirtyDaysAgo }
            }
            DateFilterOption.SPECIFIC_DATE -> {
                val targetMillis = dateConfig.specificDateEpochMillis ?: now
                val cal = java.util.Calendar.getInstance().apply {
                    timeInMillis = targetMillis
                    set(java.util.Calendar.HOUR_OF_DAY, 0)
                    set(java.util.Calendar.MINUTE, 0)
                    set(java.util.Calendar.SECOND, 0)
                    set(java.util.Calendar.MILLISECOND, 0)
                }
                val startOfDay = cal.timeInMillis
                val endOfDay = startOfDay + 86400000L - 1
                all.filter { it.createdAt in startOfDay..endOfDay }
            }
        }

        dateFiltered.filter { rx ->
            val matchesPatient = patientId == null ||
                    rx.patientId.equals(patientId, ignoreCase = true) ||
                    rx.patientName.contains(patientId, ignoreCase = true)
            val matchesDoctor = doctorId == null ||
                    rx.doctorId.equals(doctorId, ignoreCase = true) ||
                    rx.doctorName.contains(doctorId, ignoreCase = true)
            val matchesQuery = query.isBlank() ||
                    rx.patientName.contains(query, ignoreCase = true) ||
                    rx.diagnosisICD10.contains(query, ignoreCase = true) ||
                    rx.medicationsJson.contains(query, ignoreCase = true) ||
                    rx.id.contains(query, ignoreCase = true) ||
                    rx.doctorName.contains(query, ignoreCase = true)

            matchesPatient && matchesDoctor && matchesQuery
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = emptyList()
    )

    /**
     * Reactive StateFlow of recent prescriptions (latest 5).
     */
    val recentPrescriptions: StateFlow<List<PrescriptionEntity>> = this.repository.getRecentPrescriptions(5)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = emptyList()
        )

    /**
     * Total number of prescription records.
     */
    val totalPrescriptionCount: StateFlow<Int> = this.repository.getPrescriptionCount()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = 0
        )

    /**
     * Updates search query for full-text clinical prescription search.
     */
    fun setSearchQuery(query: String) {
        _searchQuery.value = query
        _uiState.update { it.copy(searchQuery = query) }
    }

    /**
     * Sets date range filter option.
     */
    fun filterByDate(option: DateFilterOption) {
        _dateFilterConfig.value = DateFilterConfig(option, null)
        _uiState.update {
            it.copy(
                selectedDateFilter = option,
                selectedDateEpochMillis = null
            )
        }
    }

    /**
     * Sets filter for a specific calendar date (epoch milliseconds).
     */
    fun filterBySpecificDate(epochMillis: Long) {
        _dateFilterConfig.value = DateFilterConfig(DateFilterOption.SPECIFIC_DATE, epochMillis)
        _uiState.update {
            it.copy(
                selectedDateFilter = DateFilterOption.SPECIFIC_DATE,
                selectedDateEpochMillis = epochMillis
            )
        }
    }

    /**
     * Sets patient filter with optional display name.
     */
    fun filterByPatient(patientId: String?, patientName: String? = null) {
        _filterPatientId.value = patientId
        _filterPatientName.value = patientName
        _uiState.update { it.copy(filterPatientId = patientId, filterPatientName = patientName) }
    }

    /**
     * Sets doctor filter.
     */
    fun filterByDoctor(doctorId: String?) {
        _filterDoctorId.value = doctorId
        _uiState.update { it.copy(filterDoctorId = doctorId) }
    }

    /**
     * Clears all filters and search query.
     */
    fun clearFilters() {
        _searchQuery.value = ""
        _filterPatientId.value = null
        _filterPatientName.value = null
        _filterDoctorId.value = null
        _dateFilterConfig.value = DateFilterConfig(DateFilterOption.ALL, null)
        _uiState.update {
            it.copy(
                searchQuery = "",
                filterPatientId = null,
                filterPatientName = null,
                filterDoctorId = null,
                selectedDateFilter = DateFilterOption.ALL,
                selectedDateEpochMillis = null
            )
        }
    }

    /**
     * Selects a prescription to display in detail mode and verifies its digital signature.
     */
    fun selectPrescription(prescription: PrescriptionEntity?) {
        val isValid = prescription?.let { repository.verifySignature(it) }
        _uiState.update {
            it.copy(
                selectedPrescription = prescription,
                isSignatureValid = isValid
            )
        }
    }

    /**
     * Loads a prescription asynchronously by its ID.
     */
    fun loadPrescriptionById(id: String): Job = viewModelScope.launch {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        val rx = repository.getPrescriptionById(id)
        if (rx != null) {
            val isValid = repository.verifySignature(rx)
            _uiState.update {
                it.copy(
                    isLoading = false,
                    selectedPrescription = rx,
                    isSignatureValid = isValid
                )
            }
        } else {
            _uiState.update {
                it.copy(
                    isLoading = false,
                    errorMessage = "Prescription not found with ID: $id"
                )
            }
        }
    }

    /**
     * Generates, cryptographically signs, and saves a clinical prescription asynchronously.
     */
    fun createPrescription(
        appointmentId: String,
        doctorId: String,
        doctorName: String,
        doctorQualifications: String,
        doctorSpeciality: String,
        councilRegistrationNumber: String,
        clinicAffiliation: String,
        patientId: String,
        patientName: String,
        patientAge: Int,
        patientGender: String,
        patientWeightKg: Double,
        patientAllergies: String,
        patientChronicConditions: String,
        diagnosisICD10: String,
        vitalsBp: String,
        vitalsPulse: String,
        vitalsSpO2: String,
        vitalsTemp: String,
        medications: List<MedicationItem>,
        clinicalAdvice: String,
        labOrders: String,
        followUpText: String,
        followUpDateTimestamp: Long,
        onSuccess: (PrescriptionEntity) -> Unit = {}
    ): Job = viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, successMessage = null) }

            // Serialize medications to compact JSON
            val medJsonBuilder = StringBuilder("[")
            medications.forEachIndexed { i, m ->
                medJsonBuilder.append(
                    """{"id":"${m.id}","name":"${m.name}","dosageForm":"${m.dosageForm}","dosageSchedule":"${m.dosageSchedule}","timing":"${m.timing}","durationDays":${m.durationDays},"totalQuantity":${m.totalQuantity},"instructions":"${m.instructions}"}"""
                )
                if (i < medications.size - 1) medJsonBuilder.append(",")
            }
            medJsonBuilder.append("]")

            val result = repository.generateAndSignPrescription(
                appointmentId = appointmentId,
                doctorId = doctorId,
                doctorName = doctorName,
                doctorQualifications = doctorQualifications,
                doctorSpeciality = doctorSpeciality,
                councilRegistrationNumber = councilRegistrationNumber,
                clinicAffiliation = clinicAffiliation,
                patientId = patientId,
                patientName = patientName,
                patientAge = patientAge,
                patientGender = patientGender,
                patientWeightKg = patientWeightKg,
                patientAllergies = patientAllergies,
                patientChronicConditions = patientChronicConditions,
                diagnosisICD10 = diagnosisICD10,
                vitalsBp = vitalsBp,
                vitalsPulse = vitalsPulse,
                vitalsSpO2 = vitalsSpO2,
                vitalsTemp = vitalsTemp,
                medicationsJson = medJsonBuilder.toString(),
                clinicalAdviceJson = clinicalAdvice,
                labOrdersJson = labOrders,
                followUpText = followUpText,
                followUpDateTimestamp = followUpDateTimestamp
            )

            if (result.isSuccess) {
                val prescription = result.getOrThrow()
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        lastGeneratedPrescription = prescription,
                        selectedPrescription = prescription,
                        isSignatureValid = true,
                        successMessage = "Prescription ${prescription.id} issued and digitally signed."
                    )
                }
                onSuccess(prescription)
            } else {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = result.exceptionOrNull()?.message ?: "Failed to generate prescription"
                    )
                }
            }
        }

    /**
     * Asynchronously updates a prescription in the local Room database.
     */
    fun updatePrescription(prescription: PrescriptionEntity): Job = viewModelScope.launch {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        try {
            repository.updatePrescription(prescription)
            _uiState.update {
                it.copy(
                    isLoading = false,
                    selectedPrescription = prescription,
                    successMessage = "Prescription updated successfully"
                )
            }
        } catch (e: Exception) {
            _uiState.update {
                it.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Failed to update prescription"
                )
            }
        }
    }

    /**
     * Asynchronously deletes a prescription by ID.
     */
    fun deletePrescription(id: String): Job = viewModelScope.launch {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        try {
            repository.deletePrescriptionById(id)
            _uiState.update {
                it.copy(
                    isLoading = false,
                    selectedPrescription = if (it.selectedPrescription?.id == id) null else it.selectedPrescription,
                    successMessage = "Prescription deleted"
                )
            }
        } catch (e: Exception) {
            _uiState.update {
                it.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Failed to delete prescription"
                )
            }
        }
    }

    /**
     * Verifies the authenticity and signature hash of a prescription record.
     */
    fun verifySignature(prescription: PrescriptionEntity): Boolean {
        val isValid = repository.verifySignature(prescription)
        _uiState.update { it.copy(isSignatureValid = isValid) }
        return isValid
    }

    /**
     * Clears banner messages from UI state.
     */
    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null, successMessage = null) }
    }
}
