package com.hyphenate.scenarios.callkit.extensions

import android.content.Context
import com.hyphenate.easeui.EaseIM
import com.hyphenate.easeui.R
import com.hyphenate.easeui.common.ChatCmdMessageBody
import com.hyphenate.easeui.common.ChatCustomMessageBody
import com.hyphenate.easeui.common.ChatException
import com.hyphenate.easeui.common.ChatLog
import com.hyphenate.easeui.common.ChatMessage
import com.hyphenate.easeui.common.ChatMessageDirection
import com.hyphenate.easeui.common.ChatMessageType
import com.hyphenate.easeui.common.ChatNormalFileMessageBody
import com.hyphenate.easeui.common.ChatTextMessageBody
import com.hyphenate.easeui.common.ChatType
import com.hyphenate.easeui.common.EaseConstant
import com.hyphenate.easeui.common.extensions.getUserCardInfo
import com.hyphenate.easeui.common.extensions.isAlertMessage
import com.hyphenate.easeui.common.extensions.isUserCardMessage
import com.hyphenate.easeui.model.EaseProfile
import com.hyphenate.easeui.provider.getSyncUser
import com.hyphenate.scenarios.DemoHelper
import com.hyphenate.scenarios.bean.GiftEntityProtocol
import com.hyphenate.scenarios.bean.MatchUserInfo
import com.hyphenate.scenarios.callkit.EM1v1CallKitManager
import com.hyphenate.scenarios.callkit.EMCallConstant
import com.hyphenate.scenarios.common.room.entity.parse
import org.json.JSONObject

/**
 * Parse userinfo from message when receiving a message.
 */
internal fun ChatMessage.parseUserInfo(){
    var profile: EaseProfile? = null
    var userId = ""
    if (from == "admin"){
        ChatLog.e("ChatMessage","接收 admin cmd消息: ${(body as ChatCmdMessageBody).action()} + ext:${ext()}")
        if ( ext().containsKey("matchedChatUser") ){
            userId = getStringAttribute("matchedChatUser")
        }else{ return }
        val matchUserInfo = MatchUserInfo(userId)
        try {
            EM1v1CallKitManager.updateRtcToken(getStringAttribute("rtcToken",""))
            matchUserInfo.channelName = getStringAttribute("channelName","")
            EM1v1CallKitManager.channelName = getStringAttribute("channelName","")
            matchUserInfo.matchedUser = getStringAttribute("matchedUser","")
            matchUserInfo.agoraUid = getStringAttribute("agoraUid")
            EM1v1CallKitManager.remoteUid = getStringAttribute("agoraUid").toInt()
            DemoHelper.getInstance().dataModel.updateMatchUserInfo(matchUserInfo)
            EM1v1CallKitManager.otherMatchInfo = matchUserInfo
            DemoHelper.getInstance().dataModel.setExceptionCancelMatch(true)
        } catch (e: ChatException) {
            ChatLog.e("CallMessageHelper","parse match user info error: ${e.errorCode} ${e.message}")
        }
    }else{ userId = from }

    try {
        getJSONObjectAttribute(EaseConstant.MESSAGE_EXT_USER_INFO_KEY)?.let { info ->
            profile = EaseProfile(
                id = userId,
                name = info.optString(EaseConstant.MESSAGE_EXT_USER_INFO_NICKNAME_KEY),
                avatar = info.optString(EaseConstant.MESSAGE_EXT_USER_INFO_AVATAR_KEY),
                remark = info.optString(EaseConstant.MESSAGE_EXT_USER_INFO_REMARK_KEY)
            )
            profile?.let {
                DemoHelper.getInstance().dataModel.insertUser(it,true)
                DemoHelper.getInstance().dataModel.updateUserCache(it.id)
            }
        }
    } catch (e: ChatException) {
        profile = DemoHelper.getInstance().dataModel.getUser(from)?.parse()
    }

}

internal fun ChatMessage.parseGiftInfo():GiftEntityProtocol?{
    var gift:GiftEntityProtocol? = null
    if(body is ChatCmdMessageBody){
        if ((body as ChatCmdMessageBody).action() == EMCallConstant.EMMob1v1ChatGift){
            gift = GiftEntityProtocol()
            try {
                getJSONObjectAttribute(EMCallConstant.EMMob1v1ChatGift)?.let { info ->
                    gift.giftId = info.getString("giftId")
                    gift.giftName = info.getString("giftName")
                    gift.giftPrice = info.getString("giftPrice")
                    gift.giftCount = info.getInt("giftCount")
                    gift.giftIcon = info.getString("giftIcon")
                    gift.giftEffect = info.getString("giftEffect")
                }
            } catch (e: ChatException) {
                ChatLog.e("ChatMessage","parseGiftInfo fail ${e.errorCode} ${e.message}")
            }
        }
    }
    return gift
}

