package com.pamneuroncraft.jobapplicationtracker.domain.repository

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import platform.AuthenticationServices.*
import platform.Foundation.NSData
import platform.Foundation.NSString
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.base64EncodedStringWithOptions
import platform.Foundation.create
import platform.UIKit.UIApplication
import platform.UIKit.UIViewController
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.BetaInteropApi

class IosSocialAuthManager : SocialAuthManager {

    @Composable
    override fun RequestGoogleSignIn(onResult: (SocialAuthResult?) -> Unit) {
        // Requires GoogleSignIn SDK via CocoaPods
        LaunchedEffect(Unit) {
            onResult(null)
        }
    }

    @OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
    @Composable
    override fun RequestAppleSignIn(onResult: (SocialAuthResult?) -> Unit) {
        LaunchedEffect(Unit) {
            val appleIDProvider = ASAuthorizationAppleIDProvider()
            val request = appleIDProvider.createRequest().apply {
                requestedScopes = listOf(ASAuthorizationScopeFullName, ASAuthorizationScopeEmail)
            }

            val controller = ASAuthorizationController(listOf(request))
            val delegate = object : platform.darwin.NSObject(), ASAuthorizationControllerDelegateProtocol {
                override fun authorizationController(
                    controller: ASAuthorizationController,
                    didCompleteWithAuthorization: ASAuthorization
                ) {
                    val appleIDCredential = didCompleteWithAuthorization.credential as? ASAuthorizationAppleIDCredential
                    val idTokenData = appleIDCredential?.identityToken
                    if (idTokenData != null) {
                        val idToken = NSString.create(data = idTokenData, encoding = NSUTF8StringEncoding).toString()
                        MainScope().launch {
                            onResult(SocialAuthResult(idToken = idToken))
                        }
                    } else {
                        MainScope().launch { onResult(null) }
                    }
                }

                override fun authorizationController(
                    controller: ASAuthorizationController,
                    didCompleteWithError: platform.Foundation.NSError
                ) {
                    MainScope().launch { onResult(null) }
                }
            }
            
            controller.delegate = delegate
            controller.performRequests()
        }
    }
}

actual fun createSocialAuthManager(): SocialAuthManager = IosSocialAuthManager()

