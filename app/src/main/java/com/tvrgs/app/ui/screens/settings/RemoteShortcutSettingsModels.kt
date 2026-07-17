package com.tvrgs.app.ui.screens.settings

import com.tvrgs.domain.model.RemoteColorButton
import com.tvrgs.domain.model.RemoteShortcutProfile

internal data class RemoteShortcutDialogTarget(
    val profile: RemoteShortcutProfile,
    val button: RemoteColorButton
) {
    fun storageKey(): String = "${profile.storageValue}:${button.storageValue}"

    companion object {
        fun fromStorageKey(value: String?): RemoteShortcutDialogTarget? {
            val parts = value?.split(':') ?: return null
            if (parts.size != 2) return null
            val profile = RemoteShortcutProfile.fromStorage(parts[0]) ?: return null
            val button = RemoteColorButton.fromStorage(parts[1]) ?: return null
            return RemoteShortcutDialogTarget(profile, button)
        }
    }
}
