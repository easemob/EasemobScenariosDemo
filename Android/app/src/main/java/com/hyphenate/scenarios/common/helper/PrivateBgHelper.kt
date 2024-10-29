package com.hyphenate.scenarios.common.helper

import android.annotation.SuppressLint
import android.graphics.drawable.Drawable
import com.hyphenate.easeui.EaseIM
import kotlin.random.Random


object PrivateBgHelper {

    val randomNumber = Random.nextInt(1, 11)

    @SuppressLint("UseCompatLoadingForDrawables", "DiscouragedApi")
    fun randomBg(): Drawable? {
        // 构建资源 ID
        val context = EaseIM.getContext()
        val resourceName = "pure1v1_user_bg$randomNumber"
        val resourceId = context?.resources?.getIdentifier(resourceName, "drawable", context.packageName)?:0
        return if (resourceId != 0) {
            context?.getDrawable(resourceId)
        } else {
            null // 如果找不到资源，返回 null
        }
    }
}