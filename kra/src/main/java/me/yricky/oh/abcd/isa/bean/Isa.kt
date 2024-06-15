package me.yricky.oh.abcd.isa.bean

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class Isa(
    @JsonProperty("chapters")
    val chapters:List<Chapter> = emptyList(),
    @JsonProperty("min_version")
    val minVersion:String = "",
    @JsonProperty("version")
    val version:String = "",
    @JsonProperty(value = "api_version_map")
    val apiVersionMap:List<List<Any>> = emptyList(),
    @JsonProperty("incompatible_version")
    val incompatibleVersion:List<String> = emptyList(),
    @JsonProperty("properties")
    val properties:List<TagDescription> = emptyList(),
    @JsonProperty("exceptions")
    val exceptions:List<TagDescription> = emptyList(),
    @JsonProperty("verification")
    val verification:List<TagDescription> = emptyList(),
    @JsonProperty("prefixes")
    val prefixes:List<InsPrefix> = emptyList(),
    @JsonProperty("groups")
    val groups:List<InsGroup> = emptyList()
    )