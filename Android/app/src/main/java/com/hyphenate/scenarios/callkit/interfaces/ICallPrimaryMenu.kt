package com.hyphenate.scenarios.callkit.interfaces

import android.widget.EditText

interface ICallPrimaryMenu {

    /**
     * Show EditText but hide soft keyboard.
     */
    fun showNormalStatus()

    /**
     * Hide soft keyboard.
     */
    fun hideSoftKeyboard()

    /**
     * Show emoticon extend menu and EditText, hide soft keyboard and other status.
     */
    fun showEmojiconStatus()

    /**
     * Enter emoticon event
     * @param emojiContent
     */
    fun onEmojiconInputEvent(emojiContent: CharSequence?)

    /**
     * Delete emoticon event
     */
    fun onEmojiconDeleteEvent()

    /**
     * Insert text
     * @param text
     */
    fun onTextInsert(text: CharSequence?)

    /**
     * Show EditText and soft keyboard.
     */
    fun showTextStatus()

    /**
     * Get EditText
     * @return
     */
    val editText: EditText?

    /**
     * Set up monitoring
     * @param listener
     */
    fun setChatPrimaryMenuListener(listener: CallChatPrimaryMenuListener?)

}