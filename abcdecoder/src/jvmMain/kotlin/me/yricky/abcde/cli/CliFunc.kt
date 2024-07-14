package me.yricky.abcde.cli

import me.yricky.abcde.util.SelectedAbcFile
import java.io.File
import java.io.OutputStream
import java.io.PrintStream

class CliFunc(
    val name:String,
    val helpInfo:List<String>,
    val action:(List<String>) -> Unit
)

val dumpClass = Pair(
    "--dump-class",
    CliFunc(
        "Dump class",
        listOf("/path/to/module.abc --out=/path/to/outFile")
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