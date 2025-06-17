package me.yricky.oh.abcd.decompiler

import me.yricky.oh.abcd.decompiler.behaviour.IrOp
import me.yricky.oh.abcd.isa.Asm

sealed interface CodeSegment{
    sealed interface BasicBlock:CodeSegment

    //代码段的首个item
    val item: Asm.AsmItem
    val itemCount: Int
    /**
     * 代码段的后继代码段
     * 对于[AsLinear]，该值表示执行完本代码块后将执行的下一个代码块的codeOffset，因此[AsLinear.isTail]为真时该值无意义；
     * 对于[Condition]，该值表示未跳转情况下的下一个代码块offset
     */
    val next: Int

    sealed class AsLinear(override val item: Asm.AsmItem,override val  itemCount: Int,override val  next: Int) :CodeSegment{

        //为true表示不应有后继结点
        abstract fun isTail():Boolean
    }
    class IfPattern(
        val jumpCondition: InsCondition,
        //不跳转的时候执行的内容
        val body:AsLinear
    ):AsLinear(jumpCondition.item,jumpCondition.itemCount + body.itemCount, body.next){
        init {
            assert(jumpCondition.jmpTo == body.next)
            assert(jumpCondition.next == body.item.codeOffset)
        }

        override fun isTail(): Boolean = false

        override fun toString(): String {
            return "if(0x${jumpCondition.item.codeOffset.toString(16)}){ $body }"
        }
    }
    class IfElsePattern(
        val condition: InsCondition,
        val ifBody:AsLinear,
        val elseBody:AsLinear
    ):AsLinear(condition.item,condition.itemCount + ifBody.itemCount + elseBody.itemCount, if(elseBody.isTail()) ifBody.next else elseBody.next){
        override fun isTail(): Boolean = ifBody.isTail() && elseBody.isTail()

        override fun toString(): String {
            return "if(0x${condition.item.codeOffset.toString(16)}){ $ifBody }else{ $elseBody }"
        }

        init {
            assert(condition.jmpTo == ifBody.item.codeOffset)
            assert(condition.next == elseBody.item.codeOffset)
//            assert(ifBody.next == elseBody.next)
        }
    }
    class WhilePattern(
        val condition: InsCondition,
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
    class Linear(item: Asm.AsmItem, itemCount: Int, next: Int) :AsLinear(item, itemCount, next),BasicBlock, Sequence<Asm.AsmItem>{
        override fun isTail(): Boolean = false

        override fun toString(): String {
            return StringBuilder().apply {
                append("(${item.index}-${item.index + itemCount - 1} ")
                append("then 0x${next.toString(16)})")
            }.toString()
        }

        override fun iterator(): Iterator<Asm.AsmItem> = object : Iterator<Asm.AsmItem>{
            var currItem: Asm.AsmItem? = item
            var currIndex = 0
            override fun hasNext(): Boolean = currIndex < itemCount

            override fun next(): Asm.AsmItem {
                val ret = currItem
                currItem = currItem?.next
                ++currIndex
                return ret!!
            }
        }
    }

    class JumpMark(item: Asm.AsmItem): AsLinear(item,1,(item.irOp as IrOp.Jump).offset + item.codeOffset),BasicBlock {
        override fun isTail(): Boolean = false
    }

    class Return(item: Asm.AsmItem):AsLinear(item, 1, item.nextOffset),BasicBlock{
        override fun isTail(): Boolean = true

        override fun toString(): String {
            return "return(0x${item.codeOffset.toString(16)},${item.index})"
        }
    }


    /**
     * 当满足条件的时候进行跳转
     * @param condition [IrOp.JumpIf.condition]
     */
    class InsCondition(
        override val item: Asm.AsmItem,
        val condition: IrOp.Expression,
        val jmpTo: Int
    ) :BasicBlock{
        override val itemCount: Int = 1
        override val next: Int get() = item.nextOffset

        override fun toString(): String {
            return "insCondition(nxt:0x${next.toString(16)},jmp:0x${jmpTo.toString(16)})"
        }
    }


    companion object{
        fun genGraph(asm:Asm):Map<Int,BasicBlock>{
            /*
             * 字节码的执行顺序可以用一张有向图表示，图的边就是顺序执行的字节码片段，节点则是分支或汇入的字节码位置。
             * 这一步的操作是找出图的所有节点
             */
            val pcItemMap = mutableMapOf<Int,Asm.AsmItem>()
            //首个字节码的位置显然是一个节点
            pcItemMap[asm.list.first().codeOffset] = asm.list.first()
            asm.list.forEach { item ->
                val operation = item.irOp
                if(operation is IrOp.Return){
                    //返回处的字节码显然是一个节点
                    pcItemMap[item.codeOffset] = item
                    item.next?.let { nxt ->
                        pcItemMap[nxt.codeOffset] = nxt
                    }
                } else if(operation is IrOp.Jump) {
                    //无条件跳转的目标位置显然是一个节点，无条件跳转的字节码的下一个位置若想被执行，一定是从其他地方跳转到这里，因此也是一个节点。但其本身的位置不应成为一个节点。
                    pcItemMap[item.codeOffset] = item
                    item.next?.let { nxt -> pcItemMap[nxt.codeOffset] = nxt }
                    val jmpTargetOff = item.codeOffset + operation.offset
                    pcItemMap[jmpTargetOff] = asm.list.first { i -> i.codeOffset == jmpTargetOff }
                } else if(operation is IrOp.JumpIf){
                    //条件跳转的目标位置、字节码的下一个位置、和其本身的位置都视为一个节点
                    pcItemMap[item.codeOffset] = item
                    pcItemMap[item.nextOffset] = item.next!!
                    val jmpTargetOff = item.codeOffset + operation.offset
                    pcItemMap[jmpTargetOff] = asm.list.first { i -> i.codeOffset == jmpTargetOff }
                }
            }

            val sortedList = pcItemMap.values.sortedBy { it.codeOffset }

            /*
             * 现在我们有了节点，接下来从节点构建边。
             * 边有两种，一种唯一指向另一条边，另一种（末端是jumpIf）唯一指向另两条边
             */
            val codeSegmentMap = mutableMapOf<Int,BasicBlock>()
            sortedList.forEachIndexed { index, curr ->
                val nxt = sortedList.getOrNull(index + 1)
                val ope = curr.irOp
                if(ope is IrOp.Return){
                    codeSegmentMap[curr.codeOffset] = Return(curr)
                } else if(ope is IrOp.JumpIf) {
                    assert(curr.nextOffset == nxt!!.codeOffset)
                    codeSegmentMap[curr.codeOffset] = InsCondition(
                        curr, ope.condition, curr.codeOffset + ope.offset
                    )
                } else if(ope is IrOp.Jump){
//                    val len = (nxt?.index ?: asm.list.size) - curr.index
//                    assert(len == 1)
                    codeSegmentMap[curr.codeOffset] = JumpMark(curr)
                } else {
                    val len = (nxt?.index ?: asm.list.size) - curr.index
                    val last = asm.list[curr.index + len - 1]
                    val next = nxt?.codeOffset ?: last.nextOffset
                    codeSegmentMap[curr.codeOffset] = Linear(curr,len,next)
                }
            }
            return codeSegmentMap
        }

        fun genLinear(asm: Asm):AsLinear{
            val codeSegmentMap = genGraph(asm)
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
            return if(linearized){
                lastCodeSegments[0] as AsLinear
            } else throw IllegalStateException("无法将字节码转化为线性结构，这可能是由于此方法中包含try-catch\n" +
                    "孤立的字节码offset：${lastCodeSegments.keys.filter { it != 0 }.map { "0x" + it.toString(16) }}")
        }
    }

}