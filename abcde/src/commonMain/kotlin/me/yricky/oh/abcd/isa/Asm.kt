package me.yricky.oh.abcd.isa

import me.yricky.oh.abcd.cfm.AbcMethod
import me.yricky.oh.abcd.code.Code
import me.yricky.oh.abcd.code.TryBlock
import me.yricky.oh.abcd.isa.Inst.Companion.toUnsignedInt
import me.yricky.oh.abcd.isa.util.ExternModuleParser
import me.yricky.oh.abcd.isa.util.InstParser
import me.yricky.oh.abcd.literal.LiteralArray
import me.yricky.oh.utils.DataAndNextOff
import me.yricky.oh.utils.nextOffset
import me.yricky.oh.utils.value

expect fun loadInnerAsmMap():AsmMap

/**
 * 用于解析方法中代码段的类
 */
class Asm(
    val code: Code,
) {
    companion object{
        val asmMap by lazy { loadInnerAsmMap() }
        val hexCharList = "0123456789ABCDEF"
    }
    val list:List<AsmItem> by lazy{
        val li = ArrayList<AsmItem>()
        var off = 0
        while (off < code.codeSize){
            val initOff = off
            val opCode = code.instructions.get(off)
            off += 1
            asmMap.prefixInstMap[opCode]?.let {
                val subOpCode = code.instructions.get(off)
                off += 1
                val inst = it[subOpCode] ?: throw IllegalStateException("No this subOpCode:${subOpCode.toString(16)} in opCode:${opCode.toString(16)}")
                off += inst.argSize()
                li.add(AsmItem(this,inst,initOff))
            } ?: asmMap.insMap[opCode]?.let { ins ->
                off += ins.argSize()
                li.add(AsmItem(this,ins, initOff))
            } ?: throw IllegalStateException("No this opCode:${opCode.toString(16)},off:${off - 1}")
        }
        li
    }


    /**
     * 字节码中单个指令的汇编对象
     * @param asm 指令所在方法
     * @param codeOffset 本条指令在方法指令段中的相对offset，从0开始
     * @param ins 本条指令的指令格式
     */
    class AsmItem(
        val asm:Asm,
        val ins:Inst,
        val codeOffset:Int
    ){
        val tryBlocks:List<TryBlock> get() = asm.code.tryBlocks.filter {
            codeOffset in (it.startPc until (it.startPc+ it.length))
        }

        /**
         * 将指令的原始二进制数据拆分为一个个语义化单元，并以List<Number>表示
         *
         * 其格式为 [prefix] opcode oprand1 oprand2 ...
         */
        val opRand:DataAndNextOff<List<Number>> by lazy {
            val instructions = asm.code.instructions
            val oprand = mutableListOf<Number>()
            val iter = ins.format.iterator()
            var off = codeOffset
            while (iter.hasNext()){
                val thisFmt = iter.next()
                if(thisFmt is InstFmt.Prefix || thisFmt is InstFmt.OpCode){
                    oprand.add(instructions.get(off))
                    off += 1
                } else if(thisFmt.bitSize == 4){
                    val nextFmt = iter.next()
                    if(nextFmt.bitSize != 4){
                        throw IllegalStateException()
                    }
                    val value = instructions.get(off).toUByte().toInt()
                    oprand.add((value and  0xf).toByte())
                    oprand.add(value.shr(4).toByte())
                    off += 1
                } else if(thisFmt.bitSize == 8){
                    val value = instructions.get(off)
                    oprand.add(value)
                    off += 1
                } else if(thisFmt.bitSize == 16){
                    val value = instructions.getShort(off)
                    oprand.add(value)
                    off += 2
                } else if(thisFmt.bitSize == 32){
                    val value = instructions.getInt(off)
                    oprand.add(value)
                    off += 4
                } else if(thisFmt.bitSize == 64){
                    val value = instructions.getLong(off)
                    oprand.add(value)
                    off += 8
                } else throw IllegalStateException("Unsupported bitSize")
            }
            DataAndNextOff(oprand,off)
        }

        val disassembleString:String by lazy {
            val sb = StringBuilder()
            val initOff = codeOffset
            sb.append(InstParser.asmString(this, listOf(ExternModuleParser)))
            sb.append(" ".repeat((8 - sb.length%8)))
            sb.append("//0x")
            (initOff until opRand.nextOffset).forEach {
                val b = asm.code.instructions.get(it).toUByte().toInt()
                sb.append("${hexCharList[b/16]}${hexCharList[b%16]}")
            }
            sb.toString()
        }
    }
}

val Asm.AsmItem.calledMethods:List<AbcMethod> get() = buildList<AbcMethod> {
    ins.format.forEachIndexed { index, instFmt ->
        if(instFmt is InstFmt.MId){
            val value = opRand.value[index].toUnsignedInt().let { asm.code.m.region.mslIndex[it] }
            val method = asm.code.m.abc.method(value)
            if(method is AbcMethod){
                add(method)
            }
        } else if(instFmt is InstFmt.LId){
            val value = opRand.value[index].toUnsignedInt()
            val literalArray = asm.code.m.abc.literalArray(asm.code.m.region.mslIndex[value])
            literalArray.content.forEach {
                if(it is LiteralArray.Literal.LiteralMethod){
                    val method = it.get(asm.code.m.abc)
                    if(method is AbcMethod){
                        add(method)
                    }
                }
            }
        }
    }
}