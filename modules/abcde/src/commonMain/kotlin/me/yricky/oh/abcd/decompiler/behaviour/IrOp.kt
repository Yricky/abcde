package me.yricky.oh.abcd.decompiler.behaviour

import me.yricky.oh.abcd.isa.Asm.AsmItem
import me.yricky.oh.abcd.isa.Inst.Companion.toUnsignedInt
import me.yricky.oh.abcd.isa.InstFmt
import me.yricky.oh.abcd.literal.LiteralArray
import me.yricky.oh.abcd.literal.ModuleLiteralArray
import me.yricky.oh.abcd.literal.OhmUrl

sealed interface IrOp {
    companion object {
        fun from(item: AsmItem): IrOp {
            val opCode = item.ins.opCode
            item.prefix?.let { prefix ->
                return when(prefix){
                    0xfb.toByte() -> when(opCode){
                        0x01.toByte() -> AssignObj(ObjField.Value(regId(item.opUnits[4]), regId(item.opUnits[3])), LoadReg.acc)
                        0x02.toByte() -> AssignObj(ObjField.Index(regId(item.opUnits[4]),item.opUnits[3].toUnsignedInt()), LoadReg.acc)

                        0x09.toByte() -> LoadExternalModule(item.asm.code.method.clazz!!.moduleInfo!!.regularImports[item.opUnits[2].toUnsignedInt()]).st2Acc()

                        0x13.toByte() -> UaExp.IsTrue(LoadReg.acc).st2Acc()
                        0x14.toByte() -> UaExp.IsFalse(LoadReg.acc).st2Acc()
                        else -> UnImplemented(item)
                    }
                    0xfc.toByte() -> Deprecated
                    0xfd.toByte() -> when(opCode){
                        0x0a.toByte() -> AssignObj(ObjField.Index(regId(item.opUnits[2]), item.opUnits[3].toUnsignedInt()), LoadReg.acc)
                        0x11.toByte() -> LoadExternalModule(item.asm.code.method.clazz!!.moduleInfo!!.regularImports[item.opUnits[2].toUnsignedInt()]).st2Acc()
                        else -> UnImplemented(item)
                    }
                    0xfe.toByte() -> when(opCode){
                        0x00.toByte() -> Throw.Acc
                        0x01.toByte() -> Throw.Error("notexists")
                        0x02.toByte() -> Throw.Error("patternnoncoercible")
                        0x03.toByte() -> Throw.Error("deletesuperproperty")
                        0x04.toByte() -> Throw.Error("constassignment", "${item.ins.format[2]}")

                        0x09.toByte() -> JustAnno("acc: ${(item.ins.format[2] as InstFmt.SId).getString(item)}")
                        else -> UnImplemented(item)
                    }
                    else -> UnImplemented(item)
                }
            }

            return when(opCode){
                0x00.toByte() -> JSValue.Undefined.just().st2Acc()
                0x01.toByte() -> JSValue.Null.just().st2Acc()
                0x02.toByte() -> JSValue.True.just().st2Acc()
                0x03.toByte() -> JSValue.False.just().st2Acc()
                0x04.toByte() -> JSValue.ObjInst(emptyMap()).just().st2Acc()
                0x05.toByte() -> JSValue.ArrInst(emptyList()).just().st2Acc()
                0x06.toByte() -> JSValue.asArr(item.asm,(item.ins.format[2] as InstFmt.LId).getLA(item)).just().st2Acc()
                0x07.toByte() -> JSValue.asObj(item.asm,(item.ins.format[2] as InstFmt.LId).getLA(item)).just().st2Acc()
                0x08.toByte() -> {
                    val clazz = item.opUnits[3].toUnsignedInt()
                    val argC = item.opUnits[2].toUnsignedInt()
                    val args = if(argC.toUnsignedInt() > 1) ((clazz + 1) until (clazz + argC)) else emptyList()
                    AssignReg(FunSimCtx.RegId.ACC, NewInst(regId(clazz), args.map { regId(it) }))
                }
                0x09.toByte() -> NewLex(item.opUnits[1].toUnsignedInt())
                0x0a.toByte() -> BiExp.Add(regId(item.opUnits[2]).ld(), LoadReg.acc).st2Acc()
                0x0b.toByte() -> BiExp.Sub(regId(item.opUnits[2]).ld(), LoadReg.acc).st2Acc()
                0x0c.toByte() -> BiExp.Mul(regId(item.opUnits[2]).ld(), LoadReg.acc).st2Acc()
                0x0d.toByte() -> BiExp.Div(regId(item.opUnits[2]).ld(), LoadReg.acc).st2Acc()
                0x0e.toByte() -> BiExp.Mod(regId(item.opUnits[2]).ld(), LoadReg.acc).st2Acc()
                0x0f.toByte() -> BiExp.Eq(regId(item.opUnits[2]).ld(), LoadReg.acc).st2Acc()
                0x10.toByte() -> BiExp.NEq(regId(item.opUnits[2]).ld(), LoadReg.acc).st2Acc()
                0x11.toByte() -> BiExp.Less(regId(item.opUnits[2]).ld(), LoadReg.acc).st2Acc()
                0x12.toByte() -> BiExp.LEq(regId(item.opUnits[2]).ld(), LoadReg.acc).st2Acc()
                0x13.toByte() -> BiExp.Ge(regId(item.opUnits[2]).ld(), LoadReg.acc).st2Acc()
                0x14.toByte() -> BiExp.GEq(regId(item.opUnits[2]).ld(), LoadReg.acc).st2Acc()
                0x15.toByte() -> BiExp.Shl(regId(item.opUnits[2]).ld(), LoadReg.acc).st2Acc()
                0x16.toByte() -> BiExp.Shr(regId(item.opUnits[2]).ld(), LoadReg.acc).st2Acc()
                0x17.toByte() -> BiExp.AShr(regId(item.opUnits[2]).ld(), LoadReg.acc).st2Acc()
                0x18.toByte() -> BiExp.And(regId(item.opUnits[2]).ld(), LoadReg.acc).st2Acc()
                0x19.toByte() -> BiExp.Or(regId(item.opUnits[2]).ld(), LoadReg.acc).st2Acc()
                0x1a.toByte() -> BiExp.Xor(regId(item.opUnits[2]).ld(), LoadReg.acc).st2Acc()
                0x1b.toByte() -> BiExp.Exp(regId(item.opUnits[2]).ld(), LoadReg.acc).st2Acc()
                0x1c.toByte() -> UaExp.TypeOf(LoadReg.acc).st2Acc()
                0x1d.toByte() -> UaExp.ToNumber(LoadReg.acc).st2Acc()
                0x1e.toByte() -> UaExp.ToNumeric(LoadReg.acc).st2Acc()
                0x1f.toByte() -> UaExp.Neg(LoadReg.acc).st2Acc()
                0x20.toByte() -> UaExp.Not(LoadReg.acc).st2Acc()
                0x21.toByte() -> UaExp.Inc(LoadReg.acc).st2Acc()
                0x22.toByte() -> UaExp.Dec(LoadReg.acc).st2Acc()
                0x23.toByte() -> UaExp.IsTrue(LoadReg.acc).st2Acc()
                0x24.toByte() -> UaExp.IsFalse(LoadReg.acc).st2Acc()
                0x25.toByte() -> BiExp.IsIn(LoadReg(regId(item.opUnits[2])), LoadReg(FunSimCtx.RegId.ACC)).st2Acc()
                0x26.toByte() -> BiExp.InstOf(LoadReg(regId(item.opUnits[2])), LoadReg(FunSimCtx.RegId.ACC)).st2Acc()
                0x27.toByte() -> BiExp.StrictNEq(LoadReg(FunSimCtx.RegId.ACC), LoadReg(regId(item.opUnits[2]))).st2Acc()
                0x28.toByte() -> BiExp.StrictEq(LoadReg(FunSimCtx.RegId.ACC), LoadReg(regId(item.opUnits[2]))).st2Acc()
                0x29.toByte() -> CallAcc().st2Acc()
                0x2a.toByte() -> CallAcc(listOf(regId(item.opUnits[2]))).st2Acc()
                0x2b.toByte() -> CallAcc(listOf(regId(item.opUnits[2]), regId(item.opUnits[3].toUnsignedInt()))).st2Acc()
                0x2c.toByte() -> CallAcc(listOf(
                    regId(item.opUnits[2]),
                    regId(item.opUnits[3].toUnsignedInt()),
                    regId(item.opUnits[4].toUnsignedInt())
                )).st2Acc()
                0x2d.toByte() -> CallAcc(overrideThis = regId(item.opUnits[2])).st2Acc()
                0x2e.toByte() -> CallAcc(listOf(regId(item.opUnits[3])), regId(item.opUnits[2])).st2Acc()
                0x2f.toByte() -> CallAcc(listOf(
                    regId(item.opUnits[3]),
                    regId(item.opUnits[4].toUnsignedInt())
                ), regId(item.opUnits[2])).st2Acc()
                0x30.toByte() -> CallAcc(listOf(
                    regId(item.opUnits[3]),
                    regId(item.opUnits[4].toUnsignedInt()),
                    regId(item.opUnits[5].toUnsignedInt())
                ), regId(item.opUnits[2])).st2Acc()

                0x33.toByte() -> JSValue.Function(
                    (item.ins.format[2] as InstFmt.MId).getMethod(item),
                    item.opUnits[3].toUnsignedInt()
                ).just().st2Acc()

                0x35.toByte() -> NewClass(
                    JSValue.Function((item.ins.format[2] as InstFmt.MId).getMethod(item), item.opUnits[4].toUnsignedInt()),
                    (item.ins.format[3] as InstFmt.LId).getLA(item),
                    regId(item.opUnits[5])
                ).st2Acc()

                0x37.toByte() -> AssignReg(LoadReg.ACC,ObjField.Value(regId(item.opUnits[2]), LoadReg.ACC))
                0x38.toByte() -> AssignObj(ObjField.Value(regId(item.opUnits[2]), regId(item.opUnits[3])), LoadReg.acc)

                0x3a.toByte() -> AssignReg(LoadReg.ACC, ObjField.Index(LoadReg.ACC,item.opUnits[2].toUnsignedInt()))
                0x3b.toByte() -> AssignObj(ObjField.Index(regId(item.opUnits[2]), item.opUnits[3].toUnsignedInt()), LoadReg.acc)
                0x3c.toByte() -> LoadReg(FunSimCtx.RegId.lexId(item.opUnits[1].toUnsignedInt(),item.opUnits[2].toUnsignedInt())).st2Acc()
                0x3d.toByte() -> AssignReg(FunSimCtx.RegId.lexId(item.opUnits[1].toUnsignedInt(),item.opUnits[2].toUnsignedInt()), LoadReg.acc)
                0x3e.toByte() -> JSValue.Str((item.ins.format[1] as InstFmt.SId).getString(item)).just().st2Acc()
                0x3f.toByte() -> ObjField.Name(FunSimCtx.RegId.GLOBAL, (item.ins.format[2] as InstFmt.SId).getString(item)).st2Acc()
                0x40.toByte() -> AssignObj(ObjField.Name(FunSimCtx.RegId.GLOBAL, (item.ins.format[2] as InstFmt.SId).getString(item)),LoadReg.acc)
                0x41.toByte() -> ObjField.Name(FunSimCtx.RegId.GLOBAL, (item.ins.format[2] as InstFmt.SId).getString(item)).st2Acc()
                0x42.toByte() -> ObjField.Name(LoadReg.ACC, (item.ins.format[2] as InstFmt.SId).getString(item)).st2Acc()
                0x43.toByte() -> AssignObj(ObjField.Name(regId(item.opUnits[3]),(item.ins.format[2] as InstFmt.SId).getString(item)), LoadReg.acc)
                0x44.toByte() -> AssignReg(regId(item.opUnits[1]), regId(item.opUnits[2]).ld())
                0x45.toByte() -> AssignReg(regId(item.opUnits[1]), regId(item.opUnits[2]).ld())

                0x49.toByte() -> ObjField.Name(
                    FunSimCtx.RegId.THIS,
                    (item.ins.format[2] as InstFmt.SId).getString(item)
                ).st2Acc()
                0x4a.toByte() -> AssignObj(ObjField.Name(regId(item.opUnits[3]),(item.ins.format[2] as InstFmt.SId).getString(item)), FunSimCtx.RegId.THIS.ld())
                0x4b.toByte() -> ObjField.Value(FunSimCtx.RegId.THIS, LoadReg.ACC).st2Acc()
                0x4c.toByte() -> AssignObj(ObjField.Value(FunSimCtx.RegId.THIS, regId(item.opUnits[3])), LoadReg.acc)
                0x4d.toByte() -> Jump(item.opUnits[1].toInt())
                0x4e.toByte() -> Jump(item.opUnits[1].toInt())
                0x4f.toByte() -> JumpIf(item.opUnits[1].toInt(), BiExp.Eq(LoadReg.acc, JSValue.Number(0).just()))
                0x50.toByte() -> JumpIf(item.opUnits[1].toInt(), BiExp.Eq(LoadReg.acc, JSValue.Number(0).just()))
                0x51.toByte() -> JumpIf(item.opUnits[1].toInt(), BiExp.NEq(LoadReg.acc, JSValue.Number(0).just()))
                in (0x52.toByte() .. 0x5f.toByte()) -> Disabled
                0x60.toByte() -> AssignReg(LoadReg.ACC, regId(item.opUnits[1]).ld())
                0x61.toByte() -> AssignReg(regId(item.opUnits[1]), LoadReg.acc)
                0x62.toByte() -> AssignReg(LoadReg.ACC, JustImm(JSValue.Number(item.opUnits[1])))
                0x63.toByte() -> AssignReg(LoadReg.ACC, JustImm(JSValue.Number(Double.fromBits(item.opUnits[1] as Long))))
                0x64.toByte() -> Return.ReturnAcc
                0x65.toByte() -> Return.ReturnUndefined

                0x6a.toByte() -> JSValue.Nan.just().st2Acc()
                0x6b.toByte() -> JSValue.Infinity.just().st2Acc()
                0x6c.toByte() -> LoadReg(FunSimCtx.RegId.ARGUMENTS).st2Acc()
                0x6d.toByte() -> LoadReg(FunSimCtx.RegId.GLOBAL).st2Acc()
                0x6e.toByte() -> Disabled
                0x6f.toByte() -> LoadReg(FunSimCtx.RegId.THIS).st2Acc()
                0x70.toByte() -> JSValue.Hole.just().st2Acc()

                0x74.toByte() -> JSValue.Function(
                    (item.ins.format[2] as InstFmt.MId).getMethod(item),
                    item.opUnits[3].toUnsignedInt()
                ).just().st2Acc()
                0x75.toByte() -> NewClass(
                    JSValue.Function((item.ins.format[2] as InstFmt.MId).getMethod(item), item.opUnits[4].toUnsignedInt()),
                    (item.ins.format[3] as InstFmt.LId).getLA(item),
                    regId(item.opUnits[5])
                ).st2Acc()
                0x76.toByte() -> UaExp.GetTemplateObject(LoadReg.acc).st2Acc()
                0x77.toByte() -> AssignObj(ObjField.Name(LoadReg.ACC, JSValue.PROTO), regId(item.opUnits[2]).ld())
                0x78.toByte() -> AssignObj(ObjField.Value(regId(item.opUnits[2]), regId(item.opUnits[3])), LoadReg.acc)
                0x79.toByte() -> AssignObj(ObjField.Index(regId(item.opUnits[2]),item.opUnits[3].toUnsignedInt()), LoadReg.acc)
                0x7a.toByte() -> AssignObj(
                    ObjField.Name(regId(item.opUnits[3]),(item.ins.format[2] as InstFmt.SId).getString(item)),
                    LoadReg.acc
                )
                0x7b.toByte() -> GetModuleNamespace(item.asm.code.method.clazz!!.moduleInfo!!.moduleRequests[item.opUnits[1].toUnsignedInt()]).st2Acc()

                0x7e.toByte() -> LoadExternalModule(item.asm.code.method.clazz!!.moduleInfo!!.regularImports[item.opUnits[1].toUnsignedInt()]).st2Acc()

                0x80.toByte() -> JSValue.ArrInst(emptyList()).just().st2Acc()
                0x81.toByte() -> JSValue.asArr(item.asm,(item.ins.format[2] as InstFmt.LId).getLA(item)).just().st2Acc()
                0x82.toByte() -> JSValue.asObj(item.asm,(item.ins.format[2] as InstFmt.LId).getLA(item)).just().st2Acc()
                0x83.toByte() -> {
                    val clazz = item.opUnits[3].toUnsignedInt()
                    val argC = item.opUnits[2].toUnsignedInt()
                    val args = if(argC.toUnsignedInt() > 1) ((clazz + 1) until (clazz + argC)) else emptyList()
                    AssignReg(FunSimCtx.RegId.ACC, NewInst(regId(clazz), args.map { regId(it) }))
                }
                0x84.toByte() -> UaExp.TypeOf(LoadReg.acc).st2Acc()
                0x85.toByte() -> AssignReg(LoadReg.ACC,ObjField.Value(regId(item.opUnits[2]), LoadReg.ACC))
                0x86.toByte() -> AssignObj(ObjField.Value(regId(item.opUnits[2]), regId(item.opUnits[3])), LoadReg.acc)

                0x88.toByte() -> AssignReg(LoadReg.ACC, ObjField.Index(LoadReg.ACC,item.opUnits[2].toUnsignedInt()))
                0x89.toByte() -> AssignObj(ObjField.Index(regId(item.opUnits[2]),item.opUnits[3].toUnsignedInt()),LoadReg.acc)

                0x8c.toByte() -> ObjField.Name(FunSimCtx.RegId.GLOBAL, (item.ins.format[2] as InstFmt.SId).getString(item)).st2Acc()

                0x90.toByte() -> ObjField.Name(LoadReg.ACC, (item.ins.format[2] as InstFmt.SId).getString(item)).st2Acc()
                0x91.toByte() -> AssignObj(ObjField.Name(regId(item.opUnits[3]),(item.ins.format[2] as InstFmt.SId).getString(item)), LoadReg.acc)

                0x8f.toByte() -> AssignReg(regId(item.opUnits[1]), regId(item.opUnits[2]).ld())

                0x98.toByte() -> Jump(item.opUnits[1].toInt())

                0x9a.toByte() -> JumpIf(item.opUnits[1].toInt(), BiExp.Eq(LoadReg.acc, JSValue.Number(0).just()))
                0x9b.toByte() -> JumpIf(item.opUnits[1].toInt(), BiExp.NEq(LoadReg.acc, JSValue.Number(0).just()))
                0x9c.toByte() -> JumpIf(item.opUnits[1].toInt(), BiExp.NEq(LoadReg.acc, JSValue.Number(0).just()))
                in (0x9d.toByte() .. 0xaa.toByte()) -> Disabled

                0xad.toByte() -> JSValue.Symbol.SymbolObj.just().st2Acc()

                0xb0.toByte() -> Debugger

                0xbd.toByte() -> DynamicImport().st2Acc()

                0xc1.toByte() -> UaExp.GetTemplateObject(LoadReg.acc).st2Acc()
                0xc2.toByte() -> DeleteProp(regId(item.opUnits[1]), FunSimCtx.RegId.ACC)

                0xc7.toByte() -> AssignObj(ObjField.Name(LoadReg.ACC, JSValue.PROTO), regId(item.opUnits[2]).ld())
                0xc8.toByte() -> AssignObj(ObjField.Value(regId(item.opUnits[2]), regId(item.opUnits[3])), LoadReg.acc)

                0xcb.toByte() -> AssignObj(ObjField.Index(regId(item.opUnits[2]),item.opUnits[3].toUnsignedInt()), LoadReg.acc)

                0xd3.toByte() -> JSValue.BigInt((item.ins.format[1] as InstFmt.SId).getString(item)).just().st2Acc()

                0xd5.toByte() -> NOP

                0xdb.toByte() -> AssignObj(
                    ObjField.Name(regId(item.opUnits[3]),(item.ins.format[2] as InstFmt.SId).getString(item)),
                    LoadReg.acc
                )
                0xdc.toByte() -> AssignObj(
                    ObjField.Name(regId(item.opUnits[3]),(item.ins.format[2] as InstFmt.SId).getString(item)),
                    LoadReg.acc
                )

                else -> UnImplemented(item)
            }
        }
    }

