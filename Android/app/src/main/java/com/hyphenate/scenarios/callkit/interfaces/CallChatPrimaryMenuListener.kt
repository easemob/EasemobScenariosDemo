package com.hyphenate.scenarios.callkit.interfaces

import android.text.Editable
import android.view.KeyEvent
import android.view.View

interface CallChatPrimaryMenuListener {
    /**
     * After typing on the editing text layout.
     */
    fun afterTextChanged(s: Editable?)

    /**
     * Edit text layout key events.
     */
    fun editTextOnKeyListener(v: View?, keyCode: Int, event: KeyEvent?):Boolean

    /**
     * toggle on/off text button
     */
    fun onToggleTextBtnClicked()

    /**
     * toggle on/off emoji icon
     * @param extend
     */
    fun onToggleEmojiconClicked(extend: Boolean)

    /**
     * when send button clicked
     * @param content
     */
    fun onSendBtnClicked(content: String?)

    /**
     * when send gift button clicked
     */
    fun onSendGiftBtnClicked()

    /**
     * if edit text has focus
     */
    fun onEditTextHasFocus(hasFocus: Boolean)
}