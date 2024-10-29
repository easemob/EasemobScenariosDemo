package com.hyphenate.scenarios.bean

data class DefaultGifts(
    val tabId: Int,
    val displayName: String,
    val gifts: List<GiftEntityProtocol>
)
