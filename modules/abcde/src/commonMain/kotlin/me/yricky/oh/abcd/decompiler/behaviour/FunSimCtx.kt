package me.yricky.oh.abcd.decompiler.behaviour

import me.yricky.oh.abcd.isa.Asm

class FunSimCtx(
    val asm: Asm.AsmItem,
    val lexEnv: LexEnv
) {
    /**
     * 标记一个可以存储JSValue的位置，如acc、寄存器和词法环境槽位
     */
    @JvmInline
    value class RegId(val value: Long){
        companion object{
            val ACC = RegId(0)
            val GLOBAL = RegId(0x7f00_0000_0000_0001L)
            val THIS   = RegId(0x7f00_0000_0000_0002L)
            const val MASK_REG = 0x1000_0000_0000_0000L
            const val MASK_LEX = 0x2000_0000_0000_0000L
            const val MASK_OTH = 0x7f00_0000_0000_0000L
            fun regId(reg: Int) = RegId(MASK_REG or reg.toLong())
            fun lexId(lvl:Int,slot:Int) = RegId(MASK_LEX or (lvl.and(0xffff).shl(16) and slot.and(0xffff)).toLong().and(0xffffffff))
        }
    }

    val registers: Map<RegId, JSValue> = emptyMap()

    class LexEnv(
        val tag: String,
        val parent: LexEnv?,
        val sendable: Boolean,
        val content: List<JSValue>
    )

    interface Effect{
        /**
         * 读取了哪些位置
         */
        fun read(): Sequence<RegId> = emptySequence()

        /**
         * 可能会影响（写入）哪些位置的值
         */
        fun effected(): Sequence<RegId> = emptySequence()
    }
}