package me.yricky.oh.hapde

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class NameCache(
    @SerialName("obfName")
    val obfName:String,
    @SerialName("IdentifierCache")
    val identifierCache:Map<String,String>,
    @SerialName("MemberMethodCache")
    val memberMethodCache:Map<String,String>
)