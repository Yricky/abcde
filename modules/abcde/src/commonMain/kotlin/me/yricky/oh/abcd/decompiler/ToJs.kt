package me.yricky.oh.abcd.decompiler

import me.yricky.oh.abcd.cfm.argsStr
import me.yricky.oh.abcd.decompiler.behaviour.FunSimCtx
import me.yricky.oh.abcd.decompiler.behaviour.JSValue
import me.yricky.oh.abcd.decompiler.behaviour.IrOp
import me.yricky.oh.abcd.decompiler.behaviour.assignLeftAcc
import me.yricky.oh.abcd.decompiler.behaviour.assignLeftContainsAcc
import me.yricky.oh.abcd.decompiler.behaviour.assignRightAcc
import me.yricky.oh.abcd.decompiler.behaviour.assignRightContainsAcc
import me.yricky.oh.abcd.isa.Asm
import me.yricky.oh.abcd.isa.asmName
import me.yricky.oh.abcd.literal.ModuleLiteralArray

class ToJs(val asm: Asm) {
    class UnImplementedError(val item:Asm.AsmItem):Throwable("对字节码${item.asmName}的解析尚未实现")

    fun toJS(enableOptimize: Boolean = true):String{
        val fc = FunctionDecompilerContext(enableOptimize)
        val sb = StringBuilder()
        sb.append("function ").append(asm.code.method.name).append(asm.code.method.argsStr()).append("{\n")
        sb.append(fc.toJS(CodeSegment.genLinear(asm),1))
        sb.append("}")
        return ("${fc.imports.joinToString(separator = ";\n") { it.toString() }}\n" +
                "${fc.nsImports.joinToString(separator = ";\n") { it.toString() }}\n" +
                "\n${sb}").trim()
    }

    private fun FunctionDecompilerContext.toJS(op:IrOp):String{
        return when(op){
            IrOp.Debugger -> "/* debugger */"
            IrOp.Deprecated -> "/* deprecated */"
            IrOp.Disabled -> "/* disabled */"
            IrOp.NOP -> "/* nop */"
            is IrOp.NewLex -> "/* newLex(${op.size}) */"
            is IrOp.UnImplemented -> throw UnImplementedError(op.item)
            is IrOp.JustAnno -> "/* ${op.anno} */"
            is IrOp.Statement -> {
                when(op){
                    is IrOp.AssignReg -> "${toJS(op.left)} = ${toJS(op.right)};"
                    is IrOp.AssignObj -> "${toJS(op.left)} = ${toJS(op.right)};"
                    is IrOp.Jump -> throw IllegalStateException("jump wtf")// "/* jump */"
                    is IrOp.JumpIf -> throw IllegalStateException("jumpIf wtf")//"/* jumpIf */"
                    is IrOp.Return -> "return ${if(op.hasValue) toJS(FunSimCtx.RegId.ACC) else "undefined"};"
                    IrOp.Throw.Acc -> "throw ${toJS(FunSimCtx.RegId.ACC)};"
                    is IrOp.Throw.Error -> "throw Error(${op.msg});"
                    is IrOp.DeleteProp -> "delete ${toJS(op.obj)}[${toJS(op.prop)}];"
                }
            }

        }
    }

