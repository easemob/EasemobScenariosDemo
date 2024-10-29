package com.hyphenate.scenarios.ui.conversation

import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.hyphenate.easeui.EaseIM
import com.hyphenate.easeui.common.ChatLog
import com.hyphenate.easeui.configs.setAvatarStyle
import com.hyphenate.easeui.feature.conversation.EaseConversationListFragment
import com.hyphenate.easeui.model.EaseConversation
import com.hyphenate.scenarios.DemoHelper
import com.hyphenate.scenarios.R

class ConversationListFragment: EaseConversationListFragment(){
    companion object{
        const val TAG = "ConversationListFragment"
    }
    private var isFirstLoadData = false

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        binding?.titleConversations?.let {
            EaseIM.getConfig()?.avatarConfig?.setAvatarStyle(it.getLogoView())
            it.setTitle(R.string.em_private_conversation_title)
        }
    }

    override fun loadConversationListSuccess(userList: List<EaseConversation>) {
        if (!isFirstLoadData){
            fetchFirstVisibleData()
            isFirstLoadData = true
        }
    }

    override fun defaultMenu() {
        binding?.titleConversations?.hideDefaultMenu()
    }

    private fun fetchFirstVisibleData(){
        binding?.listConversation?.let { layout->
            (layout.conversationList.layoutManager as? LinearLayoutManager)?.let { manager->
                layout.post {
                    val firstVisibleItemPosition = manager.findFirstVisibleItemPosition()
                    val lastVisibleItemPosition = manager.findLastVisibleItemPosition()
                    val visibleList = layout.getListAdapter()?.mData?.filterIndexed { index, _ ->
                        index in firstVisibleItemPosition..lastVisibleItemPosition
                    }
                    val fetchList = visibleList?.filter { conv ->
                        val u = DemoHelper.getInstance().dataModel.getUser(conv.conversationId)
                        (u == null || u.updateTimes == 0) && (u?.name.isNullOrEmpty() || u?.avatar.isNullOrEmpty())
                    }
                    fetchList?.let {
                        it.map {
                            ChatLog.e(TAG,"fetchFirstVisibleData ${it.conversationId}")
                        }
                        layout.fetchConvUserInfo(it)
                    }
                }
            }
        }
    }
}