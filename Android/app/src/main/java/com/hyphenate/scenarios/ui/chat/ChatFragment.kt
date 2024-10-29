package com.hyphenate.scenarios.ui.chat

import android.os.Bundle
import android.text.TextUtils
import android.view.MenuItem
import android.view.View
import androidx.core.view.forEach
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.hyphenate.easeui.EaseIM
import com.hyphenate.easeui.common.ChatLog
import com.hyphenate.easeui.common.ChatMessage
import com.hyphenate.easeui.common.ChatPresence
import com.hyphenate.easeui.common.ChatPresenceListener
import com.hyphenate.easeui.common.bus.EaseFlowBus
import com.hyphenate.easeui.common.extensions.catchChatException
import com.hyphenate.easeui.feature.chat.EaseChatFragment
import com.hyphenate.easeui.feature.chat.enums.EaseChatType
import com.hyphenate.easeui.feature.chat.widgets.EaseChatLayout
import com.hyphenate.easeui.menu.chat.EaseChatMenuHelper
import com.hyphenate.easeui.model.EaseEvent
import com.hyphenate.scenarios.DemoHelper
import com.hyphenate.scenarios.R
import com.hyphenate.scenarios.callkit.CallRtcTokenManager
import com.hyphenate.scenarios.callkit.CallStatus
import com.hyphenate.scenarios.callkit.EM1v1CallKitEndReason
import com.hyphenate.scenarios.callkit.EM1v1CallKitManager
import com.hyphenate.scenarios.callkit.EMCallConstant
import com.hyphenate.scenarios.callkit.EMMatchManager
import com.hyphenate.scenarios.callkit.helper.CallDialogHelper
import com.hyphenate.scenarios.callkit.helper.CountdownTimerHelper
import com.hyphenate.scenarios.callkit.interfaces.CallKitListener
import com.hyphenate.scenarios.common.DemoConstant
import com.hyphenate.scenarios.common.PresenceCache
import com.hyphenate.scenarios.common.helper.MenuFilterHelper
import com.hyphenate.scenarios.common.room.entity.parse
import com.hyphenate.scenarios.interfaces.IPresenceRequest
import com.hyphenate.scenarios.interfaces.IPresenceResultView
import com.hyphenate.scenarios.utils.EasePresenceUtil
import com.hyphenate.scenarios.viewmodel.PresenceViewModel
import com.hyphenate.scenarios.viewmodel.ProfileInfoViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ChatFragment: EaseChatFragment(),IPresenceResultView {
    companion object{
        const val TAG = "ChatFragment"
    }
    private var presenceViewModel: IPresenceRequest? = null
    private var currentStatus:Boolean = true
    private var isShowMenu:Boolean = true

    private val mProfileViewModel: ProfileInfoViewModel by lazy {
        ViewModelProvider(this)[ProfileInfoViewModel::class.java]
    }

    private var presenceListener = ChatPresenceListener {
        it.forEach { presence->
            PresenceCache.insertPresences(presence.publisher,presence)
            updatePresence()
        }
    }

    private val callListener = object : CallKitListener {

        override fun onCallStatusChanged(status: CallStatus, reason: String) {
            when(status){
                CallStatus.ALERT -> {
                    if (currentStatus){
                        onGlobalShowAlert(reason)
                    }
                }
                else -> {  }
            }
        }
    }
    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        if (isShowMenu){
            binding?.titleBar?.inflateMenu(R.menu.demo_chat_menu)
        }
        updatePresence()
    }

    override fun initEventBus() {
        super.initEventBus()
        EaseFlowBus.with<EaseEvent>(EaseEvent.EVENT.UPDATE.name).register(this) {
            if (it.isPresenceChange && it.message.equals(conversationId) ) {
                updatePresence()
            }
        }
    }

    override fun initViewModel() {
        super.initViewModel()
        presenceViewModel = ViewModelProvider(this)[PresenceViewModel::class.java]
        presenceViewModel?.attachView(this)
    }

    override fun initData() {
        super.initData()
        conversationId?.let {
            if (it != EaseIM.getCurrentUser()?.id){
                presenceViewModel?.fetchChatPresence(mutableListOf(it))
                presenceViewModel?.subscribePresences(mutableListOf(it))
            }
        }
    }

    override fun onResume() {
        super.onResume()
        currentStatus = true
        val listener = EM1v1CallKitManager.getCallKitListener(TAG)
        listener?.let {}?:kotlin.run {
            EM1v1CallKitManager.addCallKitListener(TAG,callListener)
        }
    }

    override fun onPause() {
        super.onPause()
        currentStatus = false
    }

    override fun initListener() {
        super.initListener()
        binding?.titleBar?.setLogoClickListener(null)
        binding?.titleBar?.setTitleClickListener(null)
        EM1v1CallKitManager.addCallKitListener(TAG,callListener)
        EaseIM.addPresenceListener(presenceListener)
    }

    override fun onDestroy() {
        EaseIM.removePresenceListener(presenceListener)
        EM1v1CallKitManager.removeCallKitListener(TAG)
        conversationId?.let {
            if (it != EaseIM.getCurrentUser()?.id){
                presenceViewModel?.unsubscribePresences(mutableListOf(it))
            }
        }
        super.onDestroy()
    }

    override fun onUserAvatarClick(userId: String?) {}

    override fun setMenuItemClick(item: MenuItem): Boolean {
        when(item.itemId) {
//            R.id.chat_menu_more -> {
//
//                return true
//            }
            R.id.chat_menu_video_call -> {
                showVideoCall()
                return true
            }
        }
        return super.setMenuItemClick(item)
    }

    fun hideMenu(){
        isShowMenu = false
    }

    private fun showVideoCall() {
        EM1v1CallKitManager.reset()
        //主动呼叫时 取消匹配 通知刷新ui
        EMMatchManager.cancelMatch()
        // 从服务端获取rtcToken 进行呼叫
        getRtcTokenByCall()
    }

    override fun onPreMenu(helper: EaseChatMenuHelper?, message: ChatMessage?) {
        super.onPreMenu(helper, message)
        MenuFilterHelper.filterMenu(helper, message)
    }

    private fun updatePresence(){
        CoroutineScope(Dispatchers.Main).launch {
            if (chatType == EaseChatType.SINGLE_CHAT){
                conversationId?.let {
                    val presence = PresenceCache.getUserPresence(it)
                    val logoStatus = EasePresenceUtil.getPresenceIcon(mContext,presence)
                    val subtitle = EasePresenceUtil.getPresenceString(mContext,presence)
                    binding?.run{
                        titleBar.setLogoStatusMargin(end = -1, bottom = -1)
                        titleBar.setLogoStatus(logoStatus)
                        titleBar.setSubtitle(subtitle)
                        titleBar.getStatusView().visibility = View.VISIBLE
                        titleBar.setLogoStatusSize(resources.getDimensionPixelSize(R.dimen.em_title_bar_status_icon_size))
                        titleBar.getToolBar().menu.forEach { item->
                            if (item.itemId == R.id.chat_menu_video_call){
                                item.isEnabled = subtitle == context?.resources?.getString(R.string.em_presence_online)
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onPeerTyping(action: String?) {
        if (TextUtils.equals(action, EaseChatLayout.ACTION_TYPING_BEGIN)) {
            binding?.titleBar?.setSubtitle(getString(com.hyphenate.easeui.R.string.alert_during_typing))
            binding?.titleBar?.visibility = View.VISIBLE
        } else if (TextUtils.equals(action, EaseChatLayout.ACTION_TYPING_END)) {
            updatePresence()
        }
    }


    override fun fetchChatPresenceSuccess(presence: MutableList<ChatPresence>) {
        updatePresence()
    }

    private fun getRtcTokenByCall(){
        conversationId?.let { userId->
            lifecycleScope.launch {
                mProfileViewModel.synchronizeRtcToken(userId)
                    .catchChatException { e ->
                        ChatLog.e(TAG, "synchronizeRtcToken error " + e.description)
                    }.collect { result->
                        EM1v1CallKitManager.rtcToken = result.accessToken
                        EM1v1CallKitManager.getCurrentMatchInfo()?.apply {
                            agoraUid = result.agoraUid
                        }?.let {
                            DemoHelper.getInstance().dataModel.updateMatchUserInfo(it)
                        }
                        DemoHelper.getInstance().dataModel.getUser(userId)?.parse()?.apply {
                            EM1v1CallKitManager.otherMatchInfo?.avatar = avatar
                            EM1v1CallKitManager.otherMatchInfo?.name = name
                        }
                        EM1v1CallKitManager.startCall(userId,EMCallConstant.EM1v1CallKit1v1ChatInvite)
                        CallDialogHelper.showComingDialog(
                            mContext,
                            isIncomingCall = false,
                            onCallClick = {},
                            onRefuseClick = {
                                if (CountdownTimerHelper.isCountdownRunning()){
                                    CountdownTimerHelper.cancelCountdown()
                                }
                                EM1v1CallKitManager.endCall(userId,EM1v1CallKitEndReason.CANCELEND.value )
                            },
                        )
                    }
            }
        }
    }

    fun onGlobalShowAlert(userId:String) {
        // 如果不是匹配呼叫弹窗 则取消匹配
        if (!EM1v1CallKitManager.isMatchCall){
            EMMatchManager.cancelMatch()
        }
        // 收到弹窗信令 获取自己的状态 只有online 情况下 打开弹窗
        ChatLog.d(TAG,"onGlobalShowAlert callId:${EM1v1CallKitManager.callId}")
        if (EM1v1CallKitManager.callId.isNotEmpty()){
            presenceViewModel?.publishPresence(DemoConstant.PRESENCE_BUSY)
            CallDialogHelper.showComingDialog(
                requireContext(),
                onCallClick = {
                    if (EM1v1CallKitManager.isMatchCall){
                        EM1v1CallKitManager.acceptCall(userId)
                    }else{
                        // 获取rtcToken 和 channel
                        chat1v1CallAccept(userId)
                    }
                },
                onRefuseClick = {
                    EM1v1CallKitManager.endCall( userId,EM1v1CallKitEndReason.REFUSEEND.value )
                    presenceViewModel?.publishPresence(DemoConstant.PRESENCE_ONLINE)
                },
            )
        }else{
            EM1v1CallKitManager.endCall( userId,EM1v1CallKitEndReason.BUSYEND.value )
        }
    }

    private fun chat1v1CallAccept(userId:String){
        CallRtcTokenManager.synchronizeRtcToken(
            conversationId = userId,
            onSuccess = { result->
                EM1v1CallKitManager.rtcToken = result.accessToken
                val current = EaseIM.getCurrentUser()?.id
                val channel = (current+userId).lowercase().toCharArray().sorted().joinToString("")
                EM1v1CallKitManager.channelName = channel
                EM1v1CallKitManager.getCurrentMatchInfo()?.apply {
                    agoraUid = result.agoraUid
                }?.let {
                    DemoHelper.getInstance().dataModel.updateMatchUserInfo(it)
                }
                DemoHelper.getInstance().dataModel.getUser(userId)?.parse()?.apply {
                    EM1v1CallKitManager.otherMatchInfo?.avatar = avatar
                    EM1v1CallKitManager.otherMatchInfo?.name = name
                }
                EM1v1CallKitManager.acceptCall(userId)
            },
            onError = { code, error ->
                ChatLog.e(TAG, "synchronizeRtcToken error $code $error")
            }
        )
    }
}