    class UnImplemented(val item: AsmItem): IrOp

    sealed interface TraitNOP: IrOp
    class JustAnno(val anno: String): TraitNOP

    object NOP: TraitNOP
    object Debugger: TraitNOP
    object Disabled: IrOp //指令功能未使能，暂不可用。
    object Deprecated: IrOp
    class NewLex(val size:Int): IrOp


    sealed interface Statement: IrOp, FunSimCtx.Effect

    sealed interface Assign:Statement{

        //若左值是寄存器位置，则返回对应寄存器位置，否则返回null
        val leftReg:FunSimCtx.RegId?
        val right: Expression
        fun replaceRight(newValue: Expression): Assign
    }
    class AssignReg(val left: FunSimCtx.RegId, override val right: Expression): Assign {
        override fun read(): Sequence<FunSimCtx.RegId> = right.read()
        override fun effected(): Sequence<FunSimCtx.RegId> = right.effected() + left

        override val leftReg: FunSimCtx.RegId get() = left
        override fun replaceRight(newValue: Expression): Assign {
            return AssignReg(left, newValue)
        }

    }
    class AssignObj(val left: ObjField, override val right: Expression): Assign {
        override fun read(): Sequence<FunSimCtx.RegId> = right.read() + left.read()
        override fun effected(): Sequence<FunSimCtx.RegId> = right.effected() + left.obj

