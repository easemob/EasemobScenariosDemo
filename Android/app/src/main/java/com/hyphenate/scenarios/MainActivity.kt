package com.hyphenate.scenarios

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.bottomnavigation.BottomNavigationItemView
import com.google.android.material.bottomnavigation.BottomNavigationMenuView
import com.google.android.material.navigation.NavigationBarView
import com.hyphenate.easeui.EaseIM
import com.hyphenate.easeui.common.ChatLog
import com.hyphenate.easeui.common.ChatMessage
import com.hyphenate.easeui.feature.conversation.EaseConversationListFragment
import com.hyphenate.easeui.interfaces.EaseMessageListener
import com.hyphenate.easeui.model.EaseProfile
import com.hyphenate.scenarios.base.BaseInitActivity
import com.hyphenate.scenarios.callkit.CallRtcTokenManager
import com.hyphenate.scenarios.callkit.CallStatus
import com.hyphenate.scenarios.callkit.EM1v1CallKitEndReason
import com.hyphenate.scenarios.callkit.EM1v1CallKitManager
import com.hyphenate.scenarios.callkit.EMMatchManager
import com.hyphenate.scenarios.callkit.helper.CallDialogHelper
import com.hyphenate.scenarios.callkit.interfaces.ApplicationStatusListener
import com.hyphenate.scenarios.callkit.interfaces.CallKitListener
import com.hyphenate.scenarios.common.DemoConstant
import com.hyphenate.scenarios.common.room.entity.parse
import com.hyphenate.scenarios.databinding.ActivityMainBinding
import com.hyphenate.scenarios.interfaces.IMainResultView
import com.hyphenate.scenarios.interfaces.IProfileResultView
import com.hyphenate.scenarios.ui.chat.ChatFragment
import com.hyphenate.scenarios.ui.conversation.ConversationListFragment
import com.hyphenate.scenarios.ui.me.AboutMeFragment
import com.hyphenate.scenarios.ui.room.PrivateRoomFragment
import com.hyphenate.scenarios.viewmodel.MainViewModel
import com.hyphenate.scenarios.viewmodel.PresenceViewModel
import com.hyphenate.scenarios.viewmodel.ProfileInfoViewModel

