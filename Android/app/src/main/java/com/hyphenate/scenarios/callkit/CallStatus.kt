package com.hyphenate.scenarios.callkit

enum class CallStatus {
    PREPARING,  //匹配到 matching user
    CALLING,    //通话中
    ENDED,      //结束通话
    ALERT,      //有通话请求
    JOIN,       //加入
    IDLE,       //空闲
}