package com.tvrgs.app.ui.screens.welcome

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Surface
import androidx.tv.material3.SurfaceDefaults
import androidx.tv.material3.Text
import com.tvrgs.app.BuildConfig
import com.tvrgs.app.R
import com.tvrgs.app.ui.components.LoginTextField
import com.tvrgs.app.ui.design.AppColors
import com.tvrgs.app.ui.interaction.TvButton
import com.tvrgs.data.sync.SyncProgressBus
import com.tvrgs.domain.repository.ProviderRepository
import com.tvrgs.domain.sync.Section
import com.tvrgs.domain.sync.SyncProgress
import com.tvrgs.domain.usecase.M3uProviderSetupCommand
import com.tvrgs.domain.usecase.ValidateAndAddProvider
import com.tvrgs.domain.usecase.XtreamProviderSetupCommand
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

@HiltViewModel
class WelcomeViewModel @Inject constructor(
    private val providerRepository: ProviderRepository,
    private val validateAndAddProvider: ValidateAndAddProvider,
    syncProgressBus: SyncProgressBus
) : ViewModel() {

    private val _hasProviders = MutableStateFlow<Boolean?>(null)
    val hasProviders: StateFlow<Boolean?> = _hasProviders.asStateFlow()

    // Raw, unfiltered sync progress. `syncProgressBus` is an app-wide singleton, so the
    // screen itself decides when a value is trustworthy (see WelcomeScreen's `syncProgress`,
    // which only shows this while a login/sync attempt is actually in flight).
    val syncProgress: StateFlow<SyncProgress?> = syncProgressBus.flow

    init {
        viewModelScope.launch {
            maybeSeedDevProvider()
            providerRepository.getProviders()
                .map { it.isNotEmpty() }
                .collect { _hasProviders.value = it }
        }
    }

    private suspend fun maybeSeedDevProvider() {
        if (providerRepository.getProviders().first().isNotEmpty()) return

        val xtreamServer = BuildConfig.XTREAM_DEV_SERVER
        val xtreamUser = BuildConfig.XTREAM_DEV_USERNAME
        val xtreamPass = BuildConfig.XTREAM_DEV_PASSWORD
        if (xtreamServer.isNotBlank() && xtreamUser.isNotBlank() && xtreamPass.isNotBlank()) {
            validateAndAddProvider.loginXtream(
                XtreamProviderSetupCommand(
                    serverUrl = xtreamServer,
                    username = xtreamUser,
                    password = xtreamPass,
                    name = BuildConfig.XTREAM_DEV_NAME.ifBlank { "Dev (seeded)" },
                    xtreamFastSyncEnabled = true
                )
            )
            return
        }

        val m3uUrl = BuildConfig.M3U_DEV_URL
        if (m3uUrl.isNotBlank()) {
            validateAndAddProvider.addM3u(
                M3uProviderSetupCommand(
                    url = m3uUrl,
                    name = BuildConfig.M3U_DEV_NAME.ifBlank { "Dev M3U (seeded)" }
                )
            )
        }
    }
}

@Composable
fun WelcomeScreen(
    onNavigateToHome: () -> Unit,
    startupReady: Boolean = true,
    onNavigateToSetup: () -> Unit,
    viewModel: WelcomeViewModel = hiltViewModel(),
    loginViewModel: QuickLoginViewModel = hiltViewModel()
) {
    val hasProviders by viewModel.hasProviders.collectAsStateWithLifecycle()
    val rawSyncProgress by viewModel.syncProgress.collectAsStateWithLifecycle()
    val loginUiState by loginViewModel.uiState.collectAsStateWithLifecycle()

    // SyncProgressBus is an app-wide singleton, so only trust its value while a login
    // triggered from this screen is actually in flight — otherwise a stale reading from an
    // earlier session could flash on screen for a frame.
    val syncProgress = if (loginUiState.isSubmitting) rawSyncProgress else null

    LaunchedEffect(hasProviders, startupReady, loginUiState.isSubmitting) {
        // `hasProviders` flips true as soon as the login credentials are saved — which
        // happens *before* the channels/movies/series/EPG sync that follows has finished.
        // Waiting on `!isSubmitting` too means we only leave this screen once that sync is
        // fully done, so Home opens with content already loaded instead of empty lists.
        if (hasProviders == true && startupReady && !loginUiState.isSubmitting) {
            onNavigateToHome()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.22f),
                            AppColors.HeroTop,
                            AppColors.HeroBottom
                        )
                    )
                )
        )

        // Scrollable + centered: centers the card when it fits the screen, and lets it
        // scroll instead of overflowing off-screen on shorter/lower-resolution displays.
        Box(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            contentAlignment = Alignment.Center
        ) {
            // Loading state covers both "still checking if a login exists" (null) and
            // "logged in, syncing" (true) — only the confirmed-empty case shows the form.
            val showLoadingCard = hasProviders != false
            Crossfade(
                targetState = showLoadingCard,
                animationSpec = tween(durationMillis = 380, easing = FastOutSlowInEasing),
                label = "welcomeCardTransition"
            ) { loading ->
                if (loading) {
                    WelcomeLoadingCard(
                        syncProgress = syncProgress,
                        modifier = Modifier.padding(32.dp)
                    )
                } else {
                    WelcomeStartCard(
                        loginViewModel = loginViewModel,
                        modifier = Modifier.padding(32.dp)
                    )
                }
            }
        }
    }
}

/**
 * Overall fraction (0f–1f) across the whole sync — channels, then movies, then series each
 * count for a third of the ring, so it fills continuously across the three stages instead of
 * resetting per section.
 */
