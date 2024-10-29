package com.hyphenate.scenarios.viewmodel

import androidx.lifecycle.viewModelScope
import com.hyphenate.easeui.EaseIM
import com.hyphenate.easeui.common.ChatClient
import com.hyphenate.easeui.common.ChatUserInfoType
import com.hyphenate.easeui.common.extensions.catchChatException
import com.hyphenate.easeui.viewmodel.EaseBaseViewModel
import com.hyphenate.scenarios.DemoHelper
import com.hyphenate.scenarios.bean.MatchUserInfo
import com.hyphenate.scenarios.callkit.EM1v1CallKitManager
import com.hyphenate.scenarios.callkit.extensions.parseToMatchInfo
import com.hyphenate.scenarios.common.room.entity.parse
import com.hyphenate.scenarios.interfaces.IProfileRequest
import com.hyphenate.scenarios.interfaces.IProfileResultView
import com.hyphenate.scenarios.repository.ProfileInfoRepository
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch

class ProfileInfoViewModel: EaseBaseViewModel<IProfileResultView>(), IProfileRequest {
    private val mRepository: ProfileInfoRepository = ProfileInfoRepository()
    override fun uploadAvatar(filePath: String?) {
        viewModelScope.launch {
            flow {
                emit(mRepository.uploadAvatar(filePath))
            }.flatMapConcat { result ->
                EaseIM.getCurrentUser()?.let {
                    it.avatar = result
                    DemoHelper.getInstance().dataModel.insertUser(it)
                    DemoHelper.getInstance().dataModel.updateUserCache(it.id)
                }
                flow {
                    emit(mRepository.uploadAvatarToChatServer(result))
                }
            }
                .catchChatException { e ->
                    view?.uploadAvatarFail(e.errorCode, e.description)
                }
                .collect {
                    view?.uploadAvatarSuccess()
                }
        }
    }

    override fun updateUserNickName(nickname: String) {
        viewModelScope.launch {
            flow {
                emit(mRepository.updateNickname(nickname))
            }
                .catchChatException { e ->
                     view?.updateNickNameFail(e.errorCode, e.description)
                 }
                .collect {
                    val user = EaseIM.getCurrentUser()?.let {profile ->
                        profile.name = nickname
                        DemoHelper.getInstance().dataModel.insertUser(profile)
                        EaseIM.updateCurrentUser(profile)
                        profile
                    }
                    view?.updateNickNameSuccess(user)
                }
        }
    }

    override fun synchronizeProfile(isSyncFromServer: Boolean) {
        viewModelScope.launch {
            flow {
                emit(mRepository.synchronizeProfile(isSyncFromServer))
            }
                .catchChatException { e ->
                    view?.synchronizeProfileFail(e.errorCode, e.description)
                }
                .collect {
                    it?.let {
                        DemoHelper.getInstance().dataModel.insertUser(it,true)
                        DemoHelper.getInstance().dataModel.updateUserCache(it.id)
                        view?.synchronizeProfileSuccess(it)
                    }
                }
        }
    }


    override fun synchronizeRtcToken(conversationId: String) =
        flow {
            val phone = DemoHelper.getInstance().dataModel.getPhoneNumber()
            val current = ChatClient.getInstance().currentUser
            val channel = (current+conversationId).lowercase().toCharArray().sorted().joinToString("")
            val match = DemoHelper.getInstance().dataModel.getUser(conversationId)?.parse()?.parseToMatchInfo()?:MatchUserInfo(conversationId)
            EM1v1CallKitManager.channelName = channel
            EM1v1CallKitManager.otherMatchInfo = match
            if (phone.isNotEmpty() && channel.isNotEmpty()){
                 emit(mRepository.synchronizeRtcToken(channel,phone))
            }
        }

    /**
     * Fetch the user info attribute.
     */
    override fun fetchUserInfoAttribute(userIds: List<String>, attributes: List<ChatUserInfoType>) {
        viewModelScope.launch {
            flow {
                emit(mRepository.getUserInfoAttribute(userIds, attributes))
            }
                .catchChatException { e ->
                    view?.fetchUserInfoAttributeFail(e.errorCode, e.description)
                }
                .collect {
                    view?.fetchUserInfoAttributeSuccess(it)
                }
        }

    }
}