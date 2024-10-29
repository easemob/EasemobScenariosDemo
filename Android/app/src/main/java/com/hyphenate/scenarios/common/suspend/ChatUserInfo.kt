package com.hyphenate.scenarios.common.suspend

import com.hyphenate.easeui.common.ChatUserInfo
import com.hyphenate.easeui.model.EaseProfile

internal fun ChatUserInfo.toProfile(): EaseProfile {
    return EaseProfile(
        id = userId,
        name = nickname,
        avatar = avatarUrl
    )
}