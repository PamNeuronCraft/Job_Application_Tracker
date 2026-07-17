package com.pamneuroncraft.jobapplicationtracker

interface AppConfig {
    val featureAiImport: Boolean
    val featureGoogleDriveBackup: Boolean
}

class CommonAppConfig : AppConfig {
    override val featureAiImport: Boolean = AppBuildKonfig.FEATURE_AI_IMPORT
    override val featureGoogleDriveBackup: Boolean = AppBuildKonfig.FEATURE_GOOGLE_DRIVE_BACKUP
}
