package me.yricky.abcde.cli

/**
 * @param argList "--cli"之后的命令行入参
 */
class CliEntry(
    val argList:List<String>
) {
    companion object{
        val cliFuncMap = mapOf(
            dumpClass,

        )
    }

    fun run(){
        cliFuncMap[argList.first()]?.action?.invoke(argList.subList(1,argList.size))
    }
}