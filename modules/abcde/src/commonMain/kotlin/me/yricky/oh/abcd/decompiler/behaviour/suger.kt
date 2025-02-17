package me.yricky.oh.abcd.decompiler.behaviour

import me.yricky.oh.abcd.isa.Inst.Companion.toUnsignedInt

fun Operation.Expression.st2Acc(): Operation = Operation.Assign(FunSimCtx.RegId.ACC, this)
fun JSValue.just(): Operation.Expression = Operation.JustImm(this)
fun FunSimCtx.RegId.ld(): Operation.Expression = Operation.LoadReg(this)
fun regId(opUnit: Number): FunSimCtx.RegId = FunSimCtx.RegId.regId(opUnit.toUnsignedInt())