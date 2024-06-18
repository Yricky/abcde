package me.yricky.oh.abcd.isa.bean

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class Chapter(
    @JsonProperty("name")
    val name:String = "",
    @JsonProperty("text")
    val text:String = ""
)