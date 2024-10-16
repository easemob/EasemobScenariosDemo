package com.hyphenate.scenarios.callkit.extensions

import com.hyphenate.easeui.model.EaseProfile
import com.hyphenate.scenarios.bean.MatchUserInfo

internal fun EaseProfile.parseToMatchInfo() = MatchUserInfo(id, name, avatar, remark)