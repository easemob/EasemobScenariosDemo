package com.hyphenate.scenarios.callkit.helper

import com.hyphenate.easeui.EaseIM
import com.hyphenate.easeui.common.ChatCallback
import com.hyphenate.easeui.common.ChatClient
import com.hyphenate.easeui.common.ChatCmdMessageBody
import com.hyphenate.easeui.common.ChatConversationType
import com.hyphenate.easeui.common.ChatException
import com.hyphenate.easeui.common.ChatLog
import com.hyphenate.easeui.common.ChatMessage
import com.hyphenate.easeui.common.ChatMessageStatus
import com.hyphenate.easeui.common.ChatMessageType
import com.hyphenate.easeui.common.ChatTextMessageBody
import com.hyphenate.easeui.common.ChatType
import com.hyphenate.easeui.common.EaseConstant
import com.hyphenate.scenarios.DemoHelper
import com.hyphenate.scenarios.R
import com.hyphenate.scenarios.bean.MatchUserInfo
import com.hyphenate.scenarios.bean.PresenceData
import com.hyphenate.scenarios.callkit.CallStatus
import com.hyphenate.scenarios.callkit.EM1v1CallKitEndReason
import com.hyphenate.scenarios.callkit.EM1v1CallKitManager
import com.hyphenate.scenarios.callkit.EMCallConstant
import com.hyphenate.scenarios.callkit.extensions.addUserInfo
import com.hyphenate.scenarios.callkit.extensions.parseToMatchInfo
import com.hyphenate.scenarios.callkit.extensions.parseUserInfo
import com.hyphenate.scenarios.common.PresenceCache
import com.hyphenate.scenarios.utils.EasePresenceUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object CallMessageHelper {

    fun sendStartCallMsg(
        conversationId:String,
        callId:String,
        type: String,
        callback:ChatCallback? = null
    ){
        val message = ChatMessage.createSendMessage(ChatMessageType.TXT)
        callback?.let {  message.setMessageStatusCallback(it) }
        DemoHelper.getInstance().context.resources?.let {
            message.setAttribute(EMCallConstant.EM1v1CallKit1v1Invite,EMCallConstant.EM1v1CallKit1v1Invite)
            message.setAttribute(EMCallConstant.CALL_MSG_TYPE,type)
            message.setAttribute(EMCallConstant.EM1v1CallKitCallId,callId)
            message.deliverOnlineOnly(true)
            message.to = conversationId
            message.chatType = ChatType.Chat
            val txtBody = ChatTextMessageBody(it.getString(R.string.em_start_call))
            EaseIM.getCurrentUser()?.let { profile ->
               message.addUserInfo(profile.name, profile.avatar)
            }
            message.addBody(txtBody)
            ChatClient.getInstance().chatManager().sendMessage(message)
        }
    }

    fun sendEndedCallMsg(
        reason: String,
        conversationId:String,
        callId:String,
        callback:ChatCallback? = null
    ){
        val message = ChatMessage.createSendMessage(ChatMessageType.CMD)
        callback?.let {  message.setMessageStatusCallback(it) }
        DemoHelper.getInstance().context.resources?.let {
            message.setAttribute(EMCallConstant.EM1v1CallKit1v1Signaling,EMCallConstant.EM1v1CallKit1v1Signaling)
            message.setAttribute(EMCallConstant.EM1v1EndCallReason,reason)
            message.setAttribute(EMCallConstant.EM1v1CallKitCallId,callId)
            message.deliverOnlineOnly(true)
            message.to = conversationId
            message.chatType = ChatType.Chat
            val cmdBody = ChatCmdMessageBody(EMCallConstant.EM1v1CallKit1v1End)
            EaseIM.getCurrentUser()?.let { profile ->
                message.addUserInfo(profile.name, profile.avatar)
            }
            message.addBody(cmdBody)
            ChatClient.getInstance().chatManager().sendMessage(message)
        }
    }

    fun sendAcceptCallMsg(
        conversationId:String,
        callId:String,
        callback:ChatCallback? = null
    ){
        val message = ChatMessage.createSendMessage(ChatMessageType.CMD)
        callback?.let {  message.setMessageStatusCallback(it) }
        DemoHelper.getInstance().context.resources?.let {
            message.setAttribute(EMCallConstant.EM1v1CallKit1v1Signaling,EMCallConstant.EM1v1CallKit1v1Signaling)
            message.setAttribute(EMCallConstant.EM1v1CallKitCallId,callId)
            message.deliverOnlineOnly(true)
            message.to = conversationId
            message.chatType = ChatType.Chat
            val txtBody = ChatCmdMessageBody(EMCallConstant.EM1v1CallKit1v1Accept)
            EaseIM.getCurrentUser()?.let { profile ->
                message.addUserInfo(profile.name, profile.avatar)
            }
            message.addBody(txtBody)
            ChatClient.getInstance().chatManager().sendMessage(message)
        }
    }

    fun cancelMatchNotify(
        conversationId:String,
        callback:ChatCallback? = null
    ){
        val message = ChatMessage.createSendMessage(ChatMessageType.CMD)
        callback?.let {  message.setMessageStatusCallback(it) }
        DemoHelper.getInstance().context.resources?.let {
            message.deliverOnlineOnly(true)
            message.to = conversationId
            message.chatType = ChatType.Chat
            val txtBody = ChatCmdMessageBody(EMCallConstant.EM1v1SomeUserMatchCanceled)
            EaseIM.getCurrentUser()?.let { profile ->
                message.addUserInfo(profile.name, profile.avatar)
            }
            message.addBody(txtBody)
            ChatClient.getInstance().chatManager().sendMessage(message)
        }
    }

    fun insertEndMessage(
        conversationId: String,
        callId:String
    ){
        if (callId.isNotEmpty()){
            val message = ChatMessage.createSendMessage(ChatMessageType.TXT)
            DemoHelper.getInstance().context.resources?.let {
                message.to = conversationId
                message.chatType = ChatType.Chat
                message.setStatus(ChatMessageStatus.SUCCESS)
                val txtBody = ChatTextMessageBody(it.getString(R.string.em_end_call))
                message.addBody(txtBody)
                ChatClient.getInstance().chatManager().getConversation(
                    conversationId,
                    ChatConversationType.Chat,
                    true
                ).apply {
                    insertMessage(message)
                }
            }
        }
    }

    fun onCmdMsgDidReceive(messages: MutableList<ChatMessage>?){
        parseUserInfo(messages)
        messages?.forEach {
            val body = it.body
            if (it.from == "admin"){
                //配对人的 id
                 if (it.ext().containsKey("matchedChatUser")){
                     val matchedChatUser = it.getStringAttribute("matchedChatUser")
                     if (body is ChatCmdMessageBody && body.action() == EMCallConstant.EM1v1SomeUserMatchedYou){
                         EM1v1CallKitManager.onCallStatusChanged(CallStatus.PREPARING,"Call You")
                         ChatLog.d("CallMessageHelper","admin onCmdMessageReceived CallStatus.PREPARING")
                     }
                     if (body is ChatCmdMessageBody && body.action() == EMCallConstant.EM1v1SomeUserMatchCanceled){
                         val userInfo = EaseIM.getUserProvider()?.getUser(matchedChatUser)
                         val nickName = userInfo?.getNotEmptyName()?:matchedChatUser
                         EM1v1CallKitManager.onCallStatusChanged(CallStatus.IDLE,"${nickName}取消配对")
                         ChatLog.d("CallMessageHelper","admin onCmdMessageReceived CallStatus.IDLE")
                     }
                }
            }else{
                if (body is ChatCmdMessageBody && body.action() == EMCallConstant.EM1v1SomeUserMatchCanceled){
                    val userInfo = EaseIM.getUserProvider()?.getUser(it.from)
                    val nickName = userInfo?.getNotEmptyName()?:it.from
                    if (it.from == EM1v1CallKitManager.otherMatchInfo?.matchedChatUser){
                        EM1v1CallKitManager.onCallStatusChanged(CallStatus.IDLE,"${nickName}取消配对")
                        ChatLog.d("CallMessageHelper","onCmdMessageReceived CallStatus.IDLE")
                    }
                }

                if (body is ChatCmdMessageBody && body.action() == EMCallConstant.EM1v1CallKit1v1End){
                    val reason = it.getStringAttribute(EMCallConstant.EM1v1EndCallReason,"")
                    if (reason.isNotEmpty()){
                        if (it.from == EM1v1CallKitManager.otherMatchInfo?.matchedChatUser){
                            insertEndMessage(it.conversationId(),EM1v1CallKitManager.callId)
                            EM1v1CallKitManager.onCallStatusChanged(CallStatus.ENDED,reason)
                            ChatLog.e("CallMessageHelper","onCmdMessageReceived CallStatus.ENDED $reason")
                        }
                    }
                }

                if (body is ChatCmdMessageBody && body.action() == EMCallConstant.EM1v1CallKit1v1Accept){
                    ChatLog.e("CallMessageHelper","EM1v1CallKit1v1Accept cancelCountdown from:${it.from} - ${EM1v1CallKitManager.otherMatchInfo?.matchedChatUser}")
                    if ( CountdownTimerHelper.isCountdownRunning()){
                        CountdownTimerHelper.cancelCountdown()
                    }
                    if (it.from == EM1v1CallKitManager.otherMatchInfo?.matchedChatUser){
                        val conversationId = it.conversationId()
                        EM1v1CallKitManager.onCallStatusChanged(CallStatus.JOIN,conversationId)
                        ChatLog.e("CallMessageHelper","onCmdMessageReceived CallStatus.JOIN $conversationId")
                    }
                }
            }
        }

    }

    fun onMsgDidReceive(messages: MutableList<ChatMessage>?){
        parseUserInfo(messages)
        messages?.forEach { msg->
            if (msg.to == EaseIM.getCurrentUser()?.id){
                val inviteCallId = msg.getStringAttribute(EMCallConstant.EM1v1CallKitCallId,"")
                val callType = msg.getStringAttribute(EMCallConstant.CALL_MSG_TYPE,"")
                val inviteKey = msg.getStringAttribute(EMCallConstant.EM1v1CallKit1v1Invite,"")
                if (inviteCallId.isNotEmpty() && inviteKey == EMCallConstant.EM1v1CallKit1v1Invite){
                    if (callType == EMCallConstant.EM1v1CallKit1v1ChatInvite){
                        val channel = (msg.to + msg.conversationId()).lowercase().toCharArray().sorted().joinToString("")
                        EM1v1CallKitManager.channelName = channel
                        EM1v1CallKitManager.isMatchCall = false
                        try {
                            msg.getJSONObjectAttribute(EaseConstant.MESSAGE_EXT_USER_INFO_KEY)?.let { info ->
                                val match = EaseIM.getUserProvider()?.getUser(msg.conversationId())?.parseToMatchInfo()?:MatchUserInfo(msg.conversationId())
                                match.name = info.optString(EaseConstant.MESSAGE_EXT_USER_INFO_NICKNAME_KEY)
                                match.avatar = info.optString(EaseConstant.MESSAGE_EXT_USER_INFO_AVATAR_KEY)
                                ChatLog.d("CallMessageHelper","接收 1v1 chat parse userInfo ${match.matchedChatUser} - ${match.name} - ${match.avatar}")
                                EM1v1CallKitManager.otherMatchInfo = match
                            }
                        } catch (e: ChatException) {
                            ChatLog.e("CallMessageHelper","parse error ${e.message}")
                        }
                    }else{
                        EM1v1CallKitManager.isMatchCall = true
                    }
                    EM1v1CallKitManager.callId = inviteCallId
                    EaseIM.getContext()?.let {
                        val selfPresence = PresenceCache.getUserPresence(msg.to)
                        val status = EasePresenceUtil.getPresenceString(it,selfPresence)
                        if (status == it.getString(PresenceData.ONLINE.presence)){
                            EM1v1CallKitManager.onCallStatusChanged(CallStatus.ALERT,msg.conversationId())
                        }else{
                            sendEndedCallMsg(EM1v1CallKitEndReason.BUSYEND.value,msg.from,inviteCallId)
                        }
                    }
                }
            }
        }
    }

    private fun parseUserInfo(messages: MutableList<ChatMessage>?){
        CoroutineScope(Dispatchers.Main).launch {
            messages?.forEach { msg ->
                msg.parseUserInfo()
            }
        }
    }

}