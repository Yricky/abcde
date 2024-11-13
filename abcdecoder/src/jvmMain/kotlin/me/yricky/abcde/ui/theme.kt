package me.yricky.abcde.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.platform.Font
import androidx.compose.ui.unit.dp
import me.yricky.oh.abcd.cfm.*
import me.yricky.oh.abcd.literal.LiteralArray

@Composable
fun AbcField.icon():Painter{
    return Icons.field()
}

@Composable
fun AbcMethod.icon():Painter{
    return Icons.method()
}

val CODE_FONT = FontFamily(Font("fonts/jbMono/JetBrainsMono-Regular.ttf"))
val commentColor = Color(0xff72737a)
val codeStyle @Composable get() = TextStyle(
    fontFamily = CODE_FONT,
    color = if(isDarkTheme()) Color(0xffa9b7c6) else Color(0xff080808),
    fontSize = MaterialTheme.typography.bodyMedium.fontSize
)

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
    fun chevronDown()= if (isDarkTheme()) {
        painterResource("ic/chevronDown/chevronDown_dark.svg")
    } else {
        painterResource("ic/chevronDown/chevronDown.svg")
    }

    @Composable
    fun checkMark()= if (isDarkTheme()) {
        painterResource("ic/checkmark/checkmark_dark.svg")
    } else {
        painterResource("ic/checkmark/checkmark.svg")
    }

    @Composable
    fun stub()= if (isDarkTheme()) {
        painterResource("ic/stub/stub_dark.svg")
    } else {
        painterResource("ic/stub/stub.svg")
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
    fun xml() = if (isDarkTheme()) {
        painterResource("ic/xml/xml_dark.svg")
    } else {
        painterResource("ic/xml/xml.svg")
    }

    @Composable
    fun folder() = if (isDarkTheme()) {
        painterResource("ic/folder/folder_dark.svg")
    } else {
        painterResource("ic/folder/folder.svg")
    }

    @Composable
    fun moduleGroup() = if (isDarkTheme()) {
        painterResource("ic/moduleGroup/moduleGroup_dark.svg")
    } else {
        painterResource("ic/moduleGroup/moduleGroup.svg")
    }

    @Composable
    fun library() = if (isDarkTheme()) {
        painterResource("ic/library/library_dark.svg")
    } else {
        painterResource("ic/library/library.svg")
    }

    @Composable
    fun libraryFolder() = if (isDarkTheme()) {
        painterResource("ic/libraryFolder/libraryFolder_dark.svg")
    } else {
        painterResource("ic/libraryFolder/libraryFolder.svg")
    }

    @Composable
    fun resourcesRoot() = if (isDarkTheme()) {
        painterResource("ic/resourcesRoot/resourcesRoot_dark.svg")
    } else {
        painterResource("ic/resourcesRoot/resourcesRoot.svg")
    }

    @Composable
    fun asm() = if (isDarkTheme()) {
        painterResource("ic/asm/asm_dark.svg")
    } else {
        painterResource("ic/asm/asm.svg")
    }

    @Composable
    fun editorConfig() = if (isDarkTheme()) {
        painterResource("ic/editorConfig/editorConfig_dark.svg")
    } else {
        painterResource("ic/editorConfig/editorConfig.svg")
    }

    @Composable
    fun archive() = if (isDarkTheme()) {
        painterResource("ic/archive/archive_dark.svg")
    } else {
        painterResource("ic/archive/archive.svg")
    }

    @Composable
    fun anyType() = if (isDarkTheme()) {
        painterResource("ic/anyType/anyType_dark.svg")
    } else {
        painterResource("ic/anyType/anyType.svg")
    }

    @Composable
    fun indexCluster() = if (isDarkTheme()) {
        painterResource("ic/indexCluster/indexCluster_dark.svg")
    } else {
        painterResource("ic/indexCluster/indexCluster.svg")
    }

    @Composable
    fun listFiles() = if (isDarkTheme()) {
        painterResource("ic/listFiles/listFiles_dark.svg")
    } else {
        painterResource("ic/listFiles/listFiles.svg")
    }

    @Composable
    fun lightTheme() = if (isDarkTheme()) {
        painterResource("ic/lightTheme/lightTheme_dark.svg")
    } else {
        painterResource("ic/lightTheme/lightTheme.svg")
    }

    @Composable
    fun darkTheme() = if (isDarkTheme()) {
        painterResource("ic/darkTheme/darkTheme_dark.svg")
    } else {
        painterResource("ic/darkTheme/darkTheme.svg")
    }

    @Composable
    fun json() = if (isDarkTheme()) {
        painterResource("ic/json/json_dark.svg")
    } else {
        painterResource("ic/json/json.svg")
    }

    @Composable
    fun image() = if (isDarkTheme()) {
        painterResource("ic/image/image_dark.svg")
    } else {
        painterResource("ic/image/image.svg")
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

    @Composable
    fun trait() = if (isDarkTheme()) {
        painterResource("ic/interface/interface_dark.svg")
    } else {
        painterResource("ic/interface/interface.svg")
    }

    @Composable
    fun annotation() = if (isDarkTheme()) {
        painterResource("ic/annotation/annotation_dark.svg")
    } else {
        painterResource("ic/annotation/annotation.svg")
    }

    @Composable
    fun classAbstract() = if (isDarkTheme()) {
        painterResource("ic/classAbstract/classAbstract_dark.svg")
    } else {
        painterResource("ic/classAbstract/classAbstract.svg")
    }

    @Composable
    fun showAsTree() = if (isDarkTheme()) {
        painterResource("ic/showAsTree/showAsTree_dark.svg")
    } else {
        painterResource("ic/showAsTree/showAsTree.svg")
    }

    @Composable
    fun key() = if (isDarkTheme()) {
        painterResource("ic/greyKey/greyKey_dark.svg")
    } else {
        painterResource("ic/greyKey/greyKey.svg")
    }
}

@Composable
fun isDarkTheme():Boolean{
    return LocalAppConfig.current.darkTheme ?: isSystemInDarkTheme()
}

@Composable
fun experimentalFeatures():Boolean{
    return LocalAppConfig.current.futureFeature
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
    if(isScopeNames()){
        getIntValue()?.let {
            LiteralArray(abc,it)
        }?.let {
            sb.append("= ${it}")
        }
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
    sb.append(argsStr())
    sb.toString()
}

fun MethodItem.argsStr():String{
    val sb = StringBuilder()
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
    return sb.toString()
}

@Composable
fun ClassItem.icon():Painter{
    return if (this is AbcClass){
        when {
            accessFlags.isEnum -> Icons.enum()
            accessFlags.isInterface -> Icons.trait()
            accessFlags.isAnnotation -> Icons.annotation()
            accessFlags.isAbstract -> Icons.classAbstract()
            else -> Icons.clazz()
        }
    } else {
        Icons.classAbstract()
    }
}

@Composable
fun TitleCard(
    modifier: Modifier = Modifier,
    title:String,
    content:@Composable ColumnScope.()->Unit
){
    Card(modifier) { Column(modifier = Modifier.padding(12.dp).fillMaxWidth()) {
        Text(title, style = MaterialTheme.typography.titleMedium)
        content()
    }}
}