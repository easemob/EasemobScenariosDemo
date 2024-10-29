package com.hyphenate.scenarios.repository

import com.hyphenate.cloud.HttpClientManager
import com.hyphenate.easeui.EaseIM
import com.hyphenate.easeui.common.ChatClient
import com.hyphenate.easeui.common.ChatError
import com.hyphenate.easeui.common.ChatException
import com.hyphenate.easeui.common.ChatLog
import com.hyphenate.easeui.common.ChatValueCallback
import com.hyphenate.easeui.common.impl.OnError
import com.hyphenate.easeui.common.impl.OnSuccess
import com.hyphenate.easeui.model.EaseProfile
import com.hyphenate.easeui.model.EaseUser
import com.hyphenate.exceptions.HyphenateException
import com.hyphenate.scenarios.BuildConfig
import com.hyphenate.scenarios.DemoApplication
import com.hyphenate.scenarios.DemoHelper
import com.hyphenate.scenarios.R
import com.hyphenate.scenarios.base.ErrorCode
import com.hyphenate.scenarios.bean.LoginResult
import com.hyphenate.scenarios.bean.MatchUserInfo
import com.hyphenate.util.EMLog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONException
import org.json.JSONObject
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

/**
 * As the repository of ChatClient, handles ChatClient related logic
 */
class ChatClientRepository: BaseRepository() {

    companion object {
        private const val TAG = "ChatClientRepository"
        private const val LOGIN_URL = BuildConfig.APP_SERVER_PROTOCOL + "://" + BuildConfig.APP_SERVER_DOMAIN +
                BuildConfig.APP_BASE_USER + BuildConfig.APP_SERVER_LOGIN
        private const val SEND_SMS_URL = BuildConfig.APP_SERVER_PROTOCOL + "://" + BuildConfig.APP_SERVER_DOMAIN +
                BuildConfig.APP_SEND_SMS_FROM_SERVER
        private const val CANCEL_ACCOUNT = BuildConfig.APP_SERVER_PROTOCOL + "://" + BuildConfig.APP_SERVER_DOMAIN +
                BuildConfig.APP_BASE_USER
    }

    /**
     * 登录过后需要加载的数据
     * @return
     */
    suspend fun loadAllInfoFromHX(): Boolean =
        withContext(Dispatchers.IO) {
            suspendCoroutine { continuation ->
                ChatLog.e(TAG,"isLoggedInBefore ${ChatClient.getInstance().isLoggedInBefore} - autoLogin ${ChatClient.getInstance().options.autoLogin}")
                if (ChatClient.getInstance().isLoggedInBefore && ChatClient.getInstance().options.autoLogin) {
                    loadAllConversationsAndGroups()
                    continuation.resume(true)
                } else {
                    continuation.resumeWithException(ChatException(ErrorCode.EM_NOT_LOGIN, ""))
                }
            }
        }

    /**
     * 从本地数据库加载所有的对话及群组
     */
    private fun loadAllConversationsAndGroups() {
        // 从本地数据库加载所有的对话及群组
        ChatClient.getInstance().chatManager().loadAllConversations()
        ChatClient.getInstance().groupManager().loadAllGroups()
    }

    /**
     * 注册
     * @param userName
     * @param pwd
     * @return
     */
    suspend fun registerToHx(userName: String?, pwd: String?): String? =
        withContext(Dispatchers.IO) {
            suspendCoroutine { continuation ->
                try {
                    ChatClient.getInstance().createAccount(userName, pwd)
                    continuation.resume(userName)
                } catch (e: HyphenateException) {
                    continuation.resumeWithException(ChatException(e.errorCode, e.message))
                }
            }
        }

    /**
     * 登录到服务器，可选择密码登录或者token登录
     * @param userName
     * @param pwd
     * @param isTokenFlag
     * @return
     */
    suspend fun loginToServer(
        userName: String,
        pwd: String,
        isTokenFlag: Boolean
    ): EaseUser =
        withContext(Dispatchers.IO) {
            suspendCoroutine { continuation ->
                if (ChatClient.getInstance().isLoggedIn.not()) {
                    if (DemoHelper.getInstance().dataModel.isCustomSetEnable()) {
                        DemoHelper.getInstance().dataModel.getCustomAppKey()?.let {
                            if (it.isNotEmpty()) {
                                ChatClient.getInstance().changeAppkey(it)
                            }else{
                                ChatClient.getInstance().options.enableDNSConfig(true)
                                ChatClient.getInstance().changeAppkey(BuildConfig.APPKEY)
                            }
                        }
                    } else {
                        ChatClient.getInstance().changeAppkey(BuildConfig.APPKEY)
                    }
                }
                if (isTokenFlag) {
                    EaseIM.login(EaseProfile(userName), pwd, onSuccess = {
                        successForCallBack(continuation)
                    }, onError = { code, error ->
                        if(code == ChatError.USER_ALREADY_LOGIN){
                            if (EaseIM.getCurrentUser()?.id == userName){
                                successForCallBack(continuation)
                            }else{
                                EaseIM.logout(true)
                                continuation.resumeWithException(ChatException(code, error))
                            }
                        }else{
                            continuation.resumeWithException(ChatException(code, error))
                        }
                    })
                } else {
                    EaseIM.login(userName, pwd, onSuccess = {
                        successForCallBack(continuation)
                    }, onError = { code, error ->
                        continuation.resumeWithException(ChatException(code, error))
                    })
                }
            }
        }

