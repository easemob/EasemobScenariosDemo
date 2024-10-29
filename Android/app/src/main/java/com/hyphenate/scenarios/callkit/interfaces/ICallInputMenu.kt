package com.hyphenate.scenarios.callkit.interfaces

import com.hyphenate.easeui.feature.chat.interfaces.IChatEmojiconMenu

interface ICallInputMenu {

    /**
     * Get menu
     * @return
     */
    val chatPrimaryMenu: ICallPrimaryMenu?

    /**
     * Get emoji menu
     * @return
     */
    val chatEmojiMenu: IChatEmojiconMenu?

    /**
     * Hide input menu exclude top extend menu.
     */
    fun hideInputMenu()

    /**
     * Whether to show the primary menu
     * @param show
     */
    fun showPrimaryMenu(show: Boolean)

    /**
     * Whether to show the emoji menu
     * @param show
     */
    fun showEmojiconMenu(show: Boolean)

    /**
     * Hide soft keyboard
     */
    fun hideSoftKeyboard()

    /**
     * Set menu listener
     * @param listener
     */
    fun setChatInputMenuListener(listener: CallChatInputMenuListener?)

    /**
     * Click back
     * @return
     */
    fun onBackPressed(): Boolean

}