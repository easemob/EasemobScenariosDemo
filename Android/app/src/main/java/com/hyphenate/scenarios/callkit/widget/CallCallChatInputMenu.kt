package com.hyphenate.scenarios.callkit.widget

import android.content.Context
import android.text.Editable
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.hyphenate.easeui.R
import com.hyphenate.easeui.common.ChatLog
import com.hyphenate.easeui.feature.chat.interfaces.EaseEmojiconMenuListener
import com.hyphenate.easeui.feature.chat.interfaces.IChatEmojiconMenu
import com.hyphenate.easeui.feature.chat.widgets.EaseEmojiconMenu
import com.hyphenate.easeui.model.EaseEmojicon
import com.hyphenate.scenarios.callkit.extensions.getEmojiText
import com.hyphenate.scenarios.callkit.interfaces.CallChatInputMenuListener
import com.hyphenate.scenarios.callkit.interfaces.CallChatPrimaryMenuListener
import com.hyphenate.scenarios.callkit.interfaces.ICallInputMenu
import com.hyphenate.scenarios.callkit.interfaces.ICallPrimaryMenu
import com.hyphenate.scenarios.databinding.DemoChatInputMenuLayoutBinding

class CallCallChatInputMenu@JvmOverloads constructor(
    private val context: Context,
    private val attrs: AttributeSet? = null,
    private val defStyleAttr: Int = 0
): FrameLayout(context, attrs, defStyleAttr),ICallInputMenu,
    EaseEmojiconMenuListener, CallChatPrimaryMenuListener {

    private val TAG = CallCallChatInputMenu::class.java.simpleName

    private val binding: DemoChatInputMenuLayoutBinding by lazy { DemoChatInputMenuLayoutBinding.inflate(
        LayoutInflater.from(context), this, true) }

    private var primaryMenu: ICallPrimaryMenu? = null
    private var emojiMenu: IChatEmojiconMenu? = null
    private var menuListener: CallChatInputMenuListener? = null

    init {
        showPrimaryMenu()
    }

    private fun showPrimaryMenu() {
        if (primaryMenu == null) {
            primaryMenu = CallChatPrimaryMenu(getContext())
        }
        primaryMenu?.let {
            if (it is View) {
                binding.primaryMenuContainer.visibility = VISIBLE
                binding.primaryMenuContainer.removeAllViews()
                binding.primaryMenuContainer.addView(it)
            }
            if (it is Fragment && getContext() is AppCompatActivity) {
                val manager = (getContext() as AppCompatActivity).supportFragmentManager
                manager.beginTransaction().replace(
                    R.id.primary_menu_container,
                    (it as Fragment)
                ).commitAllowingStateLoss()
            }
            it.setChatPrimaryMenuListener(this)
        }
    }

    private fun showEmojiconMenu() {
        if (emojiMenu == null) {
            emojiMenu = EaseEmojiconMenu(context)
            (emojiMenu as EaseEmojiconMenu).init()
            (emojiMenu as EaseEmojiconMenu).setBackgroundColor(
                ContextCompat.getColor(context,R.color.ease_color_background)
            )
        }
        if (emojiMenu is View) {
            postDelayed({
                binding.run {
                    extendMenuContainer.visibility = View.VISIBLE
                    extendMenuContainer.alpha = 0f
                    extendMenuContainer.animate().alpha(1f).setDuration(400).start()
                    extendMenuContainer.removeAllViews()
                    extendMenuContainer.addView(emojiMenu as View?)
                    emojiMenu?.setEmojiconMenuListener(this@CallCallChatInputMenu)
                }
            }, 200) // 延迟时间，单位是毫秒
        }
        if (emojiMenu is Fragment && context is AppCompatActivity) {
            postDelayed({
                binding.run {
                    extendMenuContainer.visibility = View.VISIBLE
                    extendMenuContainer.alpha = 0f
                    extendMenuContainer.animate().alpha(1f).setDuration(400).start()
                    emojiMenu?.setEmojiconMenuListener(this@CallCallChatInputMenu)
                }
            }, 200) // 延迟时间，单位是毫秒
            val manager = context.supportFragmentManager
            manager.beginTransaction().replace(
                R.id.extend_menu_container, (emojiMenu as Fragment)
            ).commitAllowingStateLoss()
        }
    }

    ///setEaseChatPrimaryMenuListener start
    override fun onSendBtnClicked(content: String?) {
        menuListener?.onSendMessage(content)
    }

    override fun onSendGiftBtnClicked() {
        menuListener?.onSendGiftBtnClicked()
    }

    override fun afterTextChanged(s: Editable?) {
        menuListener?.afterTextChanged(s)
    }

    override fun editTextOnKeyListener(v: View?, keyCode: Int, event: KeyEvent?): Boolean {
        menuListener?.let {
            return it.editTextOnKeyListener(v, keyCode, event)
        }?:kotlin.run {
            return false
        }
    }

    override fun onEditTextHasFocus(hasFocus: Boolean) {
        ChatLog.i(TAG, "onEditTextHasFocus: hasFocus = $hasFocus")
    }

    override fun onToggleEmojiconClicked(extend: Boolean) {
        showEmojiconMenu(extend)
    }

    override fun onToggleTextBtnClicked() {

    }
    /// setEaseChatPrimaryMenuListener end


    /// setEmojiconMenuListener start
    override fun onExpressionClicked(emojiIcon: Any?) {
        if (emojiIcon is EaseEmojicon) {
            val easeEmojicon: EaseEmojicon = emojiIcon
            if (easeEmojicon.type !== EaseEmojicon.Type.BIG_EXPRESSION) {
                primaryMenu?.onEmojiconInputEvent(
                    easeEmojicon.emojiText.getEmojiText(context)
                )
            } else {
                menuListener?.onExpressionClicked(emojiIcon)
            }
        } else {
            menuListener?.onExpressionClicked(emojiIcon)
        }
    }

    override fun onDeleteImageClicked() {
        primaryMenu?.onEmojiconDeleteEvent()
    }

    override fun onSendIconClicked() {
        primaryMenu?.run {
            editText?.let {
                val content = it.text.toString().trim()
                if (content.isNotEmpty()) {
                    it.setText("")
                    menuListener?.onSendMessage(content)
                }
            }
        }
    }
    /// setEmojiconMenuListener end

    override fun hideInputMenu() {
        binding.primaryMenuContainer.visibility = GONE
    }

    override fun showPrimaryMenu(show: Boolean) {
        if (show) {
            showPrimaryMenu()
        } else {
            binding.primaryMenuContainer.visibility = GONE
        }
    }

    override fun showEmojiconMenu(show: Boolean) {
        if (show) {
            showEmojiconMenu()
        } else {
            hideEmojiKeyboard()
        }
    }

    override fun hideSoftKeyboard() {
        primaryMenu?.hideSoftKeyboard()
    }

    override fun setChatInputMenuListener(listener: CallChatInputMenuListener?) {
        this.menuListener = listener
    }

    override val chatPrimaryMenu: ICallPrimaryMenu?
        get() = primaryMenu
    override val chatEmojiMenu: IChatEmojiconMenu?
        get() = emojiMenu

    override fun onBackPressed(): Boolean {
        if (binding.extendMenuContainer.visibility == VISIBLE) {
            binding.extendMenuContainer.visibility = GONE
            return false
        }
        return true
    }

    fun hideEmojiKeyboard() {
        binding.extendMenuContainer.visibility = View.GONE
    }
}