package com.tvrgs.domain.model

/**
 * A single Xtream Codes server ("DNS") that the quick-login flow can try automatically.
 * The client only ever types a username and password; the app resolves which host to
 * use behind the scenes.
 */
data class DnsHost(
    val name: String,
    val serverUrl: String,
    val priority: Int = 0
)