        override val leftReg: FunSimCtx.RegId? get() = null
        override fun replaceRight(newValue: Expression): Assign {
            return AssignObj(left, newValue)
        }
    }
    class DeleteProp(val obj: FunSimCtx.RegId, val prop: FunSimCtx.RegId): Statement{
        override fun effected(): Sequence<FunSimCtx.RegId> = sequenceOf(obj)
        override fun read(): Sequence<FunSimCtx.RegId> = sequenceOf(obj,prop)
    }

    class Jump(val offset: Int): Statement
    class JumpIf(val offset: Int,val condition: Expression): Statement{
        override fun read(): Sequence<FunSimCtx.RegId> = condition.read()
        override fun effected(): Sequence<FunSimCtx.RegId> = condition.effected()
    }
    class Return private constructor(val hasValue: Boolean): Statement{
        companion object{
            val ReturnUndefined = Return(false)
            val ReturnAcc = Return(true)
        }
    }
    sealed interface Throw: Statement {
        object Acc: Throw
        class Error(val type: String,val msg: String = ""): Throw
    }

    sealed interface Expression: FunSimCtx.Effect
    sealed interface NoRegExpression: Expression
    class JustImm(val value: JSValue): NoRegExpression
    class LoadReg(val regId: FunSimCtx.RegId): Expression {
        override fun read(): Sequence<FunSimCtx.RegId> = sequenceOf(regId)
        companion object{
            val ACC = FunSimCtx.RegId.ACC
            val acc = FunSimCtx.RegId.ACC.ld()
        }
    }
    class DynamicImport(val regId: FunSimCtx.RegId = LoadReg.ACC): Expression {
        override fun read(): Sequence<FunSimCtx.RegId> = sequenceOf(regId)
    }
    class NewClass(
        val constructor: JSValue.Function,
        val fields: LiteralArray,
        val parent: FunSimCtx.RegId
    ): Expression {
        override fun read(): Sequence<FunSimCtx.RegId> = sequenceOf(parent)
    }
    class NewInst(val clazz: FunSimCtx.RegId, val constructorArgs: List<FunSimCtx.RegId>): Expression {
        override fun read(): Sequence<FunSimCtx.RegId> {
            return constructorArgs.asSequence() + clazz
        }
    }
    class CallAcc(val args: List<FunSimCtx.RegId> = emptyList(), val overrideThis: FunSimCtx.RegId? = null):
        Expression {
        override fun read(): Sequence<FunSimCtx.RegId> = args.asSequence() + FunSimCtx.RegId.ACC
        override fun effected(): Sequence<FunSimCtx.RegId> = sequenceOf(FunSimCtx.RegId.ACC)
    }
    class LoadExternalModule(val ext: ModuleLiteralArray.RegularImport): NoRegExpression
    class GetModuleNamespace(val ns: OhmUrl) : NoRegExpression

