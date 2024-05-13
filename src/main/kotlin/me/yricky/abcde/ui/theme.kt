package me.yricky.abcde.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import me.yricky.oh.abcd.cfm.*

@Composable
fun AbcField.icon():Painter{
    return if (isDarkTheme()) {
        painterResource("ic/field/field_dark.svg")
    } else {
        painterResource("ic/field/field.svg")
    }
}

@Composable
fun AbcMethod.icon():Painter{
    return if (isDarkTheme()) {
        painterResource("ic/method/method_dark.svg")
    } else {
        painterResource("ic/method/method.svg")
    }
}

object Icons{
    @Composable
    fun enum()= if (isDarkTheme()) {
        painterResource("ic/enum/enum_dark.svg")
    } else {
        painterResource("ic/enum/enum.svg")
    }

    @Composable
    fun watch()= if (isDarkTheme()) {
        painterResource("ic/watch/watch_dark.svg")
    } else {
        painterResource("ic/watch/watch.svg")
    }

    @Composable
    fun search()= if (isDarkTheme()) {
        painterResource("ic/search/search_dark.svg")
    } else {
        painterResource("ic/search/search.svg")
    }

    @Composable
    fun chevronRight()= if (isDarkTheme()) {
        painterResource("ic/chevronRight/chevronRight_dark.svg")
    } else {
        painterResource("ic/chevronRight/chevronRight.svg")
    }

    @Composable
    fun homeFolder()= if (isDarkTheme()) {
        painterResource("ic/homeFolder/homeFolder_dark.svg")
    } else {
        painterResource("ic/homeFolder/homeFolder.svg")
    }

    @Composable
    fun clazz() = if (isDarkTheme()) {
        painterResource("ic/class/class_dark.svg")
    } else {
        painterResource("ic/class/class.svg")
    }

    @Composable
    fun pkg() = if (isDarkTheme()) {
        painterResource("ic/package/package_dark.svg")
    } else {
        painterResource("ic/package/package.svg")
    }

    @Composable
    fun listFiles() = if (isDarkTheme()) {
        painterResource("ic/listFiles/listFiles_dark.svg")
    } else {
        painterResource("ic/listFiles/listFiles.svg")
    }
}

fun isDarkTheme() = true

val grayColorFilter = ColorFilter.colorMatrix(ColorMatrix().apply {
    setToSaturation(0f)
})

fun AbcField.defineStr():String = run {
    val sb = StringBuilder()
    if(accessFlags.isPublic){
        sb.append("public ")
    }
    if(accessFlags.isPrivate){
        sb.append("private ")
    }
    if(accessFlags.isProtected){
        sb.append("protected ")
    }
    if(accessFlags.isStatic){
        sb.append("static ")
    }
    if(accessFlags.isFinal){
        sb.append("final ")
    }
    if(accessFlags.isVolatile){
        sb.append("volatile ")
    }

    sb.append("${type.name} $name")
    if(isModuleRecordIdx()){
        val moduleRecordOffset = getIntValue()
        sb.append("= 0x${moduleRecordOffset?.toString(16)}")
    }
    sb.toString()
}

fun AbcMethod.defineStr():String = run {
    val sb = StringBuilder()
    if(accessFlags.isPublic){
        sb.append("public ")
    }
    if(accessFlags.isPrivate){
        sb.append("private ")
    }
    if(accessFlags.isProtected){
        sb.append("protected ")
    }
    if(accessFlags.isStatic){
        sb.append("static ")
    }
    if(accessFlags.isAbstract){
        sb.append("abstract ")
    }
    if(accessFlags.isFinal){
        sb.append("final ")
    }
//    if(accessFlags.isNative){
//        sb.append("native ")
//    }
    if(accessFlags.isSynchronized){
        sb.append("synchronized ")
    }
    sb.append("${proto.shortyReturn} ${name}(${proto.shortyParams})")
    sb.toString()
}

@Composable
fun AbcClass.icon():Painter{
    return if (this is ClassItem){
        when {
            accessFlags.isEnum -> if (isDarkTheme()) {
                painterResource("ic/enum/enum_dark.svg")
            } else {
                painterResource("ic/enum/enum.svg")
            }

            accessFlags.isInterface -> if (isDarkTheme()) {
                painterResource("ic/interface/interface_dark.svg")
            } else {
                painterResource("ic/interface/interface.svg")
            }

            accessFlags.isAnnotation -> if (isDarkTheme()) {
                painterResource("ic/annotation/annotation_dark.svg")
            } else {
                painterResource("ic/annotation/annotation.svg")
            }

            accessFlags.isAbstract -> if (isDarkTheme()) {
                painterResource("ic/classAbstract/classAbstract_dark.svg")
            } else {
                painterResource("ic/classAbstract/classAbstract.svg")
            }

            else -> Icons.clazz()
        }
    } else {
        Icons.clazz()
    }
}