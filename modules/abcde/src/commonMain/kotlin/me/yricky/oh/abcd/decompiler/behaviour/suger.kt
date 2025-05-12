package me.yricky.oh.abcd.decompiler.behaviour

import me.yricky.oh.abcd.isa.Inst.Companion.toUnsignedInt

fun Operation.Expression.st2Acc(): Operation = Operation.AssignReg(FunSimCtx.RegId.ACC, this)
fun JSValue.just(): Operation.Expression = Operation.JustImm(this)
fun FunSimCtx.RegId.ld(): Operation.Expression = Operation.LoadReg(this)
fun regId(opUnit: Number): FunSimCtx.RegId = FunSimCtx.RegId.regId(opUnit.toUnsignedInt())

val Operation.assignLeftAcc: Boolean get() = (this is Operation.Assign) && leftReg == FunSimCtx.RegId.ACC
val Operation.assignLeftContainsAcc: Boolean get() = assignLeftAcc ||
        ((this as? Operation.AssignObj)?.left?.effected()?.contains(FunSimCtx.RegId.ACC) == true) ||
        ((this as? Operation.AssignObj)?.left?.read()?.contains(FunSimCtx.RegId.ACC) == true)
val Operation.assignRightAcc: Boolean get() = (this is Operation.Assign) && (right as? Operation.LoadReg)?.regId == FunSimCtx.RegId.ACC
val Operation.assignRightContainsAcc: Boolean get() = (this is Operation.Assign) &&
        (right.effected().contains(FunSimCtx.RegId.ACC) || right.read().contains(FunSimCtx.RegId.ACC))
fun <T> Iterator<T>.nextOrNull():T? = if (hasNext()) next() else null