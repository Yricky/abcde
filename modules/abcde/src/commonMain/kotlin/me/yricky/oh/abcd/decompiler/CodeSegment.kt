package me.yricky.oh.abcd.decompiler

import me.yricky.oh.abcd.decompiler.Linearizer.Companion.buildRefMap
import me.yricky.oh.abcd.isa.Asm

sealed class CodeSegment{
    //代码段的首个item
    abstract val item: Asm.AsmItem
    abstract val itemCount: Int
    /**
     * 代码段的后继代码段
     * 对于[AsLinear]，该值表示执行完本代码块后将执行的下一个代码块的codeOffset，因此[AsLinear.isTail]为真时该值无意义；
     * 对于[Condition]，该值表示未跳转情况下的下一个代码块offset
     */
    abstract val next: Int

    sealed class AsLinear(override val item: Asm.AsmItem,override val  itemCount: Int,override val  next: Int) :CodeSegment(){

        //为true表示不应有后继结点
        abstract fun isTail():Boolean
    }
    class IfPattern(
        val condition: InsCondition,
        val ifBody:AsLinear
    ):AsLinear(condition.item,condition.itemCount + ifBody.itemCount, ifBody.next){
        init {
            assert(condition.jmpTo == ifBody.next)
            assert(condition.next == ifBody.item.codeOffset)
        }

        override fun isTail(): Boolean = false

        override fun toString(): String {
            return "if(0x${condition.item.codeOffset.toString(16)}){ $ifBody }"
        }
    }
    class IfElsePattern(
        val condition: Condition,
        val ifBody:AsLinear,
        val elseBody:AsLinear
    ):AsLinear(condition.item,condition.itemCount + ifBody.itemCount + elseBody.itemCount, if(elseBody.isTail()) ifBody.next else elseBody.next){
        override fun isTail(): Boolean = ifBody.isTail() && elseBody.isTail()

        override fun toString(): String {
            return "if(0x${condition.item.codeOffset.toString(16)}){ $ifBody }else{ $elseBody }"
        }

        init {
            assert(condition.jmpTo == elseBody.item.codeOffset)
            assert(condition.next == ifBody.item.codeOffset)
//            assert(ifBody.next == elseBody.next)
        }
    }
    class WhilePattern(
        val condition: Condition,
        val whileBody:AsLinear
    ):AsLinear(whileBody.item,condition.itemCount + whileBody.itemCount, condition.next){
        init {
            assert(condition.jmpTo == whileBody.item.codeOffset)
            assert(whileBody.next == condition.item.codeOffset)
        }
        override fun isTail(): Boolean = false


        override fun toString(): String {
            return "while(0x${condition.item.codeOffset.toString(16)}){$whileBody}"
        }
    }
    class LoopPattern(
        val loopBody:AsLinear
    ):AsLinear(loopBody.item,loopBody.itemCount,loopBody.next){
        init {
            assert(loopBody.next == loopBody.item.codeOffset)
        }

        override fun isTail(): Boolean = true
        override fun toString(): String {
            return "loop{ $loopBody }"
        }
    }
    class LoopBreakPattern(
        val body1:AsLinear,
        val breakCondition:InsCondition,
        val body2:AsLinear
    ):AsLinear(body1.item,body1.itemCount + breakCondition.itemCount + body2.itemCount, breakCondition.jmpTo){
        override fun isTail(): Boolean = false
        override fun toString(): String {
            return "loop{ $body1 > breakIf(0x${breakCondition.item.codeOffset.toString(16)} to 0x${breakCondition.jmpTo.toString(16)}) > $body2 }"
        }
    }
    class LinearPattern(
        val l1:AsLinear,
        val l2:AsLinear
    ):AsLinear(l1.item, l1.itemCount + l2.itemCount, l2.next){
        override fun isTail(): Boolean = l2.isTail()

        override fun toString(): String {
            return "$l1 -> $l2"
        }
    }
    class Linear(item: Asm.AsmItem, itemCount: Int, next: Int) :AsLinear(item, itemCount, next){
        override fun isTail(): Boolean = false

