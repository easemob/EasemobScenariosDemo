package com.hyphenate.scenarios.ui.chat

import android.text.TextUtils
import android.view.ViewGroup
import com.hyphenate.easeui.common.ChatMessage
import com.hyphenate.easeui.common.ChatMessageDirection
import com.hyphenate.easeui.feature.chat.adapter.EaseMessagesAdapter
import com.hyphenate.easeui.feature.chat.viewholders.EaseTextViewHolder
import com.hyphenate.easeui.widget.chatrow.EaseChatRowText
import com.hyphenate.scenarios.callkit.EMCallConstant

class CustomMessagesAdapter: EaseMessagesAdapter() {

    companion object {
        const val VIEW_TYPE_MESSAGE_CALL_SEND = 1000
        const val VIEW_TYPE_MESSAGE_CALL_RECEIVE = 1001
    }
    //继承EaseMessagesAdapter 重写getItemNotEmptyViewType 添加自定义ViewType
    //下方示例 增加自定义 call 消息提醒类型
    override fun getItemNotEmptyViewType(position: Int): Int {
        getItem(position)?.let {
            val msgType = it.getStringAttribute(EMCallConstant.CALL_MSG_TYPE,"")
            if (TextUtils.equals(msgType, EMCallConstant.EM1v1CallKit1v1Signaling)) {
                return if (it.direct() == ChatMessageDirection.SEND) VIEW_TYPE_MESSAGE_CALL_SEND else VIEW_TYPE_MESSAGE_CALL_RECEIVE
            }
        }
        return super.getItemNotEmptyViewType(position)
    }

    // 继承EaseMessagesAdapter getViewHolder 添加自定义ViewHolder 和 ui布局
    // 下方示例 增加单聊、群聊 call 消息提醒类型布局和事件处理
    override fun getViewHolder(parent: ViewGroup, viewType: Int): ViewHolder<ChatMessage> {
        when (viewType) {
            VIEW_TYPE_MESSAGE_CALL_SEND, VIEW_TYPE_MESSAGE_CALL_RECEIVE -> {
                return EaseTextViewHolder(EaseChatRowText(parent.context, isSender = viewType == VIEW_TYPE_MESSAGE_CALL_SEND))
            }
        }
        return super.getViewHolder(parent, viewType)
    }
}