package me.yricky.abcde.ui

import androidx.compose.foundation.LocalScrollbarStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import me.yricky.oh.abcd.cfm.*

@Composable
fun AbcField.icon():Painter{
    return Icons.field()
}

@Composable
fun AbcMethod.icon():Painter{
    return Icons.method()
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
    fun field() = if (isDarkTheme()) {
        painterResource("ic/field/field_dark.svg")
    } else {
        painterResource("ic/field/field.svg")
    }

    @Composable
    fun method() = if (isDarkTheme()) {
        painterResource("ic/method/method_dark.svg")
    } else {
        painterResource("ic/method/method.svg")
    }


    @Composable
    fun pkg() = if (isDarkTheme()) {
        painterResource("ic/package/package_dark.svg")
    } else {
        painterResource("ic/package/package.svg")
    }

    @Composable
    fun asm() = if (isDarkTheme()) {
        painterResource("ic/asm/asm_dark.svg")
    } else {
        painterResource("ic/asm/asm.svg")
    }

    @Composable
    fun listFiles() = if (isDarkTheme()) {
        painterResource("ic/listFiles/listFiles_dark.svg")
    } else {
        painterResource("ic/listFiles/listFiles.svg")
    }

    @Composable
    fun info() = if (isDarkTheme()) {
        painterResource("ic/info/info_dark.svg")
    } else {
        painterResource("ic/info/info.svg")
    }

    @Composable
    fun close() = if (isDarkTheme()) {
        painterResource("ic/closeSmallHovered/closeSmallHovered_dark.svg")
    } else {
        painterResource("ic/closeSmallHovered/closeSmallHovered.svg")
    }
}

fun isDarkTheme() = true

@Composable
fun AbcdeTheme(content:@Composable ()->Unit) {
    MaterialTheme(
        colorScheme = if (isDarkTheme()) darkColorScheme() else lightColorScheme(),
    ) {
        CompositionLocalProvider(
            LocalScrollbarStyle provides LocalScrollbarStyle.current.copy(
                unhoverColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f),
                hoverColor = MaterialTheme.colorScheme.tertiary
            )
        ) {
            Surface {
                content()
            }
        }
    }
}

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

fun MethodItem.defineStr(showClass:Boolean = false):String = run {
    val sb = StringBuilder()
//    if(indexData.isPublic){
//        sb.append("public ")
//    }
//    if(indexData.isPrivate){
//        sb.append("private ")
//    }
//    if(indexData.isProtected){
//        sb.append("protected ")
//    }
//    if(indexData.isStatic){
//        sb.append("static ")
//    }
//    if(indexData.isAbstract){
//        sb.append("abstract ")
//    }
//    if(indexData.isFinal){
//        sb.append("final ")
//    }
//    if(accessFlags.isNative){
//        sb.append("native ")
//    }
//    if(indexData.isSynchronized){
//        sb.append("synchronized ")
//    }
//    sb.append("${proto?.shortyReturn ?: ""} ")
    if(showClass){
        sb.append("${clazz.name}.")
    }
    sb.append(name)
    if(this is AbcMethod && codeItem != null){
        val code = codeItem!!
        val argCount = code.numArgs - 3
        if(argCount >= 0){
            sb.append("(FunctionObject, NewTarget, this")
            repeat(argCount){
                sb.append(", arg$it")
            }
            sb.append(')')
        }
    }
    sb.toString()
}

@Composable
fun ClassItem.icon():Painter{
    return if (this is AbcClass){
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