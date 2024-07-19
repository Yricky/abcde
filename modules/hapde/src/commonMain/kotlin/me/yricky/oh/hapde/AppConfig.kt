package me.yricky.oh.hapde

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import me.yricky.oh.resde.IndexString

@Serializable
class AppConfig(
    @SerialName("bundleName")
    val bundleName:String,
    @SerialName("bundleType")
    val bundleType:String = "app",
    @SerialName("debug")
    val debug:Boolean = false,
    @SerialName("icon")
    private val _icon:String,
    @SerialName("label")
    private val _label:String,
    @SerialName("versionCode")
    val versionCode:Int,
    @SerialName("versionName")
    val versionName:String,
){
    val icon :IndexString get() = IndexString(_icon)
    val label:IndexString get() = IndexString(_label)
}