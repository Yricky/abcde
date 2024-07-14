package me.yricky.oh.findstr

import com.google.gson.GsonBuilder
import me.yricky.oh.abcd.cfm.AbcClass
import me.yricky.oh.abcd.isa.calledStrings
import me.yricky.oh.utils.asAbcBuf
import java.io.File
import java.util.regex.Pattern

fun <T,R> MutableMap<T,R>.getOrSet(key:T,defaultV:()->R):R{
    val r = get(key)
    if(r == null){
        val v = defaultV()
        put(key,v)
        return v
    }
    return r
}

fun printHelp(){
    println("Usage:")
    println("java -jar findstr.jar [path-to-abc-file]")
}

fun main(args:Array<String>){
    val filePath = args.lastOrNull { !it.startsWith("-") }
    if(filePath == null){
        println("Invalid arg.")
        printHelp()
        return
    }
    val file = File(filePath)
    if(!file.isFile() || file.extension.uppercase() != "ABC"){
        println("Input must be a abc file.")
        printHelp()
        return
    }
    val abc = file.asAbcBuf()
    val pattStr = "[\\u4E00-\\u9FA5！，。（）《》“”？：；【】]"
    val p: Pattern = Pattern.compile(pattStr)
    val map = mutableMapOf<String,MutableMap<String,MutableList<String>>>()
    abc.classes.forEach { (_, clz) ->
        if(clz is AbcClass){
            clz.methods.forEach { method ->
                method.codeItem?.asm?.list?.forEach {
                    val strs = it.calledStrings
                    strs.filter { p.matcher(it).find() }.forEach {
                        map.getOrSet(clz.name){ mutableMapOf() }.getOrSet(method.name){ mutableListOf() }.add(it)
                    }
                }
            }
        }
    }
    val jsonStr = GsonBuilder().setPrettyPrinting().create().toJson(mapOf(
        "regex" to pattStr,
        "match_result" to map
    ))
    println(jsonStr)
}