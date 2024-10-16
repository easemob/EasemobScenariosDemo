package com.hyphenate.scenarios.callkit.widget

import android.app.Activity
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import com.hyphenate.easeui.common.extensions.showSoftKeyboard
import com.hyphenate.easeui.widget.EaseInputEditText
import com.hyphenate.scenarios.R
import com.hyphenate.scenarios.callkit.interfaces.CallChatPrimaryMenuListener
import com.hyphenate.scenarios.callkit.interfaces.ICallPrimaryMenu
import com.hyphenate.scenarios.databinding.DemoChatPrimaryMenuLayoutBinding

class CallChatPrimaryMenu@JvmOverloads constructor(
    private val context: Context,
    private val attrs: AttributeSet? = null,
    private val defStyleAttr: Int = 0,
): FrameLayout(context, attrs, defStyleAttr), View.OnClickListener,
    EaseInputEditText.OnEditTextChangeListener, TextWatcher,View.OnKeyListener, ICallPrimaryMenu {

    private val binding: DemoChatPrimaryMenuLayoutBinding by lazy { DemoChatPrimaryMenuLayoutBinding.inflate(
        LayoutInflater.from(context), this, true) }

    private var listener: CallChatPrimaryMenuListener? = null

    private var handler: Handler? = null

    private val inputManager: InputMethodManager by lazy { context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager }

    init {
        binding.etSendmessage.requestFocus()
        binding.etSendmessage.run {
            setHorizontallyScrolling(false)
            var maxLines = context.resources.getInteger(com.hyphenate.easeui.R.integer.ease_input_edit_text_max_lines)
            maxLines = if (maxLines <= 0) 4 else maxLines
            setMaxLines(maxLines)
        }
        binding.etSendmessage.setOnClickListener(this)
        binding.etSendmessage.setOnEditTextChangeListener(this)
        binding.etSendmessage.setOnKeyListener(this)
        binding.btnSend.setOnClickListener(this)
        binding.ivFaceNormal.setOnClickListener(this)
        binding.ivFaceChecked.setOnClickListener(this)
        binding.rlBottom.setOnClickListener(this)
        binding.ivGiftBtn.setOnClickListener(this)
        handler = Handler(Looper.getMainLooper())
        showNormalStatus()
    }

    override val editText: EditText
        get() = binding.etSendmessage

    override fun setChatPrimaryMenuListener(listener: CallChatPrimaryMenuListener?) {
        this.listener = listener
    }

    override fun showNormalStatus() {
        hideSoftKeyboard()
        binding.rootLayout.setBackgroundColor(
            ContextCompat.getColor(context, R.color.transparent)
        )
        binding.rlBottom.visibility = VISIBLE
        binding.ivGiftBtn.visibility = VISIBLE
        binding.edittextLayout.visibility = GONE
        binding.rlFace.visibility = GONE
        binding.btnSend.visibility = GONE
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        binding.etSendmessage.addTextChangedListener(this)
    }

    override fun onDetachedFromWindow() {
        binding.etSendmessage.removeTextChangedListener(this)
        handler = null
        super.onDetachedFromWindow()
    }


    override fun hideSoftKeyboard() {
        if (context !is Activity) {
            return
        }
        binding.etSendmessage.requestFocus()
        if (context.window.attributes.softInputMode != WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN) {
            context.currentFocus?.let {
                inputManager.hideSoftInputFromWindow(it.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
            }
        }
    }

    override fun onEmojiconInputEvent(emojiContent: CharSequence?) {
        binding.etSendmessage.append(emojiContent)
    }

    override fun onEmojiconDeleteEvent() {
        if (!TextUtils.isEmpty(binding.etSendmessage.text)) {
            val event =
                KeyEvent(0, 0, 0, KeyEvent.KEYCODE_DEL, 0, 0, 0, 0, KeyEvent.KEYCODE_ENDCALL)
            binding.etSendmessage.dispatchKeyEvent(event)
        }
    }

    override fun onTextInsert(text: CharSequence?) {
        val start = binding.etSendmessage.selectionStart
        val editable = binding.etSendmessage.editableText
        editable.insert(start, text)
        showTextStatus()
    }

    override fun showTextStatus() {
        binding.edittextLayout.visibility = VISIBLE
        binding.rlBottom.visibility = GONE
        binding.rootLayout.setBackgroundColor(
            ContextCompat.getColor(context, com.hyphenate.easeui.R.color.ease_color_background)
        )
        binding.btnSend.visibility = VISIBLE
        binding.rlFace.visibility = VISIBLE
        binding.ivGiftBtn.visibility = GONE
        showSoftKeyboard(editText)
        listener?.onToggleTextBtnClicked()
    }

    override fun showEmojiconStatus() {
        binding.edittextLayout.visibility = VISIBLE
        if (binding.ivFaceNormal.visibility == VISIBLE) {
            hideSoftKeyboard()
            showSelectedFaceImage()
        } else {
            showNormalFaceImage()
            postDelayed({
                showSoftKeyboard(editText)
                showTextStatus()
            }, 50) // 延迟时间，单位是毫秒
        }
        listener?.onToggleEmojiconClicked(binding.ivFaceChecked.visibility == VISIBLE)
    }

    override fun onClick(v: View?) {
        when(v?.id){
            R.id.iv_gift_btn->{
                listener?.onSendGiftBtnClicked()
            }
            R.id.btn_send -> {
                if (listener != null) {
                    val s = binding.etSendmessage.text.toString()
                    binding.etSendmessage.setText("")
                    listener?.onSendBtnClicked(s)
                }
            }
            R.id.et_sendmessage -> {
                showTextStatus()
            }
            R.id.iv_face_normal -> {
                showEmojiconStatus()
            }
            R.id.iv_face_checked -> {
                showEmojiconStatus()
            }
            R.id.rl_bottom -> {
                showTextStatus()
            }
            else -> {}
        }
    }

    override fun onClickKeyboardSendBtn(content: String?) {
        listener?.onSendBtnClicked(content)
    }

    override fun onEditTextHasFocus(hasFocus: Boolean) {
        listener?.onEditTextHasFocus(hasFocus)
    }

    override fun onKey(v: View?, keyCode: Int, event: KeyEvent?): Boolean {
        listener?.let {
            return it.editTextOnKeyListener(v, keyCode, event)
        }?:kotlin.run {
            return false
        }
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

    }

    override fun afterTextChanged(s: Editable?) {
        listener?.afterTextChanged(s)
    }


    /**
     * show soft keyboard
     * @param et
     */
    private fun showSoftKeyboard(et: EditText?) {
        et?.showSoftKeyboard()
    }

    private fun showNormalFaceImage() {
        binding.ivFaceNormal.visibility = VISIBLE
        binding.ivFaceChecked.visibility = GONE
    }

    private fun showSelectedFaceImage() {
        binding.ivFaceNormal.visibility = GONE
        binding.ivFaceChecked.visibility = VISIBLE
    }


}