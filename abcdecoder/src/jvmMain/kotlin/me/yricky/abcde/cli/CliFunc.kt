package me.yricky.abcde.cli

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import me.yricky.abcde.util.SelectedAbcFile
import me.yricky.abcde.util.SelectedIndexFile
import java.io.File
import java.io.OutputStream
import java.io.PrintStream

class CliFunc(
    val name:String,
    val usage:String,
    val action:(List<String>) -> Unit
)

val dumpClass = Pair(
    "--dump-class",
    CliFunc(
        "Dump class",
        "/path/to/module.abc --out=/path/to/outFile.txt"
    ){ args ->
        val iterator = args.iterator()
        var outFile: OutputStream = System.out
        var inputFile:SelectedAbcFile? = null
        while (iterator.hasNext()){
            val arg = iterator.next()
            if(arg.startsWith("--out=")){
                outFile = File(arg.removePrefix("--out=")).outputStream()
            } else {
                inputFile = SelectedAbcFile(File(arg))
            }
        }
        println("${inputFile?.file?.path}")
        if(inputFile?.valid() == true){
            val ps = PrintStream(outFile)
            inputFile.abcBuf.classes.forEach{ (k,v) ->
                ps.println(v.name)
            }
        }
    }
)

val dumpIndex = Pair(
    "--dump-index",
    CliFunc(
        "Dump index",
        "/path/to/resource.index --out=/path/to/outFile.json"
    ){ args ->
        val iterator = args.iterator()
        var outFile: OutputStream = System.out
        var inputFile:SelectedIndexFile? = null
        while (iterator.hasNext()){
            val arg = iterator.next()
            if(arg.startsWith("--out=")){
                outFile = File(arg.removePrefix("--out=")).outputStream()
            } else {
                inputFile = SelectedIndexFile(File(arg))
            }
        }
        val json = Json {
            prettyPrint = true
        }
        println("${inputFile?.file?.path}")
        val map = mutableMapOf<Int,List<Map<String,String>>>()
        if(inputFile?.valid() == true){
            val ps = PrintStream(outFile)
            inputFile.resBuf.resMap.forEach { (t, u) ->
                map[t] = u.map {
                    mapOf(
                        "type" to it.resType.toString(),
                        "param" to it.limitKey,
                        "name" to it.fileName,
                        "data" to it.data
                    )
                }
            }
            ps.print(json.encodeToString(map))
        }
    }
)