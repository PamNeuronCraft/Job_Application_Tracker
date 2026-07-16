package com.Pamneuroncraft.jobapplicationtracker.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.Pamneuroncraft.jobapplicationtracker.domain.usecase.BackupRestoreUseCase
import com.Pamneuroncraft.jobapplicationtracker.domain.usecase.GoogleDriveUseCase
import com.google.api.services.drive.Drive
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.io.InputStream
import java.io.OutputStream
import javax.inject.Inject

@HiltViewModel
class BackupViewModel @Inject constructor(
    private val backupRestoreUseCase: BackupRestoreUseCase,
    private val googleDriveUseCase: GoogleDriveUseCase
) : ViewModel() {

    fun exportData(outputStream: OutputStream) {
        viewModelScope.launch {
            backupRestoreUseCase.exportToJson(outputStream)
        }
    }

    fun importData(inputStream: InputStream) {
        viewModelScope.launch {
            backupRestoreUseCase.importFromJson(inputStream)
        }
    }

    fun backupToDrive(driveService: Drive) {
        viewModelScope.launch {
            googleDriveUseCase.backupToDrive(driveService)
        }
    }

    fun restoreFromDrive(driveService: Drive) {
        viewModelScope.launch {
            googleDriveUseCase.restoreFromDrive(driveService)
        }
    }
}
