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
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

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

    private var errorJob: Job? = null

    private val _registrationSuccess = mutableStateOf(false)
    val registrationSuccess: State<Boolean> = _registrationSuccess

    private val _passwordResetSent = mutableStateOf(false)
    val passwordResetSent: State<Boolean> = _passwordResetSent

    private fun showError(message: String?) {
        _error.value = message
        errorJob?.cancel()
        if (message != null) {
            errorJob = viewModelScope.launch {
                delay(5000.milliseconds)
                _error.value = null
            }
        }
    }

    fun signUp(email: String, password: String, name: String) {
        viewModelScope.launch {
            _isLoading.value = true
            showError(null)
            authService.signUp(email, password, name)
                .onSuccess { 
                    _registrationSuccess.value = true
                    onAuthSuccess()
                }
                .onFailure { showError(it.message) }
            _isLoading.value = false
        }
    }

    fun signIn(email: String, password: String) {
        viewModelScope.launch {
            _isLoading.value = true
            showError(null)
            authService.signIn(email, password)
                .onSuccess { onAuthSuccess() }
                .onFailure { showError(it.message) }
            _isLoading.value = false
        }
    }

    fun signInWithGoogle(idToken: String) {
        viewModelScope.launch {
            _isLoading.value = true
            showError(null)
            authService.signInWithGoogle(idToken)
                .onSuccess { onAuthSuccess() }
                .onFailure { showError(it.message) }
            _isLoading.value = false
        }
    }

    fun signInWithApple(idToken: String, rawNonce: String) {
        viewModelScope.launch {
            _isLoading.value = true
            showError(null)
            authService.signInWithApple(idToken, rawNonce)
                .onSuccess { onAuthSuccess() }
                .onFailure { showError(it.message) }
            _isLoading.value = false
        }
    }

    fun resetPassword(email: String) {
        viewModelScope.launch {
            _isLoading.value = true
            showError(null)
            authService.sendPasswordResetEmail(email)
                .onSuccess { 
                    _passwordResetSent.value = true
                }
                .onFailure { showError(it.message) }
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

    fun resetPasswordState() {
        _passwordResetSent.value = false
    }

    fun showValidationError(message: String) {
        showError(message)
    }
}
