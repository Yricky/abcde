package me.yricky.oh.abcd.isa

import me.yricky.oh.abcd.isa.bean.Isa

actual fun loadInnerAsmMap(): AsmMap {
    return AsmMap(Isa())
}