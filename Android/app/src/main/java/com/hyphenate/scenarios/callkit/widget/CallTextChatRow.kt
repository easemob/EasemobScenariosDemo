package com.hyphenate.scenarios.callkit.widget

import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Build
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.text.style.ImageSpan
import android.util.AttributeSet
import android.util.Log
import android.util.TypedValue
import android.widget.TextView
import androidx.core.content.ContextCompat
import coil.ImageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.hyphenate.easeui.EaseIM
import com.hyphenate.easeui.common.ChatTextMessageBody
import com.hyphenate.easeui.common.extensions.dpToPx
import com.hyphenate.easeui.common.extensions.getDateFormat
import com.hyphenate.easeui.model.EaseProfile
import com.hyphenate.easeui.widget.chatrow.EaseChatRow
import com.hyphenate.scenarios.R
import com.hyphenate.scenarios.callkit.CallChatRowConfig
import com.hyphenate.scenarios.callkit.extensions.getEmojiText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
open class CallTextChatRow @JvmOverloads constructor(
    private val context: Context,
    private val attrs: AttributeSet? = null,
    private val defStyleAttr: Int = 0,
    isSender: Boolean
) : EaseChatRow(context, attrs, defStyleAttr, isSender) {

    protected val contentView: TextView? by lazy { findViewById(R.id.tv_chatcontent) }

    override fun onInflateView() {
        inflater.inflate(R.layout.demo_call_row_text_message, this)
    }

    override fun onSetUpView() {
        message?.from?.let { from ->
            val match = EaseIM.getUserProvider()?.getUser(from)
            match?.let {info->
                // 动态拼接文本和图片
                val spannableStringBuilder = SpannableStringBuilder()

                addTime(spannableStringBuilder)

                if (CallChatRowConfig.enableShowAvatar){
                    val imageUrl:String? = info.avatar
                    imageUrl?.let {
                        // 使用 Coil 加载图片
                        val imageLoader = ImageLoader(context)
                        val request = ImageRequest.Builder(context)
                            .transformations(RoundedCornersTransformation(36f))
                            .size(18.dpToPx(context))
                            .data(imageUrl)
                            .build()

                        CoroutineScope(Dispatchers.IO).launch {
                            val result = imageLoader.execute(request)
                            if (result is SuccessResult) {
                                val drawable: Drawable = result.drawable
                                // 设置图片边界
                                drawable.setBounds(0, 0, drawable.intrinsicWidth, drawable.intrinsicHeight)
                                val imageSpan = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                    ImageSpan(drawable, ImageSpan.ALIGN_CENTER)
                                } else {
                                    ImageSpan(drawable, ImageSpan.ALIGN_BASELINE)
                                }
                                // 添加空格以便插入图片
                                spannableStringBuilder.append(" ")
                                spannableStringBuilder.setSpan(
                                    imageSpan,
                                    spannableStringBuilder.length - 1,
                                    spannableStringBuilder.length,
                                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                                )

                                // 在主线程更新 TextView
                                withContext(Dispatchers.Main) {
                                    contentView?.text = spannableStringBuilder
                                    setSpanMargin(1.5f,spannableStringBuilder)
                                    // 添加 nickname
                                    addNickName(info,spannableStringBuilder)
                                    setSpanMargin(1.5f,spannableStringBuilder)
                                    // 添加文本内容
                                    addContent(spannableStringBuilder)
                                }
                            }
                        }
                    }
                }else{
                    // 添加 nickname
                    addNickName(info,spannableStringBuilder)
                    // 添加文本内容
                    addContent(spannableStringBuilder)
                }
            }
        }

    }

    private fun setSpanMargin(sp:Float,span:SpannableStringBuilder){
        if (span.isNotEmpty()){
            val start = span.length - 1
            val spacingInPx = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                sp,
                resources.displayMetrics
            ).toInt()
            // 使用空格添加间距
            val space = SpannableStringBuilder(" ".repeat(spacingInPx / 2)) // 添加空格
            span.insert(start + 1, space)
        }
    }

    private fun addTime(span:SpannableStringBuilder){
        if (CallChatRowConfig.enableShowTime){
            val time = message?.getDateFormat(true)?:""
            if (time.isNotEmpty()){
                span.append(time)
                span.setSpan(
                    ForegroundColorSpan(ContextCompat.getColor(context,R.color.secondary_80)),
                    span.length - time.length,
                    span.length,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                setSpanMargin(1.5f,span)
            }
        }
    }

    // 添加 nickname
    private fun addNickName(user:EaseProfile,span:SpannableStringBuilder){
        if (CallChatRowConfig.enableShowNickname){
            val nickname = user.getNotEmptyName()
            if (span.isNotEmpty() && nickname.isNotEmpty()){
                span.append(nickname)
                span.setSpan(
                    ForegroundColorSpan(ContextCompat.getColor(context, com.hyphenate.easeui.R.color.ease_primary_80)), // 设置文本颜色
                    span.length - nickname.length,
                    span.length,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
        }
    }

    // 添加文本内容
    private fun addContent(span:SpannableStringBuilder){
        if (message?.body is ChatTextMessageBody){
            val body = (message?.body) as ChatTextMessageBody
            val content = body.message.getEmojiText(context)
            span.append(content)
            contentView?.text = span
        }
    }

}