class MainActivity : BaseInitActivity<ActivityMainBinding>(),
    NavigationBarView.OnItemSelectedListener, IMainResultView, IProfileResultView,
    ApplicationStatusListener {
    companion object{
        const val TAG = "MainActivity"
    }
    private var mPrivateRoomFragment: Fragment? = null
    private var mConversationFragment: Fragment? = null
    private var mAboutMeFragment: Fragment? = null
    private var mCurrentFragment: Fragment? = null
    private val badgeMap = mutableMapOf<Int, TextView>()
    private var currentStatus: Boolean = true
    private val mainViewModel: MainViewModel by lazy {
        ViewModelProvider(this)[MainViewModel::class.java]
    }
    private val mProfileViewModel: ProfileInfoViewModel by lazy {
        ViewModelProvider(this)[ProfileInfoViewModel::class.java]
    }
    private val presenceViewModel: PresenceViewModel by lazy { PresenceViewModel() }

    private val chatMessageListener = object : EaseMessageListener() {
        override fun onMessageReceived(messages: MutableList<ChatMessage>?) {
            mainViewModel.getUnreadMessageCount()
        }
    }

    override fun setActivityTheme() {
        setFitSystemForTheme(false, ContextCompat.getColor(this, R.color.transparent), true)
    }

    override fun getViewBinding(inflater: LayoutInflater): ActivityMainBinding {
       return ActivityMainBinding.inflate(inflater)
    }

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        binding.navView.itemIconTintList = null
        mainViewModel.getUnreadMessageCount()
        savedInstanceState?.let {
            checkIfShowSavedFragment(it)
        }?:kotlin.run {
            switchToHome()
        }
        addTabBadge()
    }

    override fun initData() {
        super.initData()
        mainViewModel.attachView(this)
        mProfileViewModel.attachView(this)
        presenceViewModel.publishPresence(DemoConstant.PRESENCE_ONLINE)
        synchronizeProfile()
    }

    override fun initListener() {
        super.initListener()
        EaseIM.addChatMessageListener(chatMessageListener)
        binding.navView.setOnItemSelectedListener(this)
        EM1v1CallKitManager.addCallKitListener(TAG,callListener)
        EM1v1CallKitManager.setApplicationStatusListener(this)
    }

    override fun onDestroy() {
        binding.navView.setOnItemSelectedListener(null)
        EaseIM.removeChatMessageListener(chatMessageListener)
        EM1v1CallKitManager.removeCallKitListener(TAG)
        EM1v1CallKitManager.setApplicationStatusListener(null)
        super.onDestroy()
    }

    override fun onResume() {
        super.onResume()
        currentStatus = true
        val listener = EM1v1CallKitManager.getCallKitListener(TAG)
        listener?.let {}?:kotlin.run {
            EM1v1CallKitManager.addCallKitListener(TAG,callListener)
        }
    }

    override fun onPause() {
        super.onPause()
        currentStatus = false
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        var showNavigation = false
        when (item.itemId) {
            R.id.em_main_nav_home -> {
                switchToHome()
                showNavigation = true
            }

            R.id.em_main_nav_messages -> {
                switchToConversation()
                showNavigation = true
            }

            R.id.em_main_nav_me -> {
                switchToAboutMe()
                showNavigation = true
            }
        }
        invalidateOptionsMenu()
        return showNavigation
    }

    private fun switchToHome() {
        if (mPrivateRoomFragment == null) {
            mPrivateRoomFragment = PrivateRoomFragment()
        }
        mPrivateRoomFragment?.let {
            replace(it, "1v1_private_room")
        }
    }
    private fun switchToConversation(){
        if (mConversationFragment == null) {
            mConversationFragment = EaseConversationListFragment.Builder()
                .useTitleBar(true)
                .enableTitleBarPressBack(false)
                .useSearchBar(true)
                .setCustomFragment(ConversationListFragment())
                .build()
        }
        mConversationFragment?.let {
            replace(it, "1v1_conversation")
        }
    }

    private fun switchToAboutMe(){
        if (mAboutMeFragment == null){
            mAboutMeFragment = AboutMeFragment()
        }
        mAboutMeFragment?.let {
            replace(it, "1v1_about_me")
        }
    }

    private fun replace(fragment: Fragment, tag: String) {
        if (mCurrentFragment !== fragment) {
            val t = supportFragmentManager.beginTransaction()
            mCurrentFragment?.let {
                t.hide(it)
            }
            mCurrentFragment = fragment
            if (!fragment.isAdded) {
                t.add(R.id.fl_main_fragment, fragment, tag).show(fragment).commit()
            } else {
                t.show(fragment).commit()
            }
        }
    }

    private fun synchronizeProfile(){
        mProfileViewModel.synchronizeProfile(true)
    }

    override fun synchronizeProfileSuccess(profile: EaseProfile) {
        DemoHelper.getInstance().dataModel.getMatchUserInfo(profile.id)?.apply {
            this.name = profile.name
            this.avatar = profile.avatar
        }?.let {
            DemoHelper.getInstance().dataModel.updateMatchUserInfo(it)
        }
    }

    /**
     * 用于展示是否已经存在的Fragment
     * @param savedInstanceState
     */
    private fun checkIfShowSavedFragment(savedInstanceState: Bundle?) {
        if (savedInstanceState != null) {
            val tag = savedInstanceState.getString("tag")
            if (!tag.isNullOrEmpty()) {
                val fragment = supportFragmentManager.findFragmentByTag(tag)
                if (fragment is Fragment) {
                    replace(fragment, tag)
                }
            }
        }
    }

    private fun addTabBadge() {
        (binding.navView.getChildAt(0) as? BottomNavigationMenuView)?.let { menuView->
            val childCount = menuView.childCount
            for (i in 0 until childCount) {
                val itemView = menuView.getChildAt(i) as BottomNavigationItemView
                val badge = LayoutInflater.from(this).inflate(R.layout.demo_badge_home, menuView, false)
                badgeMap[i] = badge.findViewById(R.id.tv_main_home_msg)
                itemView.addView(badge)
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mCurrentFragment?.let {
            outState.putString("tag", it.tag)
        }
    }

    override fun getUnreadCountSuccess(count: String?) {
        if (count.isNullOrEmpty()) {
            badgeMap[1]?.text = ""
            badgeMap[1]?.visibility = View.GONE
        } else {
            badgeMap[1]?.text = count
            badgeMap[1]?.visibility = View.VISIBLE
        }
    }

   fun onGlobalShowAlert(userId:String) {
       // 如果不是匹配呼叫弹窗 则取消匹配
       if (!EM1v1CallKitManager.isMatchCall){
           EMMatchManager.cancelMatch()
       }
        // 收到弹窗信令 获取自己的状态 只有online 情况下 打开弹窗
        if (EM1v1CallKitManager.callId.isNotEmpty()){
            presenceViewModel.publishPresence(DemoConstant.PRESENCE_BUSY)
            CallDialogHelper.showComingDialog(
                MainActivity@this,
                onCallClick = {
                    if (EM1v1CallKitManager.isMatchCall){
                        EM1v1CallKitManager.acceptCall(userId)
                    }else{
                        // 获取rtcToken 和 channel
                        chat1v1CallAccept(userId)
                    }
                },
                onRefuseClick = {
                    EM1v1CallKitManager.endCall( userId,EM1v1CallKitEndReason.REFUSEEND.value )
                    presenceViewModel.publishPresence(DemoConstant.PRESENCE_ONLINE)
                },
            )
        }else{
            EM1v1CallKitManager.endCall( userId,EM1v1CallKitEndReason.BUSYEND.value )
        }
    }

    private val callListener = object : CallKitListener {

        override fun onCallStatusChanged(status: CallStatus, reason: String) {
            when(status){
                CallStatus.ALERT -> {
                    if (currentStatus){
                        onGlobalShowAlert(reason)
                    }
                }
                CallStatus.ENDED -> {
                    onGlobalCallEnd(reason)
                }
                else -> {  }
            }
        }
    }

     fun onGlobalCallEnd(reason: String) {
        if (
            reason == EM1v1CallKitEndReason.CANCELEND.value ||
            reason == EM1v1CallKitEndReason.TIMEOUTEND.value ||
            reason == EM1v1CallKitEndReason.REFUSEEND.value ||
            reason == EM1v1CallKitEndReason.BUSYEND.value
        ){
            CallDialogHelper.dismissComingDialog()
            presenceViewModel.publishPresence(DemoConstant.PRESENCE_ONLINE)
        }
    }

    override fun onBackgroundStatusChanged(isOnForeground:Boolean) {
        if (!EM1v1CallKitManager.onCalling ){
            if (!isOnForeground){
                //不在通话中 并且切换到后台 取消匹配
                EM1v1CallKitManager.otherMatchInfo?.let {
                    EMMatchManager.cancelMatch()
                }
            }
        }
    }

    private fun chat1v1CallAccept(userId:String){
        CallRtcTokenManager.synchronizeRtcToken(
            conversationId = userId,
            onSuccess = { result->
                EM1v1CallKitManager.rtcToken = result.accessToken
                val current = EaseIM.getCurrentUser()?.id
                val channel = (current+userId).lowercase().toCharArray().sorted().joinToString("")
                EM1v1CallKitManager.channelName = channel
                EM1v1CallKitManager.getCurrentMatchInfo()?.apply {
                    agoraUid = result.agoraUid
                }?.let {
                    DemoHelper.getInstance().dataModel.updateMatchUserInfo(it)
                }
                DemoHelper.getInstance().dataModel.getUser(userId)?.parse()?.apply {
                    EM1v1CallKitManager.otherMatchInfo?.avatar = avatar
                    EM1v1CallKitManager.otherMatchInfo?.name = name
                }
                EM1v1CallKitManager.acceptCall(userId)
            },
            onError = { code, error ->
                ChatLog.e(ChatFragment.TAG, "synchronizeRtcToken error $code $error")
            }
        )
    }
}