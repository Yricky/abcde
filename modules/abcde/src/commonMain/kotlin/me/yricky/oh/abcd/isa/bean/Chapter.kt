package me.yricky.oh.abcd.isa.bean

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Chapter(
    @SerialName("name")
    val name:String = "",
    @SerialName("text")
    val text:String = ""
)