package com.hyphenate.scenarios.callkit.viewholder

import android.view.ViewGroup
import com.hyphenate.easeui.base.EaseBaseRecyclerViewAdapter
import com.hyphenate.easeui.common.ChatMessage
import com.hyphenate.easeui.common.ChatMessageType
import com.hyphenate.easeui.feature.chat.viewholders.EaseUnknownViewHolder
import com.hyphenate.easeui.widget.chatrow.EaseChatRowUnknown
import com.hyphenate.scenarios.callkit.enums.CallMessageViewType
import com.hyphenate.scenarios.callkit.widget.CallTextChatRow
import com.hyphenate.scenarios.callkit.widget.CallGiftChatRow

object CallChatViewHolderFactory {
    fun createViewHolder(
        parent: ViewGroup,
        viewType: CallMessageViewType
    ): EaseBaseRecyclerViewAdapter.ViewHolder<ChatMessage> {
        return when (viewType) {
            CallMessageViewType.VIEW_TYPE_CALL_MESSAGE_TXT -> CallTextViewHolder(
                CallTextChatRow(
                    parent.context,
                    isSender = true
                )
            )
            CallMessageViewType.VIEW_TYPE_CALL_MESSAGE_GIFT -> CallGiftViewHolder(
                CallGiftChatRow(
                    parent.context,
                    isSender = true
                )
            )
            else -> EaseUnknownViewHolder(EaseChatRowUnknown(parent.context, isSender = false))
        }
    }

    fun getViewType(message: ChatMessage?): Int {
        return message?.let { getChatType(it).value } ?: CallMessageViewType.VIEW_TYPE_MESSAGE_UNKNOWN_OTHER.value
    }

    fun getChatType(message: ChatMessage): CallMessageViewType {
        val type: CallMessageViewType
        val messageType = message.type
        type = if (messageType == ChatMessageType.CMD) {
            CallMessageViewType.VIEW_TYPE_CALL_MESSAGE_GIFT
        }else if (messageType == ChatMessageType.TXT){
            CallMessageViewType.VIEW_TYPE_CALL_MESSAGE_TXT
        }else{
            CallMessageViewType.VIEW_TYPE_MESSAGE_UNKNOWN_OTHER
        }
        return type
    }

}