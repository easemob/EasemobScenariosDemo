package com.hyphenate.scenarios.callkit.widget

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.hyphenate.easeui.EaseIM
import com.hyphenate.easeui.common.ChatClient
import com.hyphenate.easeui.common.ChatMessage
import com.hyphenate.easeui.common.extensions.dpToPx
import com.hyphenate.easeui.databinding.EaseChatMessageListBinding
import com.hyphenate.easeui.interfaces.EaseMessageListener
import com.hyphenate.scenarios.callkit.CallChatRowConfig
import com.hyphenate.scenarios.callkit.EM1v1CallKitManager
import com.hyphenate.scenarios.callkit.EMCallConstant
import com.hyphenate.scenarios.callkit.adapter.CallGiftAdapter
import com.hyphenate.scenarios.callkit.extensions.parseGiftInfo
import com.hyphenate.scenarios.callkit.interfaces.OnGiftMessageListener
import com.hyphenate.scenarios.databinding.DemoCallGiftMessageListLayoutBinding
import com.hyphenate.scenarios.databinding.DemoCallMessageListLayoutBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CallGiftMessageLayout@JvmOverloads constructor(
    private val context: Context,
    private val attrs: AttributeSet? = null,
    private val defStyleAttr: Int = 0
): FrameLayout(context, attrs, defStyleAttr) {
    companion object{
        const val delay:Int = 3000
    }

    private var handler: Handler ? = null
    private var task:Runnable? = null

    private var giftMessageListener:OnGiftMessageListener? = null

    private val binding: DemoCallGiftMessageListLayoutBinding by lazy {
        DemoCallGiftMessageListLayoutBinding.inflate(LayoutInflater.from(context), this, true)
    }

    private lateinit var adapter: CallGiftAdapter

    private val chatMessageListener = object : EaseMessageListener() {

        override fun onCmdMessageReceived(messages: MutableList<ChatMessage>?) {
            if (!CallChatRowConfig.enableShowGiftInCell){
                messages?.forEach {
                    val hasGift = it.ext().containsKey(EMCallConstant.EMMob1v1ChatGift)
                    if (
                        it.from == EM1v1CallKitManager.otherMatchInfo?.matchedChatUser &&
                        it.to == ChatClient.getInstance().currentUser && hasGift
                    ){
                        loadGiftMessage(it)
                        giftMessageListener?.onReceiveGiftMsg(it.parseGiftInfo())
                    }
                }
            }
        }
    }

    // 开启定时任务
    private fun startTask() {
        stopTask()
        handler?.postDelayed(object : Runnable {
            override fun run() {
                // 在这里执行具体的任务
                if (adapter.getData().size > 0) {
                    adapter.removeAll()
                }
                // 任务执行完后再次调用postDelayed开启下一次任务
                handler?.postDelayed(this, delay.toLong())
            }
        }.also { task = it }, delay.toLong())
    }

    // 停止定时任务
    private fun stopTask() {
        task?.let {
            handler?.removeCallbacks(it)
            task = null
        }
    }

    fun clearTask() {
        task?.let {
            removeCallbacks(it)
        }
        handler = null
    }

    init {
        initView()
        initListener()
    }


    fun initView(){
        handler = Handler(Looper.getMainLooper())
        adapter = CallGiftAdapter(mutableListOf())

        binding.messageList.layoutManager = LinearLayoutManager(context).apply {
            stackFromEnd = true
        }
        binding.messageList.adapter = adapter

        //设置item 间距
        val itemDecoration = DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL)
        val drawable = GradientDrawable()
        drawable.setSize(0, 6.dpToPx(context))
        itemDecoration.setDrawable(drawable)
        binding.messageList.addItemDecoration(itemDecoration)

        //设置item动画
        val defaultItemAnimator = DefaultItemAnimator()
        defaultItemAnimator.addDuration = 500
        defaultItemAnimator.removeDuration = 500
        binding.messageList.itemAnimator = defaultItemAnimator
    }

    @SuppressLint("ClickableViewAccessibility")
    fun initListener(){
        EaseIM.addChatMessageListener(chatMessageListener)
    }

    override fun onDetachedFromWindow() {
        EaseIM.removeChatMessageListener(chatMessageListener)
        clearTask()
        super.onDetachedFromWindow()
    }

    fun setGiftMessageListener(listener:OnGiftMessageListener?){
        this.giftMessageListener = listener
    }

    /**
     * 定时清理礼物列表信息
     */
    private fun clearTiming() {
        if (childCount > 0) {
            startTask()
        }
    }

    fun loadGiftMessage(message:ChatMessage){
        CoroutineScope(Dispatchers.Main).launch {
            adapter.addItem(message)
            binding.messageList.smoothScrollToPosition(adapter.getData().size)
            clearTiming()
        }
    }
}
