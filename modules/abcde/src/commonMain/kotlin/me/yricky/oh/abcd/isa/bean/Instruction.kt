package me.yricky.oh.abcd.isa.bean

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Instruction(
    @SerialName("sig")
    val sig:String,
    @SerialName("acc")
    val acc:String,
    @SerialName("opcode_idx")
    val opcodeIdx:List<Int>,
    @SerialName("format")
    val format: List<String>,
    @SerialName("properties")
    val properties:List<String>? = null,
    @SerialName("prefix")
    val prefix:String? = null
)
