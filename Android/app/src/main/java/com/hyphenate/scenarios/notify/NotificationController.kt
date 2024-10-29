package com.hyphenate.scenarios.notify

import android.app.Activity
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.FragmentActivity
import com.hyphenate.easeui.common.ChatLog
import com.hyphenate.easeui.common.ChatMessage
import com.hyphenate.scenarios.callkit.extensions.parseUserInfo
import com.hyphenate.scenarios.ui.chat.ChatBottomSheetFragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NotificationController(
    private val context: Activity,
    private val rootLayout:ViewGroup
) {
    private var notifyView:NotificationGroupView? = null
    private var fragment:ChatBottomSheetFragment? = null
    private var countDownTimer: CountDownTimer? = null
    private var isTaskRunning = false

    private var handler: Handler? = null

    companion object{
        const val mIntervalDuration:Long = 3000
        const val mCountDownInterval:Long = 1000
    }

    fun initNotify(){
        handler = Handler(Looper.getMainLooper())
        notifyView = NotificationGroupView(context)
        notifyView?.setGestureListener(object : NotificationGestureListener{
            override fun onClick(message: ChatMessage?) {
                if (context is FragmentActivity){
                    fragment?.show(context.supportFragmentManager, "notification_chat")
                }else{
                    ChatLog.e("NotificationController","Context must be a FragmentActivity")
                }
            }
        })
        val layoutParams = ConstraintLayout.LayoutParams(
            ConstraintLayout.LayoutParams.MATCH_PARENT,
            ConstraintLayout.LayoutParams.MATCH_PARENT
        )
        rootLayout.addView(notifyView, layoutParams)
    }
    fun showNotify(message: ChatMessage,id:String){
        fragment = ChatBottomSheetFragment(id)
        CoroutineScope(Dispatchers.Main).launch {
            message.parseUserInfo()
            notifyView?.addNotification(message)
            startTask()
        }
    }

    private fun startTask() {
        // 如果任务正在运行，先取消
        if (isTaskRunning) {
            cancelTask()
        }
        // 开始新的任务 countDownInterval
        isTaskRunning = true
        countDownTimer = object : CountDownTimer(mIntervalDuration, mCountDownInterval) {
            override fun onTick(millisUntilFinished: Long) {
                // 这里可以更新 UI 或执行其他任务
            }

            override fun onFinish() {
                // 任务完成
                isTaskRunning = false
                notifyView?.clearNotify()
            }
        }.start()
    }

    private fun cancelTask() {
        countDownTimer?.cancel()
        isTaskRunning = false
    }


}