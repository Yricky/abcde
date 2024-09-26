package me.yricky.oh.abcd.decompiler

import me.yricky.oh.abcd.isa.Asm
import me.yricky.oh.abcd.isa.AsmMap
import me.yricky.oh.abcd.isa.InstFmt
import me.yricky.oh.abcd.isa.bean.InsGroup
import me.yricky.oh.abcd.isa.bean.Instruction

class PandaDecompileUtility(
    val asmMap: AsmMap
) {
    private val jmpGroup:InsGroup by lazy {
        asmMap.isa.groups.first { it.pseudo == "pc += imm\n" }
    }

    /**
     * 无条件跳转的指令
     */
    private val jmpIns:Instruction by lazy {
        jmpGroup.instructions.first { it.sig.startsWith("jmp ") }
    }

    private val returnGroup:InsGroup by lazy {
        asmMap.isa.groups.first { it.properties?.contains("return") == true }
    }

    fun conditionJmpTarget(item: Asm.AsmItem):Int?{
        if(item.ins.group != jmpGroup){
            return null
        }
        if(item.ins.instruction == jmpIns){
            return null
        }
//        println("isCondition:${item.codeOffset}, group:${item.ins.group}")
        item.ins.format.forEachIndexed { i,it ->
            if(it is InstFmt.ImmI){
                return item.opUnits[i].toInt()
            }
        }
        return null
    }

    fun jmpTarget(item: Asm.AsmItem):Int?{
        if(item.ins.instruction != jmpIns){
            return null
        }
        return item.opUnits[1].toInt()
    }

    fun isReturn(item: Asm.AsmItem):Boolean{
        return item.ins.group == returnGroup
    }

}