package me.yricky.oh.utils

import me.yricky.oh.common.DataAndNextOff
import me.yricky.oh.common.LEByteBuf

object MUtf8 {
    fun getMUtf8String(bytes: LEByteBuf, offset:Int, utf16Length:Int): DataAndNextOff<String> {
        val sb = StringBuilder(utf16Length)
        var at = offset
        repeat(utf16Length){
            val v0: Int = bytes.get(at).toInt() and 255
            val out: Char
            val v1: Int
            val v2: Int
            when (v0 shr 4) {
                0, 1, 2, 3, 4, 5, 6, 7 -> {
                    if (v0 == 0) {
                        throwBadUtf8(v0, at)
                    }

                    out = v0.toChar()
                    ++at
                }

                8, 9, 10, 11 -> throwBadUtf8(v0, at)
                12, 13 -> {
                    v1 = bytes.get(at + 1).toInt() and 255
                    if ((v1 and 192) != 128) {
                        throwBadUtf8(v1, at + 1)
                    }

                    v2 = (v0 and 31) shl 6 or (v1 and 63)
                    if (v2 != 0 && v2 < 128) {
                        throwBadUtf8(v1, at + 1)
                    }

                    out = v2.toChar()
                    at += 2
                }

                14 -> {
                    v1 = bytes.get(at + 1).toInt() and 255
                    if ((v1 and 192) != 128) {
                        throwBadUtf8(v1, at + 1)
                    }

                    v2 = bytes.get(at + 2).toInt() and 255
                    if ((v2 and 192) != 128) {
                        throwBadUtf8(v2, at + 2)
                    }

                    val value = (v0 and 15) shl 12 or ((v1 and 63) shl 6) or (v2 and 63)
                    if (value < 2048) {
                        throwBadUtf8(v2, at + 2)
                    }

                    out = value.toChar()
                    at += 3
                }

                else -> throwBadUtf8(v0, at)
            }
            sb.append(out)

        }
        return Pair(sb.toString(),at + 1)
    }

    private fun throwBadUtf8(value: Int, offset: Int):Nothing = throw IllegalArgumentException(
        "bad utf-8 byte $value at offset $offset"
    )
}