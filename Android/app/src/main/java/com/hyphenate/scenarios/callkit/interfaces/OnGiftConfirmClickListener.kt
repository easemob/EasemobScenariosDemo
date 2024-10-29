package com.hyphenate.scenarios.callkit.interfaces

import android.view.View
import com.hyphenate.scenarios.bean.GiftEntityProtocol

interface OnGiftConfirmClickListener {
    fun onConfirmClick(view: View?, bean: GiftEntityProtocol?)
    fun onFirstItem(firstBean: GiftEntityProtocol?)
}