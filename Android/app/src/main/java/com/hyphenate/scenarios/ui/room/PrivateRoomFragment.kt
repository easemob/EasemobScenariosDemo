package com.hyphenate.scenarios.ui.room

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.RotateAnimation
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import coil.load
import com.hyphenate.easeui.base.EaseBaseFragment
import com.hyphenate.easeui.common.ChatLog
import com.hyphenate.easeui.common.bus.EaseFlowBus
import com.hyphenate.easeui.model.EaseEvent
import com.hyphenate.easeui.model.EaseProfile
import com.hyphenate.scenarios.DemoHelper
import com.hyphenate.scenarios.bean.MatchUserInfo
import com.hyphenate.scenarios.callkit.CallStatus
import com.hyphenate.scenarios.callkit.EM1v1CallKitEndReason
import com.hyphenate.scenarios.callkit.EM1v1CallKitManager
import com.hyphenate.scenarios.callkit.EMCallConstant
import com.hyphenate.scenarios.callkit.EMMatchManager
import com.hyphenate.scenarios.callkit.helper.CallDialogHelper
import com.hyphenate.scenarios.callkit.helper.CountdownTimerHelper
import com.hyphenate.scenarios.callkit.interfaces.ApplicationStatusListener
import com.hyphenate.scenarios.callkit.interfaces.CallKitListener
import com.hyphenate.scenarios.common.DemoConstant
import com.hyphenate.scenarios.common.helper.PrivateBgHelper
import com.hyphenate.scenarios.databinding.FragmentPrivateRoomLayoutBinding
import com.hyphenate.scenarios.interfaces.IPrivate1v1Request
import com.hyphenate.scenarios.interfaces.IPrivate1v1RoomResultView
import com.hyphenate.scenarios.viewmodel.PresenceViewModel
import com.hyphenate.scenarios.viewmodel.Private1v1RoomViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PrivateRoomFragment:EaseBaseFragment<FragmentPrivateRoomLayoutBinding>()
    ,IPrivate1v1RoomResultView, ApplicationStatusListener {
    companion object{
        const val TAG = "PrivateRoomFragment"
    }
    private val rotateAnimation: RotateAnimation = RotateAnimation(
        0f, 360f, // 从0度旋转到360度
        Animation.RELATIVE_TO_SELF, 0.5f, // 以自身宽度的50%为中心
        Animation.RELATIVE_TO_SELF, 0.5f // 以自身高度的50%为中心
    )
    private val presenceViewModel: PresenceViewModel by lazy { PresenceViewModel() }
    private var private1v1RoomViewModel:IPrivate1v1Request? = null

    private var phoneNumber: String = ""
    private var lastClickTime: Long = 0

    private val PERMISSION_REQ_ID = 22
    private val REQUESTED_PERMISSIONS = arrayOf(
        Manifest.permission.RECORD_AUDIO,
        Manifest.permission.CAMERA,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    )

    private val callListener = object : CallKitListener {

        override fun onCallStatusChanged(status: CallStatus, reason: String) {
            when(status){
                CallStatus.PREPARING -> {
                    // 匹配到user 更新ui
                    updateRemoteInfo()
                }
                CallStatus.JOIN -> {
                    // 收到对方 Accept 进入音视频页面
                    CallDialogHelper.dismissComingDialog()
                    EM1v1CallKitManager.startVideoCallActivity(mContext)
                }
                CallStatus.IDLE -> {
                    ChatLog.e(TAG,"CallStatus.IDLE reset")
                    updateMatchCard()
                }
                CallStatus.ALERT -> {
                    if (!EM1v1CallKitManager.isMatchCall){
                        updateMatchCard()
                    }
                }

                else -> {  }
            }
        }

        override fun onCallTokenWillExpire() {

        }

    }


    override fun getViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentPrivateRoomLayoutBinding {
       return FragmentPrivateRoomLayoutBinding.inflate(inflater)
    }

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        initRotateAnim()
        phoneNumber = DemoHelper.getInstance().dataModel.getPhoneNumber()

        //开启设备权限
        if (checkSelfPermission(REQUESTED_PERMISSIONS[0], PERMISSION_REQ_ID) &&
            checkSelfPermission(REQUESTED_PERMISSIONS[1], PERMISSION_REQ_ID) &&
            checkSelfPermission(REQUESTED_PERMISSIONS[2], PERMISSION_REQ_ID)) {
        }

    }

    override fun initListener() {
        super.initListener()
        binding?.let {
            it.ivPrivateRoomRefresh.setOnClickListener {
                val currentTime = System.currentTimeMillis()
                if (currentTime - lastClickTime < 1000) { // 1秒内不允许再次点击
                    return@setOnClickListener
                }
                lastClickTime = currentTime
                startLoadingAnim()
                private1v1RoomViewModel?.matchUserFromServer(phoneNumber)
            }
            it.ivPrivateCall.setOnClickListener { view ->
                presenceViewModel.publishPresence(DemoConstant.PRESENCE_BUSY)
                EM1v1CallKitManager.otherMatchInfo?.let { matchUser->
                    EM1v1CallKitManager.startCall(matchUser.matchedChatUser,EMCallConstant.EM1v1CallKit1v1Signaling)
                    CallDialogHelper.showComingDialog(
                        mContext,
                        isIncomingCall = false,
                        onCallClick = {

                        },
                        onRefuseClick = {
                            if (CountdownTimerHelper.isCountdownRunning()){
                                CountdownTimerHelper.cancelCountdown()
                            }
                            EM1v1CallKitManager.endCall( matchUser.matchedChatUser,EM1v1CallKitEndReason.CANCELEND.value )
                            presenceViewModel.publishPresence(DemoConstant.PRESENCE_ONLINE)
                        },
                    )
                }

            }
            EM1v1CallKitManager.addCallKitListener(TAG,callListener)
        }
    }

    override fun onResume() {
        super.onResume()
        val listener = EM1v1CallKitManager.getCallKitListener(TAG)
        listener?.let {}?:kotlin.run {
            EM1v1CallKitManager.addCallKitListener(TAG,callListener)
        }
    }

    override fun initData() {
        private1v1RoomViewModel?.matchUserFromServer(phoneNumber,true)
        EaseFlowBus.with<EaseEvent>(EaseEvent.EVENT.UPDATE + EaseEvent.TYPE.NOTIFY).register(this) {
            if (it.isNotifyChange){
                when(it.event){
                    DemoConstant.EVENT_UPDATE_UI -> {
                        ChatLog.e(TAG,"EaseFlowBus EVENT_UPDATE_UI")
                        updateMatchCard()
                    }
                    DemoConstant.EVENT_UPDATE_REMOTE_UI -> {
                        ChatLog.e(TAG,"EaseFlowBus EVENT_UPDATE_REMOTE_UI")
                        updateRemoteInfo()
                    }
                    else -> {}
                }
            }
        }
    }

    override fun initViewModel() {
        super.initViewModel()
        private1v1RoomViewModel = ViewModelProvider(this)[Private1v1RoomViewModel::class.java]
        private1v1RoomViewModel?.attachView(this)
    }

    override fun onDestroyView() {
        EM1v1CallKitManager.removeCallKitListener(TAG)
        CallDialogHelper.clearDialog()
        super.onDestroyView()
    }

    private fun initRotateAnim(){
        // 设置动画持续时间和其他属性
        rotateAnimation.duration = 3000 // 动画持续3秒
        rotateAnimation.repeatCount = 3  // Animation.INFINITE 无限循环
        rotateAnimation.interpolator = android.view.animation.LinearInterpolator() // 使用匀速插值器
    }

    private fun startLoadingAnim(){
        binding?.ivPrivateRoomRefresh?.startAnimation(rotateAnimation)
    }

    private fun endLoadingAnim(){
        binding?.ivPrivateRoomRefresh?.let { refreshView->
            refreshView.animation?.let {
                refreshView.clearAnimation() // 停止动画
            }
        }
    }

    override fun onDestroy() {
        endLoadingAnim()
        rotateAnimation.cancel()
        super.onDestroy()
    }

    override fun matchUserFail(code: Int, error: String) {
        ChatLog.e(TAG,"matchUserFail $code $error")
        if (code == 400 && error == "no user matched"){
            EM1v1CallKitManager.reset()
        }
        endLoadingAnim()
        updateMatchCard()
    }

    fun updateRemoteInfo(){
        ChatLog.e(TAG,"匹配到用户 更新ui信息: ${EM1v1CallKitManager.otherMatchInfo?.matchedChatUser}")
        EM1v1CallKitManager.otherMatchInfo?.let {
            private1v1RoomViewModel?.fetchMatchedUserInfo(it)
        }
    }

    override fun fetchMatchUserInfoSuccess(profile: EaseProfile?) {
        endLoadingAnim()
        profile?.let { pr->
            val match = DemoHelper.getInstance().dataModel.getMatchUserInfo(pr.id)
            match?.let {
                it.name = pr.name
                it.avatar = pr.avatar
                DemoHelper.getInstance().dataModel.updateMatchUserInfo(it)
                if (pr.id != EM1v1CallKitManager.getCurrentMatchInfo()?.matchedChatUser){
                    EM1v1CallKitManager.otherMatchInfo = it
                    updateMatchCard(true)
                }
            }?:kotlin.run {
                val matchUserInfo = MatchUserInfo(matchedChatUser = profile.id)
                matchUserInfo.avatar = profile.avatar
                matchUserInfo.name = profile.name
                DemoHelper.getInstance().dataModel.updateMatchUserInfo(matchUserInfo)
            }
        }
        setOtherMatchInfo()
        updateMatchCard(true)
    }

    override fun fetchMatchUserInfoFail(code: Int, error: String) {
        ChatLog.e(TAG,"fetchMatchUserInfoFail $code $error")
        setOtherMatchInfo()
    }

    private fun updateMatchCard(isMatched:Boolean = false){
        CoroutineScope(Dispatchers.Main).launch {
            binding?.let {
                if (isMatched){
                    // 如果匹配到用户 更新ui
                    val bg = PrivateBgHelper.randomBg()
                    it.ivUserCard.setImageDrawable(bg)
                    it.ivUserCard.visibility = View.VISIBLE
                    it.ivUserCardLayout.visibility = View.VISIBLE
                    // 显示图标
                    it.ivTab.visibility = View.VISIBLE
                    it.ivUserAvatar.visibility = View.VISIBLE
                    it.tvUserName.visibility = View.VISIBLE
                    it.ivPrivateCall.visibility = View.VISIBLE
                    it.ivPrivateCallBg.visibility = View.VISIBLE
                    // 隐藏空icon 和 文本提示
                    it.ivEmpty.visibility = View.GONE
                    it.tvContent.visibility = View.GONE
                }else{
                    // 未匹配到用户
                    // 显示icon 和 文本提示
                    it.ivEmpty.visibility = View.VISIBLE
                    it.tvContent.visibility = View.VISIBLE
                    // 隐藏图标
                    it.ivUserCard.visibility = View.GONE
                    it.ivUserCardLayout.visibility = View.GONE
                    it.ivTab.visibility = View.GONE
                    it.ivUserAvatar.visibility = View.GONE
                    it.tvUserName.visibility = View.GONE
                    it.ivPrivateCall.visibility = View.GONE
                    it.ivPrivateCallBg.visibility = View.GONE
                }
            }
        }
    }

    private fun setOtherMatchInfo(){
        val matchInfo = EM1v1CallKitManager.otherMatchInfo
        binding?.let {
            it.ivUserAvatar.load(matchInfo?.avatar)
            it.tvUserName.text = matchInfo?.getNotEmptyName()?:matchInfo?.matchedChatUser
        }
    }

    private fun checkSelfPermission(permission: String, requestCode: Int): Boolean {
        if (ContextCompat.checkSelfPermission(mContext, permission) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                mContext,
                REQUESTED_PERMISSIONS,
                requestCode
            )
            return false
        }
        return true
    }

    override fun onBackgroundStatusChanged(isOnForeground: Boolean) {
        if (!EM1v1CallKitManager.onCalling ){
            if (!isOnForeground){
                EMMatchManager.cancelMatch(
                    onSuccess = {
                        EM1v1CallKitManager.reset()
                        //不在通话中 并且切换到后台 取消匹配
                        updateMatchCard()
                    },
                    onError = {code, error ->
                        ChatLog.e(TAG," onBackground cancel match error $code $error ")
                    }
                )
            }
        }
    }

}