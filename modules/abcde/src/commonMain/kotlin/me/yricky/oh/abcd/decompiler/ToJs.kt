package me.yricky.oh.abcd.decompiler

import me.yricky.oh.abcd.cfm.argsStr
import me.yricky.oh.abcd.decompiler.behaviour.FunSimCtx
import me.yricky.oh.abcd.decompiler.behaviour.JSValue
import me.yricky.oh.abcd.decompiler.behaviour.Operation
import me.yricky.oh.abcd.decompiler.behaviour.isLeftAcc
import me.yricky.oh.abcd.decompiler.behaviour.isLeftContainsAcc
import me.yricky.oh.abcd.decompiler.behaviour.isRightAcc
import me.yricky.oh.abcd.decompiler.behaviour.isRightContainsAcc
import me.yricky.oh.abcd.decompiler.behaviour.nextOrNull
import me.yricky.oh.abcd.isa.Asm
import me.yricky.oh.abcd.isa.asmName
import me.yricky.oh.abcd.literal.ModuleLiteralArray

class ToJs(val asm: Asm) {
    class UnImplementedError(val item:Asm.AsmItem):Throwable("对字节码${item.asmName}的解析尚未实现")

    fun toJS():String{
        val fc = FunctionDecompilerContext()
        val sb = StringBuilder()
        sb.append("function ").append(asm.code.method.name).append(asm.code.method.argsStr()).append("{\n")
        sb.append(fc.toJS(CodeSegment.genLinear(asm),1))
        sb.append("}")
        return ("${fc.imports.joinToString(separator = ";\n") { it.toString() }}\n" +
                "${fc.nsImports.joinToString(separator = ";\n") { it.toString() }}\n" +
                "\n${sb}").trim()
    }

    private fun FunctionDecompilerContext.toJS(op:Operation):String{
        return when(op){
            Operation.Debugger -> "/* debugger */"
            Operation.Deprecated -> "/* deprecated */"
            Operation.Disabled -> "/* disabled */"
            Operation.NOP -> "/* nop */"
            is Operation.NewLex -> "/* newLex(${op.size}) */"
            is Operation.UnImplemented -> throw UnImplementedError(op.item)
            is Operation.JustAnno -> "/* ${op.anno} */"
            is Operation.Statement -> {
                when(op){
                    is Operation.AssignReg -> "${toJS(op.left)} = ${toJS(op.right)};"
                    is Operation.AssignObj -> "${toJS(op.left)} = ${toJS(op.right)};"
                    is Operation.Jump -> throw IllegalStateException("jump wtf")// "/* jump */"
                    is Operation.JumpIf -> throw IllegalStateException("jumpIf wtf")//"/* jumpIf */"
                    is Operation.Return -> "return ${if(op.hasValue) toJS(FunSimCtx.RegId.ACC) else "undefined"};"
                    Operation.Throw.Acc -> "throw ${toJS(FunSimCtx.RegId.ACC)};"
                    is Operation.Throw.Error -> "throw Error(${op.msg});"
                }
            }

        }
    }

