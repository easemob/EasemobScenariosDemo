package com.hyphenate.scenarios.callkit.utils

import android.annotation.TargetApi
import android.graphics.Outline
import android.os.Build
import android.view.View
import android.view.ViewOutlineProvider
import android.view.WindowManager
import com.hyphenate.scenarios.callkit.EMCallKitConfig

object CallKitUtils {


    /**
     * 获取用户振铃文件
     * @return
     */
    fun getRingFile(): String? {
        val callKitConfig = EMCallKitConfig()
        callKitConfig.let {
            if (callKitConfig.RingFile != null) {
                return callKitConfig.RingFile
            }
        }
        return null
    }

    fun getSupportedWindowType(): Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            WindowManager.LayoutParams.TYPE_SYSTEM_ALERT
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    fun setBgRadius(view: View, bgRadius: Int) {
        if (Build.VERSION.SDK_INT >= 21) {
            //Set the rounded corner size
            view.outlineProvider = object : ViewOutlineProvider() {
                override fun getOutline(view: View, outline: Outline) {
                    outline.setRoundRect(0, 0, view.width, view.height, bgRadius.toFloat())
                }
            }
            //set shadow
            view.elevation = 10f
            //set rounded corners Clip
            view.clipToOutline = true
        }
    }


}