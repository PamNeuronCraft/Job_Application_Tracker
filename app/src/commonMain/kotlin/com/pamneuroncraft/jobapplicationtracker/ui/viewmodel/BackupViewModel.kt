package com.pamneuroncraft.jobapplicationtracker.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pamneuroncraft.jobapplicationtracker.domain.repository.CloudBackupService
import com.pamneuroncraft.jobapplicationtracker.domain.usecase.CloudBackupUseCase
import kotlinx.coroutines.launch

class BackupViewModel(
    private val cloudBackupUseCase: CloudBackupUseCase,
    private val cloudBackupService: CloudBackupService
) : ViewModel() {

    val isUserSignedIn: Boolean
        get() = cloudBackupService.isUserSignedIn

    fun backupToCloud() {
        viewModelScope.launch {
            cloudBackupUseCase.backupToCloud()
        }
    }

    fun restoreFromCloud() {
        viewModelScope.launch {
            cloudBackupUseCase.restoreFromCloud()
        }
    }
}