    private fun FunctionDecompilerContext.toJS(exp:Operation.Expression):String {
        return when(exp){
            is Operation.BiExp.AShr -> "${toJS(exp.l)} >> ${toJS(exp.r)}"
            is Operation.BiExp.Add -> "${toJS(exp.l)} + ${toJS(exp.r)}"
            is Operation.BiExp.And -> "${toJS(exp.l)} & ${toJS(exp.r)}"
            is Operation.BiExp.Div -> "${toJS(exp.l)} / ${toJS(exp.r)}"
            is Operation.BiExp.Eq -> "${toJS(exp.l)} == ${toJS(exp.r)}"
            is Operation.BiExp.Exp -> "${toJS(exp.l)} ** ${toJS(exp.r)}"
            is Operation.BiExp.GEq -> "${toJS(exp.l)} >= ${toJS(exp.r)}"
            is Operation.BiExp.Ge -> "${toJS(exp.l)} > ${toJS(exp.r)}"
            is Operation.BiExp.InstOf -> "${toJS(exp.l)} instanceof ${toJS(exp.r)}"
            is Operation.BiExp.IsIn -> "${toJS(exp.l)} in ${toJS(exp.r)}"
            is Operation.BiExp.LEq -> "${toJS(exp.l)} <= ${toJS(exp.r)}"
            is Operation.BiExp.Less -> "${toJS(exp.l)} < ${toJS(exp.r)}"
            is Operation.BiExp.Mod -> "${toJS(exp.l)} % ${toJS(exp.r)}"
            is Operation.BiExp.Mul -> "${toJS(exp.l)} * ${toJS(exp.r)}"
            is Operation.BiExp.NEq -> "${toJS(exp.l)} != ${toJS(exp.r)}"
            is Operation.BiExp.Or -> "${toJS(exp.l)} | ${toJS(exp.r)}"
            is Operation.BiExp.Shl -> "${toJS(exp.l)} << ${toJS(exp.r)}"
            is Operation.BiExp.Shr -> "${toJS(exp.l)} >>> ${toJS(exp.r)}"
            is Operation.BiExp.StrictEq -> "${toJS(exp.l)} === ${toJS(exp.r)}"
            is Operation.BiExp.StrictNEq -> "${toJS(exp.l)} !== ${toJS(exp.r)}"
            is Operation.BiExp.Sub -> "${toJS(exp.l)} - ${toJS(exp.r)}"
            is Operation.BiExp.Xor -> "${toJS(exp.l)} ^ ${toJS(exp.r)}"
            is Operation.CallAcc -> "${ exp.overrideThis?.let { toJS(it) } ?: "this" }.${toJS(FunSimCtx.RegId.ACC)}(${exp.args.joinToString { toJS(it) }})"
            Operation.DynamicImport -> "import(${toJS(FunSimCtx.RegId.ACC)})"
            is Operation.JustImm -> toJS(exp.value)
            is Operation.LoadExternalModule -> "${exp.ext.also { imports.add(it) }.localName}"
            is Operation.LoadReg -> toJS(exp.regId)
            is Operation.NewClass -> TODO("解析NewClass操作尚未实现")
            is Operation.NewInst -> "new ${toJS(exp.clazz)}(${exp.constructorArgs.joinToString { toJS(it) }})"
            is Operation.ObjField.Index -> "${toJS(exp.obj)}[${exp.index}]"
            is Operation.ObjField.Name -> "${toJS(exp.obj)}.${exp.name}"
            is Operation.ObjField.Value -> "${toJS(exp.obj)}[${toJS(exp.value)}]"
            is Operation.UaExp.Dec -> "${toJS(exp.source)} - 1"
            is Operation.GetModuleNamespace -> "import(${exp.ns.str})"
            is Operation.UaExp.GetTemplateObject -> TODO("解析GetTemplateObject尚未实现")
            is Operation.UaExp.Inc -> "${toJS(exp.source)} + 1"
            is Operation.UaExp.IsFalse -> "${toJS(exp.source)} == false"
            is Operation.UaExp.IsTrue -> "${toJS(exp.source)} == true"
            is Operation.UaExp.Neg -> "-${toJS(exp.source)}"
            is Operation.UaExp.Not -> "~${toJS(exp.source)}"
            is Operation.UaExp.ToNumber -> "ToNumber(${toJS(exp.source)})"
            is Operation.UaExp.ToNumeric -> "ToNumeric(${toJS(exp.source)})"
            is Operation.UaExp.TypeOf -> "typeof(${toJS(exp.source)})"
        }
    }