        override fun toString(): String {
            return StringBuilder().apply {
                append("(${item.index}-${item.index + itemCount - 1} ")
                append("then 0x${next.toString(16)})")
            }.toString()
        }
    }
    class Return(item: Asm.AsmItem):AsLinear(item, 1, item.nextOffset){
        override fun isTail(): Boolean = true

        override fun toString(): String {
            return "return(0x${item.codeOffset.toString(16)},${item.index})"
        }
    }

    sealed class Condition:CodeSegment(){
        abstract val jmpTo: Int

    }
    class InsCondition(
        override val item: Asm.AsmItem,
        override val jmpTo: Int
    ) :Condition(){
        override val itemCount: Int = 1
        override val next: Int = item.nextOffset

        override fun toString(): String {
            return "insCondition(nxt:0x${next.toString(16)},jmp:0x${jmpTo.toString(16)})"
        }
    }


    companion object{
        fun genGraph(asm: Asm):AsLinear{
            val pcItemMap = mutableMapOf<Int,Asm.AsmItem>()
            val utility = PandaDecompileUtility(asm.asmMap)
            pcItemMap[asm.list.first().codeOffset] = asm.list.first()

            asm.list.forEach { item ->
                if(utility.isReturn(item)){
                    pcItemMap[item.codeOffset] = item
                    item.next?.let { nxt ->
                        pcItemMap[nxt.codeOffset] = nxt
                    }
                } else {
                    utility.jmpTarget(item)?.let { jmpOff ->
                        item.next?.let { nxt ->
                            pcItemMap[nxt.codeOffset] = nxt
                        }
                        val jmpTargetOff = item.codeOffset + jmpOff
                        pcItemMap.put(jmpTargetOff,asm.list.first { i -> i.codeOffset == jmpTargetOff })
                    } ?: utility.conditionJmpTarget(item)?.let {jmpOff ->
                        pcItemMap[item.codeOffset] = item
                        pcItemMap[item.nextOffset] = item.next!!
                        val jmpTargetOff = item.codeOffset + jmpOff
                        pcItemMap.put(jmpTargetOff,asm.list.first { i -> i.codeOffset == jmpTargetOff })
                    }
                }
            }

            val sortedList = pcItemMap.values.sortedBy { it.codeOffset }
            //生成codeOffset -> CodeSegment的map
            val codeSegmentMap = mutableMapOf<Int,CodeSegment>()
            sortedList.forEachIndexed { index, curr ->
                val nxt = sortedList.getOrNull(index + 1)
                if(utility.isReturn(curr)){
                    codeSegmentMap[curr.codeOffset] = Return(curr)
                } else {
                    utility.conditionJmpTarget(curr)?.let { jmpOff ->
                        assert(curr.nextOffset == nxt!!.codeOffset)
                        codeSegmentMap[curr.codeOffset] = InsCondition(
                            curr, curr.codeOffset + jmpOff
                        )
                    } ?: let {
                        val len = (nxt?.index ?: asm.list.size) - curr.index
                        val last = asm.list[curr.index + len - 1]
                        val next = utility.jmpTarget(last)?.let { it + last.codeOffset } ?: nxt?.codeOffset ?: last.nextOffset
                        codeSegmentMap[curr.codeOffset] = Linear(curr,len,next)
                    }
                }
            }
            return linearize(codeSegmentMap)
        }

        private fun linearize(codeSegments:Map<Int,CodeSegment>):AsLinear{
            var lastCodeSegments = codeSegments
            var linearized:Boolean = false
            do {
                val (merged, newCodeSegments) = Linearizer1().linearize(lastCodeSegments)
                linearized = newCodeSegments.size == 1 && newCodeSegments[0] is AsLinear
                lastCodeSegments = newCodeSegments
            } while (!linearized && merged)
            val refMap = buildRefMap(lastCodeSegments)
            lastCodeSegments.forEach { off, seg ->
                println("    0x${off.toString(16)} ----(${refMap[off]})---> $seg")
            }
            return if(linearized){
                lastCodeSegments[0] as AsLinear
            } else throw IllegalStateException()
        }
    }

}