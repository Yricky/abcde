package me.yricky.oh.resde

import kotlin.jvm.JvmInline

@JvmInline
value class ResType(val value:Int) {
    override fun toString(): String {
        return when(value){
            0 -> "ELEMENT"
            6 -> "RAW"
            8 -> "INTEGER"
            9 -> "STRING"
            10 -> "STRARRAY"
            11 -> "INTARRAY"
            12 -> "BOOLEAN"
            14 -> "COLOR"
            15 -> "ID"
            16 -> "THEME"
            17 -> "PLURAL"
            18 -> "FLOAT"
            19 -> "MEDIA"
            20 -> "PROF"
            22 -> "PATTERN"
            23 -> "SYMBOL"
            24 -> "RES"
            else -> "UNKNOWN$value"
        }
    }
}