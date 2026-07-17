package com.tvrgs.app.update

import com.tvrgs.app.BuildConfig
import java.time.Instant
import kotlin.math.max

enum class AppUpdateActionState {
    None,
    DownloadLatest,
    Downloading,
    InstallLatest,
    InstallPermissionRequired
}

fun isRemoteVersionNewer(
    remoteVersionCode: Int?,
    remoteVersionName: String,
    remotePublishedAt: String? = null
): Boolean {
    return isRemoteVersionNewerForBuild(
        remoteVersionCode = remoteVersionCode,
        remoteVersionName = remoteVersionName,
        remotePublishedAt = remotePublishedAt,
        currentVersionCode = BuildConfig.VERSION_CODE,
        currentVersionName = BuildConfig.VERSION_NAME,
        currentBuildTimestampUtc = BuildConfig.BUILD_TIMESTAMP_UTC,
        currentChannel = AppUpdateChannel.fromCurrentBuild()
    )
}

fun isRemoteVersionNewerForBuild(
    remoteVersionCode: Int?,
    remoteVersionName: String,
    remotePublishedAt: String?,
    currentVersionCode: Int,
    currentVersionName: String,
    currentBuildTimestampUtc: Long,
    currentChannel: AppUpdateChannel
): Boolean {
    val remoteDescriptor = parseAppVersionDescriptor(remoteVersionName)
    if (remoteDescriptor.channel != currentChannel) {
        return false
    }

    if (remoteVersionCode != null && remoteVersionCode > currentVersionCode) {
        return true
    }

    val versionComparison = compareVersionNamesStatic(
        remoteDescriptor.baseVersionName,
        parseAppVersionDescriptor(currentVersionName).baseVersionName
    )
    if (versionComparison != 0) {
        return versionComparison > 0
    }

    if (currentChannel == AppUpdateChannel.Beta) {
        val remotePublishedAtMillis = remotePublishedAt.toEpochMillisOrNull() ?: return false
        return remotePublishedAtMillis > currentBuildTimestampUtc
    }

    return false
}

fun compareVersionNamesStatic(left: String, right: String): Int {
    val leftParts = left.removePrefix("v").split('.')
    val rightParts = right.removePrefix("v").split('.')
    val length = max(leftParts.size, rightParts.size)
    for (index in 0 until length) {
        val leftValue = leftParts.getOrNull(index)?.toIntOrNull() ?: 0
        val rightValue = rightParts.getOrNull(index)?.toIntOrNull() ?: 0
        if (leftValue != rightValue) {
            return leftValue.compareTo(rightValue)
        }
    }
    return 0
}

fun isLatestAppUpdateDownloaded(
    latestVersionName: String?,
    downloadState: AppUpdateDownloadState
): Boolean {
    return !latestVersionName.isNullOrBlank() &&
        downloadState.status == AppUpdateDownloadStatus.Downloaded &&
        downloadState.versionName == latestVersionName
}

fun latestAppUpdateAction(
    latestVersionName: String?,
    downloadUrl: String?,
    isUpdateAvailable: Boolean,
    downloadState: AppUpdateDownloadState
): AppUpdateActionState {
    if (latestVersionName.isNullOrBlank()) {
        return AppUpdateActionState.None
    }

    val latestDownloaded = isLatestAppUpdateDownloaded(latestVersionName, downloadState)
    if (latestDownloaded) {
        return if (downloadState.installPermissionRequired) {
            AppUpdateActionState.InstallPermissionRequired
        } else {
            AppUpdateActionState.InstallLatest
        }
    }

    if (downloadState.status == AppUpdateDownloadStatus.Downloading &&
        downloadState.versionName == latestVersionName
    ) {
        return AppUpdateActionState.Downloading
    }

    return if (isUpdateAvailable && !downloadUrl.isNullOrBlank()) {
        AppUpdateActionState.DownloadLatest
    } else {
        AppUpdateActionState.None
    }
}

private data class ParsedAppVersionDescriptor(
    val baseVersionName: String,
    val channel: AppUpdateChannel
)

private val LEADING_VERSION_REGEX = Regex("""^(\d+(?:\.\d+)*)""")

private fun parseAppVersionDescriptor(versionName: String): ParsedAppVersionDescriptor {
    val normalized = versionName.removePrefix("v").trim()
    val channel = if (normalized.contains("-beta", ignoreCase = true)) {
        AppUpdateChannel.Beta
    } else {
        AppUpdateChannel.Stable
    }
    // Strips any build-type suffix (e.g. "-debug", "-beta") down to the leading
    // dotted numeric version, so a locally-built "1.0.16-debug" compares equal
    // to a release tagged "1.0.16" instead of failing to parse and looking older.
    val baseVersionName = LEADING_VERSION_REGEX.find(normalized)?.value ?: normalized
    return ParsedAppVersionDescriptor(baseVersionName = baseVersionName, channel = channel)
}

private fun String?.toEpochMillisOrNull(): Long? {
    val value = this?.trim().orEmpty()
    if (value.isEmpty()) return null
    return runCatching { Instant.parse(value).toEpochMilli() }.getOrNull()
}
