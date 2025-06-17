package me.yricky.oh.abcd.decompiler.behaviour

import me.yricky.oh.abcd.isa.Inst.Companion.toUnsignedInt

fun IrOp.Expression.st2Acc(): IrOp = IrOp.AssignReg(FunSimCtx.RegId.ACC, this)
fun JSValue.just(): IrOp.Expression = IrOp.JustImm(this)
fun FunSimCtx.RegId.ld(): IrOp.Expression = IrOp.LoadReg(this)
fun regId(opUnit: Number): FunSimCtx.RegId = FunSimCtx.RegId.regId(opUnit.toUnsignedInt())

val IrOp.assignLeftAcc: Boolean get() = (this is IrOp.Assign) && leftReg == FunSimCtx.RegId.ACC
val IrOp.assignLeftContainsAcc: Boolean get() = assignLeftAcc ||
        ((this as? IrOp.AssignObj)?.left?.effected()?.contains(FunSimCtx.RegId.ACC) == true) ||
        ((this as? IrOp.AssignObj)?.left?.read()?.contains(FunSimCtx.RegId.ACC) == true)
val IrOp.assignRightAcc: Boolean get() = (this is IrOp.Assign) && (right as? IrOp.LoadReg)?.regId == FunSimCtx.RegId.ACC
val IrOp.assignRightContainsAcc: Boolean get() = (this is IrOp.Assign) &&
        (right.effected().contains(FunSimCtx.RegId.ACC) || right.read().contains(FunSimCtx.RegId.ACC))

fun IrOp.Expression.replaceReg(reg: FunSimCtx.RegId, replaceTo: FunSimCtx.RegId): IrOp.Expression {
    return when (this) {
        is IrOp.NoRegExpression -> this
        is IrOp.CallAcc -> TODO()
        is IrOp.DynamicImport -> if (reg == regId) IrOp.DynamicImport(replaceTo) else this
        is IrOp.LoadReg -> if (reg == regId) IrOp.LoadReg(replaceTo) else this
        is IrOp.NewClass -> if (parent == reg) IrOp.NewClass(constructor, fields, replaceTo) else this
        is IrOp.NewInst -> TODO()
        is IrOp.ObjField.Index -> if (obj == reg) IrOp.ObjField.Index(reg, index) else this
        is IrOp.ObjField.Name -> if (obj == reg) IrOp.ObjField.Name(reg, name) else this
        is IrOp.ObjField.Value -> TODO()
        is IrOp.BiExp.AShr -> TODO()
        is IrOp.BiExp.Add -> TODO()
        is IrOp.BiExp.And -> TODO()
        is IrOp.BiExp.Div -> TODO()
        is IrOp.BiExp.Eq -> TODO()
        is IrOp.BiExp.Exp -> TODO()
        is IrOp.BiExp.GEq -> TODO()
        is IrOp.BiExp.Ge -> TODO()
        is IrOp.BiExp.InstOf -> TODO()
        is IrOp.BiExp.IsIn -> TODO()
        is IrOp.BiExp.LEq -> TODO()
        is IrOp.BiExp.Less -> TODO()
        is IrOp.BiExp.Mod -> TODO()
        is IrOp.BiExp.Mul -> TODO()
        is IrOp.BiExp.NEq -> TODO()
        is IrOp.BiExp.Or -> TODO()
        is IrOp.BiExp.Shl -> TODO()
        is IrOp.BiExp.Shr -> TODO()
        is IrOp.BiExp.StrictEq -> TODO()
        is IrOp.BiExp.StrictNEq -> TODO()
        is IrOp.BiExp.Sub -> TODO()
        is IrOp.BiExp.Xor -> TODO()
        is IrOp.UaExp.Dec -> TODO()
        is IrOp.UaExp.GetTemplateObject -> TODO()
        is IrOp.UaExp.Inc -> TODO()
        is IrOp.UaExp.IsFalse -> TODO()
        is IrOp.UaExp.IsTrue -> TODO()
        is IrOp.UaExp.Neg -> TODO()
        is IrOp.UaExp.Not -> TODO()
        is IrOp.UaExp.ToNumber -> TODO()
        is IrOp.UaExp.ToNumeric -> TODO()
        is IrOp.UaExp.TypeOf -> TODO()
    }
}