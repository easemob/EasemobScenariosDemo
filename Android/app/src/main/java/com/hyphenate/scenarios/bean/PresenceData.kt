package com.hyphenate.scenarios.bean

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.hyphenate.scenarios.R

enum class PresenceData(
    @field:StringRes @get:StringRes
    @param:StringRes var presence: Int, @field:DrawableRes @get:DrawableRes
    @param:DrawableRes var presenceIcon: Int
) {
    ONLINE(
        R.string.em_presence_online,
        R.drawable.em_presence_online
    ),
    BUSY(
        R.string.em_presence_busy,
        R.drawable.em_presence_busy
    ),
    DO_NOT_DISTURB(
        R.string.em_presence_do_not_disturb,
        R.drawable.em_presence_do_not_disturb
    ),
    AWAY(
        R.string.em_presence_away,
        R.drawable.em_presence_away
    ),
    OFFLINE(
        R.string.em_presence_offline,
        R.drawable.em_presence_offline
    ),
    CUSTOM(R.string.em_presence_custom, R.drawable.em_presence_custom)

}