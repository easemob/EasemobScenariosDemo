package com.hyphenate.scenarios.callkit


enum class EM1v1CallKitEndReason(var value:String) {
    NORMALEND("normal"),
    CANCELEND("cancel"),
    REFUSEEND("refuse"),
    TIMEOUTEND("timeout"),
    BUSYEND("busy"),
    RTCERROR("rtcError");

    companion object {
        fun from(value: String): EM1v1CallKitEndReason {
            val types = EM1v1CallKitEndReason.values()
            val length = types.size
            for (i in 0 until length) {
                val type = types[i]
                if (type.value == value) {
                    return type
                }
            }
            return NORMALEND
        }
    }

}