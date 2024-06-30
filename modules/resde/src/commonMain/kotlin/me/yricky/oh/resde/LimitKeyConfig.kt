package me.yricky.oh.resde

class LimitKeyConfig(
    idSetOffset:Int,
    keyCount:Int,
    data:List<KeyParam>
) {

    @JvmInline
    value class KeyParam(private val raw:Long){
        val keyType get() = (raw and 0xffffffffL).toInt()
        val value get() = raw.ushr(32).toInt()
    }

    companion object{
        const val TAG = 0x5359454B // "KEYS"
    }
}