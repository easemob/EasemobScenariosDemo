package com.hyphenate.scenarios.notify

import com.hyphenate.easeui.common.ChatMessage

interface NotificationGestureListener {
    fun onClick(message: ChatMessage?){}
    fun onSwipeLeft(){}

}