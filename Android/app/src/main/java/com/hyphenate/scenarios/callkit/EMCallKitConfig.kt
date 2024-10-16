package com.hyphenate.scenarios.callkit

import com.hyphenate.easeui.model.EaseProfile

class EMCallKitConfig {
    var defaultHeadImage: String? = null
    var userInfoMap: MutableMap<String, EaseProfile>? = mutableMapOf()
    var RingFile: String? = null
    var agoraAppId: String? = null
    var callTimeOut = (30 * 1000).toLong()
    var enableRTCToken = false

    fun setUserInfoMaps(userMap: Map<String, EaseProfile>?) {
        userInfoMap?.clear()
        if (!userMap.isNullOrEmpty()) {
            val userSet = userMap.keys
            for (userId in userSet) {
                val userInfo: EaseProfile? = userMap[userId]
                if (userInfo != null) {
                    val newUserInfo = EaseProfile(userInfo.getNotEmptyName(), userInfo.avatar)
                    userInfoMap?.set(userId, newUserInfo)
                }
            }
        }
    }

    fun setUserInfo(userName: String, userInfo: EaseProfile?) {
        if (userInfoMap == null) {
            userInfoMap = mutableMapOf()
        }
        userInfo?.let {
            userInfoMap?.set(userName,userInfo)
        }

    }
}