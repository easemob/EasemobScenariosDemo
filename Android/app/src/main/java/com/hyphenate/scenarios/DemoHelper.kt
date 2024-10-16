package com.hyphenate.scenarios

import android.annotation.SuppressLint
import android.content.Context
import com.hyphenate.easeui.EaseIM
import com.hyphenate.easeui.common.ChatClient
import com.hyphenate.easeui.common.ChatLog
import com.hyphenate.easeui.common.ChatOptions
import com.hyphenate.easeui.common.PushConfigBuilder
import com.hyphenate.scenarios.callkit.EM1v1CallKitManager
import com.hyphenate.scenarios.callkit.EMCallKitConfig
import com.hyphenate.scenarios.callkit.activity.EM1v1CallActivity
import com.hyphenate.scenarios.callkit.helper.CallGiftHelper
import com.hyphenate.scenarios.common.DemoDataModel
import com.hyphenate.scenarios.common.ListenersWrapper
import com.hyphenate.scenarios.common.extensions.internal.checkAppKey
import com.hyphenate.scenarios.uikit.UIKitManager

class DemoHelper private constructor(){

    val dataModel: DemoDataModel by lazy { DemoDataModel(context) }
    var hasAppKey = false
    lateinit var context: Context

    @Synchronized
    fun init(context: Context) {
        this.context = context.applicationContext
    }

    /**
     * Check if the SDK has been initialized.
     */
    fun isSDKInited(): Boolean {
        return EaseIM.isInited()
    }

    /**
     * Initialize the SDK.
     */
    @Synchronized
    fun initSDK() {
        if (::context.isInitialized.not()) {
            ChatLog.e(TAG, "Please call init method first.")
            return
        }
        initChatOptions(context).apply {
            requireAck = false
            hasAppKey = checkAppKey(context)
            if (!hasAppKey) {
                ChatLog.e(TAG, "App key is null or empty.")
                return
            }
            // Register necessary listeners
            ListenersWrapper.registerListeners()
            EaseIM.init(context, this)
            if (EaseIM.isInited()) {
                // debug mode, you'd better set it to false, if you want release your App officially.
                ChatClient.getInstance().setDebugMode(true)
                // Set the UIKit options.
                addUIKitSettings()
                addCallKitSettings()
            }
        }
    }

    private fun addUIKitSettings() {
        UIKitManager.addUIKitSettings(context)
    }

    private fun addCallKitSettings(){
        //设置callkit配置项
        EMCallKitConfig().apply {
            // Set call timeout.
            callTimeOut = 30 * 1000
            // Set RTC appId.
            agoraAppId = BuildConfig.RTC_APPID
            // Set whether token verification is required.
            enableRTCToken = true
            ChatLog.e(TAG,"addCallKitSettings")
            EM1v1CallKitManager.setCallKitConfig(context,this)
        }
        EM1v1CallKitManager.register1v1CallClass(EM1v1CallActivity::class.java)
        CallGiftHelper.initGiftData(context)
    }

    /**
     * Get the notifier.
     */
    fun getNotifier() = EaseIM.getNotifier()


    /**
     * Set chat options.
     * Note: Developers need to set the options according to needs.
     */
    private fun initChatOptions(context: Context): ChatOptions {
        return ChatOptions().apply {
            // set the appkey
            appKey = BuildConfig.APPKEY
            // set if accept the invitation automatically, default true
            acceptInvitationAlways = false
            // set if you need read ack
            requireAck = true
            // Set whether the sent message is included in the message listener, default false
            isIncludeSendMessageInMessageListener = true

            dataModel.setUseFCM(true)

            /**
             * Note: Developers need to apply your own push accounts and replace the following
             */
            pushConfig = PushConfigBuilder(context)
                .build()

            if (dataModel.isDeveloperMode()) {

                if (dataModel.getCustomAppKey().isNotEmpty()){
                    dataModel.getCustomAppKey().let {
                        appKey = it
                    }
                }

                if (dataModel.isCustomServerEnable()) {
                    // Turn off DNS configuration
                    enableDNSConfig(false)
                    restServer = dataModel.getRestServer()?.ifEmpty { null }
                    setIMServer(dataModel.getIMServer()?.let {
                        if (it.contains(":")) {
                            imPort = it.split(":")[1].toInt()
                            it.split(":")[0]
                        } else {
                            it.ifEmpty { null }
                        }
                    })
                    val port = dataModel.getIMServerPort()
                    if (port != 0) {
                        imPort = port
                    }
                }
            }
        }
    }

    companion object {
        private const val TAG = "DemoHelper"
        @SuppressLint("StaticFieldLeak")
        private var instance: DemoHelper? = null
        fun getInstance(): DemoHelper {
            if (instance == null) {
                synchronized(DemoHelper::class.java) {
                    if (instance == null) {
                        instance = DemoHelper()
                    }
                }
            }
            return instance!!
        }
    }
}