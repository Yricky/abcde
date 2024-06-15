package me.yricky.oh.abcd.isa

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper
import me.yricky.oh.abcd.code.Code
import me.yricky.oh.abcd.isa.bean.Isa

class Asm(
    val code: Code,
) {
    companion object{
        val yaml = YAMLMapper()
        val asmMap = AsmMap(yaml.readValue(Asm::class.java.classLoader.getResourceAsStream("abcde/isa.yaml"),object : TypeReference<Isa>(){}))
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
                li.add(AsmItem(inst,initOff))
            } ?: asmMap.insMap[opCode]?.let { ins ->
                off += ins.argSize()
                li.add(AsmItem(ins, initOff))
            } ?: throw IllegalStateException("No this opCode:${opCode.toString(16)},off:${off - 1}")
        }
        li
    }

    fun asmString(item: AsmItem):String{
        val sb = StringBuilder()
        val initOff = item.codeOffset
        var off = item.codeOffset //opCode之后的

        with(code){
            val oprand = mutableListOf<Number>()
            val iter = item.ins.format.iterator()
            while (iter.hasNext()){
                val thisFmt = iter.next()
                if(thisFmt is Inst.InstFmt.Prefix || thisFmt is Inst.InstFmt.OpCode){
                    off += 1
                } else if(thisFmt.bitSize == 4){
                    val nextFmt = iter.next()
                    if(nextFmt.bitSize != 4){
                        throw IllegalStateException()
                    }
                    val value = instructions.get(off).toUByte().toInt()
                    oprand.add(value.shr(4))
                    oprand.add(value and  0xf)
                    off += 1
                } else if(thisFmt.bitSize == 8){
                    val value = instructions.get(off)
                    oprand.add(value)
                    off += 1
                } else if(thisFmt.bitSize == 16){
                    val value = instructions.getShort(off).toUShort()
                    oprand.add(value.toInt())
                    off += 2
                } else if(thisFmt.bitSize == 32){
                    val value = instructions.getInt(off)
                    oprand.add(value)
                    off += 4
                } else if(thisFmt.bitSize == 64){
                    val value = instructions.getLong(off)
                    oprand.add(value)
                    off += 8
                } else throw IllegalStateException()
            }
            sb.append(item.ins.asmString(code,oprand))
//            when(val ins = item.ins){
//                is Ins.Ins1 -> {
//                    sb.append(ins.symbol).append(' ')
//                    ins.fmt.units.forEach {
//                        when(it){
//                            is IMM8 -> {
//                                val value = instructions.get(off).toUByte().toInt()
//                                sb.append(String.format("%02X,",value))
//                            }
//                            LID16 -> {
//                                val value = instructions.getShort(off).toUShort().toInt()
//                                val literalArray = LiteralArray(m.abc,m.region.mslIndex[value])
//                                sb.append("${literalArray},")
//                            }
//                            SID16 -> {
//                                val value = instructions.getShort(off).toUShort().toInt()
//                                val str = m.abc.stringItem(m.region.mslIndex[value])
//                                sb.append("\"${str.value}\",")
//                            }
//                            MID16 -> {
//                                val value = instructions.getShort(off).toUShort().toInt().let { m.region.mslIndex[it] }
//                                val method = if(m.abc.isForeignOffset(value)) ForeignMethod(m.abc,value) else AbcMethod(m.abc,value)
//                                sb.append("${method.clazz.name}.${method.name},")
//                            }
//                            IMM16 -> {
//                                val value = instructions.getShort(off).toUShort().toInt()
//                                sb.append(String.format("%04X,",value))
//                            }
//                            IMM32 -> {
//                                val value = instructions.getInt(off)
//                                sb.append(String.format("%08X,",value))
//                            }
//                            IMM4IMM4 -> {
//                                val value = instructions.get(off).toUByte().toInt()
//                                sb.append(String.format("[44]%02X,",value))
//                            }
//                            IMM64 -> {
//                                val value = instructions.getLong(off)
//                                sb.append(String.format("%16X,",value))
//                            }
//                            V16 -> {
//                                val value = instructions.getShort(off).toUShort().toInt()
//                                sb.append(String.format("V%04X,",value))
//                            }
//                            V4V4 -> {
//                                val value = instructions.get(off).toUByte().toInt()
//                                sb.append(String.format("[vv]%02X,",value))
//                            }
//                            V8 -> {
//                                val value = instructions.get(off).toUByte().toInt()
//                                sb.append(String.format("v%02X,",value))
//                            }
//                        }
//                        off += it.size
//                    }
//                }
//                is Ins.Ins2 -> {
//                    sb.append(ins.symbol).append('.')
//                    val subOpCode = instructions.get(off)
//                    off += 1
//                    val subIns = ins.map[subOpCode] ?: throw IllegalStateException("No this subOpCode:${subOpCode.toString(16)} in opCode:${ins.opCode.toString(16)}")
//                    sb.append(subIns.symbol).append(' ')
//                    subIns.fmt.units.forEach {
//                        when(it){
//                            is IMM8 -> {
//                                val value = instructions.get(off).toUByte().toInt()
//                                sb.append(String.format("%02X,",value))
//                            }
//                            LID16 -> {
//                                val value = instructions.getShort(off).toUShort().toInt()
//                                val literalArray = LiteralArray(m.abc,m.region.mslIndex[value])
//                                sb.append("${literalArray},")
//                            }
//                            SID16 -> {
//                                val value = instructions.getShort(off).toUShort().toInt()
//                                val str = m.abc.stringItem(m.region.mslIndex[value])
//                                sb.append("\"${str.value}\",")
//                            }
//                            MID16 -> {
//                                println("ins:${ins.symbol}")
//                                val value = instructions.getShort(off).toUShort().toInt().let { m.region.mslIndex[it] }
//                                val method = if(m.abc.isForeignOffset(value)) ForeignMethod(m.abc,value) else AbcMethod(m.abc,value)
//                                sb.append("${method.clazz.name}.${method.name},")
//                            }
//                            IMM16 -> {
//                                val value = instructions.getShort(off).toUShort().toInt()
//                                sb.append(String.format("%04X,",value))
//                            }
//                            IMM32 -> {
//                                val value = instructions.getInt(off)
//                                sb.append(String.format("%08X,",value))
//                            }
//                            IMM4IMM4 -> {
//                                val value = instructions.get(off).toUByte().toInt()
//                                sb.append(String.format("[44]%02X,",value))
//                            }
//                            IMM64 -> {
//                                val value = instructions.getLong(off)
//                                sb.append(String.format("%16X,",value))
//                            }
//                            V16 -> {
//                                val value = instructions.getShort(off).toUShort().toInt()
//                                sb.append(String.format("V%04X,",value))
//                            }
//                            V4V4 -> {
//                                val value = instructions.get(off).toUByte().toInt()
//                                sb.append(String.format("[vv]%02X,",value))
//                            }
//                            V8 -> {
//                                val value = instructions.get(off).toUByte().toInt()
//                                sb.append(String.format("v%02X,",value))
//                            }
//                        }
//                        off += it.size
//                    }
//
//                }
//            }

        }
//        if(sb.last() == ','){
//            sb.deleteCharAt(sb.lastIndex)
//        }
        sb.append(" ".repeat((8 - sb.length%8)))
        sb.append("//")
        (initOff until off).forEach {
            sb.append(String.format("%02X",code.instructions.get(it)))
        }
        return sb.toString()
    }

    class AsmItem(
        val ins:Inst,
        val codeOffset:Int
    )
}