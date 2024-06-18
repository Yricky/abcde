package me.yricky.oh.abcd.isa.bean

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class TagDescription(
    @JsonProperty("tag")
    val tag:String,
    @JsonProperty("description")
    val description: String
)