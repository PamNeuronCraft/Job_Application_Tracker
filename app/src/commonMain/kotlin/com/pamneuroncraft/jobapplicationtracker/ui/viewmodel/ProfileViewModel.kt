package com.pamneuroncraft.jobapplicationtracker.ui.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pamneuroncraft.jobapplicationtracker.domain.repository.AuthService
import com.pamneuroncraft.jobapplicationtracker.domain.repository.User
import com.pamneuroncraft.jobapplicationtracker.domain.repository.SyncManager
import com.pamneuroncraft.jobapplicationtracker.domain.repository.BillingManager
import com.pamneuroncraft.jobapplicationtracker.domain.repository.JobRepository
import com.pamneuroncraft.jobapplicationtracker.util.AnalyticsHelper
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val authService: AuthService,
    private val syncManager: SyncManager,
    private val billingManager: BillingManager,
    private val jobRepository: JobRepository,
    private val analyticsHelper: AnalyticsHelper
) : ViewModel() {

    val currentUser: StateFlow<User?> = authService.currentUser
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    private val _error = mutableStateOf<String?>(null)
    val error: State<String?> = _error

    private val _registrationSuccess = mutableStateOf(false)
    val registrationSuccess: State<Boolean> = _registrationSuccess

    fun signUp(email: String, password: String, name: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            authService.signUp(email, password, name)
                .onSuccess { 
                    _registrationSuccess.value = true
                    onAuthSuccess()
                }
                .onFailure { _error.value = it.message }
            _isLoading.value = false
        }
    }

    fun signIn(email: String, password: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            authService.signIn(email, password)
                .onSuccess { onAuthSuccess() }
                .onFailure { _error.value = it.message }
            _isLoading.value = false
        }
    }

    fun signInWithGoogle(idToken: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            authService.signInWithGoogle(idToken)
                .onSuccess { onAuthSuccess() }
                .onFailure { _error.value = it.message }
            _isLoading.value = false
        }
    }

    fun signInWithApple(idToken: String, rawNonce: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            authService.signInWithApple(idToken, rawNonce)
                .onSuccess { onAuthSuccess() }
                .onFailure { _error.value = it.message }
            _isLoading.value = false
        }
    }

    private fun onAuthSuccess() {
        viewModelScope.launch {
            authService.currentUser.first()?.let { user ->
                // Claim ownerless jobs created while signed out
                jobRepository.updateJobsUserId(user.uid)
                billingManager.logIn(user.uid)
                analyticsHelper.setUserId(user.uid)
                analyticsHelper.logEvent("login_success")
            }
            syncManager.triggerSync()
        }
    }

    fun signOut() {
        viewModelScope.launch {
            billingManager.logOut()
            analyticsHelper.setUserId(null)
            analyticsHelper.logEvent("sign_out")
            authService.signOut()
        }
    }
    
    fun resetRegistrationState() {
        _registrationSuccess.value = false
    }
}
