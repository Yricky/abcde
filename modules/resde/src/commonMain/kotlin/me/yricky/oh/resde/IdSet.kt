package me.yricky.oh.resde

class IdSet(
    val count:Int,
    val idOffsetMap: List<IdOffset>
) {

    @JvmInline
    value class IdOffset(private val raw:Long){
        val id get() = (raw and 0xffffffffL).toInt()
        val offset get() = raw.ushr(32).toInt()
        override fun toString(): String {
            return "($id,$offset)"
        }
    }
    companion object{
        const val TAG = 0x53534449 // "IDSS"
    }
}