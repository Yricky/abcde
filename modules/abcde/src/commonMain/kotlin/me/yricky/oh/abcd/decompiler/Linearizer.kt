package me.yricky.oh.abcd.decompiler

import me.yricky.oh.abcd.decompiler.CodeSegment.LoopPattern
import me.yricky.oh.abcd.isa.asmName

/**
 * 线性化器，消除代码段中的分支结构
 */
abstract class Linearizer {
    fun linearize(lastCodeSegments: Map<Int, CodeSegment>): Pair<Boolean, Map<Int, CodeSegment>> {
        var merged = false
        val newCodeSegments = mutableMapOf<Int,CodeSegment>()
        val usedSet = mutableSetOf<Int>() //这些offset表示的代码块已经包含在newCodeSegments中了
        val transparentSet = mutableSetOf<Int>() //usedSet的子集，这些代码块本轮未做处理
        val offsetRefMap = buildRefMap(lastCodeSegments)
        lastCodeSegments.forEach{ (_,seg) ->
            if (usedSet.contains(seg.item.codeOffset)){
                //这个代码块被合并了
            } else {
                usedSet.add(seg.item.codeOffset)
                merged = merged or handle(
                    seg,offsetRefMap,usedSet,transparentSet,lastCodeSegments,newCodeSegments
                ){
                    handleSegUsed(it,usedSet, transparentSet, newCodeSegments, offsetRefMap)
                }
            }
        }
        return Pair(merged,newCodeSegments)
    }

    protected abstract fun handle(
        seg:CodeSegment,
        offsetRefMap: Map<Int, Int>,
        usedSet: MutableSet<Int>,
        transparentSet: MutableSet<Int>,
        lastCodeSegments: Map<Int, CodeSegment>,
        newCodeSegments: MutableMap<Int, CodeSegment>,
        segUsed:(CodeSegment) -> Unit
    ):Boolean

    companion object{

        /**
         * 处理一个代码块被使用了
         * “被使用”的定义：下一轮中，该代码块的被引用数-1
         */
        fun handleSegUsed(
            seg: CodeSegment,
            usedSet:MutableSet<Int>,
            transparentSet:MutableSet<Int>,
            newCodeSegments:MutableMap<Int,CodeSegment>,
            offsetRefMap:Map<Int,Int>
        ){
            assert(transparentSet.contains(seg.item.codeOffset) || !usedSet.contains(seg.item.codeOffset))
            val refCount = offsetRefMap[seg.item.codeOffset]!!
//            println("handle:0x${seg.item.codeOffset.toString(16)}, ref:$refCount")
            //这个代码被使用了，因此从transparentSet中移除先
            if(transparentSet.remove(seg.item.codeOffset)){
                if(refCount == 1){ //引用数为1，代表仅被本次使用的地方引用，这个代码块不应再带到下一轮中
                    newCodeSegments.remove(seg.item.codeOffset)
                }
            } else { //移除失败了，证明这个代码块还没被迭代到
                usedSet.add(seg.item.codeOffset) //移到被使用set中，防止改代码块被迭代
                if(refCount > 1){ //引用数大于1，代表还有其他地方引用，这个代码块应带到下一轮中
                    newCodeSegments[seg.item.codeOffset] = seg
                }
            }
        }


        /**
         * 如果代码块A执行后可能会执行代码块B，则代码块B的codeOffset对应的refCount加1。
         * 这个函数按照这样的规则构建一个codeOffset->refCount的map
         *
         * @return Map<codeOffset,refCount>
         */
        fun buildRefMap(
            codeSegments: Map<Int, CodeSegment>,
        ):Map<Int,Int>{
            val offsetRefMap = mutableMapOf<Int,Int>()
            codeSegments.forEach{ (_, seg) ->
                val nxt = codeSegments[seg.next]
                if((seg as? CodeSegment.AsLinear)?.isTail() == true){
                    //没有后继结点，不会引用其他代码块
                }else if(nxt == null){
                    if(seg.next != seg.item.asm.list.last().nextOffset){
                        throw IllegalStateException("not valid next,nxt:0x${seg.next.toString(16)},item:${seg.item.asmName},segments:${codeSegments}")
                    }
                } else {
                    offsetRefMap[nxt.item.codeOffset] = (offsetRefMap[nxt.item.codeOffset]?:0) + 1
                }
                if(seg is CodeSegment.InsCondition){
//                    println("$seg, ${seg.jmpTo}, context:${seg.item.asm.code.method.name}")
                    val jmp = codeSegments[seg.jmpTo]!!
                    offsetRefMap[jmp.item.codeOffset] = (offsetRefMap[jmp.item.codeOffset]?:0) + 1
                }
            }
            return offsetRefMap
        }
    }
}

