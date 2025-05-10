package me.yricky.oh.abcd.cfm

import me.yricky.oh.abcd.AbcBuf

@JvmInline
value class FieldType(private val offset: Int){
    fun getClass(abc: AbcBuf):AbcClass? = if (isPrimitive) null else (abc.classes[offset] as? AbcClass)

    val isPrimitive:Boolean get() = offset <= 0xb
    val primitiveType:String? get() = PRIMITIVE_NAMES.getOrNull(offset)

    companion object{
        private val PRIMITIVE_NAMES = listOf(
            "u1", "i8", "u8", "i16",
            "u16", "i32", "u32", "f32",
            "f64", "i64", "u64", "any"
        )
        fun fromOffset(offset:Int):FieldType{
            return FieldType(offset)
        }
    }
}

