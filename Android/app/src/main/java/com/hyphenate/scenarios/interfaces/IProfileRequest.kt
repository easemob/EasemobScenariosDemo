package com.hyphenate.scenarios.interfaces

import com.hyphenate.easeui.common.ChatUserInfoType
import com.hyphenate.scenarios.bean.RtcTokenResult
import kotlinx.coroutines.flow.Flow

interface IProfileRequest {

    fun uploadAvatar(filePath: String?)

    fun updateUserNickName(nickname:String)

    fun synchronizeProfile(isSyncFromServer:Boolean = false)

    fun synchronizeRtcToken(conversationId:String): Flow<RtcTokenResult>?

    /**
     * Fetch the user info attribute.
     */
    fun fetchUserInfoAttribute(userIds: List<String>, attributes: List<ChatUserInfoType>)
}