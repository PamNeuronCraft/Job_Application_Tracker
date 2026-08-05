package com.pamneuroncraft.jobapplicationtracker.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.pamneuroncraft.jobapplicationtracker.domain.repository.BillingManager
import com.pamneuroncraft.jobapplicationtracker.domain.repository.SocialAuthManager
import com.pamneuroncraft.jobapplicationtracker.ui.components.AdBanner
import com.pamneuroncraft.jobapplicationtracker.ui.components.PremiumUpsellDialog
import com.pamneuroncraft.jobapplicationtracker.ui.theme.JobApplicationTrackerTheme
import com.pamneuroncraft.jobapplicationtracker.ui.viewmodel.ProfileViewModel
import com.pamneuroncraft.jobapplicationtracker.ui.util.rememberPlatformContext
import kotlinx.coroutines.launch
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import org.jetbrains.compose.resources.stringResource
import com.pamneuroncraft.jobapplicationtracker.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onBack: () -> Unit,
    onSubscriptionClick: () -> Unit,
    viewModel: ProfileViewModel = koinViewModel(),
    socialAuthManager: SocialAuthManager = koinInject(),
    billingManager: BillingManager = koinInject()
) {
    val user by viewModel.currentUser.collectAsState()
    val isPremium by billingManager.isPremium.collectAsState()
    val isLoading by viewModel.isLoading
    val error by viewModel.error
    val registrationSuccess by viewModel.registrationSuccess
    
    val scope = rememberCoroutineScope()
    val platformContext = rememberPlatformContext()

    var isSignUp by remember { mutableStateOf(false) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }

    if (registrationSuccess) {
        PremiumUpsellDialog(
            onDismiss = { viewModel.resetRegistrationState() },
            onNavigateToSubscription = onSubscriptionClick
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(Res.string.profile)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(Res.string.back))
                    }
                },
                actions = {
                    if (user != null) {
                        IconButton(onClick = { viewModel.signOut() }) {
                            Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = stringResource(Res.string.sign_out))
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = user?.displayName?.firstOrNull()?.toString()?.uppercase() 
                        ?: user?.email?.firstOrNull()?.toString()?.uppercase() 
                        ?: "G",
                    style = MaterialTheme.typography.displayMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (user != null) {
                Text(
                    text = stringResource(Res.string.hello_user, user?.displayName ?: stringResource(Res.string.default_user)),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = user?.email ?: "",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = onSubscriptionClick,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                ) {
                    Icon(Icons.Default.Star, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(Res.string.manage_subscription))
                }

                Spacer(modifier = Modifier.weight(1f))
                
                Button(
                    onClick = { viewModel.signOut() },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(stringResource(Res.string.sign_out))
                }
            } else {
                Text(
                    text = stringResource(if (isSignUp) Res.string.create_account else Res.string.welcome_back),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )

                if (isSignUp) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text(stringResource(Res.string.label_full_name)) },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isLoading
                    )
                }

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text(stringResource(Res.string.label_email)) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading
                )

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text(stringResource(Res.string.label_password)) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading
                )

                if (error != null) {
                    Surface(
                        color = MaterialTheme.colorScheme.errorContainer,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = error!!,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }

                Button(
                    onClick = {
                        if (isSignUp) {
                            viewModel.signUp(email, password, name)
                        } else {
                            viewModel.signIn(email, password)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading && email.isNotBlank() && password.isNotBlank()
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(stringResource(if (isSignUp) Res.string.sign_up else Res.string.sign_in))
                    }
                }

                TextButton(
                    onClick = { isSignUp = !isSignUp },
                    enabled = !isLoading
                ) {
                    Text(stringResource(if (isSignUp) Res.string.already_have_account else Res.string.dont_have_account))
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                OutlinedButton(
                    onClick = { 
                        scope.launch {
                            try {
                                val result = socialAuthManager.signInWithGoogle(platformContext)
                                if (result != null) {
                                    viewModel.signInWithGoogle(result.idToken)
                                }
                            } catch (_: Exception) {
                                // Silent for now or handle appropriately in ViewModel
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading
                ) {
                    Text(stringResource(Res.string.continue_with_google))
                }

                if (socialAuthManager.isAppleSignInSupported) {
                    OutlinedButton(
                        onClick = { 
                            scope.launch {
                                val result = socialAuthManager.signInWithApple(platformContext)
                                result?.let { viewModel.signInWithApple(it.idToken, it.rawNonce ?: "") }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isLoading
                    ) {
                        Text(stringResource(Res.string.continue_with_apple))
                    }
                }
                
                Spacer(modifier = Modifier.weight(1f))
            }

            if (!isPremium) {
                AdBanner(modifier = Modifier.fillMaxWidth())
            }
        }
    }
}

@Preview
@Composable
fun ProfileScreenPreview() {
    JobApplicationTrackerTheme {
        ProfileScreen(onBack = {}, onSubscriptionClick = {})
    }
}
