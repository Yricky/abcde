package me.yricky.oh.resde

import kotlin.jvm.JvmInline

@JvmInline
value class ResType(val value:Int) {
    override fun toString(): String {
        return when(value){
            ELEMENT.value -> "ELEMENT"
            RAW.value -> "RAW"
            INTEGER.value -> "INTEGER"
            STRING.value -> "STRING"
            STRARRAY.value -> "STRARRAY"
            INTARRAY.value -> "INTARRAY"
            BOOLEAN.value -> "BOOLEAN"
            COLOR.value -> "COLOR"
            ID.value -> "ID"
            THEME.value -> "THEME"
            PLURAL.value -> "PLURAL"
            FLOAT.value -> "FLOAT"
            MEDIA.value -> "MEDIA"
            PROF.value -> "PROF"
            PATTERN.value -> "PATTERN"
            SYMBOL.value -> "SYMBOL"
            RES.value -> "RES"
            else -> "UNKNOWN$value"
        }
    }

    companion object{
        val ELEMENT = ResType(0)
        val RAW = ResType(6)
        val INTEGER = ResType(8)
        val STRING = ResType(9)
        val STRARRAY = ResType(10)
        val INTARRAY = ResType(11)
        val BOOLEAN = ResType(12)
        val COLOR = ResType(14)
        val ID = ResType(15)
        val THEME = ResType(16)
        val PLURAL = ResType(17)
        val FLOAT = ResType(18)
        val MEDIA = ResType(19)
        val PROF = ResType(20)
        val PATTERN = ResType(21)
        val SYMBOL = ResType(23)
        val RES = ResType(24)
    }
}