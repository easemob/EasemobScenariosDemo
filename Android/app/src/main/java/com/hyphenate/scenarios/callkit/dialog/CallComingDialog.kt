package com.hyphenate.scenarios.callkit.dialog

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import coil.load
import com.hyphenate.easeui.common.extensions.dpToPx
import com.hyphenate.easeui.widget.EaseImageView
import com.hyphenate.scenarios.R
import com.hyphenate.scenarios.bean.MatchUserInfo
import com.hyphenate.scenarios.callkit.EM1v1CallKitManager

class CallComingDialog(
    context: Context,
    private var isIncomingCall:Boolean = true,
    private var onCallClickListener: ((v:View) -> Unit)? = {},
    private val onCallRefuseClick: ((v:View) -> Unit)? = {},
) : Dialog(context), View.OnClickListener {

    private var ivAvatar:EaseImageView? = null
    private var ivCall: AppCompatImageView? = null
    private var ivRefuse: AppCompatImageView? = null
    private var tvName: AppCompatTextView? = null
    private var tvContent: AppCompatTextView? = null
    private var matchInfo:MatchUserInfo? = null
    private var content:String = ""

    init {
        val view = LayoutInflater.from(context).inflate(R.layout.demo_call_coming_dialog_layout, null)
        setContentView(view)
        setCancelable(false)
        window?.setGravity(Gravity.BOTTOM)
        setCanceledOnTouchOutside(false)
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        ivAvatar = findViewById<EaseImageView>(R.id.iv_avatar)
        ivCall = findViewById<EaseImageView>(R.id.iv_call)
        ivRefuse = findViewById<EaseImageView>(R.id.iv_refuse)
        tvName = findViewById<AppCompatTextView>(R.id.tv_name)
        tvContent = findViewById<AppCompatTextView>(R.id.tv_content)

        content = if (isIncomingCall){
            context.resources.getString(R.string.em_private_room_coming_call)
        }else{
            context.resources.getString(R.string.em_private_room_send_call)
        }

        matchInfo = EM1v1CallKitManager.otherMatchInfo
        matchInfo?.let {
            tvName?.text = it.getNotEmptyName()
            ivAvatar?.load(it.avatar)
        }
        tvContent?.text = content
        if (isIncomingCall){
            ivCall?.visibility = View.VISIBLE
        }else{
            ivCall?.visibility = View.GONE
        }

        ivCall?.setOnClickListener(this)
        ivRefuse?.setOnClickListener(this)

        ivAvatar?.setBorderWidth(2.dpToPx(context))
        ivAvatar?.setBorderColor(Color.WHITE)
        EM1v1CallKitManager.otherMatchInfo?.let {
            ivAvatar?.load(it.avatar)
            tvName?.text = it.getNotEmptyName()?:it.id
        }
    }

    override fun onClick(v: View) {
        when(v.id){
            R.id.iv_call -> {
                onCallClickListener?.invoke(v)
                dismiss()
            }
            R.id.iv_refuse -> {
                onCallRefuseClick?.invoke(v)
                dismiss()
            }
            else -> {}
        }
    }
}