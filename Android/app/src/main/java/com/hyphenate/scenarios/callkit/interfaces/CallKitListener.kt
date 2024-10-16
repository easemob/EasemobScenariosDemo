package com.hyphenate.scenarios.callkit.interfaces

import com.hyphenate.scenarios.callkit.CallStatus
import io.agora.rtc2.RtcEngine

interface CallKitListener {
    // When call status changed.
    // - Parameter status: The status of the ``CallStatus``.
    // - Parameter reason: The reason of the end call .
    fun onCallStatusChanged(status: CallStatus, reason: String){}

    // When rtc token will expired.Need request from server to refresh token.
    fun onCallTokenWillExpire(){}

    fun onJoinChannelSuccess(channel: String?, uid: Int, elapsed: Int){}

}