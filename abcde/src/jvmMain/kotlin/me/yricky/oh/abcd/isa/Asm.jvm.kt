package me.yricky.oh.abcd.isa


import kotlinx.serialization.json.Json
import me.yricky.oh.abcd.isa.bean.Isa

actual fun loadInnerAsmMap(): AsmMap {
    val json = Json { ignoreUnknownKeys = true }
    return AsmMap(
        json.decodeFromString(
            Isa.serializer(),
            Asm::class.java.classLoader.getResourceAsStream("abcde/isa.json")!!.reader().readText())
    )
}