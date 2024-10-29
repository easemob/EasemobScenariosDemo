package com.hyphenate.scenarios.callkit.activity

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.view.LayoutInflater
import android.view.View
import androidx.activity.addCallback
import androidx.lifecycle.ViewModelProvider
import coil.load
import com.hyphenate.easeui.EaseIM
import com.hyphenate.easeui.common.ChatClient
import com.hyphenate.easeui.common.ChatLog
import com.hyphenate.easeui.common.ChatMessage
import com.hyphenate.easeui.common.extensions.dpToPx
import com.hyphenate.easeui.feature.chat.interfaces.OnMessageListTouchListener
import com.hyphenate.easeui.interfaces.EaseMessageListener
import com.hyphenate.scenarios.R
import com.hyphenate.scenarios.bean.GiftEntityProtocol
import com.hyphenate.scenarios.callkit.CallStatus
import com.hyphenate.scenarios.callkit.EM1v1CallKitEndReason
import com.hyphenate.scenarios.callkit.EM1v1CallKitManager
import com.hyphenate.scenarios.callkit.EMCallConstant
import com.hyphenate.scenarios.callkit.dialog.GiftBottomDialog
import com.hyphenate.scenarios.callkit.helper.CallGiftHelper
import com.hyphenate.scenarios.callkit.interfaces.CallChatInputMenuListener
import com.hyphenate.scenarios.callkit.interfaces.CallKitListener
import com.hyphenate.scenarios.callkit.interfaces.Chat1v1Service
import com.hyphenate.scenarios.callkit.interfaces.ICallResultView
import com.hyphenate.scenarios.callkit.interfaces.OnGiftMessageListener
import com.hyphenate.scenarios.callkit.utils.CallKitUtils
import com.hyphenate.scenarios.callkit.viewmodel.EM1v1CallViewModel
import com.hyphenate.scenarios.callkit.widget.CallChronometer
import com.hyphenate.scenarios.common.DemoConstant
import com.hyphenate.scenarios.databinding.Activity1v1CallLayoutBinding
import com.hyphenate.scenarios.interfaces.IPresenceRequest
import com.hyphenate.scenarios.interfaces.IPresenceResultView
import com.hyphenate.scenarios.notify.NotificationController
import com.hyphenate.scenarios.viewmodel.PresenceViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


