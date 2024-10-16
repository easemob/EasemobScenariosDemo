package com.hyphenate.scenarios.interfaces

import com.hyphenate.easeui.common.interfaces.IControlDataView
import com.hyphenate.easeui.model.EaseProfile
import com.hyphenate.scenarios.bean.MatchUserInfo

interface IPrivate1v1RoomResultView: IControlDataView {

    /**
     * match user successfully.
     */
    fun matchUserSuccess(match:MatchUserInfo){}

    /**
     * match user fail.
     */
    fun matchUserFail(code:Int,error:String){}

    /**
     * fetch match user info successfully.
     */
    fun fetchMatchUserInfoSuccess(profile:EaseProfile?){}

    /**
     * fetch match user info fail.
     */
    fun fetchMatchUserInfoFail(code:Int,error:String){}

    /**
     * cancel match user successfully.
     */
    fun cancelMatchUserSuccess() {}

    /**
     * cancel match user fail.
     */
    fun cancelMatchUserFail(code:Int,error:String){}
}