    /**
     * 一元表达式
     */
    sealed class UaExp(
        val source: Expression
    ): Expression {
        override fun read(): Sequence<FunSimCtx.RegId> = source.read()
        override fun effected(): Sequence<FunSimCtx.RegId> = source.effected()

        class TypeOf(source: Expression) : UaExp(source)
        class ToNumber(source: Expression) : UaExp(source)
        class ToNumeric(source: Expression) : UaExp(source)
        class Neg(source: Expression) : UaExp(source)
        class Not(source: Expression) : UaExp(source)
        class Inc(source: Expression) : UaExp(source)
        class Dec(source: Expression) : UaExp(source)
        class IsTrue(source: Expression) : UaExp(source)
        class IsFalse(source: Expression) : UaExp(source)

        class GetTemplateObject(source: Expression) : UaExp(source)

    }

    /**
     * 二元表达式
     */
    sealed class BiExp(
        val l: Expression,
        val r: Expression,
    ): Expression {
        override fun read(): Sequence<FunSimCtx.RegId> = l.read() + r.read()
        override fun effected(): Sequence<FunSimCtx.RegId> = l.effected() + r.effected()
        class Add(l: Expression, r: Expression) : BiExp(l, r)
        class Sub(l: Expression, r: Expression) : BiExp(l, r)
        class Mul(l: Expression, r: Expression) : BiExp(l, r)
        class Div(l: Expression, r: Expression) : BiExp(l, r)
        class Mod(l: Expression, r: Expression) : BiExp(l, r)
        class Eq(l: Expression, r: Expression) : BiExp(l, r)
        class NEq(l: Expression, r: Expression) : BiExp(l, r)
        class Less(l: Expression, r: Expression) : BiExp(l, r)
        class LEq(l: Expression, r: Expression) : BiExp(l, r)
        class Ge(l: Expression, r: Expression) : BiExp(l, r)
        class GEq(l: Expression, r: Expression) : BiExp(l, r)
        class Shl(l: Expression, r: Expression) : BiExp(l, r)
        class Shr(l: Expression, r: Expression) : BiExp(l, r)
        class AShr(l: Expression, r: Expression) : BiExp(l, r)
        class And(l: Expression, r: Expression) : BiExp(l, r)
        class Or(l: Expression, r: Expression) : BiExp(l, r)
        class Xor(l: Expression, r: Expression) : BiExp(l, r)
        class Exp(l: Expression, r: Expression) : BiExp(l, r)

        class IsIn(l: Expression, r: Expression) : BiExp(l, r)
        class InstOf(l: Expression, r: Expression) : BiExp(l, r)
        class StrictNEq(l: Expression, r: Expression) : BiExp(l, r)
        class StrictEq(l: Expression, r: Expression) : BiExp(l, r)
    }

    sealed class ObjField(val obj: FunSimCtx.RegId): Expression {
        override fun read(): Sequence<FunSimCtx.RegId> {
            return sequenceOf(obj)
        }
        class Name(obj: FunSimCtx.RegId, val name: String): ObjField(obj)
        class Index(obj: FunSimCtx.RegId, val index: Int): ObjField(obj)
        class Value(obj: FunSimCtx.RegId, val value: FunSimCtx.RegId): ObjField(obj){
            override fun read(): Sequence<FunSimCtx.RegId> {
                return super.read() + sequenceOf(value)
            }
        }
    }

}