package com.hyphenate.scenarios.callkit.extensions

import android.content.Context
import android.text.Spannable
import com.hyphenate.easeui.R
import com.hyphenate.easeui.common.helper.EaseEmojiHelper

/**
 * Get emoji text from text message.
 */
internal fun String.getEmojiText(context: Context
                                 , emojiIconSize: Int = context.resources.getDimensionPixelSize(
        R.dimen.ease_chat_emoji_icon_size_show_in_spannable)
): Spannable {
    return EaseEmojiHelper.getEmojiText(context, this, emojiIconSize)
}