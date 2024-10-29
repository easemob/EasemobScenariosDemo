package com.hyphenate.scenarios.callkit.window

import android.animation.ValueAnimator
import android.animation.ValueAnimator.AnimatorUpdateListener
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.graphics.Point
import android.os.SystemClock
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.SurfaceView
import android.view.View
import android.view.View.OnTouchListener
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ImageView
import androidx.viewbinding.ViewBinding
import com.hyphenate.scenarios.R
import com.hyphenate.scenarios.callkit.EM1v1CallKitManager
import com.hyphenate.scenarios.callkit.activity.EMBaseCallActivity
import com.hyphenate.scenarios.callkit.utils.CallKitUtils
import com.hyphenate.scenarios.callkit.widget.CallChronometer
import com.hyphenate.util.EMLog
import io.agora.rtc2.RtcEngine
import kotlin.math.abs

@SuppressLint("StaticFieldLeak")
object EMCallFloatWindow {

    private val TAG = "EaseCallFloatWindow"
    private var context: Context? = null

    private var windowManager: WindowManager? = null
    private var layoutParams: WindowManager.LayoutParams? = null

    private var floatView: View? = null
    private var avatarView: ImageView? = null
    private val surfaceView: SurfaceView? = null

    private var screenWidth = 0
    private var floatViewWidth = 0

    private var rtcEngine: RtcEngine? = null
    private var chronometer: CallChronometer? = null

    private var singleCallInfo: SingleCallInfo? = null

    var costSeconds: Long = 0

    fun setRtcEngine(context: Context?, rtcEngine: RtcEngine?) {
        this.rtcEngine = rtcEngine
        initFloatWindow(context)
    }

    private fun initFloatWindow(context: Context?) {
        context?.let {
            this.context = it
            windowManager = it.applicationContext.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            val point = Point()
            windowManager?.defaultDisplay?.getSize(point)
            screenWidth = point.x
        }
    }

    /**
     * add float window
     */
    @SuppressLint("ClickableViewAccessibility")
    fun show() {
        if (floatView != null) {
            return
        }
        layoutParams = WindowManager.LayoutParams()
        layoutParams?.gravity = Gravity.END or Gravity.TOP
        layoutParams?.width = WindowManager.LayoutParams.WRAP_CONTENT
        layoutParams?.height = WindowManager.LayoutParams.WRAP_CONTENT
        layoutParams?.format = PixelFormat.TRANSPARENT
        layoutParams?.type = CallKitUtils.getSupportedWindowType()
        layoutParams?.flags =
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM

        floatView = LayoutInflater.from(context).inflate(R.layout.activity_float_window, null)
        floatView?.isFocusableInTouchMode = true

        if (floatView is ViewGroup) {
            chronometer = context?.let { CallChronometer(it) }
            val params = ViewGroup.LayoutParams(0, 0)
            (floatView as ViewGroup).addView(chronometer, params)
        }

        windowManager?.addView(floatView, layoutParams)
        startCount()
        singleCallInfo = SingleCallInfo()

        floatView?.post { // Get the size of floatView;
            if (floatView != null) {
                floatViewWidth = floatView?.width ?:0
            }
        }
        avatarView = floatView?.findViewById<View>(R.id.iv_avatar) as ImageView

        floatView?.setOnClickListener {
            val callClass: Class<out EMBaseCallActivity<out ViewBinding>>? = EM1v1CallKitManager.curCallCls
            EMLog.d(TAG, "current call class: $callClass")
            if (callClass != null) {
                val intent = Intent(context, callClass)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                intent.putExtra("isClickByFloat", true)
                EM1v1CallKitManager.appContext?.startActivity(intent)
            } else {
                EMLog.e(TAG, "Current call class is null, please not call EM1v1CallKitManager.releaseCall() before the call is finished")
            }
        }

        floatView?.setOnTouchListener(object : OnTouchListener {
            var result = false
            var left = 0
            var top = 0
            var startX = 0f
            var startY = 0f
            override fun onTouch(v: View, event: MotionEvent): Boolean {
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        result = false
                        startX = event.rawX
                        startY = event.rawY
                        left = layoutParams!!.x
                        top = layoutParams!!.y
                        EMLog.d(TAG, "startX: $startX, startY: $startY, left: $left, top: $top")
                    }

                    MotionEvent.ACTION_MOVE -> {
                        if (abs(event.rawX - startX) > 20 || abs(event.rawY - startY) > 20) {
                            result = true
                        }
                        val deltaX = (startX - event.rawX).toInt()
                        layoutParams?.x = left + deltaX
                        layoutParams?.y = (top + event.rawY - startY).toInt()
                        EMLog.d(TAG, "startX: " + (event.rawX - startX) + ", startY: " +
                                (event.rawY - startY) + ", left: " + left + ", top: " + top
                        )
                        windowManager?.updateViewLayout(floatView, layoutParams)
                    }

                    MotionEvent.ACTION_UP -> smoothScrollToBorder()
                }
                return result
            }
        })

    }

    fun isShowing(): Boolean {
        return floatView != null
    }

    private fun startCount() {
        chronometer?.let {
            it.base =SystemClock.elapsedRealtime()
            it.start()
        }
    }

    private fun smoothScrollToBorder() {
        EMLog.d(TAG, "screenWidth: $screenWidth, floatViewWidth: $floatViewWidth")
        val splitLine = screenWidth / 2 - floatViewWidth / 2
        val left = layoutParams?.x ?:0
        val top = layoutParams?.y ?:0
        val targetX: Int = if (left < splitLine) {
            // 滑动到最左边
            0
        } else {
            // 滑动到最右边
            screenWidth - floatViewWidth
        }
        val animator = ValueAnimator.ofInt(left, targetX)
        animator.setDuration(100)
            .addUpdateListener(AnimatorUpdateListener { animation ->
                if (floatView == null) return@AnimatorUpdateListener
                val value = animation.animatedValue as Int
                EMLog.d(TAG, "onAnimationUpdate, value: $value")
                layoutParams?.x = value
                layoutParams?.y = top
                windowManager?.updateViewLayout(floatView, layoutParams)
            })
        animator.start()
    }


    class SingleCallInfo {
        /**
         * Current user's uid
         */
        var curUid = 0

        /**
         * The other size of uid
         */
        var remoteUid = 0

        /**
         * Camera direction: front or back
         */
        var isCameraFront = true

        /**
         * A tag used to mark the switch between local and remote video
         */
        var changeFlag = false
    }

}