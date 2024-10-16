package com.hyphenate.scenarios.common.room.extensions

import com.hyphenate.easeui.common.ChatUserInfo
import com.hyphenate.easeui.model.EaseProfile
import com.hyphenate.scenarios.common.room.entity.DemoUser

internal fun EaseProfile.parseToDbBean() = DemoUser(id, name, avatar, remark)

internal fun ChatUserInfo.parseToDbBean(): DemoUser {
    return DemoUser(
        userId = userId,
        name = nickname,
        avatar = avatarUrl
    )
}