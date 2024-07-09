package me.yricky.abcde.page

import me.yricky.abcde.ui.defineStr
import me.yricky.oh.abcd.AbcBuf
import me.yricky.oh.abcd.cfm.AbcClass
import me.yricky.oh.abcd.code.Code
import me.yricky.oh.resde.ResIndexBuf
import java.util.zip.ZipFile

sealed class PageTag{
    companion object{
        private fun asNavString(prefix:String,str:String) = "$prefix${String.format("%04d",str.length)}${str}"
    }

    abstract val navString:String
    abstract val name:String

    override fun equals(other: Any?): Boolean {
        if(javaClass != other?.javaClass){
            return false
        }
        return navString == (other as PageTag).navString
    }

    override fun hashCode(): Int {
        return navString.hashCode()
    }

    class HapTag(val hap:ZipFile):PageTag(){
        override val navString: String = asNavString("HAP",hap.name)
        override val name: String = hap.name
    }

    class AbcTag(
        val hapTag: HapTag? = null,
        val abc:AbcBuf
    ):PageTag(){
        override val navString: String = "${hapTag?.navString ?: ""}${asNavString("ABC",abc.tag)}"
        override val name: String = if(hapTag == null){
            abc.tag
        } else "${hapTag.name}/${abc.tag}"
    }
    class ClassTag(
        val abcTag:AbcTag,
        val clazz:AbcClass
    ):PageTag(){
        override val navString: String = "${abcTag.navString}${asNavString("CLZ",clazz.name)}"
        override val name: String = "${abcTag.name}/${clazz.name}"
    }

    class CodeTag(
        val clz:ClassTag?,
        val code:Code
    ):PageTag(){
        override val navString: String = "${clz?.navString ?: ""}${asNavString("ASM",code.method.defineStr(clz == null))}"
        override val name: String = if(clz == null){
            code.method.defineStr(true)
        } else "${clz.name}/${code.method.name}"
    }

    class ResIndexTag(
        val hapTag: HapTag? = null,
        val res:ResIndexBuf,
        val tag:String
    ):PageTag(){
        override val navString: String = "${hapTag?.navString ?: ""}${asNavString("REI",tag)}"
        override val name: String = if(hapTag == null){
            tag
        } else "${hapTag.name}/$tag"
    }
}