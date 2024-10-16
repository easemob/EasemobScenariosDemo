package com.hyphenate.scenarios.callkit

import android.content.Context
import android.content.Intent
import android.view.TextureView
import androidx.viewbinding.ViewBinding
import com.hyphenate.easeui.EaseIM
import com.hyphenate.easeui.common.ChatCallback
import com.hyphenate.easeui.common.ChatClient
import com.hyphenate.easeui.common.ChatLog
import com.hyphenate.easeui.common.ChatLoginExtensionInfo
import com.hyphenate.easeui.common.ChatMessage
import com.hyphenate.easeui.interfaces.EaseConnectionListener
import com.hyphenate.easeui.interfaces.EaseMessageListener
import com.hyphenate.scenarios.DemoHelper
import com.hyphenate.scenarios.bean.MatchUserInfo
import com.hyphenate.scenarios.callkit.activity.EM1v1CallActivity
import com.hyphenate.scenarios.callkit.activity.EMBaseCallActivity
import com.hyphenate.scenarios.callkit.helper.CallMessageHelper
import com.hyphenate.scenarios.callkit.helper.CountdownTimerHelper
import com.hyphenate.scenarios.callkit.helper.CountdownTimerHelper.cancelCountdown
import com.hyphenate.scenarios.callkit.interfaces.ApplicationStatusListener
import com.hyphenate.scenarios.callkit.interfaces.CallKitListener
import com.hyphenate.scenarios.callkit.interfaces.CallProtocol
import io.agora.rtc2.Constants
import io.agora.rtc2.IRtcEngineEventHandler
import io.agora.rtc2.RtcEngine
import io.agora.rtc2.video.VideoCanvas
import io.agora.rtc2.video.VideoEncoderConfiguration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Collections

object EM1v1CallKitManager:CallProtocol{
    private val TAG: String = EM1v1CallKitManager::class.java.simpleName

    val handlers = Collections.synchronizedMap(mutableMapOf<String,CallKitListener>())

    private var mRtcEngine: RtcEngine? = null

    var appContext: Context? = null

    var callId = ""
    var currentEndTime = 0L

    var remoteUid: Int = 0
    private var localUid: Int = 0

    var channelName: String? = null
    var rtcToken: String? = null

    var otherMatchInfo: MatchUserInfo? = null

    var onCalling = false
    var isMatchCall = true

    var statusListener: ApplicationStatusListener? = null

    var callKitConfig: EMCallKitConfig? = null

    var curCallCls: Class<out EMBaseCallActivity<out ViewBinding>>? = null

    /**
     * If use the default class, you should register it to AndroidManifest
     */
    private var default1vCallCls: Class<out EM1v1CallActivity> = EM1v1CallActivity::class.java

    private val mRtcEventHandler = object : IRtcEngineEventHandler(){

        override fun onError(code: Int) {
            ChatLog.e(TAG,"Rtc onError $code")
            endCall("",EM1v1CallKitEndReason.RTCERROR.value)
        }

        override fun onTokenPrivilegeWillExpire(token: String?) {
            ChatLog.e(TAG,"onTokenPrivilegeWillExpire")
            handlers.values.forEach {
                CoroutineScope(Dispatchers.Main).launch {
                    it?.onCallTokenWillExpire()
                }
            }
        }

        override fun onJoinChannelSuccess(channel: String?, uid: Int, elapsed: Int) {
            onCalling = true
            handlers.values.forEach {
                CoroutineScope(Dispatchers.Main).launch {
                    it?.onJoinChannelSuccess(channel, uid, elapsed)
                }
            }
        }

        override fun onUserJoined(uid: Int, elapsed: Int) {
            ChatLog.d(TAG,"onUserJoined uid:$uid elapsed:$elapsed")
            remoteUid = uid
            onCallStatusChanged(CallStatus.CALLING,"$uid")
        }

        override fun onUserOffline(uid: Int, reason: Int) {
            ChatLog.d(TAG,"onUserOffline uid:$uid reason:$reason")
            var reasonDescription = ""
            when(reason){
                // 用户主动离开。
                UserOfflineReason.USER_OFFLINE_QUIT -> {
                    reasonDescription = "对方离开"
                }
                // 因过长时间收不到对方数据包，SDK 判定该远端用户超时掉线。注意：在网络连接不稳定时，该判定可能会有误。
                UserOfflineReason.USER_OFFLINE_DROPPED -> {
                    reasonDescription = "对方掉线"
                }
                else -> {}
            }
            onCallStatusChanged(CallStatus.ENDED,reasonDescription)
        }

        override fun onFirstRemoteVideoFrame(uid: Int, width: Int, height: Int, elapsed: Int) {
            remoteUid = uid
        }

        override fun onFirstRemoteAudioFrame(uid: Int, elapsed: Int) {
            remoteUid = uid
        }
    }

