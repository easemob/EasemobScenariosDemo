package com.hyphenate.scenarios.callkit.interfaces

import com.hyphenate.scenarios.bean.GiftEntityProtocol

interface OnGiftMessageListener {
    fun onReceiveGiftMsg(gift:GiftEntityProtocol?)
}