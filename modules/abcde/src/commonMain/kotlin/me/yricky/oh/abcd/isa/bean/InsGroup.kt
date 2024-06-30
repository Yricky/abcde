package me.yricky.oh.abcd.isa.bean

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class InsGroup(
    @SerialName("title")
    val title:String = "",
    @SerialName("description")
    val description:String = "",
    @SerialName("properties")
    val properties:List<String>? = null,
    @SerialName("exceptions")
    val exceptions:List<String> = emptyList(),
    @SerialName("verification")
    val verification:List<String> = emptyList(),
    @SerialName("namespace")
    val namespace:String? = null,
    @SerialName("pseudo")
    val pseudo:String = "",
    @SerialName("semantics")
    val semantics:String? = null,
    @SerialName("instructions")
    val instructions:List<Instruction> = emptyList()
)
