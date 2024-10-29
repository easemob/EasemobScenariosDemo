package com.hyphenate.scenarios.callkit.adapter

import android.view.ViewGroup
import com.hyphenate.easeui.common.ChatMessage
import com.hyphenate.easeui.feature.chat.adapter.EaseMessagesAdapter
import com.hyphenate.scenarios.callkit.enums.CallMessageViewType
import com.hyphenate.scenarios.callkit.viewholder.CallChatViewHolderFactory
import com.hyphenate.scenarios.callkit.viewholder.CallGiftViewHolder
import com.hyphenate.scenarios.callkit.viewholder.CallTextViewHolder
import com.hyphenate.scenarios.callkit.widget.CallTextChatRow
import com.hyphenate.scenarios.callkit.widget.CallGiftChatRow

class CallMessageAdapter: EaseMessagesAdapter() {

    override fun getItemNotEmptyViewType(position: Int): Int {
        return CallChatViewHolderFactory.getViewType(getItem(position))
    }

    override fun getViewHolder(parent: ViewGroup, viewType: Int): ViewHolder<ChatMessage> {
        return CallChatViewHolderFactory.createViewHolder(parent, CallMessageViewType.from(viewType))
    }

    override fun onBindViewHolder(holder: ViewHolder<ChatMessage>, position: Int) {
        super.onBindViewHolder(holder, position)
        if (holder is CallTextViewHolder && holder.itemView is CallTextChatRow) {

        }

        if (holder is CallGiftViewHolder && holder.itemView is CallGiftChatRow){

        }
    }

    override fun getItemId(position: Int): Long {
        getItem(position)?.let {
            return it.hashCode().toLong()
        }
        return super.getItemId(position)
    }


}