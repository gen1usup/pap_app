package com.dadnavigator.app.presentation.screen.decision

import androidx.lifecycle.ViewModel
import com.dadnavigator.app.domain.model.HospitalDecisionInput
import com.dadnavigator.app.domain.usecase.decision.EvaluateHospitalDecisionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * ViewModel for hospital trip decision logic.
 */
@HiltViewModel
class DecisionViewModel @Inject constructor(
    private val evaluateHospitalDecisionUseCase: EvaluateHospitalDecisionUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(DecisionUiState())
    val uiState: StateFlow<DecisionUiState> = _uiState.asStateFlow()

    fun setContractionsLessThanFiveMinutes(value: Boolean) {
        _uiState.update { it.copy(contractionsLessThanFiveMinutes = value) }
    }

    fun setContractionsLongerThanMinute(value: Boolean) {
        _uiState.update { it.copy(contractionsLongerThanMinute = value) }
    }

    fun setContractionsRegularForHour(value: Boolean) {
        _uiState.update { it.copy(contractionsRegularForHour = value) }
    }

    fun setWaterBreak(value: Boolean) {
        _uiState.update { it.copy(waterBreak = value) }
    }

    fun setBleeding(value: Boolean) {
        _uiState.update { it.copy(bleeding = value) }
    }

    fun setConstantPain(value: Boolean) {
        _uiState.update { it.copy(constantPain = value) }
    }

    fun setFeverOrWorseCondition(value: Boolean) {
        _uiState.update { it.copy(feverOrWorseCondition = value) }
    }

    fun setDecreasedFetalMovement(value: Boolean) {
        _uiState.update { it.copy(decreasedFetalMovement = value) }
    }

    fun calculate() {
        val state = _uiState.value
        val result = evaluateHospitalDecisionUseCase(
            HospitalDecisionInput(
                contractionsLessThanFiveMinutes = state.contractionsLessThanFiveMinutes,
                contractionsLongerThanMinute = state.contractionsLongerThanMinute,
                contractionsRegularForHour = state.contractionsRegularForHour,
                waterBreak = state.waterBreak,
                bleeding = state.bleeding,
                constantPain = state.constantPain,
                feverOrWorseCondition = state.feverOrWorseCondition,
                decreasedFetalMovement = state.decreasedFetalMovement
            )
        )
        _uiState.update { it.copy(result = result) }
    }
}
