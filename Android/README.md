
# 1v1 场景化Demo主题呼叫流程

## 概述

此文档描述了 1v1 场景化Demo的实现方案，包括用户匹配、呼叫、接听、通话结束等功能的逻辑流程和代码示例。该Demo基于EaseMob1v1CallKit框架，实现了用户在匹配池中随机匹配、发起视频通话邀请、通话接听与结束的全流程。针对结束流程，Demo提供了正常结束、取消呼叫、拒绝接听、超时、用户忙碌等多种结束原因的处理方法。在结束呼叫时对RTC资源做了及时释放，同时更新用户Presence状态，以便下次匹配或其他用户呼叫。

## 快速开始

1. 下载项目代码。
2. 在 `local.properties` 文件中配置 `APPKEY`、`ServerHost`、`AgoraAppId`。
3. 运行项目。

- 注意：请确保您服务端已经按照项目readme中描述的服务端api源码地址部署到自己的服务器上，并且服务器中配置的APPKEY需要与客户端中`local.properties`配置的APPKEY一致，否则会发生错误。


## 目录

1. [匹配场景](#匹配场景)
2. [呼叫流程](#呼叫流程)
3. [通话流程](#通话流程)
4. [结束流程](#结束流程)
5. [处理同时匹配与邀请](#处理同时匹配与邀请)
6. [信令协议与常量枚举](#信令协议与常量枚举)
7. [信令协议相关方法](#信令协议相关方法)
8. [服务端源码](#服务端源码)

## 匹配场景

1. 用户登录后，客户端调用匹配接口向服务端报告上线状态，或在自动登录成功后，用户调用匹配接口自动进入随机匹配池。
2. 服务端进行匹配，一旦成功，分别向双方返回匹配到的用户`matchedChatUser`以及自己的加入rtc频道所需的 `channelName`、`token`、 和 `uid`。
3. 如果是新用户上线，服务端直接返回上述信息；对于老用户，服务端通过CMD消息通知其匹配到新用户，信令Key为 `EaseMob1v1SomeUserMatchedYou`，老用户收到的CMD消息中携带上述步骤2中信息。
   
User A (Caller)   |  User B (Callee)   |  Server
---------------------------------------------------------
    Login              Login               Match Users
     |                    |                             |
     |                    |------------Matching---------|
     |<------Match Info-- |                             |
     |----Start Call----> |                             |
     |                    |<-------Receive Call---------|
     |                    |--------Accept Call--------> |
     |<---Call Connected->|                             |
     |                    |                             |
     |----End Call------> |                             |
     |                    |<--------Call Ended--------- |
     |                    |                             |


## 呼叫流程

1. 主叫方发起呼叫，发送邀请消息并附带用户展示信息和 `callId`，该 `callId` 为当前时间戳，作为本次通话的唯一标识。

   ```kotlin
    fun sendStartCallMsg(
        conversationId:String,
        callId:String,
        type: String,
        callback:ChatCallback? = null
    ){
        val message = ChatMessage.createSendMessage(ChatMessageType.TXT)
        callback?.let {  message.setMessageStatusCallback(it) }
        DemoHelper.getInstance().context.resources?.let {
            message.setAttribute(EMCallConstant.EM1v1CallKit1v1Invite,EMCallConstant.EM1v1CallKit1v1Invite)
            message.setAttribute(EMCallConstant.CALL_MSG_TYPE,type)
            message.setAttribute(EMCallConstant.EM1v1CallKitCallId,callId)
            message.deliverOnlineOnly(true)
            message.to = conversationId
            message.chatType = ChatType.Chat
            val txtBody = ChatTextMessageBody(it.getString(R.string.em_start_call))
            EaseIM.getCurrentUser()?.let { profile ->
                message.addUserInfo(profile.name, profile.avatar)
            }
            message.addBody(txtBody)
            ChatClient.getInstance().chatManager().sendMessage(message)
        }
    }
   ```

2. 被叫方收到邀请消息后，可以选择接听或拒绝。如果拒绝，无论是主动拒绝还是超时，均调用 `endCall(userId: String,reason: String)` 方法，并传入相应的结束原因。

## 通话流程

1. 被叫方同意接听，发送确认信令 `EaseMob1v1CallKit1v1Accept`，并带上展示信息，然后进入通话页面加入 `channel`。

   ```kotlin
   fun sendAcceptCallMsg(
        conversationId:String,
        callId:String,
        callback:ChatCallback? = null
    ){
        val message = ChatMessage.createSendMessage(ChatMessageType.CMD)
        callback?.let {  message.setMessageStatusCallback(it) }
        DemoHelper.getInstance().context.resources?.let {
            message.setAttribute(EMCallConstant.EM1v1CallKit1v1Signaling,EMCallConstant.EM1v1CallKit1v1Signaling)
            message.setAttribute(EMCallConstant.EM1v1CallKitCallId,callId)
            message.deliverOnlineOnly(true)
            message.to = conversationId
            message.chatType = ChatType.Chat
            val txtBody = ChatCmdMessageBody(EMCallConstant.EM1v1CallKit1v1Accept)
            EaseIM.getCurrentUser()?.let { profile ->
                message.addUserInfo(profile.name, profile.avatar)
            }
            message.addBody(txtBody)
            ChatClient.getInstance().chatManager().sendMessage(message)
        }
    }
   ```

2. 主叫方收到确认信令后，进入通话页面并显示画面。

## 结束流程

1. 通话结束时，一方调用 `endCall(userId: String,reason: String)` 方法，另一方收到结束消息，同时退出 `channel`。

   ```kotlin
   fun sendEndedCallMsg(
        reason: String,
        conversationId:String,
        callId:String,
        callback:ChatCallback? = null
    ){
        val message = ChatMessage.createSendMessage(ChatMessageType.CMD)
        callback?.let {  message.setMessageStatusCallback(it) }
        DemoHelper.getInstance().context.resources?.let {
            message.setAttribute(EMCallConstant.EM1v1CallKit1v1Signaling,EMCallConstant.EM1v1CallKit1v1Signaling)
            message.setAttribute(EMCallConstant.EM1v1EndCallReason,reason)
            message.setAttribute(EMCallConstant.EM1v1CallKitCallId,callId)
            message.deliverOnlineOnly(true)
            message.to = conversationId
            message.chatType = ChatType.Chat
            val cmdBody = ChatCmdMessageBody(EMCallConstant.EM1v1CallKit1v1End)
            EaseIM.getCurrentUser()?.let { profile ->
                message.addUserInfo(profile.name, profile.avatar)
            }
            message.addBody(cmdBody)
            ChatClient.getInstance().chatManager().sendMessage(message)
        }
    }
   ```
   
2. 主动结束方需要调用PresenceManager接口将自己的Presence状态设置为 `online`，以便下次匹配或者其他用户呼叫。
示例代码`EM1v1CallKitManager.endCallReset()`
```kotlin
     fun endCallReset(){
        mRtcEngine?.let {
            it.stopPreview()
            it.disableAudio()
            it.disableVideo()
            it.leaveChannel()
        }
        onCalling = false
    }
```

3.异常结束情况
- App进入后台时，如果用户未在通话中而且已匹配的话，需要调用`EMMatchManager.cancelMatch`取消匹配。否则对端用户会进入一直等待状态而且呼叫一直无人响应。
- App进程结束时，如果用户未在通话中而且已匹配的话，需要调用`CallMessageHelper.cancelMatchNotify()`发cmd消息告知对方取消匹配。否则对端用户会进入一直等待状态而且呼叫一直无人响应。

## 处理同时匹配与邀请

1. 为了避免同时收到匹配和邀请的情况，需要维护一个状态变量。如果用户在振铃页面，则调用 `endCall()` 方法，并告知对方用户正忙。

2. 如果用户在会话列表中与匹配过的用户聊天，根据 `Presence` 状态判断是否可以通话。如果是 `Busy` 状态或 `offline`，则不可通话。用户在唤起通话界面时，将 `Presence` 状态设置为 `Busy`，如果超时未接通，则取消通话并将 `Presence` 状态重置为 `online`，同时调用 `endCall()` 通知对方取消匹配。

## 信令协议与常量枚举

- **信令协议：**
  - `EaseMob1v1CallKit1v1Invite`: 邀请信令
  - `EaseMob1v1CallKit1v1End`: 结束信令
  - `EaseMob1v1CallKit1v1Accept`: 接听信令
  - `EaseMob1v1CallKit1v1Signaling`: 通信信令
  - `EaseMob1v1SomeUserMatchedYou`: 匹配通知
  - `EaseMob1v1SomeUserMatchCanceled`: 异常结束通知

- **结束原因枚举：**
  - `normalEnd`: 正常结束
  - `cancelEnd`: 取消呼叫
  - `refuseEnd`: 拒绝接听
  - `timeoutEnd`: 超时
  - `busyEnd`: 用户忙碌
  - `rtcError`: RTC 错误

## 信令协议相关方法

### 相关方法实现

1. **匹配消息接收：**

   ```kotlin
      private val chatMessageListener = object : EaseMessageListener() {

        override fun onMessageReceived(messages: MutableList<ChatMessage>?) {
            CallMessageHelper.onMsgDidReceive(messages)
        }

        override fun onCmdMessageReceived(messages: MutableList<ChatMessage>?) {
            CallMessageHelper.onCmdMsgDidReceive(messages)
        }
      }
      EaseIM.addChatMessageListener(chatMessageListener)
   ```

2. **拒绝接听或正常接听：**

   ```kotlin
     EM1v1CallKitManager.endCall( userId,EM1v1CallKitEndReason.REFUSEEND.value )
     EM1v1CallKitManager.acceptCall(userId)
   ```

3. **接收确认信令：**

   ```kotlin
    private val chatMessageListener = object : EaseMessageListener() {

        override fun onCmdMessageReceived(messages: MutableList<ChatMessage>?) {
            CallMessageHelper.onCmdMsgDidReceive(messages)
        }
      }
      EaseIM.addChatMessageListener(chatMessageListener)
   ```

4. **通话结束处理：**

   ```kotlin
     // 正常通话结束 endCallReset
     EM1v1CallKitManager.endCallReset()
     // 当收到对方取消匹配时调用 reset()
     fun reset(){
          callId = ""
          otherMatchInfo = null
          ChatLog.e(TAG,"otherMatchInfo = null")
          remoteUid = 0
          channelName = ""
          rtcToken = null
          currentEndTime = 0L
          mRtcEngine?.let {
              it.stopPreview()
              it.disableAudio()
              it.disableVideo()
              it.leaveChannel()
          }
          mRtcEngine = null
          onCalling = false
      }
   ```

## 服务端源码

服务端源码请参考：[1v1Video-Chat-Call-Server-Api](https://github.com/easemob/easemob-demo-appserver/tree/dev-1v1video）
