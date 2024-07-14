package me.yricky.oh.resde

import kotlin.jvm.JvmInline

@JvmInline
value class ResType(val value:Int) {
    override fun toString(): String {
        return when(value){
            ELEMENT.value -> "ELEMENT"
            RAW.value -> "RAW"
            8 -> "INTEGER"
            STRING.value -> "STRING"
            10 -> "STRARRAY"
            11 -> "INTARRAY"
            12 -> "BOOLEAN"
            COLOR.value -> "COLOR"
            15 -> "ID"
            16 -> "THEME"
            17 -> "PLURAL"
            18 -> "FLOAT"
            MEDIA.value -> "MEDIA"
            20 -> "PROF"
            22 -> "PATTERN"
            23 -> "SYMBOL"
            24 -> "RES"
            else -> "UNKNOWN$value"
        }
    }

    companion object{
        val ELEMENT = ResType(0)
        val RAW = ResType(6)
        val STRING = ResType(9)
        val COLOR = ResType(14)
        val MEDIA = ResType(19)
    }
}