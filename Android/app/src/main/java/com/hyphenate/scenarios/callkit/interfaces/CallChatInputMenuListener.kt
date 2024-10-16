package com.hyphenate.scenarios.callkit.interfaces

import android.text.Editable
import android.view.KeyEvent
import android.view.View

interface CallChatInputMenuListener {

    /**
     * when send message button pressed
     *
     * @param content
     * message content
     */
    fun onSendMessage(content: String?)

    /**
     * when send gift message button pressed
     */
    fun onSendGiftBtnClicked()

    /**
     * After typing on the editing text layout.
     */
    fun afterTextChanged(s: Editable?){}

    /**
     * Edit text layout key events.
     */
    fun editTextOnKeyListener(v: View?, keyCode: Int, event: KeyEvent?):Boolean{ return false}

    /**
     * when big icon pressed
     * @param emojiIcon
     */
    fun onExpressionClicked(emojiIcon: Any?){}



}