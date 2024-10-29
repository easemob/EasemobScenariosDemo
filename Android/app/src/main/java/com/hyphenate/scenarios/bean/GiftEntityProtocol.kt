package com.hyphenate.scenarios.bean

import java.io.Serializable

data class GiftEntityProtocol(
    var giftId: String? = "",
    var giftName:String? = "",
    var giftPrice:String? = "",
    var giftCount:Int = 0,
    var giftIcon:String? = "",
    var giftEffect:String? = "",
    // 远程资源在本地存储的文件名
    var effectMD5:String? = "",
    var isChecked:Boolean = false
): Serializable{

}