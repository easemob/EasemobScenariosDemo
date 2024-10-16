package com.hyphenate.scenarios.callkit

import com.hyphenate.easeui.common.extensions.catchChatException
import com.hyphenate.easeui.common.impl.OnError
import com.hyphenate.easeui.common.impl.OnValueSuccess
import com.hyphenate.scenarios.DemoHelper
import com.hyphenate.scenarios.bean.MatchUserInfo
import com.hyphenate.scenarios.repository.EMCall1v1RoomRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch

object EMMatchManager {

    fun matchUser(onSuccess: OnValueSuccess<MatchUserInfo>, onError: OnError,isExceptionCancelMatch:Boolean = false){
        CoroutineScope(Dispatchers.IO).launch {
            flow{
                val phone = DemoHelper.getInstance().dataModel.getPhoneNumber()
                if (phone.isNotEmpty()){
                    emit(EMCall1v1RoomRepository().matchUserFromServer(phone,isExceptionCancelMatch))
                }
            }
                .catchChatException { onError.invoke(it.errorCode, it.message) }
                .collect {
                    onSuccess.invoke(it)
                }
        }
    }

    fun cancelMatch(onSuccess: OnValueSuccess<Int>?=null, onError: OnError?=null){
        CoroutineScope(Dispatchers.IO).launch {
            flow{
                val phone = DemoHelper.getInstance().dataModel.getPhoneNumber()
                if (phone.isNotEmpty()){
                    emit(EMCall1v1RoomRepository().cancelMatchUserFromServer(phone))
                }
            }
                .catchChatException { onError?.invoke(it.errorCode, it.message) }
                .collect {
                    onSuccess?.invoke(it)
                }
        }
    }


}