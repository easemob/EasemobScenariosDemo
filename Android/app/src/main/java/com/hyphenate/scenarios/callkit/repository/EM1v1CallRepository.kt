package com.hyphenate.scenarios.callkit.repository

import com.hyphenate.easeui.EaseIM
import com.hyphenate.easeui.common.ChatClient
import com.hyphenate.easeui.common.ChatCmdMessageBody
import com.hyphenate.easeui.common.ChatException
import com.hyphenate.easeui.common.ChatLog
import com.hyphenate.easeui.common.ChatMessage
import com.hyphenate.easeui.common.ChatMessageType
import com.hyphenate.easeui.common.ChatTextMessageBody
import com.hyphenate.easeui.common.ChatType
import com.hyphenate.easeui.common.extensions.send
import com.hyphenate.scenarios.bean.GiftEntityProtocol
import com.hyphenate.scenarios.callkit.extensions.addGiftInfo
import com.hyphenate.scenarios.callkit.extensions.addUserInfo
import com.hyphenate.scenarios.repository.BaseRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class EM1v1CallRepository: BaseRepository() {

    suspend fun send1v1CallMessage(
        userId: String,
        content: String?
    ):ChatMessage =
        withContext(Dispatchers.IO){
            suspendCoroutine { continuation ->
                val message = ChatMessage.createSendMessage(ChatMessageType.TXT)
                message.deliverOnlineOnly(true)
                message.from = ChatClient.getInstance().currentUser
                message.to = userId
                ChatLog.d("EM1v1CallRepository","send message from:${message.from} - to:${message.to}")
                message.chatType = ChatType.Chat
                val txtBody = ChatTextMessageBody(content)
                EaseIM.getCurrentUser()?.let { profile ->
                    message.addUserInfo(profile.name, profile.avatar)
                }
                message.body = txtBody
                message.send(
                    onSuccess = {
                         continuation.resume(message)
                    },
                    onError = { code,error ->
                        continuation.resumeWithException(ChatException(code, error))
                    }
                )
            }
    }

    suspend fun send1v1GiftCallMessage(
        userId: String,
        eventType: String,
        gift: GiftEntityProtocol
    ):ChatMessage =
        withContext(Dispatchers.IO){
            suspendCoroutine { continuation ->
                val message = ChatMessage.createSendMessage(ChatMessageType.CMD)
                message.deliverOnlineOnly(true)
                message.to = userId
                message.chatType = ChatType.Chat
                val cmdBody = ChatCmdMessageBody(eventType)
                EaseIM.getCurrentUser()?.let { profile ->
                    message.addUserInfo(profile.name, profile.avatar)
                }
                gift.let {
                    message.addGiftInfo(it)
                }
                message.body = cmdBody
                message.send(
                    onSuccess = {
                        continuation.resume(message)
                    },
                    onError = { code,error ->
                        continuation.resumeWithException(ChatException(code, error))
                    }
                )
            }
        }

}