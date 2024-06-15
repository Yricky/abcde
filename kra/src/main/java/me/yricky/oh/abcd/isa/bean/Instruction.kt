package me.yricky.oh.abcd.isa.bean

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class Instruction(
    @JsonProperty("sig")
    val sig:String,
    @JsonProperty("acc")
    val acc:String,
    @JsonProperty("opcode_idx")
    val opcodeIdx:List<Byte> = emptyList(),
    @JsonProperty("format")
    val format: List<String> = emptyList(),
    @JsonProperty("properties")
    val properties:List<String>?,
    @JsonProperty("prefix")
    val prefix:String?
)

fun Instruction.asmName() = sig.split(' ')[0]
