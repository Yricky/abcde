package me.yricky.oh.abcd.decompiler.behaviour

import me.yricky.oh.abcd.cfm.MethodItem
import me.yricky.oh.abcd.isa.Asm
import me.yricky.oh.abcd.literal.LiteralArray

sealed interface JSValue {
    object Hole: JSValue

    object Undefined: JSValue
    object Null: JSValue
    object True: JSValue
    object False: JSValue

    object Nan: JSValue
    object Infinity: JSValue

    class Number(val value: kotlin.Number): JSValue
    class BigInt(val value: String): JSValue
    class ObjInst(val content: Map<String, JSValue>): JSValue
    class ArrInst(val content: List<JSValue>): JSValue
    class Str(val value: String): JSValue

    class Function(val method: MethodItem,val argCounts:Int? = null): JSValue
    class ClassObj(
        val constructor: Function,
        val fields: LiteralArray,
        val parent: JSValue
    ): JSValue

    class Error(): JSValue

    sealed interface Symbol: JSValue {
        object SymbolObj: JSValue
        object Iterator: Symbol
    }


    companion object{
        const val PROTO = "__proto__"

        fun asObj(asm: Asm, literalArray: LiteralArray):ObjInst {
            val objKv = mutableMapOf<String,JSValue>()
            val iter = literalArray.content.iterator()
            while (iter.hasNext()){
                val next = iter.next()
                if(next is LiteralArray.Literal.Str){
                    objKv[next.get(asm.code.abc)] = asJSValue(asm,iter.next())
                } else break
            }
            return ObjInst(objKv)
        }

        fun asArr(asm: Asm, literalArray: LiteralArray):ArrInst {
            return ArrInst(literalArray.content.map { asJSValue(asm,it) })
        }

        private fun asJSValue(asm:Asm, literal: LiteralArray.Literal):JSValue {
            return when(literal){
                is LiteralArray.Literal.Accessor -> TODO()
                is LiteralArray.Literal.Bool -> if(literal.value == 0x00.toByte()) False else True
                is LiteralArray.Literal.BuiltinTypeIndex -> TODO()
                is LiteralArray.Literal.F32 -> Number(literal.value)
                is LiteralArray.Literal.F64 -> Number(literal.value)
                is LiteralArray.Literal.I32 -> Number(literal.value)
                is LiteralArray.Literal.LiteralBufferIndex -> TODO()
                is LiteralArray.Literal.MethodAffiliate -> TODO()
                is LiteralArray.Literal.NullValue -> Null
                is LiteralArray.Literal.TagValue -> TODO()
                is LiteralArray.Literal.ArrayF32 -> TODO()
                is LiteralArray.Literal.ArrayF64 -> TODO()
                is LiteralArray.Literal.ArrayI16 -> TODO()
                is LiteralArray.Literal.ArrayI32 -> TODO()
                is LiteralArray.Literal.ArrayI64 -> TODO()
                is LiteralArray.Literal.ArrayI8 -> TODO()
                is LiteralArray.Literal.ArrayStr -> ArrInst(literal.get(asm.code.abc).map { Str(it) })
                is LiteralArray.Literal.ArrayU1 -> TODO()
                is LiteralArray.Literal.ArrayU16 -> TODO()
                is LiteralArray.Literal.ArrayU32 -> TODO()
                is LiteralArray.Literal.ArrayU64 -> TODO()
                is LiteralArray.Literal.ArrayU8 -> TODO()
                is LiteralArray.Literal.LiteralArr -> TODO()
                is LiteralArray.Literal.LiteralMethod -> Function(literal.get(asm.code.abc))
//                is LiteralArray.Literal.AsyncGeneratorMethod -> TODO()
//                is LiteralArray.Literal.GeneratorMethod -> TODO()
//                is LiteralArray.Literal.Getter -> TODO()
//                is LiteralArray.Literal.Method -> TODO()
//                is LiteralArray.Literal.Setter -> TODO()
                is LiteralArray.Literal.Str -> Str(literal.get(asm.code.abc))
            }
        }
    }
}