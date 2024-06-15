package me.yricky.oh.abcd.isa.bean

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class InsGroup(
    @JsonProperty("title")
    val title:String = "",
    @JsonProperty("description")
    val description:String = "",
    @JsonProperty("properties")
    val properties:List<String>?,
    @JsonProperty("exceptions")
    val exceptions:List<String> = emptyList(),
    @JsonProperty("verification")
    val verification:List<String> = emptyList(),
    @JsonProperty("namespace")
    val namespace:String?,
    @JsonProperty("pseudo")
    val pseudo:String = "",
    @JsonProperty("semantics")
    val semantics:String?,
    @JsonProperty("instructions")
    val instructions:List<Instruction> = emptyList()
)