    /**
     * 退出登录
     * @param unbindDeviceToken
     * @return
     */
    suspend fun logout(unbindDeviceToken: Boolean): Int =
        withContext(Dispatchers.IO) {
            suspendCoroutine { continuation ->
                EaseIM.logout(unbindDeviceToken, onSuccess = {
                    DemoHelper.getInstance().dataModel.setCurrentPhoneNumber("")
                    continuation.resume(ChatError.EM_NO_ERROR)
                }, onError = { code, error ->
                    continuation.resumeWithException(ChatException(code, error))
                })
            }
        }

    /**
     * Get all unread message count.
     */
    suspend fun getAllUnreadMessageCount(): Int =
        withContext(Dispatchers.IO) {
            ChatLog.d(TAG,"getAllUnreadMessageCount ${ChatClient.getInstance().chatManager().unreadMessageCount}")
            ChatClient.getInstance().chatManager().unreadMessageCount
        }

    private fun successForCallBack(continuation: Continuation<EaseUser>) {
        // get current user id
        val currentUser = ChatClient.getInstance().currentUser
        val user = EaseUser(currentUser)
        continuation.resume(user)

        // ** manually load all local groups and conversation
        loadAllConversationsAndGroups()
    }

    /**
     * Login to app server and get token.
     */
    suspend fun loginFromServer(userName: String, userPassword: String): LoginResult? =
        withContext(Dispatchers.IO) {
            suspendCoroutine { continuation ->
                loginFromAppServer(userName, userPassword, object : ChatValueCallback<LoginResult> {
                    override fun onSuccess(value: LoginResult?) {
                        DemoHelper.getInstance().dataModel.setCurrentPhoneNumber(value?.phone)
                        continuation.resume(value)
                    }

                    override fun onError(code: Int, error: String?) {
                        continuation.resumeWithException(ChatException(code, error))
                    }
                })
            }
        }

    private fun loginFromAppServer(
        userName: String,
        userPassword: String,
        callBack: ChatValueCallback<LoginResult>
    ) {
        try {
            val headers: MutableMap<String, String> = HashMap()
            headers["Content-Type"] = "application/json"
            val request = JSONObject()
            request.putOpt("phoneNumber", userName)
            request.putOpt("smsCode", userPassword)
            val url: String = LOGIN_URL
            EMLog.d("LoginToAppServer url : ", url)
            val response = HttpClientManager.httpExecute(
                url,
                headers,
                request.toString(),
                HttpClientManager.Method_POST
            )
            val code = response.code
            val responseInfo = response.content
            if (code == 200) {
                DemoHelper.getInstance().dataModel.setCurrentMatchInfo(responseInfo)
                val result = LoginResult()
                val `object` = JSONObject(responseInfo)
                if (`object`.has("phoneNumber")){
                    result.phone = `object`.getString("phoneNumber")
                }
                if (`object`.has("token")){
                    result.token = `object`.getString("token")
                }
                if (`object`.has("chatUserName")){
                    result.username = `object`.getString("chatUserName")
                }
                if (`object`.has("agoraUid")){
                    result.agoraUid = `object`.getString("agoraUid")
                    ChatLog.e(TAG,"Login agoraUid: ${result.agoraUid}")
                }
                if (`object`.has("avatarUrl")){
                    result.avatarUrl = `object`.getString("avatarUrl")
                }
                result.code = code
                result.username?.let {
                    val profile = EaseProfile(id = it, avatar = result.avatarUrl)
                    DemoHelper.getInstance().dataModel.insertUser(profile,true)
                    val currentMatchUser = MatchUserInfo(it)
                    currentMatchUser.matchedUser = result.phone
                    currentMatchUser.agoraUid = result.agoraUid
                    DemoHelper.getInstance().dataModel.updateMatchUserInfo(currentMatchUser)
                }
                callBack.onSuccess(result)
            } else {
                if (responseInfo != null && responseInfo.isNotEmpty()) {
                    var errorInfo: String? = null
                    try {
                        val responseObject = JSONObject(responseInfo)
                        errorInfo = responseObject.getString("errorInfo")
                        if (errorInfo.contains("phone number illegal")) {
                            errorInfo = DemoApplication.getInstance().getString(R.string.em_login_phone_illegal)
                        } else if (errorInfo.contains("verification code error") || errorInfo.contains(
                                "send SMS to get mobile phone verification code"
                            )
                        ) {
                            errorInfo = DemoApplication.getInstance().getString(R.string.em_login_illegal_code)
                        }
                    } catch (e: JSONException) {
                        e.printStackTrace()
                        errorInfo = responseInfo
                    }
                    callBack.onError(code, errorInfo)
                } else {
                    callBack.onError(code, responseInfo)
                }
            }
        } catch (e: Exception) {
            callBack.onError(ChatError.NETWORK_ERROR, e.message)
        }
    }