    private val connectionListener = object : EaseConnectionListener(){

        override fun onLogout(errorCode: Int, info: ChatLoginExtensionInfo?) {
            super.onLogout(errorCode, info)
            reset()
        }
    }


    fun setCallKitConfig(context:Context,config:EMCallKitConfig?) {
        this.appContext = context
        this.callKitConfig = config
        addMessageListener()
        addConnectionListener()
    }

    fun initRtcEngine(){
        try {
            callKitConfig?.let {
                val agoraAppId = it.agoraAppId
                mRtcEngine = RtcEngine.create(appContext, agoraAppId, mRtcEventHandler)
                // 设置为直播模式 角色设置为主播
                mRtcEngine?.setChannelProfile(Constants.CHANNEL_PROFILE_LIVE_BROADCASTING)
                mRtcEngine?.setClientRole(Constants.CLIENT_ROLE_BROADCASTER)
                mRtcEngine?.enableAudioVolumeIndication(500,5,false)
                mRtcEngine?.enableVideo()
                mRtcEngine?.setVideoEncoderConfiguration(
                    VideoEncoderConfiguration(
                        VideoEncoderConfiguration.VD_1280x720,
                        VideoEncoderConfiguration.FRAME_RATE.FRAME_RATE_FPS_15,
                        VideoEncoderConfiguration.STANDARD_BITRATE,
                        VideoEncoderConfiguration.ORIENTATION_MODE.ORIENTATION_MODE_FIXED_PORTRAIT
                    )
                )
            }
        } catch (e: Exception) {
            ChatLog.e(TAG, e.message)
            throw RuntimeException("NEED TO check rtc sdk init fatal error ${e.message}")
        }
    }

    /**
     * 渲染 本地视图
     */
    fun renderLocalCanvas(view: TextureView){
        onCalling = true
        ChatLog.d(TAG,"renderLocalCanvas $localUid")
        mRtcEngine?.setupLocalVideo(createCanvas(view, localUid))
        mRtcEngine?.startPreview()
    }

    /**
     * 渲染 远程视图
     */
    fun renderRemoteCanvas(view: TextureView){
        ChatLog.d(TAG,"renderRemoteCanvas $remoteUid")
        mRtcEngine?.setupRemoteVideo(createCanvas(view, remoteUid))
    }

    /**
     * 创建画布
     */
    private fun createCanvas(view:TextureView,agoraUid:Int):VideoCanvas{
        return VideoCanvas(view, VideoCanvas.RENDER_MODE_HIDDEN, agoraUid)
    }

    /**
     * 创建SurfaceView
     */
    fun createSurfaceView():TextureView?{
        return RtcEngine.CreateTextureView(appContext)
    }

    fun getCurrentMatchInfo():MatchUserInfo?{
        return DemoHelper.getInstance().dataModel.getMatchUserInfo(ChatClient.getInstance().currentUser)
    }

    /**
     * 加入频道
     */
    fun joinChannel(){
        getCurrentMatchInfo()?.let {
            it.agoraUid?.let {
                localUid = if (it.isNotEmpty()){ it.toInt() }else{ 0 }
            }
        }
        ChatLog.e(TAG,"joinChannel $channelName - $localUid - $rtcToken")
        mRtcEngine?.joinChannel(rtcToken, channelName , "", localUid)
    }

    /**
     * 更新 当前用户 rtcToken
     */
    fun updateRtcToken(token:String?){
        this.rtcToken = token
        getCurrentMatchInfo()?.apply {
            rtcToken = token
        }?.let {
            DemoHelper.getInstance().dataModel.updateMatchUserInfo(it)
        }
    }

    /**
     * Register the activity which you want to display video call or audio call and you have registered in AndroidManifest.xml
     * @param videoCallClass
     */
    fun register1v1CallClass(videoCallClass: Class<out EM1v1CallActivity>) {
        default1vCallCls = videoCallClass
    }

    /***
     * 添加 call kit监听
     * @param tag
     * @param listener
     * @return
     */
    fun addCallKitListener(tag:String,listener: CallKitListener) {
        handlers[tag] = listener
    }

