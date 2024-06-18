package me.yricky.oh.abcd.isa

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper
import me.yricky.oh.abcd.cfm.AbcMethod
import me.yricky.oh.abcd.code.Code
import me.yricky.oh.abcd.code.TryBlock
import me.yricky.oh.abcd.isa.Inst.Companion.toUnsignedInt
import me.yricky.oh.abcd.isa.bean.Isa
import me.yricky.oh.abcd.literal.LiteralArray
import me.yricky.oh.utils.DataAndNextOff
import me.yricky.oh.utils.nextOffset
import me.yricky.oh.utils.value

class Asm(
    val code: Code,
) {
    companion object{
        val asmMap by lazy {
            val yaml = YAMLMapper()
            AsmMap(yaml.readValue(Asm::class.java.classLoader.getResourceAsStream("abcde/isa.yaml"),object : TypeReference<Isa>(){}))
        }
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



    class AsmItem(
        val asm:Asm,
        val ins:Inst,
        val codeOffset:Int
    ){
        val tryBlocks:List<TryBlock> get() = asm.code.tryBlocks.filter {
            codeOffset in (it.startPc until (it.startPc+ it.length))
        }

        val opRand by lazy {
            val instructions = asm.code.instructions
            val oprand = mutableListOf<Number>()
            val iter = ins.format.iterator()
            var off = codeOffset
            while (iter.hasNext()){
                val thisFmt = iter.next()
                if(thisFmt is Inst.InstFmt.Prefix || thisFmt is Inst.InstFmt.OpCode){
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

            with(asm.code){
                sb.append(ins.asmString(asm.code,opRand.value))
            }
            sb.append(" ".repeat((8 - sb.length%8)))
            sb.append("//")
            (initOff until opRand.nextOffset).forEach {
                sb.append(String.format("%02X",asm.code.instructions.get(it)))
            }
            sb.toString()
        }
    }
}

val Asm.AsmItem.calledMethods:List<AbcMethod> get() = buildList<AbcMethod> {
    ins.format.forEachIndexed { index, instFmt ->
        if(instFmt is Inst.InstFmt.MId){
            val value = opRand.value[index].toUnsignedInt().let { asm.code.m.region.mslIndex[it] }
            val method = asm.code.m.abc.method(value)
            if(method is AbcMethod){
                add(method)
            }
        } else if(instFmt is Inst.InstFmt.LId){
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