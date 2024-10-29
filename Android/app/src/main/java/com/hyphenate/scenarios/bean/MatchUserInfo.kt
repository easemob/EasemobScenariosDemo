package com.hyphenate.scenarios.bean

import com.hyphenate.easeui.model.EaseProfile

data class MatchUserInfo(
    val matchedChatUser: String, // 配到的用户名对应的环信用户
    var agoraUid: String? = null,
    var channelName: String? = null,
    var rtcToken: String? = null,
    var matchedUser: String? = null, // 匹配到的用户名（手机号）
) : EaseProfile(id = matchedChatUser){

}