package me.yricky.oh.abcd.isa

import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.YamlConfiguration
import me.yricky.oh.abcd.isa.bean.Isa

actual fun loadInnerAsmMap(): AsmMap {
    val yaml = Yaml(configuration = YamlConfiguration(strictMode = false))

    return AsmMap(yaml.decodeFromString(Isa.serializer(),Asm::class.java.classLoader.getResourceAsStream("abcde/isa.yaml").reader().readText()))
}