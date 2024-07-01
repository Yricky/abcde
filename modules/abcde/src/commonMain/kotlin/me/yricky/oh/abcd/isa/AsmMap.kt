package me.yricky.oh.abcd.isa

import me.yricky.oh.abcd.isa.bean.InsPrefix
import me.yricky.oh.abcd.isa.bean.Isa

/**
 * 将官方yaml格式转换为方便解析的map数据结构的类
 */
class AsmMap(
    val isa: Isa
) {
    private val prefixMap:Map<String, InsPrefix> = isa.prefixes.associateBy { it.name }
    val prefixInstMap:Map<Byte,Map<Byte,Inst>>
    val insMap:Map<Byte,Inst>

    init {
//        println("init AsmMap")
        val _prefixInstMap:Map<Byte,MutableMap<Byte,Inst>> = isa.prefixes.associate { Pair(it.opcodeIdx.toByte(), mutableMapOf()) }
        insMap = mutableMapOf()
        isa.groups.forEach { ig ->
            ig.instructions.forEach {i ->
                (0 until i.format.size).forEach{
                    val inst = Inst.fromInstructionBean(ig,i,it)
                    if(i.prefix != null){
                        val prefix = prefixMap[i.prefix]
                        if(prefix != null){
                            _prefixInstMap[prefix.opcodeIdx.toByte()]!![inst.opCode] = inst
                        } else {
                            println("no this prefix:${i.prefix}")
                        }
                    } else {
                        insMap[inst.opCode] = inst
                    }
                }
            }
        }
        prefixInstMap = _prefixInstMap
    }
}