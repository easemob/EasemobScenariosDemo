package com.hyphenate.scenarios.callkit.interfaces

interface CallProtocol {
    /// Start call with a user.
    fun startCall(userId:String,callType:String)

    // End call with a reason.
    // - Parameter reason: The reason of ending call.
    fun endCall(userId:String,reason: String)

    /// Accept call with a user.
    fun acceptCall(userId:String)
}