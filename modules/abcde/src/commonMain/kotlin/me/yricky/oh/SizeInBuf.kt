package me.yricky.oh


/**
 * 表示在字节序列中一个数据结构的总大小由两部分组成：
 * - **自身直接存储**的部分（[Intrinsic]）
 * - **通过指针间接引用**且**唯一对应**的外部数据部分（[External]）
 *
 * ### 以结构 A 为例说明：
 * 假设结构 A 包含：
 * ```
 * 字段1: 4 字节
 * 字段2: 8 字节
 * 指针 : 8 字节（指向唯一的 B 结构）
 * ```
 *
 * 结构 B 包含：
 * ```
 * 字段a: 4 字节
 * 字段b: (a as uint32) 字节（长度由字段a的值动态决定）
 * 指针 : 8 字节（指向唯一的 C 结构）
 * ```
 *
 * 结构 C 包含：
 * ```
 * 字段c: 8 字节
 * ```
 *
 * #### 各部分大小解析：
 * 1. **intrinsicSize（自身直接存储部分）**:
 *    - 结构 A: `4（字段1） + 8（字段2） + 8（指针） = 20 字节`
 *      （指针本身占用 8 字节，属于 A 的直接存储）
 *    - 结构 B: `4（字段a） + N（字段b, N=a的值） + 8（指针）`
 *      （字段b的长度由字段a动态决定，属于 B 的直接存储）
 *    - 结构 C: `8（字段c） = 8 字节`
 *
 * 2. **externalSize（指针指向的唯一外部数据）**:
 *    - 结构 A: `B 结构的总大小（含B的intrinsic+external）`
 *      （A 的指针指向 B，且 B 唯一属于 A）
 *    - 结构 B: `C 结构的总大小（含C的intrinsic+external）`
 *      （B 的指针指向 C，且 C 唯一属于 B）
 *    - 结构 C: `0`（无外部引用）
 *
 * #### 递归计算示例：
 * 假设字段 `a = 5`（即字段b占5字节）：
 * ```
 * C总大小 = C.intrinsicSize + C.externalSize = 8 + 0 = 8
 * B总大小 = B.intrinsicSize + B.externalSize = (4+5+8) + 8 = 25
 * A总大小 = A.intrinsicSize + A.externalSize = 20 + 25 = 45
 * ```
 *
 * 关键原则：
 * - `intrinsicSize` 始终包含**当前结构直接定义的字段**（含指针本身）
 * - `externalSize` 递归包含**指针指向的唯一子结构**的总大小
 * - 总大小 = `intrinsicSize + externalSize`
 *
 * 当然以上是理想状态，实际场景可能会更复杂。例如对于A来说，某处可能会有一个唯一数组中存放了A的指针，
 * 那么为方便统计体积，这个数组中指向A的那一项的占用也被计算在[External.externalSize]内。
 */
sealed interface SizeInBuf {

    interface Intrinsic{
        /**
         * 表示这个结构在原始字节序列中自身的大小
         */
        val intrinsicSize: Int
    }

    /**
     * 如果这个结构中包含指向字节序列中其他位置的指针，且这种指针指向的内容与这个结构***唯一对应***，
     * 那么按理来说，想统计这一结构的实际体积时，应该将结构中指针指向的位置的体积一并计算。
     *
     * 如果类实现了这个接口，那么表示这个结构包含这种外部体积，此时这个接口应该获取到结构外部体积的大小。
     *
     * 注意：递归计算，性能可能较差
     */
    interface External{
        val externalSize: Int
    }
}

@JvmInline
value class OffsetRange(val inner: Long) {
    val start get() = (inner shr 32).toInt()
    val endExclusive get() = (inner and 0xffffffffL).toInt()
    val len:Int get() = endExclusive - start

    constructor(start: Int, endExclusive: Int) : this((start.toLong() shl 32) or endExclusive.toLong())

    override fun toString(): String {
        return "[${start.toString(16)},${endExclusive.toString(16)}) len:${endExclusive - start}"
    }

    companion object{
        fun from(offset:Int,len:Int) = OffsetRange(offset, offset + len)
    }
}

interface BaseOccupyRange: SizeInBuf.Intrinsic {
    fun range(): OffsetRange
    override val intrinsicSize: Int get() = range().len
}
