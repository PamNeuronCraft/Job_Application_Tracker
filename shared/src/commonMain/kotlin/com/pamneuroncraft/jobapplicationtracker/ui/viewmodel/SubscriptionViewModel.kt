package com.pamneuroncraft.jobapplicationtracker.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pamneuroncraft.jobapplicationtracker.domain.repository.AuthService
import com.pamneuroncraft.jobapplicationtracker.domain.repository.BillingManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SubscriptionViewModel(
    private val billingManager: BillingManager,
    private val authService: AuthService
) : ViewModel() {

    val isPremium: StateFlow<Boolean> = billingManager.isPremium
    
    val isUserSignedIn: Boolean get() = authService.isUserSignedIn()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun subscribe() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            val result = billingManager.purchaseSubscription()
            if (result.isFailure) {
                _error.value = result.exceptionOrNull()?.message
            }
            _isLoading.value = false
        }
    }

    fun restorePurchases() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            val result = billingManager.restorePurchases()
            if (result.isFailure) {
                _error.value = result.exceptionOrNull()?.message
            }
            _isLoading.value = false
        }
    }
}
