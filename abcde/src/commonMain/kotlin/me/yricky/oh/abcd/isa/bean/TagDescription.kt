package me.yricky.oh.abcd.isa.bean

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TagDescription(
    @SerialName("tag")
    val tag:String,
    @SerialName("description")
    val description: String
)