package me.yricky.abcde.cli

import me.yricky.abcde.desktop.DesktopUtils

/**
 * @param argList "--cli"之后的命令行入参
 */
class CliEntry(
    val argList:List<String>
) {
    companion object{
        const val CLI_ENTRY_ARG = "--cli"
        val cliFuncMap = mapOf(
            dumpClass,
            dumpIndex
        )
    }

    fun run(){
        cliFuncMap[argList.firstOrNull()]?.action?.invoke(argList.subList(1,argList.size)) ?: printlnUsages()
    }

    private fun printlnUsages(){
        println("ABCDecoder cli mode.")
        println("Version:${DesktopUtils.properties["version"]}")
        println("Functions:")
        cliFuncMap.forEach{
            println("  ${it.value.name}")
            println("    Usage: $CLI_ENTRY_ARG ${it.key} ${it.value.usage}")
        }
    }
}