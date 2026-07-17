package com.tvrgs.domain.repository

import com.tvrgs.domain.model.DnsHost
import com.tvrgs.domain.model.Result

/**
 * Supplies the ordered list of Xtream Codes "DNS" hosts configured by the operator, used by
 * the quick-login (username + password only) flow. The list is normally fetched from a remote
 * JSON config the operator controls, and cached locally so the app still works offline / if the
 * remote config is briefly unreachable.
 */
interface MultiDnsRepository {
    /**
     * Returns the configured hosts, ordered by priority (lowest first). Uses the local cache
     * when it is still fresh, unless [forceRefresh] is true. Falls back to the last cached list
     * if the remote config can't be reached.
     */
    suspend fun getHosts(forceRefresh: Boolean = false): Result<List<DnsHost>>

    /** Remembers which host last authenticated successfully, so future logins try it first. */
    suspend fun rememberWorkingHost(serverUrl: String)

    /** The last host that successfully authenticated, if any. */
    suspend fun getRememberedHost(): String?
}
