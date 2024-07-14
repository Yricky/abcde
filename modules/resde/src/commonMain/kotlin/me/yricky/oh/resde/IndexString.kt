package me.yricky.oh.resde

import kotlin.jvm.JvmInline

@JvmInline
value class IndexString(
    val indexStr:String
){
    val type:ResType get() = when{
        indexStr.startsWith("\$media:") -> ResType.MEDIA
        indexStr.startsWith("\$string:") -> ResType.STRING
        indexStr.startsWith("\$color:") -> ResType.COLOR
        else -> ResType(-1)
    }
    val fileName get() = indexStr.substring(indexStr.indexOf(':') + 1)
}