open class EM1v1CallActivity: EMBaseCallActivity<Activity1v1CallLayoutBinding>(),
    View.OnClickListener, CallChatInputMenuListener ,ICallResultView , IPresenceResultView,
    OnGiftMessageListener, OnMessageListTouchListener {

    companion object{
        const val TAG = "EM1v1CallActivity"
    }

    var listener: CallKitListener? = null

    private var mCall1v1ViewModel: Chat1v1Service? = null
    private var presenceViewModel: IPresenceRequest? = null

    private var handler: Handler? = null
    private var showTask: Runnable? = null
    private var Animation_time = 3

    private var giftDialog: GiftBottomDialog? = null
    private val notifyController: NotificationController by lazy {
        NotificationController(this, binding.root)
    }

    private val callListener = object : CallKitListener{

        override fun onCallStatusChanged(status: CallStatus, reason: String) {
            when(status){
                CallStatus.ENDED -> {
                    // 停止计时
                    stopCount()
                    // 更新presence状态
                    presenceViewModel?.publishPresence(DemoConstant.PRESENCE_ONLINE)
                    finish()
                }
                CallStatus.CALLING -> {
                    setupRemoteVideo()
                }
                else -> {}
            }
        }

        override fun onCallTokenWillExpire() {
            // 更新 RtcToken
        }

        override fun onJoinChannelSuccess(channel: String?, uid: Int, elapsed: Int) {
            ChatLog.d(TAG,"onJoinChannelSuccess $channel $uid $elapsed")
            // 加入channel 成功 将 presence 变更为忙碌状态
            presenceViewModel?.publishPresence(DemoConstant.PRESENCE_BUSY)
            // 设置本地视图
            setupLocalVideo()
            // 开启计时器
            startCount()
        }

    }

    private val chatMessageListener = object : EaseMessageListener() {

        override fun onMessageReceived(messages: MutableList<ChatMessage>?) {
            messages?.forEach {
                if (it.from != ChatClient.getInstance().currentUser &&
                    it.from != EM1v1CallKitManager.otherMatchInfo?.matchedChatUser){
                    notifyController.showNotify(it,it.conversationId())
                }
            }
        }
    }

    override fun getViewBinding(inflater: LayoutInflater): Activity1v1CallLayoutBinding? {
        return Activity1v1CallLayoutBinding.inflate(inflater)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
    }

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        CallKitUtils.setBgRadius(binding.localSurfaceLayout,20.dpToPx(this))
        EM1v1CallKitManager.initRtcEngine()
        EM1v1CallKitManager.joinChannel()
        EM1v1CallKitManager.otherMatchInfo?.let {
            binding.ivAvatar.load(it.avatar)
            binding.tvUserName.text = it.getNotEmptyName()?:it.matchedChatUser
            binding.tvUserId.text = it.id
        }
        handler = Handler(Looper.getMainLooper())
        notifyController.initNotify()
    }

    override fun initListener() {
        super.initListener()
        EM1v1CallKitManager.addCallKitListener(TAG,callListener)
        binding.ivHangUp.setOnClickListener(this)
        binding.oppositeSurfaceLayout.setOnClickListener(this)
        binding.callInputPrimary.setChatInputMenuListener(this)
        // 注册返回按键处理
        onBackPressedDispatcher.addCallback(this) {
            EM1v1CallKitManager.otherMatchInfo?.let {
                EM1v1CallKitManager.endCall(it.matchedChatUser,EM1v1CallKitEndReason.CANCELEND.value)
            }
            finish()
        }
        binding.callGiftMessageLayout.setGiftMessageListener(this)
        binding.callMessageLayout.setOnMessageListTouchListener(this)
        EaseIM.addChatMessageListener(chatMessageListener)
    }

    override fun onDestroy() {
        presenceViewModel?.publishPresence(DemoConstant.PRESENCE_ONLINE)
        binding.callInputPrimary.setChatInputMenuListener(null)
        binding.callGiftMessageLayout.setGiftMessageListener(null)
        binding.callMessageLayout.setOnMessageListTouchListener(null)
        EaseIM.removeChatMessageListener(chatMessageListener)
        EM1v1CallKitManager.removeCallKitListener(TAG)
        stopActionTask()
        handler = null
        super.onDestroy()
    }

    override fun initData() {
        super.initData()
        mCall1v1ViewModel = ViewModelProvider(this)[EM1v1CallViewModel::class.java]
        mCall1v1ViewModel?.attachView(this)

        presenceViewModel = ViewModelProvider(this)[PresenceViewModel::class.java]
        presenceViewModel?.attachView(this)
    }


    // 设置本地视图
    private fun setupLocalVideo(){
        val view = EM1v1CallKitManager.createSurfaceView()
        view?.let {
            ChatLog.d(TAG,"setupLocalVideo")
            binding.localSurfaceLayout.let { layout->
                layout.removeAllViews()
                layout.addView(it)
            }
            EM1v1CallKitManager.renderLocalCanvas(it)
        }
        EM1v1CallKitManager.getCurrentMatchInfo()?.let { info->
            binding.tvLocalName.text = info.getNotEmptyName()?:info.id
        }
    }

    // 设置远程视图
    private fun setupRemoteVideo(){
        val view = EM1v1CallKitManager.createSurfaceView()
        view?.let {
            ChatLog.d(TAG,"setupRemoteVideo")
            binding.oppositeSurfaceLayout.let { layout->
                layout.removeAllViews()
                layout.addView(it)
            }
            EM1v1CallKitManager.renderRemoteCanvas(it)
        }
        setupLocalVideo()
    }

     open fun startCount() {
         binding.callChronometer.let {
             it.base = SystemClock.elapsedRealtime()
             it.start()
         }
    }

     open fun stopCount() {
         binding.callChronometer.let {
             it.stop()
             EM1v1CallKitManager.currentEndTime = getChronometerSeconds(it)
         }
    }

    override fun onClick(v: View?) {
        when(v?.id){
            R.id.opposite_surface_layout -> {
                binding.callInputPrimary.chatPrimaryMenu?.showNormalStatus()
                binding.callInputPrimary.hideEmojiKeyboard()
            }
            R.id.iv_hang_up -> {
                // 主动点击挂断
                presenceViewModel?.publishPresence(DemoConstant.PRESENCE_ONLINE)
                EM1v1CallKitManager.otherMatchInfo?.let {
                    EM1v1CallKitManager.endCall(it.matchedChatUser,EM1v1CallKitEndReason.CANCELEND.value)
                }
                stopCount()
                finish()
            }
            else -> {}
        }
    }

    override fun onSendMessage(content: String?) {
        EM1v1CallKitManager.otherMatchInfo?.let {
            if (content.isNullOrEmpty().not()){
                mCall1v1ViewModel?.sendMessage(it.matchedChatUser, content)
            }
        }
    }

    override fun onSendGiftBtnClicked() {
        EM1v1CallKitManager.otherMatchInfo?.let {
            val to = it.matchedChatUser
            // 弹出礼物选择界面
            giftDialog = GiftBottomDialog(onSendGiftClickListener = { v, bean ->
                bean?.let {
                    mCall1v1ViewModel?.sendGiftMessage(to,EMCallConstant.EMMob1v1ChatGift,it)
                }
            })
            giftDialog?.show(this.supportFragmentManager,"bottom_gift_dialog")
        }
    }

    private fun getChronometerSeconds(cmt: CallChronometer?): Long {
        cmt?.let {
            return it.costSeconds
        }?:kotlin.run {
            ChatLog.e(TAG, "CallChronometer is null, can not get the cost seconds!")
            return 0
        }
    }

    override fun send1v1CallMessageSuccess(message: ChatMessage?) {
        ChatLog.d(TAG,"send1v1CallMessageSuccess $message")
        message?.let {
            binding.callMessageLayout.loadCallMessage(it)
            binding.callInputPrimary.chatPrimaryMenu?.showNormalStatus()
            binding.callInputPrimary.hideEmojiKeyboard()
        }
    }

    override fun send1v1CallMessageFail(code: Int, error: String) {
        ChatLog.e(TAG,"send1v1CallMessageFail $code $error")
    }

    override fun send1v1GiftCallMessageSuccess(gift: GiftEntityProtocol, message: ChatMessage?) {
        ChatLog.d(TAG,"send1v1GiftCallMessageSuccess ")
        message?.let {
            binding.callGiftMessageLayout.loadGiftMessage(it)
        }
        giftDialog?.dismiss()
        showGiftAction(gift)
    }

    override fun send1v1GiftCallMessageFail(code: Int, error: String) {
        ChatLog.e(TAG,"send1v1GiftCallMessageFail $code $error")
    }

    private fun startAnimationTask() {
        handler?.postDelayed(object : Runnable {
            override fun run() {
                // 在这里执行具体的任务
                Animation_time--
                // 任务执行完后再次调用postDelayed开启下一次任务
                if (Animation_time == 0) {
                    stopActionTask()
                    binding.let {
                        CallGiftHelper.stopGiftAction()
                        CallGiftHelper.giftRest()
                    }
                } else {
                    handler?.postDelayed(this, 1000)
                }
            }
        }.also { showTask = it }, 1000)
    }

    // 停止计时任务
     fun stopActionTask() {
        showTask?.let {
            handler?.removeCallbacks(it)
            showTask = null
            Animation_time = 3
        }
    }

    override fun onReceiveGiftMsg(gift: GiftEntityProtocol?) {
        ChatLog.d(TAG,"onReceiveGiftMsg")
        CoroutineScope(Dispatchers.Main).launch {
            showGiftAction(gift)
        }
    }

    private fun showGiftAction(gift: GiftEntityProtocol?){
        CallGiftHelper.showGiftAction(this, gift = gift)
        startAnimationTask()
    }

    override fun onFinishScroll() {

    }

    override fun onReachBottom() {

    }

    override fun onTouchItemOutside(v: View?, position: Int) {
        ChatLog.d(TAG,"onTouchItemOutside")
        binding.callInputPrimary.chatPrimaryMenu?.showNormalStatus()
        binding.callInputPrimary.hideEmojiKeyboard()
    }

    override fun onViewDragging() {

    }

}