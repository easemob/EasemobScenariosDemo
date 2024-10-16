package com.hyphenate.scenarios.callkit.activity

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.viewbinding.ViewBinding
import com.hyphenate.scenarios.base.BaseInitActivity
import com.hyphenate.scenarios.callkit.window.EMCallFloatWindow

abstract class EMBaseCallActivity<B : ViewBinding> : BaseInitActivity<B>(){
    private val REQUEST_CODE_OVERLAY_PERMISSION = 1002

    //用于防止多次打开请求悬浮框页面
     private var requestOverlayPermission = false

    /**
     * Check whether float window is showing
     * @return
     */
    fun isFloatWindowShowing(): Boolean {
        return EMCallFloatWindow.isShowing()
    }

    /**
     * Check permission and show float window
     */
    fun showFloatWindow() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (Settings.canDrawOverlays(this)) {
                doShowFloatWindow()
            } else { // To reqire the window permission.
                if (!requestOverlayPermission) {
                    try {
                        val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
                        // Add this to open the management GUI specific to this app.
                        intent.data = Uri.parse("package:$packageName")
                        startActivityForResult(intent, REQUEST_CODE_OVERLAY_PERMISSION)
                        requestOverlayPermission = true
                        // Handle the permission require result in #onActivityResult();
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        } else {
            doShowFloatWindow()
        }
    }

    fun doShowFloatWindow() {}

    fun makeMainTaskFront() {}
}