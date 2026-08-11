package com.pamneuroncraft.jobapplicationtracker

import com.pamneuroncraft.jobapplicationtracker.data.local.LocalSettings

interface AppConfig {
    val featureAiImport: Boolean
    val featureGoogleDriveBackup: Boolean
    val featureSummary: Boolean
    val featureBiometrics: Boolean
    val featureExport: Boolean
    val featureEmailSync: Boolean
    val googleWebClientId: String
    val isDebug: Boolean
}

class CommonAppConfig(
    private val localSettings: LocalSettings,
    override val isDebug: Boolean
) : AppConfig {
    override val featureAiImport: Boolean 
        get() = AppBuildKonfig.FEATURE_AI_IMPORT && (isDebug || localSettings.isPremium)
    
    override val featureGoogleDriveBackup: Boolean 
        get() = AppBuildKonfig.FEATURE_GOOGLE_DRIVE_BACKUP && (isDebug || localSettings.isPremium)
    
    override val featureSummary: Boolean 
        get() = AppBuildKonfig.FEATURE_SUMMARY && (isDebug || localSettings.isPremium)

    override val featureBiometrics: Boolean
        get() = isDebug || localSettings.isPremium

    override val featureExport: Boolean
        get() = isDebug || localSettings.isPremium

    override val featureEmailSync: Boolean
        get() = isDebug || localSettings.isPremium

    override val googleWebClientId: String
        get() = if (isDebug) AppBuildKonfig.GOOGLE_WEB_CLIENT_ID_DEBUG else AppBuildKonfig.GOOGLE_WEB_CLIENT_ID_RELEASE
}
