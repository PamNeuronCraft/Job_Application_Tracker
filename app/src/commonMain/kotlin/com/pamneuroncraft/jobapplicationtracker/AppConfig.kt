package com.pamneuroncraft.jobapplicationtracker

interface AppConfig {
    val featureAiImport: Boolean
    val featureGoogleDriveBackup: Boolean
    val featureSummary: Boolean
}

class CommonAppConfig : AppConfig {
    override val featureAiImport: Boolean = AppBuildKonfig.FEATURE_AI_IMPORT
    override val featureGoogleDriveBackup: Boolean = AppBuildKonfig.FEATURE_GOOGLE_DRIVE_BACKUP
    override val featureSummary: Boolean = AppBuildKonfig.FEATURE_SUMMARY
}
