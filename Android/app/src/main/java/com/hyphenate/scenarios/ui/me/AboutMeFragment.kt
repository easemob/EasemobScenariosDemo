package com.hyphenate.scenarios.ui.me

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import coil.load
import com.hyphenate.easeui.EaseIM
import com.hyphenate.easeui.base.EaseBaseFragment
import com.hyphenate.easeui.common.ChatClient
import com.hyphenate.easeui.common.bus.EaseFlowBus
import com.hyphenate.easeui.common.dialog.CustomDialog
import com.hyphenate.easeui.model.EaseEvent
import com.hyphenate.scenarios.DemoHelper
import com.hyphenate.scenarios.R
import com.hyphenate.scenarios.callkit.EM1v1CallKitManager
import com.hyphenate.scenarios.common.DemoConstant
import com.hyphenate.scenarios.common.room.entity.parse
import com.hyphenate.scenarios.databinding.FragmentAboutMeLayoutBinding
import com.hyphenate.scenarios.ui.login.LoginActivity

class AboutMeFragment : EaseBaseFragment<FragmentAboutMeLayoutBinding>(){
    override fun getViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentAboutMeLayoutBinding {
        return FragmentAboutMeLayoutBinding.inflate(inflater)
    }

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        updateInfo()
    }

    override fun initData() {
        EaseFlowBus.with<EaseEvent>(EaseEvent.EVENT.UPDATE + EaseEvent.TYPE.CONTACT).register(this) {
            if (it.isContactChange && it.event == DemoConstant.EVENT_UPDATE_SELF) {
                updateInfo()
            }
        }
    }

    override fun initListener() {
        binding?.tvUserName?.setOnClickListener{
            startActivity(Intent(mContext, UserInformationActivity::class.java))
        }
        binding?.ivUserAvatar?.setOnClickListener{
            startActivity(Intent(mContext, UserInformationActivity::class.java))
        }
        binding?.aboutLogout?.setOnClickListener{
            showLogoutDialog()
        }
    }

    private fun updateInfo(){
        val user = DemoHelper.getInstance().dataModel.getUser(ChatClient.getInstance().currentUser)?.parse()
        user?.let {
            binding?.ivUserAvatar?.load(it.avatar)
            binding?.tvUserName?.text = it.getNotEmptyName()
            binding?.tvUserId?.text = it.id
        }

    }

    private fun showLogoutDialog(){
        val logoutDialog = CustomDialog(
            context = mContext,
            title = resources.getString(R.string.em_about_out_hint),
            isEditTextMode = false,
            leftButtonText = resources.getString(R.string.em_cancel),
            rightButtonText = resources.getString(R.string.em_confirm),
            onLeftButtonClickListener = {

            },
            onRightButtonClickListener = {
                EaseIM.logout(false,
                    onSuccess = {
                        EM1v1CallKitManager.reset()
                        startActivity(Intent(mContext,LoginActivity::class.java))
                    }
                )
            }
        )
        logoutDialog.show()
    }


}