package com.hyphenate.scenarios.callkit.helper

import android.app.Activity
import android.content.Context
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.hyphenate.scenarios.callkit.dialog.CallComingDialog

object CallDialogHelper {
    private var dialog:CallComingDialog? = null
    fun showComingDialog(
        mContext:Context,
        isIncomingCall:Boolean = true,
        onCallClick: (View?) -> Unit = {},
        onRefuseClick: (View?) -> Unit = {},
    ){
        clearDialog()
        val activity = when(mContext){
            is Activity -> mContext
            is Fragment -> mContext.requireActivity()
            is FragmentActivity -> mContext
            else -> return // 如果不是有效的上下文，直接返回
        }
        // 检查 Activity 是否正在销毁
        if (activity.isFinishing) {
            return // 如果正在销毁，直接返回
        }
        dialog = CallComingDialog(
            isIncomingCall = isIncomingCall,
            context = mContext as AppCompatActivity,
            onCallClickListener = {onCallClick(it)},
            onCallRefuseClick = { onRefuseClick(it)}
        )
        dialog?.show()
    }

    fun isShowDialog():Boolean{
        dialog?.let {
            return it.isShowing
        }
       return false
    }

    fun dismissComingDialog(){
        dialog?.dismiss()
    }

    fun clearDialog(){
        dialog = null
    }
}