    private fun FunctionDecompilerContext.toJS(exp:IrOp.Expression):String {
        return when(exp){
            is IrOp.BiExp.AShr -> "${toJS(exp.l)} >> ${toJS(exp.r)}"
            is IrOp.BiExp.Add -> "${toJS(exp.l)} + ${toJS(exp.r)}"
            is IrOp.BiExp.And -> "${toJS(exp.l)} & ${toJS(exp.r)}"
            is IrOp.BiExp.Div -> "${toJS(exp.l)} / ${toJS(exp.r)}"
            is IrOp.BiExp.Eq -> "${toJS(exp.l)} == ${toJS(exp.r)}"
            is IrOp.BiExp.Exp -> "${toJS(exp.l)} ** ${toJS(exp.r)}"
            is IrOp.BiExp.GEq -> "${toJS(exp.l)} >= ${toJS(exp.r)}"
            is IrOp.BiExp.Ge -> "${toJS(exp.l)} > ${toJS(exp.r)}"
            is IrOp.BiExp.InstOf -> "${toJS(exp.l)} instanceof ${toJS(exp.r)}"
            is IrOp.BiExp.IsIn -> "${toJS(exp.l)} in ${toJS(exp.r)}"
            is IrOp.BiExp.LEq -> "${toJS(exp.l)} <= ${toJS(exp.r)}"
            is IrOp.BiExp.Less -> "${toJS(exp.l)} < ${toJS(exp.r)}"
            is IrOp.BiExp.Mod -> "${toJS(exp.l)} % ${toJS(exp.r)}"
            is IrOp.BiExp.Mul -> "${toJS(exp.l)} * ${toJS(exp.r)}"
            is IrOp.BiExp.NEq -> "${toJS(exp.l)} != ${toJS(exp.r)}"
            is IrOp.BiExp.Or -> "${toJS(exp.l)} | ${toJS(exp.r)}"
            is IrOp.BiExp.Shl -> "${toJS(exp.l)} << ${toJS(exp.r)}"
            is IrOp.BiExp.Shr -> "${toJS(exp.l)} >>> ${toJS(exp.r)}"
            is IrOp.BiExp.StrictEq -> "${toJS(exp.l)} === ${toJS(exp.r)}"
            is IrOp.BiExp.StrictNEq -> "${toJS(exp.l)} !== ${toJS(exp.r)}"
            is IrOp.BiExp.Sub -> "${toJS(exp.l)} - ${toJS(exp.r)}"
            is IrOp.BiExp.Xor -> "${toJS(exp.l)} ^ ${toJS(exp.r)}"
            is IrOp.CallAcc -> "${ exp.overrideThis?.let { toJS(it) } ?: "this" }.${toJS(FunSimCtx.RegId.ACC)}(${exp.args.joinToString { toJS(it) }})"
            is IrOp.DynamicImport -> "import(${toJS(exp.regId)})"
            is IrOp.JustImm -> toJS(exp.value)
            is IrOp.LoadExternalModule -> "${exp.ext.also { imports.add(it) }.localName}"
            is IrOp.LoadReg -> toJS(exp.regId)
            is IrOp.NewClass -> TODO("解析NewClass操作尚未实现")
            is IrOp.NewInst -> "new ${toJS(exp.clazz)}(${exp.constructorArgs.joinToString { toJS(it) }})"
            is IrOp.ObjField.Index -> "${toJS(exp.obj)}[${exp.index}]"
            is IrOp.ObjField.Name -> "${toJS(exp.obj)}.${exp.name}"
            is IrOp.ObjField.Value -> "${toJS(exp.obj)}[${toJS(exp.value)}]"
            is IrOp.UaExp.Dec -> "${toJS(exp.source)} - 1"
            is IrOp.GetModuleNamespace -> "import(${exp.ns.str})"
            is IrOp.UaExp.GetTemplateObject -> TODO("解析GetTemplateObject尚未实现")
            is IrOp.UaExp.Inc -> "${toJS(exp.source)} + 1"
            is IrOp.UaExp.IsFalse -> "${toJS(exp.source)} == false"
            is IrOp.UaExp.IsTrue -> "${toJS(exp.source)} == true"
            is IrOp.UaExp.Neg -> "-${toJS(exp.source)}"
            is IrOp.UaExp.Not -> "~${toJS(exp.source)}"
            is IrOp.UaExp.ToNumber -> "ToNumber(${toJS(exp.source)})"
            is IrOp.UaExp.ToNumeric -> "ToNumeric(${toJS(exp.source)})"
            is IrOp.UaExp.TypeOf -> "typeof(${toJS(exp.source)})"
        }
    }

    /**
     * [CodeSegment.IfPattern]的条件跳转意味着如果跳转，则***不执行***body，因此在转换成js代码时，需要对条件判断的表达式取反
     */
    private fun FunctionDecompilerContext.oppositeJS(exp:IrOp.Expression):String{
        return when(exp){
            is IrOp.UaExp.IsTrue -> toJS(IrOp.UaExp.IsFalse(exp.source))
            is IrOp.UaExp.IsFalse -> toJS(IrOp.UaExp.IsTrue(exp.source))
            is IrOp.BiExp.Eq -> toJS(IrOp.BiExp.NEq(exp.l,exp.r))
            is IrOp.BiExp.NEq -> toJS(IrOp.BiExp.Eq(exp.l,exp.r))
            is IrOp.BiExp.StrictEq -> toJS(IrOp.BiExp.StrictNEq(exp.l,exp.r))
            is IrOp.BiExp.StrictNEq -> toJS(IrOp.BiExp.StrictEq(exp.l,exp.r))
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
                    optimize(linear.map { it.irOp })
                } else {
                    linear.map { it.irOp }
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
            is CodeSegment.Return -> "  ".repeat(indent) + toJS(linear.item.irOp) + "\n"
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

    private class FunctionDecompilerContext(val enableOptimize: Boolean){
        val imports:MutableList<ModuleLiteralArray.RegularImport> = mutableListOf()
        val nsImports:MutableList<ModuleLiteralArray.NamespaceImport> = mutableListOf()

        fun optimize(linear: Sequence<IrOp>): Sequence<IrOp> {
            val iterator = linear.iterator()
            return sequence {
                val queue = mutableListOf<IrOp>()
                while (iterator.hasNext()){
                    queue.add(iterator.next())
                    var optimized = false
                    do {
                        optimized = trySimplify3Assign(queue,iterator)
                    } while (optimized)
                    yield(queue.removeAt(0))
                }
                yieldAll(queue)
            }
        }

        sealed class OptimizableCase{
            abstract fun judge(op: IrOp):Boolean

            object AssignToAcc:OptimizableCase(){
                override fun judge(op: IrOp): Boolean {
                    return op is IrOp.Assign && op.assignLeftAcc
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
         * @param buf Ir片段，至少包含line1 line2 line3
         * @param iter 字节码迭代器，其[Iterator.next]返回应为line2对应的字节码对象
         */
        fun trySimplify3Assign(
            buf: MutableList<IrOp>,
            iter: Iterator<IrOp>,
        ): Boolean{
            while (buf.size < 3 && iter.hasNext()){
                buf.add(iter.next())
            }
            if(buf.size < 3){
                return false
            }
            if(
                buf[0].assignLeftAcc && !buf[0].assignRightContainsAcc &&
                buf[1].assignRightAcc && !buf[1].assignLeftContainsAcc &&
                buf[2].assignLeftAcc && !buf[2].assignRightContainsAcc){
                val firstOp = buf.removeAt(0) as IrOp.Assign
                buf[0] = (buf[0] as IrOp.Assign).replaceRight(firstOp.right)
                return true
            } else {
                return false
            }
        }
    }
}