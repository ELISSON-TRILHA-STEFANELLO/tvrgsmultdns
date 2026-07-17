package com.tvrgs.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.tvrgs.domain.model.DnsHost
import com.tvrgs.domain.model.Result
import com.tvrgs.domain.repository.MultiDnsRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

private val Context.multiDnsDataStore: DataStore<Preferences> by preferencesDataStore(name = "multi_dns_preferences")

/**
 * Fetches the operator-controlled list of Xtream Codes "DNS" hosts from a remote JSON config,
 * caches it locally (so quick-login keeps working offline / if the config endpoint is briefly
 * down), and remembers which host last authenticated successfully.
 *
 * Expected JSON shape at [MULTI_DNS_CONFIG_URL]:
 * ```json
 * {
 *   "hosts": [
 *     { "name": "Servidor 1", "url": "http://dns1.example.com:8080", "priority": 1 },
 *     { "name": "Servidor 2", "url": "http://dns2.example.com:8080", "priority": 2 }
 *   ]
 * }
 * ```
 */
@Singleton
class MultiDnsRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val okHttpClient: OkHttpClient
) : MultiDnsRepository {

    companion object {
        // Update this to wherever you host the multi-DNS JSON config. See NOTICE / docs for the
        // exact JSON format expected.
        private const val MULTI_DNS_CONFIG_URL = "https://tvrgs.com/app/multidns.json"

        private const val CACHE_TTL_MS = 6 * 60 * 60 * 1000L // 6 hours
        private const val MAX_RESPONSE_BYTES = 256 * 1024L

        private val KEY_CACHED_JSON = stringPreferencesKey("cached_hosts_json")
        private val KEY_CACHED_AT = longPreferencesKey("cached_hosts_at")
        private val KEY_LAST_WORKING_HOST = stringPreferencesKey("last_working_host")
    }

    override suspend fun getHosts(forceRefresh: Boolean): Result<List<DnsHost>> =
        withContext(Dispatchers.IO) {
            val prefs = context.multiDnsDataStore.data.first()
            val cachedJson = prefs[KEY_CACHED_JSON]
            val cachedAt = prefs[KEY_CACHED_AT] ?: 0L
            val isCacheFresh = cachedJson != null &&
                (System.currentTimeMillis() - cachedAt) < CACHE_TTL_MS

            if (!forceRefresh && isCacheFresh) {
                val cachedHosts = runCatching { parseHosts(cachedJson!!) }.getOrNull()
                if (!cachedHosts.isNullOrEmpty()) {
                    return@withContext Result.success(cachedHosts)
                }
            }

            val remoteResult = fetchRemoteHosts()
            when (remoteResult) {
                is Result.Success -> {
                    context.multiDnsDataStore.edit { store ->
                        store[KEY_CACHED_JSON] = remoteResult.data.rawJson
                        store[KEY_CACHED_AT] = System.currentTimeMillis()
                    }
                    Result.success(remoteResult.data.hosts)
                }

                is Result.Error -> {
                    // Remote fetch failed — fall back to whatever is cached, even if stale,
                    // so the app keeps working while the operator's config endpoint is down.
                    val fallbackHosts = cachedJson?.let { runCatching { parseHosts(it) }.getOrNull() }
                    if (!fallbackHosts.isNullOrEmpty()) {
                        Result.success(fallbackHosts)
                    } else {
                        Result.error(remoteResult.message, remoteResult.exception)
                    }
                }

                Result.Loading -> Result.error("Unexpected loading state")
            }
        }

    override suspend fun rememberWorkingHost(serverUrl: String) {
        context.multiDnsDataStore.edit { store ->
            store[KEY_LAST_WORKING_HOST] = serverUrl
        }
    }

    override suspend fun getRememberedHost(): String? =
        context.multiDnsDataStore.data.first()[KEY_LAST_WORKING_HOST]

    private data class RemoteHostsFetch(val hosts: List<DnsHost>, val rawJson: String)

    private fun fetchRemoteHosts(): Result<RemoteHostsFetch> {
        return try {
            val request = Request.Builder()
                .url(MULTI_DNS_CONFIG_URL)
                .header("User-Agent", "TVRGS-MultiDns-Client")
                .build()

            okHttpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    return Result.error("Falha ao buscar servidores: HTTP ${response.code}")
                }

                val body = response.body ?: return Result.error("Resposta vazia do servidor de configuração.")
                if ((body.contentLength().takeIf { it >= 0 } ?: 0L) > MAX_RESPONSE_BYTES) {
                    return Result.error("Configuração de servidores excedeu o tamanho máximo permitido.")
                }
                val rawJson = body.string()
                if (rawJson.isBlank()) {
                    return Result.error("Resposta vazia do servidor de configuração.")
                }

                val hosts = parseHosts(rawJson)
                if (hosts.isEmpty()) {
                    return Result.error("Nenhum servidor válido na configuração remota.")
                }

                Result.success(RemoteHostsFetch(hosts = hosts, rawJson = rawJson))
            }
        } catch (error: IOException) {
            Result.error("Falha de rede ao buscar servidores.", error)
        } catch (error: Exception) {
            Result.error("Erro ao processar configuração de servidores: ${error.message}", error)
        }
    }

    private fun parseHosts(rawJson: String): List<DnsHost> {
        val root = JSONObject(rawJson)
        val hostsArray: JSONArray = root.optJSONArray("hosts") ?: JSONArray()
        val hosts = mutableListOf<DnsHost>()
        for (index in 0 until hostsArray.length()) {
            val entry = hostsArray.optJSONObject(index) ?: continue
            val url = entry.optString("url").trim().takeIf { it.isNotBlank() } ?: continue
            val name = entry.optString("name").trim().ifBlank { url }
            val priority = entry.optInt("priority", index)
            hosts += DnsHost(name = name, serverUrl = url.trimEnd('/'), priority = priority)
        }
        return hosts.sortedBy { it.priority }
    }
}
