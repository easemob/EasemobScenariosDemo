package com.hyphenate.scenarios.repository

import com.hyphenate.cloud.HttpClientManager
import com.hyphenate.easeui.EaseIM
import com.hyphenate.easeui.common.ChatCallback
import com.hyphenate.easeui.common.ChatClient
import com.hyphenate.easeui.common.ChatError
import com.hyphenate.easeui.common.ChatException
import com.hyphenate.easeui.common.ChatLog
import com.hyphenate.easeui.common.ChatValueCallback
import com.hyphenate.easeui.provider.fetchUsersBySuspend
import com.hyphenate.scenarios.BuildConfig
import com.hyphenate.scenarios.bean.MatchUserInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONException
import org.json.JSONObject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class EMCall1v1RoomRepository: BaseRepository() {
    companion object {
        private const val TAG = "EMCall1v1RoomRepository"
        private const val MATCH_END = BuildConfig.APP_USER_MATCH
        private const val MATCH_URL = BuildConfig.APP_SERVER_PROTOCOL + "://" + BuildConfig.APP_SERVER_DOMAIN +
                BuildConfig.APP_BASE_USER + MATCH_END
        private const val CANCEL_MATCH_URL = BuildConfig.APP_SERVER_PROTOCOL + "://" + BuildConfig.APP_SERVER_DOMAIN +
                BuildConfig.APP_BASE_USER
    }

    /**
     * match user from server.
     */
    suspend fun matchUserFromServer(
        phoneNumber:String?,
        isExceptionCancelMatch:Boolean
    ):MatchUserInfo =
        withContext(Dispatchers.IO){
            suspendCoroutine { continuation ->
                matchUser(phoneNumber,isExceptionCancelMatch,object : ChatValueCallback<MatchUserInfo>{
                    override fun onSuccess(value: MatchUserInfo?) {
                        value?.let {
                            continuation.resume(it)
                        }
                    }

                    override fun onError(code: Int, errorMsg: String?) {
                        continuation.resumeWithException(ChatException(code, errorMsg))
                    }
                })
            }
        }

    suspend fun cancelMatchUserFromServer(
        phoneNumber:String?
    ):Int =
        withContext(Dispatchers.IO){
            suspendCoroutine { continuation ->
                cancelMatch(phoneNumber,object : ChatCallback{
                    override fun onSuccess() {
                        continuation.resume(ChatError.EM_NO_ERROR)
                    }

                    override fun onError(code: Int, error: String?) {
                        continuation.resumeWithException(ChatException(code, error))
                    }
                })
            }
        }

    suspend fun fetchMatchInfo(
        matchUserInfo: MatchUserInfo
    ) = withContext(Dispatchers.IO){
            if (matchUserInfo.matchedChatUser.isEmpty()){
                throw ChatException(ChatError.INVALID_PARAM, "matchedChatUser is null or empty.")
            }
            EaseIM.getUserProvider()?.fetchUsersBySuspend(mutableListOf(matchUserInfo.matchedChatUser))
        }

    private fun matchUser(
        phoneNumber:String?,
        isExceptionCancelMatch:Boolean,
        callBack:ChatValueCallback<MatchUserInfo>
    ){
        try {
            if (phoneNumber.isNullOrEmpty()){
                callBack.onError(ChatError.INVALID_PARAM, "The phoneNumber is incorrect")
                return
            }
            val headers: MutableMap<String, String> = HashMap()
            headers["Content-Type"] = "application/json"
            headers["Authorization"]= "Bearer ${ChatClient.getInstance().accessToken}"
            val requestBody = JSONObject()
            requestBody.putOpt("phoneNumber", phoneNumber)
            if (isExceptionCancelMatch){
                // 如果是异常未及时取消匹配（直接杀进程） 需要添加该参数
                requestBody.putOpt("sendCancelMatchNotify", false)
            }
            val response = HttpClientManager.httpExecute(
                MATCH_URL,
                headers,
                requestBody.toString(),
                HttpClientManager.Method_POST
            )
            ChatLog.e(TAG,"appserver matchUser url:$MATCH_URL body:${requestBody}")
            val code = response.code
            val responseInfo = response.content
            if (code == 200) {
                val `object` = JSONObject(responseInfo)
                if (`object`.has("matchedChatUser")){
                    val matchedChatUser = `object`.getString("matchedChatUser")
                    val result = MatchUserInfo(matchedChatUser)

                    if (`object`.has("agoraUid")){
                        result.agoraUid = `object`.getString("agoraUid")
                    }
                    if (`object`.has("channelName")){
                        result.channelName = `object`.getString("channelName")
                    }
                    if (`object`.has("rtcToken")){
                        result.rtcToken = `object`.getString("rtcToken")
                    }
                    if (`object`.has("matchedUser")){
                        result.matchedUser = `object`.getString("matchedUser")
                    }
                    callBack.onSuccess(result)
                }else{
                    callBack.onError(ChatError.GENERAL_ERROR,"matchedChatUser is null or empty")
                }
            } else {
                if (responseInfo != null && responseInfo.isNotEmpty()) {
                    var errorInfo: String? = null
                    try {
                        val responseObject = JSONObject(responseInfo)
                        errorInfo = responseObject.getString("errorInfo")
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

    private fun cancelMatch(
        phoneNumber:String?,
        callBack: ChatCallback
    ){
        if (phoneNumber.isNullOrEmpty()){
            callBack.onError(ChatError.INVALID_PARAM, "The phoneNumber is incorrect")
            return
        }

        val headers: MutableMap<String, String> = HashMap()
        headers["Content-Type"] = "application/json"
        headers["Authorization"]= "Bearer ${ChatClient.getInstance().accessToken}"
        val url = "$CANCEL_MATCH_URL/$phoneNumber$MATCH_END"
        val response = HttpClientManager.httpExecute(
            url,
            headers,
            "",
            HttpClientManager.Method_DELETE
        )
        ChatLog.e(TAG,"appserver cancelMatch url:$url")
        val code = response.code
        val responseInfo = response.content
        if (code == 200) {
            callBack.onSuccess()
        }else{
            if (responseInfo != null && responseInfo.isNotEmpty()) {
                var errorInfo: String? = null
                try {
                    val responseObject = JSONObject(responseInfo)
                    errorInfo = responseObject.getString("errorInfo")
                } catch (e: JSONException) {
                    e.printStackTrace()
                    errorInfo = responseInfo
                }
                callBack.onError(code, errorInfo)
            } else {
                callBack.onError(code, responseInfo)
            }
        }
    }

}