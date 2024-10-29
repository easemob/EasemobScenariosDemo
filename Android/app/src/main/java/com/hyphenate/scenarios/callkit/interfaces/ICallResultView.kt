package com.hyphenate.scenarios.callkit.interfaces

import com.hyphenate.easeui.common.ChatMessage
import com.hyphenate.easeui.common.interfaces.IControlDataView
import com.hyphenate.scenarios.bean.GiftEntityProtocol

interface ICallResultView: IControlDataView {

    fun send1v1CallMessageSuccess(message:ChatMessage?) {}

    fun send1v1CallMessageFail(code:Int,error:String){}


    fun send1v1GiftCallMessageSuccess(gift: GiftEntityProtocol, message:ChatMessage?) {}

    fun send1v1GiftCallMessageFail(code:Int,error:String){}

}