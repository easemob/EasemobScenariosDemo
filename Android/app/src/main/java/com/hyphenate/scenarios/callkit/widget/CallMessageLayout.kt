package com.hyphenate.scenarios.callkit.widget

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.hyphenate.easeui.EaseIM
import com.hyphenate.easeui.common.ChatClient
import com.hyphenate.easeui.common.ChatMessage
import com.hyphenate.easeui.common.impl.OnItemClickListenerImpl
import com.hyphenate.easeui.feature.chat.controllers.EaseChatMessageListScrollAndDataController
import com.hyphenate.easeui.feature.chat.interfaces.OnMessageListItemClickListener
import com.hyphenate.easeui.feature.chat.interfaces.OnMessageListTouchListener
import com.hyphenate.easeui.interfaces.EaseMessageListener
import com.hyphenate.scenarios.callkit.CallChatRowConfig
import com.hyphenate.scenarios.callkit.EM1v1CallKitManager
import com.hyphenate.scenarios.callkit.EMCallConstant
import com.hyphenate.scenarios.callkit.adapter.CallMessageAdapter
import com.hyphenate.scenarios.callkit.interfaces.Chat1v1Service
import com.hyphenate.scenarios.callkit.interfaces.ICallMessageLayout
import com.hyphenate.scenarios.callkit.interfaces.ICallResultView
import com.hyphenate.scenarios.callkit.viewmodel.EM1v1CallViewModel
import com.hyphenate.scenarios.databinding.DemoCallMessageListLayoutBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CallMessageLayout @JvmOverloads constructor(
    private val context: Context,
    private val attrs: AttributeSet? = null,
    private val defStyleAttr: Int = 0
): FrameLayout(context, attrs, defStyleAttr), ICallMessageLayout , ICallResultView {

    private val binding: DemoCallMessageListLayoutBinding by lazy {
        DemoCallMessageListLayoutBinding.inflate(LayoutInflater.from(context), this, true)
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        return false
    }

    /**
     * The adapter to show messages.
     */
    private var messagesAdapter: CallMessageAdapter? = null

    /**
     * The item click listener.
     */
    private var itemMessageClickListener: OnMessageListItemClickListener? = null

    /**
     * The message list touch listener.
     */
    private var messageTouchListener: OnMessageListTouchListener? = null

    private var viewModel: Chat1v1Service? = null

    /**
     * The label whether the first time to load data.
     */
    private var isFirstLoadData: Boolean = true


    /**
     * Concat adapter
     */
    private val concatAdapter: ConcatAdapter by lazy {
        val config = ConcatAdapter.Config.Builder()
            .setStableIdMode(ConcatAdapter.Config.StableIdMode.ISOLATED_STABLE_IDS)
            .build()
        ConcatAdapter(config)
    }

    private val listScrollController: EaseChatMessageListScrollAndDataController by lazy {
        EaseChatMessageListScrollAndDataController(binding.messageList, messagesAdapter!!, context)
    }

    private val chatMessageListener = object : EaseMessageListener() {

        override fun onCmdMessageReceived(messages: MutableList<ChatMessage>?) {
            if (CallChatRowConfig.enableShowGiftInCell){
                messages?.forEach {
                    val hasGift = it.ext().containsKey(EMCallConstant.EMMob1v1ChatGift)
                    if (
                        it.from == EM1v1CallKitManager.otherMatchInfo?.matchedChatUser &&
                        it.to == ChatClient.getInstance().currentUser && hasGift
                    ){
                        loadCallMessage(it)
                    }
                }
            }
        }

        override fun onMessageReceived(messages: MutableList<ChatMessage>?) {
            messages?.forEach {
                if (
                    it.from == EM1v1CallKitManager.otherMatchInfo?.matchedChatUser &&
                    it.to == ChatClient.getInstance().currentUser
                 ){
                    loadCallMessage(it)
                }
            }
        }

    }

    init {
        initViews()
        initListener()
    }

    private fun initViews() {
        if (viewModel == null) {
            viewModel = if (context is AppCompatActivity) {
                ViewModelProvider(context)[EM1v1CallViewModel::class.java]
            } else {
                EM1v1CallViewModel()
            }
        }
        viewModel?.attachView(this)

        binding.messageList.layoutManager = LinearLayoutManager(context).apply {
            stackFromEnd = true
        }
        messagesAdapter = CallMessageAdapter()
        messagesAdapter?.setHasStableIds(true)
        messagesAdapter?.hideEmptyView(true)
        concatAdapter.addAdapter(messagesAdapter!!)
        binding.messageList.adapter = concatAdapter
        // Set not enable to load more.
    }

    override fun onDetachedFromWindow() {
        EaseIM.removeChatMessageListener(chatMessageListener)
        this.itemMessageClickListener = null
        this.messageTouchListener = null
        super.onDetachedFromWindow()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initListener(){
        EaseIM.addChatMessageListener(chatMessageListener)
        binding.messageList.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                listScrollController.onScrollStateChanged()
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    messageTouchListener?.onFinishScroll()
                    if (!this@CallMessageLayout.binding.messageList.canScrollVertically(1)) {
                        messageTouchListener?.onReachBottom()
                    }
                } else if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                    isFirstLoadData = false
                    //if recyclerView not idle should hide keyboard
                    messageTouchListener?.onViewDragging()
                }
            }
        })

        setAdapterListener()

        setOnClickListener {
            messageTouchListener?.onTouchItemOutside(it, -1)
        }

        binding.messageList.addOnLayoutChangeListener(object: OnLayoutChangeListener {
            override fun onLayoutChange(
                v: View?, left: Int, top: Int, right: Int, bottom: Int,
                oldLeft: Int, oldTop: Int, oldRight: Int, oldBottom: Int
            ) {
                if (isFirstLoadData) {
                    listScrollController.smoothScrollToBottom()
                } else {
                    binding.messageList.removeOnLayoutChangeListener(this)
                }
            }
        })
    }

    fun setOnMessageListTouchListener(listener: OnMessageListTouchListener?) {
        this.messageTouchListener = listener
    }

    private fun setAdapterListener() {
        messagesAdapter?.run {
            setOnItemClickListener(OnItemClickListenerImpl{ view, position ->
                // Add touch listener
                messageTouchListener?.onTouchItemOutside(view, position)
            })
            setOnMessageListItemClickListener(itemMessageClickListener)
        }
    }

    fun loadCallMessage(message: ChatMessage){
        CoroutineScope(Dispatchers.Main).launch {
            messagesAdapter?.addData(message)
            listScrollController.smoothScrollToBottom()
        }
    }

}