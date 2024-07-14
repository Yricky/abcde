package me.yricky.oh.hapde

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class HapConfig(
    @SerialName("app")
    val app:AppConfig,
    @SerialName("module")
    val module:ModuleConfig
)