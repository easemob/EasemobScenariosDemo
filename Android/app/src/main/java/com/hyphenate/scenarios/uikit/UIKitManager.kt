package com.hyphenate.scenarios.uikit

import android.content.Context
import android.content.Intent
import com.hyphenate.easeui.EaseIM
import com.hyphenate.easeui.common.ChatClient
import com.hyphenate.easeui.common.ChatException
import com.hyphenate.easeui.common.ChatLog
import com.hyphenate.easeui.common.ChatMessage
import com.hyphenate.easeui.common.ChatUserInfoType
import com.hyphenate.easeui.common.extensions.toProfile
import com.hyphenate.easeui.common.impl.OnValueSuccess
import com.hyphenate.easeui.feature.chat.activities.EaseChatActivity
import com.hyphenate.easeui.model.EaseGroupProfile
import com.hyphenate.easeui.model.EaseProfile
import com.hyphenate.easeui.provider.EaseCustomActivityRoute
import com.hyphenate.easeui.provider.EaseGroupProfileProvider
import com.hyphenate.easeui.provider.EaseSettingsProvider
import com.hyphenate.easeui.provider.EaseUserProfileProvider
import com.hyphenate.easeui.widget.EaseImageView
import com.hyphenate.scenarios.DemoHelper
import com.hyphenate.scenarios.bean.MatchUserInfo
import com.hyphenate.scenarios.common.suspend.toProfile
import com.hyphenate.scenarios.repository.ProfileInfoRepository
import com.hyphenate.scenarios.ui.chat.ChatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object UIKitManager {

    fun addUIKitSettings(context: Context) {
        addProviders(context)
        setUIKitConfigs(context)
    }

    fun addProviders(context: Context) {
        EaseIM.setUserProfileProvider(object : EaseUserProfileProvider {
                override fun getUser(userId: String?): EaseProfile? {
                    return DemoHelper.getInstance().dataModel.getAllContacts()[userId]?.toProfile()
                }

                override fun fetchUsers(
                    userIds: List<String>,
                    onValueSuccess: OnValueSuccess<List<EaseProfile>>
                ) {
                    ChatLog.d("UIKitManager","fetchUsers $userIds")
                    // fetch users from server and call call onValueSuccess.onSuccess(users) after successfully getting users
                    CoroutineScope(Dispatchers.IO).launch {
                        if (userIds.isEmpty()) {
                            onValueSuccess(mutableListOf())
                            return@launch
                        }
                        try {
                            val users = ProfileInfoRepository().getUserInfoAttribute(userIds, mutableListOf(ChatUserInfoType.NICKNAME, ChatUserInfoType.AVATAR_URL))
                            val callbackList = users.values.map { it.toProfile() }.map {
                                val match = DemoHelper.getInstance().dataModel.getMatchUserInfo(it.id)
                                match?.let { info->
                                    info.name = it.name
                                    info.avatar = it.avatar
                                    DemoHelper.getInstance().dataModel.updateMatchUserInfo(info)
                                }?:kotlin.run {
                                    val matchUserInfo = MatchUserInfo(matchedChatUser = it.id)
                                    matchUserInfo.avatar = it.avatar
                                    matchUserInfo.name = it.name
                                    DemoHelper.getInstance().dataModel.updateMatchUserInfo(matchUserInfo)
                                }
                                it
                            }
                            if (callbackList.isNotEmpty()) {
                                DemoHelper.getInstance().dataModel.insertUsers(callbackList)
                                DemoHelper.getInstance().dataModel.updateUsersTimes(callbackList)
                                EaseIM.updateUsersInfo(callbackList)
                            }
                            onValueSuccess(callbackList)
                        }catch (e:ChatException){
                            ChatLog.e("UIKitManager","fetchUsers error ${e.errorCode} ${e.message}")
                        }
                    }
                }
            })
            .setGroupProfileProvider(object : EaseGroupProfileProvider {

                override fun getGroup(id: String?): EaseGroupProfile? {
                    ChatClient.getInstance().groupManager().getGroup(id)?.let {
                        return EaseGroupProfile(it.groupId, it.groupName, it.extension)
                    }
                    return null
                }

                override fun fetchGroups(
                    groupIds: List<String>,
                    onValueSuccess: OnValueSuccess<List<EaseGroupProfile>>
                ) {

                }
            })
            .setSettingsProvider(object : EaseSettingsProvider {
                override fun isMsgNotifyAllowed(message: ChatMessage?): Boolean {
                    return true
                }

                override fun isMsgSoundAllowed(message: ChatMessage?): Boolean {
                    return false
                }

                override fun isMsgVibrateAllowed(message: ChatMessage?): Boolean {
                    return false
                }

                override val isSpeakerOpened: Boolean
                    get() = true

            })
            .setCustomActivityRoute(object : EaseCustomActivityRoute {
                override fun getActivityRoute(intent: Intent): Intent? {
                    intent.component?.className?.let {
                        when(it) {
                            EaseChatActivity::class.java.name -> {
                                intent.setClass(context, ChatActivity::class.java)
                            }
                            else -> {
                                return intent
                            }
                        }
                    }
                    return intent
                }

            })
    }

    fun setUIKitConfigs(context: Context) {
        EaseIM.getConfig()?.avatarConfig?.let {
            it.avatarShape = EaseImageView.ShapeType.ROUND
            it.avatarRadius = context.resources.getDimensionPixelSize(com.hyphenate.easeui.R.dimen.ease_corner_extra_small)
        }
    }
}