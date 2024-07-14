package me.yricky.oh.hapde

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ModuleConfig(
    @SerialName("name")
    val name:String,
    @SerialName("type")
    val type:String,
    @SerialName("srcEntry")
    val srcEntry:String? = null,
    @SerialName("description")
    val description:String? = null,
    @SerialName("process")
    val process:String? = null,
    @SerialName("mainElement")
    val mainElement:String? = null,
    @SerialName("deviceTypes")
    val deviceTypes:List<String>,
    @SerialName("deliveryWithInstall")
    val deliveryWithInstall:Boolean,
    @SerialName("installationFree")
    val installationFree:Boolean,
    @SerialName("virtualMachine")
    val virtualMachine:String = ""
)