/**
 * Add gift info to message when sending message.
 */
internal fun ChatMessage.addGiftInfo(gift:GiftEntityProtocol){
    val info = JSONObject()
    gift.giftId?.let { info.put("giftId", it) }
    gift.giftName?.let { info.put("giftName", it) }
    gift.giftPrice?.let { info.put("giftPrice", it) }
    gift.giftCount.let { info.put("giftCount", it) }
    gift.giftIcon?.let { info.put("giftIcon", it) }
    gift.giftEffect?.let { info.put("giftEffect", it) }
    setAttribute(EMCallConstant.EMMob1v1ChatGift, info)
}


/**
 * Add userinfo to message when sending message.
 */
internal fun ChatMessage.addUserInfo(nickname: String?, avatarUrl: String?, remark: String? = null) {
    if (nickname.isNullOrEmpty() && avatarUrl.isNullOrEmpty() && remark.isNullOrEmpty()) {
        return
    }
    val info = JSONObject()
    if (!nickname.isNullOrEmpty()) info.put(EaseConstant.MESSAGE_EXT_USER_INFO_NICKNAME_KEY, nickname)
    if (!avatarUrl.isNullOrEmpty()) info.put(EaseConstant.MESSAGE_EXT_USER_INFO_AVATAR_KEY, avatarUrl)
    if (!remark.isNullOrEmpty()) info.put(EaseConstant.MESSAGE_EXT_USER_INFO_REMARK_KEY, remark)
    setAttribute(EaseConstant.MESSAGE_EXT_USER_INFO_KEY, info)
}

internal fun ChatMessage.getMessageDigest(context: Context): String {
    return when(type) {
        ChatMessageType.LOCATION -> {
            if (direct() == ChatMessageDirection.RECEIVE) {
                var name = from
                getSyncUserFromProvider()?.let { profile ->
                    name = profile.getRemarkOrName()
                }
                context.getString(R.string.ease_location_recv, name)
            } else {
                context.getString(R.string.ease_location_prefix)
            }
        }
        ChatMessageType.IMAGE -> {
            context.getString(R.string.ease_picture)
        }
        ChatMessageType.VIDEO -> {
            context.getString(R.string.ease_video)
        }
        ChatMessageType.VOICE -> {
            context.getString(R.string.ease_voice)
        }
        ChatMessageType.FILE -> {
            val body = body as ChatNormalFileMessageBody
            val filename = body.fileName
            if (filename.isNullOrEmpty()) {
                context.getString(R.string.ease_file)
            } else {
                "${context.getString(R.string.ease_file)} $filename"
            }
        }
        ChatMessageType.CUSTOM -> {
            if (isUserCardMessage()) {
                context.getString(R.string.ease_user_card, getUserCardInfo()?.getRemarkOrName() ?: "")
            } else if (isAlertMessage()) {
                (body as ChatCustomMessageBody).params[EaseConstant.MESSAGE_CUSTOM_ALERT_CONTENT]
                    ?: context.getString(R.string.ease_custom)
            } else {
                context.getString(R.string.ease_custom)
            }
        }
        ChatMessageType.TXT -> {
            (body as ChatTextMessageBody).let {
                getBooleanAttribute(EaseConstant.MESSAGE_ATTR_IS_BIG_EXPRESSION, false).let { isBigExp ->
                    if(isBigExp) {
                        if (it.message.isNullOrEmpty()) {
                            context.getString(R.string.ease_dynamic_expression)
                        } else {
                            it.message
                        }
                    } else {
                        it.message
                    }
                } ?: it.message
            }
        }
        ChatMessageType.COMBINE -> {
            context.getString(R.string.ease_combine)
        }
        else -> {
            ""
        }
    }
}

internal fun ChatMessage.getSyncUserFromProvider(): EaseProfile? {
    return if (chatType == ChatType.Chat) {
        if (direct() == ChatMessageDirection.RECEIVE) {
            // Get user info from user profile provider.
            EaseIM.getUserProvider()?.getSyncUser(from)
        } else {
            EaseIM.getCurrentUser()
        }
    } else if (chatType == ChatType.GroupChat) {
        if (direct() == ChatMessageDirection.RECEIVE) {
            // Get user info from cache first.
            // Then get user info from user provider.
            EaseProfile.getGroupMember(conversationId(), from)
        } else {
            EaseIM.getCurrentUser()
        }
    } else {
        null
    }
}