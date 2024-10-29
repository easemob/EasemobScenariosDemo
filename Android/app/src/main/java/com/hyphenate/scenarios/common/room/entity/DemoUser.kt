package com.hyphenate.scenarios.common.room.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.hyphenate.easeui.model.EaseProfile

@Entity
data class DemoUser(
    @PrimaryKey val userId: String,
    val name: String?,
    val avatar: String?,
    val remark: String? = null,
    @ColumnInfo(name = "update_times")
    var updateTimes: Int = 0
)

/**
 * Convert the user data to the profile data.
 */
internal fun DemoUser.parse() = EaseProfile(userId, name, avatar, remark)
