package com.tvrgs.domain.usecase

import com.tvrgs.domain.model.Provider
import com.tvrgs.domain.model.Result
import com.tvrgs.domain.repository.MultiDnsRepository
import javax.inject.Inject

/**
 * Result of a quick-login attempt (username + password only, no server address).
 */
sealed class QuickLoginResult {
    data class Success(val provider: Provider) : QuickLoginResult()
    data class SavedWithWarning(val provider: Provider, val warning: String) : QuickLoginResult()
    data class ValidationError(val message: String) : QuickLoginResult()
    data class Error(val message: String) : QuickLoginResult()
}

/**
 * Logs a client in using only a username and password. Behind the scenes, it walks the
 * operator-configured list of Xtream Codes hosts ([MultiDnsRepository]) and tries each one in
 * order until one accepts the credentials, remembering whichever host worked so the next login
 * (or background resync) is instant.
 */
class QuickLogin @Inject constructor(
    private val multiDnsRepository: MultiDnsRepository,
    private val validateAndAddProvider: ValidateAndAddProvider
) {
    companion object {
        private const val DEFAULT_PROVIDER_NAME = "TVRGS"
    }

    suspend fun login(
        username: String,
        password: String,
        onProgress: ((String) -> Unit)? = null
    ): QuickLoginResult {
        val trimmedUsername = username.trim()
        val trimmedPassword = password.trim()

        if (trimmedUsername.isBlank() || trimmedPassword.isBlank()) {
            return QuickLoginResult.ValidationError("Informe usuário e senha.")
        }

        val hosts = when (val hostsResult = multiDnsRepository.getHosts()) {
            is Result.Success -> hostsResult.data
            is Result.Error -> return QuickLoginResult.Error(
                hostsResult.message.ifBlank {
                    "Não foi possível carregar os servidores. Verifique sua internet."
                }
            )
            Result.Loading -> return QuickLoginResult.Error("Estado inesperado ao carregar servidores.")
        }

        if (hosts.isEmpty()) {
            return QuickLoginResult.Error("Nenhum servidor configurado. Fale com o suporte.")
        }

        val rememberedHost = multiDnsRepository.getRememberedHost()
        val orderedHosts = hosts.sortedWith(
            compareByDescending<com.tvrgs.domain.model.DnsHost> { it.serverUrl == rememberedHost }
                .thenBy { it.priority }
        )

        var lastErrorMessage: String? = null

        for (host in orderedHosts) {
            onProgress?.invoke(host.name)

            val result = validateAndAddProvider.loginXtream(
                XtreamProviderSetupCommand(
                    serverUrl = host.serverUrl,
                    username = trimmedUsername,
                    password = trimmedPassword,
                    name = DEFAULT_PROVIDER_NAME,
                    xtreamFastSyncEnabled = true
                )
            )

            when (result) {
                is ValidateAndAddProviderResult.Success -> {
                    multiDnsRepository.rememberWorkingHost(host.serverUrl)
                    return QuickLoginResult.Success(result.provider)
                }

                is ValidateAndAddProviderResult.SavedWithWarning -> {
                    multiDnsRepository.rememberWorkingHost(host.serverUrl)
                    return QuickLoginResult.SavedWithWarning(result.provider, result.warning)
                }

                is ValidateAndAddProviderResult.ValidationError -> {
                    // Input itself is invalid (e.g. blank/too long) — this won't change by
                    // trying a different host, so stop here instead of looping needlessly.
                    return QuickLoginResult.ValidationError(result.message)
                }

                is ValidateAndAddProviderResult.Error -> {
                    lastErrorMessage = result.message
                }
            }
        }

        return QuickLoginResult.Error(
            lastErrorMessage ?: "Usuário ou senha inválidos, ou nenhum servidor respondeu."
        )
    }
}