    fun getCallKitListener(tag:String):CallKitListener?{
        return handlers[tag]
    }

    /***
     * 移除 call kit监听
     * @param tag
     * @return
     */
    fun removeCallKitListener(tag:String){
        handlers.remove(tag)
    }

    fun setApplicationStatusListener(listener:ApplicationStatusListener?){
        this.statusListener = listener
    }

    fun startVideoCallActivity(context: Context) {
        curCallCls = default1vCallCls
        Intent(context, curCallCls).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(this)
        }
    }

    /**
     * If you call [.startSingleCall], [.startSingleCall]
     * or [.startInviteMultipleCall], you should call the method of [.releaseCall] when the [.curCallCls] is finishing.
     */
    fun releaseCall() {
        if (curCallCls != null) {
            curCallCls = null
        }
    }

    private fun addMessageListener(){
        EaseIM.addChatMessageListener(chatMessageListener)
    }

    fun removeMessageListener(){
        EaseIM.removeChatMessageListener(chatMessageListener)
    }

    fun addConnectionListener(){
        EaseIM.addConnectionListener(connectionListener)
    }

    fun removeConnectionListener(){
        EaseIM.removeConnectionListener(connectionListener)
    }

    private val chatMessageListener = object : EaseMessageListener() {

        override fun onMessageReceived(messages: MutableList<ChatMessage>?) {
            CallMessageHelper.onMsgDidReceive(messages)
        }

        override fun onCmdMessageReceived(messages: MutableList<ChatMessage>?) {
            CallMessageHelper.onCmdMsgDidReceive(messages)
        }
    }

    // CallProtocol
    override fun startCall(userId: String,callType:String) {
        EMCallConstant.EM1v1CallKit1v1Signaling
        callId = System.currentTimeMillis().toString()
        CallMessageHelper.sendStartCallMsg(userId,callId,callType,object : ChatCallback{
            override fun onSuccess() {
                ChatLog.d(TAG,"sendStartCallMsg suc $userId")
                // 呼叫成功 开启计时
                CountdownTimerHelper.startCountdown(
                    onTick = {},
                    onFinish = {
                        cancelCountdown()
                        onCallStatusChanged(CallStatus.ENDED,EM1v1CallKitEndReason.TIMEOUTEND.value)
                    })
            }

            override fun onError(code: Int, error: String?) {
                ChatLog.e( TAG,"sendStartCallMsg error $code $error")
            }
        })
    }

    override fun endCall(userId: String,reason: String) {
        endCallReset()
        CallMessageHelper.sendEndedCallMsg(reason,userId, callId,object : ChatCallback{
            override fun onSuccess() {
                onCalling = false
                CallMessageHelper.insertEndMessage(userId, callId)
            }

            override fun onError(code: Int, error: String?) {
                ChatLog.e( TAG,"sendEndedCallMsg error $code $error")
            }
        })
    }

    override fun acceptCall(userId:String) {
        CallMessageHelper.sendAcceptCallMsg(userId, callId,object : ChatCallback{
            override fun onSuccess() {
                appContext?.let { it1 -> startVideoCallActivity(it1) }
            }

            override fun onError(code: Int, error: String?) {
                ChatLog.e(TAG,"sendAcceptCallMsg fail $code $error")
            }
        })
    }

    fun onCallStatusChanged(status: CallStatus,reason:String){
        CoroutineScope(Dispatchers.Main).launch {
            if (status == CallStatus.ENDED){
                // 结束本次通话 退出channel 但保持otherMatchInfo 匹配信息 可以重新呼叫
                endCallReset()
            }else if (status == CallStatus.IDLE){
                reset()
            }
            handlers.values.forEach {
                it?.onCallStatusChanged(status,reason)
            }
        }
    }

    fun endCallReset(){
        mRtcEngine?.let {
            it.stopPreview()
            it.disableAudio()
            it.disableVideo()
            it.leaveChannel()
        }
        onCalling = false
    }

    fun reset(){
        callId = ""
        otherMatchInfo = null
        ChatLog.e(TAG,"otherMatchInfo = null")
        remoteUid = 0
        channelName = ""
        rtcToken = null
        currentEndTime = 0L
        mRtcEngine?.let {
            it.stopPreview()
            it.disableAudio()
            it.disableVideo()
            it.leaveChannel()
        }
        mRtcEngine = null
        onCalling = false
    }
}