package com.tvrgs.app.ui.theme

import com.tvrgs.app.ui.design.AppSpacing
import com.tvrgs.app.ui.design.LocalAppSpacing

typealias Spacing = AppSpacing

val LocalSpacing = LocalAppSpacing

fun defaultSpacing(): Spacing = AppSpacing()