private fun overallSyncFraction(progress: SyncProgress?): Float {
    if (progress == null) return 0f
    val sectionIndex = when (progress.section) {
        Section.LIVE -> 0
        Section.VOD -> 1
        Section.SERIES -> 2
    }
    val sectionCount = 3
    val sectionFraction = if (progress.total > 0) {
        (progress.current.toFloat() / progress.total.toFloat()).coerceIn(0f, 1f)
    } else {
        0f
    }
    return ((sectionIndex + sectionFraction) / sectionCount).coerceIn(0f, 1f)
}

@Composable
private fun WelcomeLoadingCard(
    syncProgress: SyncProgress?,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(28.dp),
        colors = SurfaceDefaults.colors(containerColor = AppColors.Surface.copy(alpha = 0.9f))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 36.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val isDeterminate = syncProgress != null && syncProgress.total > 0
            val animatedFraction by animateFloatAsState(
                targetValue = overallSyncFraction(syncProgress),
                animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing),
                label = "syncRingProgress"
            )

            // The TVRGS logo sits inside a ring that fills as the sync advances — spinning
            // indeterminately until the first section reports a real total, then animating
            // smoothly toward 100% as channels, movies and series each complete.
            Box(modifier = Modifier.size(132.dp), contentAlignment = Alignment.Center) {
                if (isDeterminate) {
                    CircularProgressIndicator(
                        progress = { animatedFraction },
                        modifier = Modifier.fillMaxSize(),
                        color = AppColors.Brand,
                        trackColor = AppColors.BrandMuted,
                        strokeWidth = 6.dp
                    )
                } else {
                    CircularProgressIndicator(
                        modifier = Modifier.fillMaxSize(),
                        color = AppColors.Brand,
                        trackColor = AppColors.BrandMuted,
                        strokeWidth = 6.dp
                    )
                }
                Image(
                    painter = painterResource(R.drawable.ic_launcher_vault_art),
                    contentDescription = stringResource(R.string.app_name),
                    modifier = Modifier
                        .size(94.dp)
                        .clip(CircleShape)
                )
            }

            Spacer(modifier = Modifier.height(22.dp))

            Text(
                text = stringResource(R.string.welcome_loading_title),
                style = MaterialTheme.typography.titleLarge,
                color = AppColors.TextPrimary
            )
            Spacer(modifier = Modifier.height(6.dp))

            val subtitle = when {
                syncProgress != null && syncProgress.currentLabel.isNotBlank() -> syncProgress.currentLabel
                syncProgress != null -> stringResource(sectionLabelRes(syncProgress.section))
                else -> stringResource(R.string.welcome_loading_subtitle)
            }
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyLarge,
                color = AppColors.TextSecondary,
                textAlign = TextAlign.Center
            )

            if (syncProgress != null) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = stringResource(
                        R.string.sync_items_indexed_format,
                        syncProgress.itemsIndexed
                    ),
                    style = MaterialTheme.typography.labelLarge,
                    color = AppColors.TextSecondary
                )
            }
        }
    }
}

@Composable
private fun WelcomeStartCard(
    loginViewModel: QuickLoginViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by loginViewModel.uiState.collectAsStateWithLifecycle()
    val passwordFocusRequester = remember { FocusRequester() }

    Surface(
        modifier = modifier
            .widthIn(max = 520.dp)
            .fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = SurfaceDefaults.colors(containerColor = AppColors.Surface.copy(alpha = 0.9f))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 40.dp, vertical = 34.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Image(
                painter = painterResource(R.drawable.ic_launcher_vault_art),
                contentDescription = stringResource(R.string.app_name),
                modifier = Modifier
                    .size(88.dp)
                    .clip(RoundedCornerShape(20.dp))
            )
            Text(
                text = stringResource(R.string.welcome_login_subtitle),
                style = MaterialTheme.typography.bodyLarge,
                color = AppColors.TextSecondary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(4.dp))

            LoginTextField(
                value = uiState.username,
                onValueChange = loginViewModel::onUsernameChange,
                placeholder = stringResource(R.string.setup_username),
                imeAction = ImeAction.Next,
                onImeAction = { passwordFocusRequester.requestFocus() },
                enabled = !uiState.isSubmitting
            )
            LoginTextField(
                value = uiState.password,
                onValueChange = loginViewModel::onPasswordChange,
                placeholder = stringResource(R.string.setup_password),
                isPassword = true,
                imeAction = ImeAction.Done,
                onImeAction = { loginViewModel.submit() },
                enabled = !uiState.isSubmitting,
                focusRequester = passwordFocusRequester
            )

            if (uiState.errorMessage != null) {
                Text(
                    text = uiState.errorMessage ?: stringResource(R.string.welcome_login_error_generic),
                    style = MaterialTheme.typography.bodyMedium,
                    color = AppColors.Live,
                    textAlign = TextAlign.Center
                )
            }

            TvButton(
                onClick = { loginViewModel.submit() },
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isSubmitting
            ) {
                if (uiState.isSubmitting) {
                    CircularProgressIndicator(
                        color = AppColors.TextPrimary,
                        modifier = Modifier.height(20.dp).width(20.dp)
                    )
                } else {
                    Text(text = stringResource(R.string.welcome_login_button))
                }
            }
        }
    }
}

private fun sectionLabelRes(section: Section): Int = when (section) {
    Section.LIVE -> R.string.sync_section_live
    Section.VOD -> R.string.sync_section_vod
    Section.SERIES -> R.string.sync_section_series
}
