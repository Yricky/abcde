package me.yricky.oh.abcd.isa.bean

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class InsPrefix(
    @SerialName("name")
    val name: String,
    @SerialName("description")
    val description: String,
    @SerialName("opcode_idx")
    val opcodeIdx:Int
)