package com.hyphenate.scenarios.viewmodel

import androidx.lifecycle.viewModelScope
import com.hyphenate.easeui.EaseIM
import com.hyphenate.easeui.common.ChatException
import com.hyphenate.easeui.common.extensions.catchChatException
import com.hyphenate.easeui.provider.getSyncUser
import com.hyphenate.easeui.viewmodel.EaseBaseViewModel
import com.hyphenate.scenarios.DemoHelper
import com.hyphenate.scenarios.bean.MatchUserInfo
import com.hyphenate.scenarios.callkit.EM1v1CallKitManager
import com.hyphenate.scenarios.interfaces.IPrivate1v1Request
import com.hyphenate.scenarios.interfaces.IPrivate1v1RoomResultView
import com.hyphenate.scenarios.repository.EMCall1v1RoomRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class Private1v1RoomViewModel(
    private val stopTimeoutMillis: Long = 5000
):EaseBaseViewModel<IPrivate1v1RoomResultView>(),IPrivate1v1Request {

    private val repository = EMCall1v1RoomRepository()

    override fun matchUserFromServer(phoneNumber: String, isExceptionCancelMatch:Boolean) {
        viewModelScope.launch {
            flow {
                emit(repository.matchUserFromServer(phoneNumber,isExceptionCancelMatch))
            }
                .flatMapConcat {
                    EM1v1CallKitManager.otherMatchInfo = it
                    DemoHelper.getInstance().dataModel.setExceptionCancelMatch(true)
                    EM1v1CallKitManager.remoteUid = it.agoraUid?.toInt()?:0
                    EM1v1CallKitManager.channelName = it.channelName
                    EM1v1CallKitManager.updateRtcToken(it.rtcToken)
                    DemoHelper.getInstance().dataModel.updateMatchUserInfo(it)
                    flow {
                        try {
                            emit(repository.fetchMatchInfo(it))
                        }catch (e:ChatException){
                            view?.fetchMatchUserInfoFail(e.errorCode, e.description)
                        }
                    }
                }
                .catchChatException { e ->
                    view?.matchUserFail(e.errorCode, e.description)
                }
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(stopTimeoutMillis), null)
                .collect {
                    it?.let {
                        view?.fetchMatchUserInfoSuccess(it[0])
                    }
                }
        }
    }

    override fun fetchMatchedUserInfo(matchUserInfo: MatchUserInfo) {
        viewModelScope.launch {
            flow {
                emit(repository.fetchMatchInfo(matchUserInfo))
            }
                .catchChatException { e ->
                    view?.fetchMatchUserInfoFail(e.errorCode, e.description)
                }
                .collect {
                    it?.let {
                        if (it.isNotEmpty()){
                            view?.fetchMatchUserInfoSuccess(it[0])
                        }
                    }?:kotlin.run {
                        val profile = EaseIM.getUserProvider()?.getSyncUser(matchUserInfo.matchedChatUser)
                        view?.fetchMatchUserInfoSuccess(profile)
                    }
                }
        }
    }

    override fun cancelMatchUser(phoneNumber: String) {
        viewModelScope.launch {
            flow {
                emit(repository.cancelMatchUserFromServer(phoneNumber))
            }
                .catchChatException { e ->
                    view?.cancelMatchUserFail(e.errorCode, e.description)
                }
                .collect {
                    view?.cancelMatchUserSuccess()
                }
        }
    }

}