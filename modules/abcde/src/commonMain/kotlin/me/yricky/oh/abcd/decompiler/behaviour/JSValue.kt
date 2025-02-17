package me.yricky.oh.abcd.decompiler.behaviour

import me.yricky.oh.abcd.cfm.MethodItem
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
    sealed class ObjInst: JSValue {
        class Clear(val content: Map<String, JSValue>): ObjInst()
        class Literal(val content: LiteralArray): ArrInst()
    }
    sealed class ArrInst: JSValue {
        class Clear(val content: List<JSValue>): ArrInst()
        class Literal(val content: LiteralArray): ArrInst()
    }
    class Str(val value: String): JSValue

    class Function(val method: MethodItem,val argCounts:Int): JSValue
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
    }
}