    /**
     * Get verification code from server.
     */
    suspend fun getVerificationCode(phoneNumber: String?): Int =
        withContext(Dispatchers.IO) {
            suspendCoroutine { continuation ->
                getVerificationCodeFromServer(
                    phoneNumber,
                    onSuccess = {
                        continuation.resume(ChatError.EM_NO_ERROR)
                    },
                    onError = { code, error ->
                        continuation.resumeWithException(ChatException(code, error))
                    }
                )
            }
        }

    private fun getVerificationCodeFromServer(phoneNumber: String?, onSuccess: OnSuccess, onError: OnError) {
        if (phoneNumber.isNullOrEmpty()) {
            onError(ChatError.INVALID_PARAM, getContext().getString(R.string.em_login_phone_empty))
            return
        }
        try {
            val headers: MutableMap<String, String> = java.util.HashMap()
            headers["Content-Type"] = "application/json"
            val url = "$SEND_SMS_URL/$phoneNumber/"
            EMLog.d("getVerificationCodeFromServe url : ", url)
            val response =
                HttpClientManager.httpExecute(url, headers, null, HttpClientManager.Method_POST)
            val code = response.code
            val responseInfo = response.content
            if (code == 200) {
                onSuccess()
            } else {
                if (responseInfo != null && responseInfo.isNotEmpty()) {
                    var errorInfo: String? = null
                    try {
                        val responseObject = JSONObject(responseInfo)
                        errorInfo = responseObject.getString("errorInfo")
                        if (errorInfo.contains("wait a moment while trying to send")) {
                            errorInfo =
                                getContext().getString(R.string.em_login_error_send_code_later)
                        } else if (errorInfo.contains("exceed the limit of")) {
                            errorInfo =
                                getContext().getString(R.string.em_login_error_send_code_limit)
                        }
                    } catch (e: JSONException) {
                        e.printStackTrace()
                        errorInfo = responseInfo
                    }
                    onError(code, errorInfo)
                } else {
                    onError(code, responseInfo)
                }
            }
        } catch (e: java.lang.Exception) {
            onError(ChatError.NETWORK_ERROR, e.message)
        }
    }

    /**
     * 注销账户
     * @return
     */
    suspend fun cancelAccount(): Int? =
        withContext(Dispatchers.IO) {
            suspendCoroutine { continuation ->
                cancelAccountFromServer(
                    onSuccess = {
                        continuation.resume(ChatError.EM_NO_ERROR)
                    },
                    onError = {code, error ->
                        continuation.resumeWithException(ChatException(code,error))
                    })
            }
        }

    private fun cancelAccountFromServer(onSuccess: OnSuccess, onError: OnError){
        try {
            val headers: MutableMap<String, String> = java.util.HashMap()
            headers["Content-Type"] = "application/json"
            headers["Authorization"] = "Bearer ${ChatClient.getInstance().accessToken}"
            val url = "$CANCEL_ACCOUNT/${DemoHelper.getInstance().dataModel.getPhoneNumber()}"
            EMLog.d("cancelAccountFromServer url : ", url)
            val response =
                HttpClientManager.httpExecute(url, headers, null, HttpClientManager.Method_DELETE)
            val code = response.code
            val responseInfo = response.content
            EMLog.d("cancelAccountFromServer", "code:$code response:$responseInfo")
            if (code == 200) {
                onSuccess()
            } else {
                if (responseInfo != null && responseInfo.isNotEmpty()) {
                    val errorInfo = try {
                        val responseObject = JSONObject(responseInfo)
                        responseObject.getString("errorInfo")
                    } catch (e: JSONException) {
                        e.printStackTrace()
                        responseInfo
                    }
                    onError(code, errorInfo)
                } else {
                    onError(code, responseInfo)
                }
            }
        } catch (e: java.lang.Exception) {
            onError(ChatError.NETWORK_ERROR, e.message)
        }
    }

}