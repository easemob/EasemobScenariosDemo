package com.hyphenate.scenarios.callkit

import com.hyphenate.easeui.EaseIM
import com.hyphenate.easeui.common.extensions.catchChatException
import com.hyphenate.easeui.common.impl.OnError
import com.hyphenate.easeui.common.impl.OnValueSuccess
import com.hyphenate.scenarios.DemoHelper
import com.hyphenate.scenarios.bean.MatchUserInfo
import com.hyphenate.scenarios.bean.RtcTokenResult
import com.hyphenate.scenarios.callkit.extensions.parseToMatchInfo
import com.hyphenate.scenarios.common.room.entity.parse
import com.hyphenate.scenarios.repository.ProfileInfoRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch

object CallRtcTokenManager {

    fun synchronizeRtcToken(conversationId: String,onSuccess: OnValueSuccess<RtcTokenResult>, onError: OnError) {

        CoroutineScope(Dispatchers.IO).launch {
            val phone = DemoHelper.getInstance().dataModel.getPhoneNumber()
            val current = EaseIM.getCurrentUser()?.id
            val channel = (current+conversationId).lowercase().toCharArray().sorted().joinToString("")
            val match = DemoHelper.getInstance().dataModel.getUser(conversationId)?.parse()?.parseToMatchInfo()?: MatchUserInfo(conversationId)
            EM1v1CallKitManager.channelName = channel
            EM1v1CallKitManager.otherMatchInfo = match
            if (phone.isNotEmpty() && channel.isNotEmpty()){
                flow{
                    emit(ProfileInfoRepository().synchronizeRtcToken(channel,phone))
                }
                    .catchChatException { onError.invoke(it.errorCode, it.message) }
                    .collect {
                        onSuccess.invoke(it)
                    }
            }
        }
    }
}