class Linearizer1: Linearizer() {
    override fun handle(
        seg:CodeSegment,
        offsetRefMap: Map<Int, Int>,
        usedSet: MutableSet<Int>,
        transparentSet: MutableSet<Int>,
        lastCodeSegments: Map<Int, CodeSegment>,
        newCodeSegments: MutableMap<Int, CodeSegment>,
        segUsed:(CodeSegment) -> Unit
    ):Boolean{
        var merged = false
        val nxt = lastCodeSegments[seg.next]
        if(seg is CodeSegment.AsLinear){
            if(seg.isTail()){ //这个代码块就是终点了，暂时原样交给后续轮次
                newCodeSegments[seg.item.codeOffset] = seg
                transparentSet.add(seg.item.codeOffset)
            } else if(seg.next == seg.item.codeOffset && seg !is LoopPattern) { //代码段的下个节点指向自己，无限循环
                val newLinear = LoopPattern(seg)
                newCodeSegments[seg.item.codeOffset] = newLinear
                merged = true
            }else if(
                nxt is CodeSegment.AsLinear && offsetRefMap[nxt.item.codeOffset] == 1 &&
                (transparentSet.contains(nxt.item.codeOffset) || !usedSet.contains(nxt.item.codeOffset))
            ){
                val newLinear = CodeSegment.LinearPattern(seg, nxt)
                newCodeSegments[seg.item.codeOffset] = newLinear
                merged = true
                segUsed(nxt)
            } else if(
                nxt is CodeSegment.InsCondition && offsetRefMap[nxt.item.codeOffset] == 1 && nxt.jmpTo == seg.item.codeOffset &&
                (transparentSet.contains(nxt.item.codeOffset) || !usedSet.contains(nxt.item.codeOffset))
            ){
                val newLinear = CodeSegment.WhilePattern(nxt, seg)
                newCodeSegments[seg.item.codeOffset] = newLinear
                merged = true
                segUsed(nxt)
            }
            else if(
                nxt is CodeSegment.InsCondition && offsetRefMap[nxt.item.codeOffset] == 1 &&
                lastCodeSegments[nxt.next] is CodeSegment.AsLinear && offsetRefMap[nxt.next] == 1 &&
                lastCodeSegments[nxt.next]!!.next == seg.item.codeOffset &&
                (transparentSet.contains(nxt.item.codeOffset) || !usedSet.contains(nxt.item.codeOffset))
            ){
                val nxtnxt = lastCodeSegments[nxt.next] as CodeSegment.AsLinear
                val newLinear = CodeSegment.LoopBreakPattern(seg,nxt,nxtnxt)
                newCodeSegments[seg.item.codeOffset] = newLinear
                merged = true
                segUsed(nxt)
                segUsed(nxtnxt)
            }
            else { //这个代码块本轮不做处理，暂时原样交给后续轮次
                newCodeSegments[seg.item.codeOffset] = seg
                transparentSet.add(seg.item.codeOffset)
            }
        } else if(seg is CodeSegment.InsCondition){
            val jmp = lastCodeSegments[seg.jmpTo]!!
            nxt!!
            if(nxt is CodeSegment.AsLinear && seg.jmpTo == nxt.next && //offsetRefMap[nxt.item.codeOffset] == 1 &&
                (transparentSet.contains(nxt.item.codeOffset) || !usedSet.contains(nxt.item.codeOffset))
            ){
                val newLinear = CodeSegment.IfPattern(seg, nxt)
                newCodeSegments[seg.item.codeOffset] = newLinear
                merged = true
                segUsed(nxt)
            } else if(
                nxt is CodeSegment.AsLinear && //offsetRefMap[nxt.item.codeOffset] == 1 &&
                jmp is CodeSegment.AsLinear && //offsetRefMap[jmp.item.codeOffset] == 1 &&
                ((jmp.next == nxt.next) || ((nxt as? CodeSegment.AsLinear)?.isTail() == true) || ((jmp as? CodeSegment.AsLinear)?.isTail() == true)) &&
                (transparentSet.contains(nxt.item.codeOffset) || !usedSet.contains(nxt.item.codeOffset)) &&
                (transparentSet.contains(jmp.item.codeOffset) || !usedSet.contains(jmp.item.codeOffset))
            ){
                val newLinear = CodeSegment.IfElsePattern(seg, jmp, nxt)
                newCodeSegments[seg.item.codeOffset] = newLinear
                merged = true
                segUsed(nxt)
                segUsed(jmp)
            } else if(nxt.item.codeOffset == jmp.item.codeOffset){
                val newLinear = CodeSegment.Linear(seg.item, 1, seg.next)
                newCodeSegments[seg.item.codeOffset] = newLinear
                merged = true
            } else { //这个条件跳转这次没法处理，暂时原样交给后续轮次
                newCodeSegments[seg.item.codeOffset] = seg
                transparentSet.add(seg.item.codeOffset)
            }
        }
        return merged
    }

}