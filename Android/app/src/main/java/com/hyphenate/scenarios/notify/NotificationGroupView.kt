package com.hyphenate.scenarios.notify

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.AppCompatTextView
import androidx.constraintlayout.widget.ConstraintLayout
import coil.load
import com.hyphenate.easeui.EaseIM
import com.hyphenate.easeui.common.ChatMessage
import com.hyphenate.easeui.common.ChatMessageType
import com.hyphenate.easeui.common.ChatTextMessageBody
import com.hyphenate.easeui.provider.getSyncUser
import com.hyphenate.easeui.widget.EaseImageView
import com.hyphenate.scenarios.R
import com.hyphenate.scenarios.callkit.extensions.getEmojiText
import com.hyphenate.scenarios.callkit.extensions.getMessageDigest
import kotlin.math.abs

@SuppressLint("ClickableViewAccessibility")
class NotificationGroupView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {
    private val notifications = mutableListOf<ChatMessage>()

    private var root: View? = null
    private var ivAvatar: EaseImageView? = null
    private var tvName:AppCompatTextView? = null
    private var tvMessage:AppCompatTextView? = null
    private var moreLayout:ConstraintLayout? = null
    private var ivMoreAvatar: EaseImageView? = null
    private var tvMoreName:AppCompatTextView? = null
    private var tvMoreMessage:AppCompatTextView? = null
    private var notifyLayout:ConstraintLayout? = null

    private var initialX: Float = 0f
    private var initialY: Float = 0f
    private var isSwiping = false
    private val swipeThreshold = 300 // 设置滑动阈值
    private var gestureListener: NotificationGestureListener? = null

    init {
        root = LayoutInflater.from(context).inflate(R.layout.demo_notification_layout, this)
        root?.visibility = View.GONE
        tvName = findViewById(R.id.tv_user_name)
        tvMessage = findViewById(R.id.tv_message)
        ivAvatar =  findViewById(R.id.iv_avatar)

        tvMoreName = findViewById(R.id.tv_more_user_name)
        tvMoreMessage = findViewById(R.id.tv_more_message)
        ivMoreAvatar =  findViewById(R.id.iv_more_avatar)

        moreLayout = findViewById(R.id.more_layout)
        notifyLayout = findViewById(R.id.notify_layout)

        notifyLayout?.setOnTouchListener(object : OnTouchListener{
            override fun onTouch(v: View?, event: MotionEvent?): Boolean {
                when (event?.action) {
                    MotionEvent.ACTION_DOWN -> {
                        initialX = event.x
                        initialY = event.y
                        isSwiping = false
                        return true
                    }
                    MotionEvent.ACTION_MOVE -> {
                        val deltaX = event.x - initialX
                        if (deltaX < -swipeThreshold) {
                            isSwiping = true
                            // 更新视图位置
                            notifyLayout?.translationX = deltaX // 实现滑动效果
                        }
                        return true
                    }
                    MotionEvent.ACTION_UP -> {
                        val deltaX = event.x - initialX
                        val deltaY = event.y - initialY
                        if (isSwiping) {
                            if (deltaX < -swipeThreshold) {
                                exitAnimation()
                                gestureListener?.onSwipeLeft()
                            } else {
                                // 还原视图位置
                                notifyLayout?.animate()?.translationX(0f)?.setDuration(200)?.start()
                            }
                        }else{
                            // 点击事件
                            if (abs(deltaX) < 100 && abs(deltaY) < 100) {
                                gestureListener?.onClick(notifications[0])
                                exitAnimation()
                            }
                        }
                        return true

                    }
                }
                return false
            }
        })

    }

    fun addNotification(message:ChatMessage){
        notifications.add(0,message)
        startAnimation()
    }

    private fun showNotification(){
        if (notifications.isEmpty()) return
        if (notifications.size > 1){
            val message = notifications[1]
            message.let { msg->
                EaseIM.getUserProvider()?.getSyncUser(msg.from)?.apply {
                    tvMoreName?.text = getNotEmptyName()
                    ivMoreAvatar?.load(avatar){
                        placeholder(AppCompatResources.getDrawable(context, com.hyphenate.easeui.R.drawable.ease_default_avatar))
                        error(AppCompatResources.getDrawable(context, com.hyphenate.easeui.R.drawable.ease_default_avatar))
                    }
                }?:kotlin.run {
                    tvMoreName?.text = msg.from
                }
                if (msg.type == ChatMessageType.TXT){
                    val body = msg.body
                    if (body is ChatTextMessageBody){
                        tvMoreMessage?.text = body.message.getEmojiText(context)
                    }
                }else{
                    val content = msg.getMessageDigest(context)
                    tvMoreMessage?.text = content
                }
            }
            moreLayout?.animate()?.alpha(1f)?.setDuration(200)?.start()
            moreLayout?.visibility = View.VISIBLE
        }else{
            moreLayout?.animate()?.alpha(0f)?.setDuration(200)?.start()
            moreLayout?.visibility = View.GONE
        }
        val message = notifications[0]
        message.let { msg->
            EaseIM.getUserProvider()?.getSyncUser(msg.from)?.apply {
                tvName?.text = getNotEmptyName()
                ivAvatar?.load(avatar){
                    placeholder(AppCompatResources.getDrawable(context, com.hyphenate.easeui.R.drawable.ease_default_avatar))
                    error(AppCompatResources.getDrawable(context, com.hyphenate.easeui.R.drawable.ease_default_avatar))
                }
            }?:kotlin.run {
                tvName?.text = msg.from
            }
            if (msg.type == ChatMessageType.TXT){
                val body = msg.body
                if (body is ChatTextMessageBody){
                    tvMessage?.text = body.message.getEmojiText(context)
                }
            }else{
                val content = msg.getMessageDigest(context)
                tvMessage?.text = content
            }
        }
    }

    fun removeNotification() {
        root?.let {
            if (notifications.size > 0){
                notifications.removeAt(0)
            }
            // 如果还有其他通知，继续展示下一条
            if (notifications.isNotEmpty()) {
                startAnimation()
            }else{
                it.visibility = View.GONE
            }
        }
    }

    private fun exitAnimation() {
        notifyLayout?.animate()
            ?.translationX(-width.toFloat())
            ?.setDuration(300)
            ?.withEndAction {
                //结束动画后 隐藏并恢复原位
                notifyLayout?.visibility = View.GONE
                notifyLayout?.alpha = 0f
                notifyLayout?.animate()?.translationX(0f)?.setDuration(50)?.start()
                //移除第0条数据
                removeNotification()
            }
            ?.start()
    }

    private fun startAnimation(){
        notifyLayout?.animate()
            ?.alpha(1f)
            ?.setDuration(300)
            ?.withEndAction {
                notifyLayout?.visibility = View.VISIBLE
                root?.visibility = View.VISIBLE
                showNotification()
            }
            ?.start()
    }

    fun clearNotify(){
        root?.visibility = View.GONE
        notifications.clear()
        notifyLayout?.visibility = View.GONE
        notifyLayout?.alpha = 0f
        notifyLayout?.animate()?.translationX(0f)?.setDuration(50)?.start()
    }

    fun setGestureListener(listener: NotificationGestureListener?) {
        this.gestureListener = listener
    }

}