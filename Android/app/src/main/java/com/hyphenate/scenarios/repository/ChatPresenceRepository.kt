package com.hyphenate.scenarios.repository

import com.hyphenate.easeui.common.ChatClient
import com.hyphenate.easeui.common.ChatPresenceManager
import com.hyphenate.scenarios.common.suspend.fetchUserPresenceStatus
import com.hyphenate.scenarios.common.suspend.publishExtPresence
import com.hyphenate.scenarios.common.suspend.subscribeUsersPresence
import com.hyphenate.scenarios.common.suspend.unSubscribeUsersPresence
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ChatPresenceRepository(
    private val presenceManager: ChatPresenceManager = ChatClient.getInstance().presenceManager(),
) {

    suspend fun publishPresence(customStatus: String) =
        withContext(Dispatchers.IO) {
            presenceManager.publishExtPresence(customStatus)
        }


    suspend fun subscribePresences(userIds:MutableList<String>,expiry:Long) =
        withContext(Dispatchers.IO) {
            presenceManager.subscribeUsersPresence(userIds,expiry)
        }


    suspend fun unSubscribePresences(userIds:MutableList<String>) =
        withContext(Dispatchers.IO) {
            presenceManager.unSubscribeUsersPresence(userIds)
        }

    suspend fun fetchPresenceStatus(userIds:MutableList<String>) =
        withContext(Dispatchers.IO) {
            presenceManager.fetchUserPresenceStatus(userIds)
        }


}