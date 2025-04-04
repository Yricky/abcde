package me.yricky.oh.abcd.decompiler.behaviour

import me.yricky.oh.abcd.isa.Asm

class FunSimCtx(
    val asm: Asm.AsmItem,
    val lexEnv: LexEnv? = null
) {
    /**
     * 标记一个可以存储JSValue的位置，如acc、寄存器和词法环境槽位
     */
    @JvmInline
    value class RegId(val value: Long){
        companion object{
            val ACC = RegId(0)
            val GLOBAL = RegId(0x4000_0000_0000_0001L)
            val THIS   = RegId(0x4000_0000_0000_0002L)
            val ARGUMENTS = RegId(0x4000_0000_0000_0003L)
            const val MASK =     0x7f00_0000_0000_0000L
            const val MASK_REG = 0x1000_0000_0000_0000L
            const val MASK_LEX = 0x2000_0000_0000_0000L
            const val MASK_OTH = 0x4000_0000_0000_0000L
            fun regId(reg: Int) = RegId(MASK_REG or reg.toLong())
            fun lexId(lvl:Int,slot:Int) = RegId(MASK_LEX or (lvl.and(0xffff).shl(16) or slot.and(0xffff)).toLong().and(0xffffffff))
        }

        fun isReg() = value and MASK == MASK_REG
        fun getRegV() = value xor MASK_REG

        fun toJS(): String {
            if(value == ACC.value){
                return "_acc_"
            } else if(value == GLOBAL.value){
                return "global"
            } else if(value == THIS.value){
                return "this"
            } else if(value == ARGUMENTS.value) {
                return "arguments"
            } else if(value and MASK == MASK_REG){
                return "__v${value xor MASK_REG}__"
            } else if(value and MASK == MASK_LEX){
                return "__lex${(value and 0xffffffff).toString(16)}__"
            } else {
                return "unknown${value.toString(16)}"
            }
//            return super.toString()
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