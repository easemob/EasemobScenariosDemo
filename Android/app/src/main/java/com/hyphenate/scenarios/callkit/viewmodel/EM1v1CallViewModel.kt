package com.hyphenate.scenarios.callkit.viewmodel

import androidx.lifecycle.viewModelScope
import com.hyphenate.easeui.common.ChatError
import com.hyphenate.easeui.common.ChatMessage
import com.hyphenate.easeui.common.extensions.catchChatException
import com.hyphenate.easeui.viewmodel.EaseBaseViewModel
import com.hyphenate.scenarios.bean.GiftEntityProtocol
import com.hyphenate.scenarios.callkit.interfaces.Chat1v1Service
import com.hyphenate.scenarios.callkit.interfaces.ICallResultView
import com.hyphenate.scenarios.callkit.repository.EM1v1CallRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class EM1v1CallViewModel(
    private val stopTimeoutMillis: Long = 5000
): EaseBaseViewModel<ICallResultView>(),Chat1v1Service {

    val m1v1Repository = EM1v1CallRepository()
    override fun sendMessage(userId: String, text: String?) {
        viewModelScope.launch {
            flow {
                emit(m1v1Repository.send1v1CallMessage(userId,text))
            }.catchChatException { e ->
                view?.send1v1CallMessageFail(e.errorCode, e.description)
            }
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(stopTimeoutMillis), null)
                .collect {
                    it?.let {
                        view?.send1v1CallMessageSuccess(it)
                    }
                }
        }
    }

    override fun sendGiftMessage(
        userId: String,
        eventType: String,
        gift: GiftEntityProtocol
    ) {
        viewModelScope.launch {
            flow {
                emit(m1v1Repository.send1v1GiftCallMessage(userId, eventType, gift))
            }.catchChatException { e ->
                view?.send1v1GiftCallMessageFail(e.errorCode, e.description)
            }
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(stopTimeoutMillis), null)
                .collect {
                    it?.let {
                        view?.send1v1GiftCallMessageSuccess(gift,it)
                    }
                }
        }
    }

    override fun translateMessage(
        message: ChatMessage
    ) {
        TODO("Not yet implemented")
    }

    override fun recall(messageId: String) {
        TODO("Not yet implemented")
    }

    override fun report(
        messageId: String,
        tag: String,
        reason: String
    ) {
        TODO("Not yet implemented")
    }

}