    /**
     * [CodeSegment.IfPattern]的条件跳转意味着如果跳转，则***不执行***body，因此在转换成js代码时，需要对条件判断的表达式取反
     */
    private fun FunctionDecompilerContext.oppositeJS(exp:Operation.Expression):String{
        return when(exp){
            is Operation.UaExp.IsTrue -> toJS(Operation.UaExp.IsFalse(exp.source))
            is Operation.UaExp.IsFalse -> toJS(Operation.UaExp.IsTrue(exp.source))
            is Operation.BiExp.Eq -> toJS(Operation.BiExp.NEq(exp.l,exp.r))
            is Operation.BiExp.NEq -> toJS(Operation.BiExp.Eq(exp.l,exp.r))
            is Operation.BiExp.StrictEq -> toJS(Operation.BiExp.StrictNEq(exp.l,exp.r))
            is Operation.BiExp.StrictNEq -> toJS(Operation.BiExp.StrictEq(exp.l,exp.r))
            else -> "!(${toJS(exp)})"
        }
    }

    fun toJS(jsValue: JSValue):String{
        return when(jsValue){
            is JSValue.ArrInst -> jsValue.content.joinToString(",","[","]") { toJS(it) }
            is JSValue.ClassObj -> TODO("尚未实现的toJS操作")
            is JSValue.Error -> "Error(...)"
            JSValue.False -> "false"
            is JSValue.Function -> "function ${jsValue.method.name}()"
            JSValue.Hole -> "undefined /* hole */"
            JSValue.Infinity -> "infinity"
            JSValue.Nan -> "NaN"
            JSValue.Null -> "null"
            is JSValue.Number -> jsValue.value.toString()
            is JSValue.BigInt -> jsValue.value + 'n'
            is JSValue.ObjInst -> if(jsValue.content.isEmpty()) "{}" else jsValue.content.asSequence().joinToString(", ","{","}") {
                "${it.key}:${toJS(it.value)}"
            }
            is JSValue.Str -> "\"${jsValue.value}\""
            JSValue.Symbol.Iterator -> "Symbol.iterator"
            JSValue.Symbol.SymbolObj -> "Symbol"
            JSValue.True -> "true"
            JSValue.Undefined -> "undefined"
        }
    }

    //以\n结尾
    private fun FunctionDecompilerContext.toJS(linear:CodeSegment.AsLinear, indent:Int = 1):String{
        return when(linear){
            is CodeSegment.IfElsePattern -> {
                val sb = StringBuilder()
                sb.append("  ".repeat(indent)).append("if(${toJS(linear.condition.condition)}){\n")
                sb.append(toJS(linear.ifBody, indent + 1))
                sb.append("  ".repeat(indent)).append("} else {\n")
                sb.append(toJS(linear.elseBody, indent + 1))
                sb.append("  ".repeat(indent)).append("}\n")
                sb.toString()
            }
            is CodeSegment.IfPattern -> {
                val sb = StringBuilder()
                sb.append("  ".repeat(indent)).append("if(${oppositeJS(linear.jumpCondition.condition)}){\n")
                sb.append(toJS(linear.body, indent + 1))
                sb.append("  ".repeat(indent)).append("}\n")
                sb.toString()
            }
            is CodeSegment.Linear -> {
                val sb = StringBuilder()
                if(enableOptimize){
                    optimize(linear.map { it.operation })
                } else {
                    linear.map { it.operation }
                }.forEach { op ->
                    sb.append("  ".repeat(indent))
                    sb.append(toJS(op))
                    sb.append("\n")
                }
                sb.toString()
            }
            is CodeSegment.LinearPattern -> {
                val sb = StringBuilder()
                sb.append(toJS(linear.l1,indent))
                sb.append(toJS(linear.l2,indent))
                sb.toString()
            }
            is CodeSegment.LoopBreakPattern -> {
                val sb = StringBuilder()
                sb.append("  ".repeat(indent)).append("while(true){\n")
                sb.append(toJS(linear.body1, indent + 1))
                sb.append("  ".repeat(indent + 1)).append("if(${toJS(linear.breakCondition.condition)}){\n")
                sb.append("  ".repeat(indent + 2)).append("break;\n")
                sb.append("  ".repeat(indent + 1)).append("}\n")
                sb.append(toJS(linear.body2, indent + 1))
                sb.append("  ".repeat(indent)).append("}\n")
                sb.toString()
            }
            is CodeSegment.LoopPattern -> {
                val sb = StringBuilder()
                sb.append("  ".repeat(indent)).append("while(true){\n")
                sb.append(toJS(linear.loopBody, indent + 1))
                sb.append("  ".repeat(indent)).append("}\n")
                sb.toString()
            }
            is CodeSegment.Return -> "  ".repeat(indent) + toJS(linear.item.operation) + "\n"
            is CodeSegment.WhilePattern -> {
                val sb = StringBuilder()
                sb.append("  ".repeat(indent)).append("while(${toJS(linear.condition.condition)}){\n")
                sb.append(toJS(linear.whileBody, indent + 1))
                sb.append("  ".repeat(indent)).append("}\n")
                sb.toString()
            }

            is CodeSegment.JumpMark -> ""
        }
    }

