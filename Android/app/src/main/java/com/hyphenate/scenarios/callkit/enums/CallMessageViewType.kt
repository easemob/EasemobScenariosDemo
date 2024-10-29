package com.hyphenate.scenarios.callkit.enums

enum class CallMessageViewType(val value: Int) {
    VIEW_TYPE_CALL_MESSAGE_TXT(0),
    VIEW_TYPE_CALL_MESSAGE_GIFT(1),
    VIEW_TYPE_MESSAGE_UNKNOWN_OTHER(99);

    companion object {
        fun from(value: Int): CallMessageViewType {
            val types = CallMessageViewType.values()
            val length = types.size
            for (i in 0 until length) {
                val type = types[i]
                if (type.value == value) {
                    return type
                }
            }
            return VIEW_TYPE_MESSAGE_UNKNOWN_OTHER
        }
    }
}