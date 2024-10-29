package com.hyphenate.scenarios.interfaces

import com.hyphenate.easeui.common.ChatUserInfo
import com.hyphenate.easeui.common.interfaces.IControlDataView
import com.hyphenate.easeui.model.EaseProfile

interface IProfileResultView: IControlDataView {

    /**
     * upload avatar successful
     */
    fun uploadAvatarSuccess(){}

    /**
     * upload avatar fail
     */
    fun uploadAvatarFail(code: Int,error: String){}

    /**
     * update nickname successful
     */
    fun updateNickNameSuccess(user:EaseProfile?){}

    /**
     * update nickname fail
     */
    fun updateNickNameFail(code: Int,error: String){}

    /**
     * synchronize profile successful
     */
    fun synchronizeProfileSuccess(profile:EaseProfile) {}

    /**
     * synchronize profile fail
     */
    fun synchronizeProfileFail(code: Int,error: String){}

    /**
     * fetch user attribute successful
     */
    fun fetchUserInfoAttributeSuccess(attributes:Map<String, ChatUserInfo>){}

    /**
     * fetch user attribute fail
     */
    fun fetchUserInfoAttributeFail(code: Int,error: String){}
}