package com.hyphenate.scenarios.callkit.widget

import android.content.Context
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.os.Build
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.AbsoluteSizeSpan
import android.text.style.ForegroundColorSpan
import android.text.style.ImageSpan
import android.text.style.StyleSpan
import android.text.style.TextAppearanceSpan
import android.util.AttributeSet
import android.util.Log
import android.util.TypedValue
import android.widget.TextView
import androidx.core.content.ContextCompat
import coil.ImageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.hyphenate.easeui.EaseIM
import com.hyphenate.easeui.common.ChatCmdMessageBody
import com.hyphenate.easeui.common.extensions.dpToPx
import com.hyphenate.easeui.common.extensions.getDateFormat
import com.hyphenate.easeui.model.EaseProfile
import com.hyphenate.easeui.widget.chatrow.EaseChatRow
import com.hyphenate.scenarios.R
import com.hyphenate.scenarios.bean.GiftEntityProtocol
import com.hyphenate.scenarios.callkit.CallChatRowConfig
import com.hyphenate.scenarios.callkit.EMCallConstant
import com.hyphenate.scenarios.callkit.extensions.parseGiftInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CallGiftChatRow @JvmOverloads constructor(
    private val context: Context,
    private val attrs: AttributeSet? = null,
    private val defStyleAttr: Int = 0,
    isSender: Boolean
) : EaseChatRow(context, attrs, defStyleAttr, isSender) {
    protected val contentView: TextView? by lazy { findViewById(R.id.tv_chatcontent) }

    override fun onInflateView() {
        inflater.inflate(R.layout.demo_call_row_gift_message, this)
    }

    override fun onSetUpView() {
        if (message?.body is ChatCmdMessageBody){
            val body = (message?.body) as ChatCmdMessageBody
            if (body.action() == EMCallConstant.EMMob1v1ChatGift){
                message?.from?.let { from ->
                    val giftEntity = message?.parseGiftInfo()
                    val match = EaseIM.getUserProvider()?.getUser(from)
                    match?.let { info ->
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
                                            // 添加 图片
                                            contentView?.text = spannableStringBuilder
                                            setSpanMargin(1.0f,spannableStringBuilder)
                                            // 添加 nickname
                                            addNickName(info,spannableStringBuilder)
                                            setSpanMargin(1.0f,spannableStringBuilder)
                                            // 添加 文本内容
                                            addContent(giftEntity,spannableStringBuilder)
                                            setSpanMargin(1.5f,spannableStringBuilder)
                                            // 添加 礼物icon
                                            addGift(giftEntity,spannableStringBuilder)
                                        }
                                    }
                                }
                            }
                        }else{
                            // 添加 nickname
                            addNickName(info,spannableStringBuilder)
                            setSpanMargin(1.5f,spannableStringBuilder)
                            // 添加文本内容
                            addContent(giftEntity,spannableStringBuilder)
                            setSpanMargin(1.5f,spannableStringBuilder)
                            // 添加 礼物icon
                            addGift(giftEntity,spannableStringBuilder)
                        }
                    }
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
    private fun addNickName(user: EaseProfile, span:SpannableStringBuilder){
        if (CallChatRowConfig.enableShowNickname){
            val nickname = user.getNotEmptyName()
            if (span.isNotEmpty() && nickname.isNotEmpty()){
                span.append(nickname)
                span.setSpan(
                    AbsoluteSizeSpan(14, true),
                    span.length - nickname.length
                    , span.length,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                span.setSpan(
                    TextAppearanceSpan(context,com.hyphenate.easeui.R.style.Ease_TextAppearance_Label_Medium),
                    span.length - nickname.length
                    , span.length,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
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
    private fun addContent(giftEntity: GiftEntityProtocol?,span:SpannableStringBuilder){
        giftEntity?.let { gift ->
            if (gift.giftName.isNullOrEmpty().not()){
                val content = context.resources.getString(R.string.em_call_gift_name,gift.giftName)
                span.append(content)
                span.setSpan(
                    TextAppearanceSpan(context,com.hyphenate.easeui.R.style.Ease_TextAppearance_Body_Medium),
                    span.length - content.length
                    , span.length,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                span.setSpan(
                    ForegroundColorSpan(ContextCompat.getColor(context, R.color.white)), // 设置文本颜色
                    span.length - content.length,
                    span.length,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
        }
    }

    private fun addGift(giftEntity: GiftEntityProtocol?,span:SpannableStringBuilder){
        giftEntity?.let { gift->
            gift.giftIcon?.let {
                // 使用 Coil 加载图片
                val imageLoader = ImageLoader(context)
                val request = ImageRequest.Builder(context)
                    .size(18.dpToPx(context))
                    .data(it)
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
                        span.append(" ")
                        span.setSpan(
                            imageSpan,
                            span.length - 1,
                            span.length,
                            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                        )
                        // 在主线程更新 TextView
                        withContext(Dispatchers.Main) {
                            contentView?.text = span
                        }
                    }
                }
            }
        }
    }
}