    private fun toJS(regId: FunSimCtx.RegId):String{
        if(regId.isReg()){
            val v = regId.getRegV()
            val vRegs = asm.code.numVRegs
            return if (v < vRegs) "v${v}" else when(val aIndex = v - vRegs){
                0L -> "FunctionObject"
                1L -> "NewTarget"
                2L -> "this"
                else -> "arg${aIndex - 3}"
            }
        } else if (regId == FunSimCtx.RegId.GLOBAL) {
            return "AtkTsGlobal"
        } else if(regId == FunSimCtx.RegId.THIS) {
            return "this"
        } else return regId.toJS()
    }

    private class FunctionDecompilerContext(val enableOptimize: Boolean = true){
        val imports:MutableList<ModuleLiteralArray.RegularImport> = mutableListOf()
        val nsImports:MutableList<ModuleLiteralArray.NamespaceImport> = mutableListOf()

        fun optimize(linear: Sequence<Operation>): Sequence<Operation> {
            val iterator = linear.iterator()
            return sequence {
                while (iterator.hasNext()){
                    val curr = iterator.next()
                    if(curr is Operation.Assign){
                        val list = mutableListOf<Operation>()
                        trySimplify3Assign(curr,iterator,list)
                        yieldAll(list)
                    } else {
                        yield(curr)
                    }
                }
            }
        }

        /**
         * 对于以下逻辑片段：
         * _acc_ = xxx; (line1)
         * yyy = _acc_; (line2)
         * _acc_ = zzz; (line3)
         *
         * 可将其化简为：
         * yyy = xxx;
         * _acc_ = zzz; （这一行可以作为本化简逻辑的首行进一步尝试化简）
         *
         * @param firstOp line1对应的[Operation]
         * @param iter 字节码迭代器，其[Iterator.next]返回应为line2对应的字节码对象
         * @param out 接受输出的化简结果，包含firstOp。与该函数执行完毕后，若out代表的字节码片段的lastItemIndex为x，则iter.next()返回的字节码位置为x+1
         */
        fun trySimplify3Assign(
            firstOp: Operation.Assign,
            iter: Iterator<Operation>,
            out: MutableList<Operation>
        ){
            if(firstOp.isLeftAcc && !firstOp.isRightContainsAcc){
                val next1 = iter.nextOrNull()
                if(next1 is Operation.Assign){
                    if(next1.isRightAcc && !next1.isLeftContainsAcc){
                        val next2 = iter.nextOrNull()
                        if(next2 is Operation.Assign){
                            if (next2.isLeftAcc && !next2.isRightContainsAcc){
                                out.add(next1.replaceRight(firstOp.right))
                                trySimplify3Assign(next2, iter, out)
                            } else {
                                out.add(firstOp)
                                out.add(next1)
                                out.add(next2)
                            }
                        } else {
                            out.add(firstOp)
                            out.add(next1)
                            next2?.let { out.add(it) }
                        }
                    } else {
                        out.add(firstOp)
                        trySimplify3Assign(next1, iter, out)
                    }
                } else {
                    out.add(firstOp)
                    next1?.let { out.add(it) }
                }
            } else {
                out.add(firstOp)
            }
        }
    }
}