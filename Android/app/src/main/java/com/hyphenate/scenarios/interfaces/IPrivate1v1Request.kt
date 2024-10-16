package com.hyphenate.scenarios.interfaces

import com.hyphenate.easeui.viewmodel.IAttachView
import com.hyphenate.scenarios.bean.MatchUserInfo

interface IPrivate1v1Request:IAttachView{

    /**
     * match user
     */
    fun matchUserFromServer(phoneNumber:String, isExceptionCancelMatch:Boolean = false)

    /**
     * fetch matched user info
     */
    fun fetchMatchedUserInfo(matchUserInfo: MatchUserInfo)

    /**
     * cancel match user
     */
    fun cancelMatchUser(phoneNumber:String)
}