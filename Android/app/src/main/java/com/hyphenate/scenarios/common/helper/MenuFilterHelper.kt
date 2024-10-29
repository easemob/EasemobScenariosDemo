package com.hyphenate.scenarios.common.helper

import android.text.TextUtils
import com.hyphenate.easeui.R
import com.hyphenate.easeui.common.ChatMessage
import com.hyphenate.easeui.common.ChatMessageType
import com.hyphenate.easeui.menu.chat.EaseChatMenuHelper
import com.hyphenate.scenarios.callkit.EMCallConstant

object MenuFilterHelper {
    fun filterMenu(helper: EaseChatMenuHelper?, message: ChatMessage?){
        message?.let {
            when(it.type){
                ChatMessageType.TXT ->{
                    if (it.ext().containsKey(EMCallConstant.CALL_MSG_TYPE)){
                        val msgType = it.getStringAttribute(EMCallConstant.CALL_MSG_TYPE,"")
                        if (TextUtils.equals(msgType, EMCallConstant.EM1v1CallKit1v1Signaling)) {
                            helper?.setAllItemsVisible(false)
                            helper?.clearTopView()
                            helper?.findItemVisible(R.id.action_chat_delete,true)
                        }
                    }
                }
                else -> {}
            }
        }
    }
}