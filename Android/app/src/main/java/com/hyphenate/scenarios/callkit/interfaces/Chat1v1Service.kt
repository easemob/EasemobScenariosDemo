package com.hyphenate.scenarios.callkit.interfaces

import com.hyphenate.easeui.common.ChatMessage
import com.hyphenate.easeui.viewmodel.IAttachView
import com.hyphenate.scenarios.bean.GiftEntityProtocol

interface Chat1v1Service: IAttachView {
    // Send text message to some one.
    // - Parameters:
    //   - text: You'll send text.
    //   - completion: Send callback,what if success or error.
    fun sendMessage(userId:String,text: String?)

    // Send targeted gift message to some one.
    // - Parameters:
    //   - userIds: userIds description
    //   - eventType: A constant String value that identifies the type of event.
    //   - infoMap: Extended Information
    fun sendGiftMessage(userId:String, eventType: String,  gift: GiftEntityProtocol)

    // Translate the specified message
    // - Parameters:
    //   - message: ChatMessage kind of text message.
    fun translateMessage(message: ChatMessage)

    // Recall message.
    // - Parameters:
    //   - messageId: message id
    fun recall(messageId: String)

    // Report illegal message.
    // - Parameters:
    //   - messageId: message id
    //   - tag: Illegal type defined at console.
    //   - reason: reason
    fun report(messageId: String,tag: String,reason: String)
}