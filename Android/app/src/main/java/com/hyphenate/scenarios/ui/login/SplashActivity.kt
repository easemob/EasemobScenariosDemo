package com.hyphenate.scenarios.ui.login

import android.animation.Animator
import android.content.Context
import android.content.Intent
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.hyphenate.chatdemo.common.dialog.SimpleDialog
import com.hyphenate.easeui.common.ChatLog
import com.hyphenate.easeui.common.extensions.catchChatException
import com.hyphenate.scenarios.DemoHelper
import com.hyphenate.scenarios.MainActivity
import com.hyphenate.scenarios.R
import com.hyphenate.scenarios.base.BaseInitActivity
import com.hyphenate.scenarios.common.dialog.DemoAgreementDialogFragment
import com.hyphenate.scenarios.common.dialog.DemoDialogFragment
import com.hyphenate.scenarios.databinding.DemoSplashActivityBinding
import com.hyphenate.scenarios.viewmodel.SplashViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.system.exitProcess

class SplashActivity : BaseInitActivity<DemoSplashActivityBinding>() {
    private lateinit var model: SplashViewModel

    override fun getViewBinding(inflater: LayoutInflater): DemoSplashActivityBinding? {
        return DemoSplashActivityBinding.inflate(inflater)
    }

    override fun setActivityTheme() {
        setFitSystemForTheme(false, ContextCompat.getColor(this, R.color.transparent), true)
    }

    override fun initData() {
        super.initData()
        model = ViewModelProvider(this)[SplashViewModel::class.java]
        binding.ivSplash.animate()
            .alpha(1f)
            .setDuration(500)
            .setListener(object : Animator.AnimatorListener {
                override fun onAnimationStart(animation: Animator) {}
                override fun onAnimationEnd(animation: Animator) {
                    checkIfAgreePrivacy()
                }

                override fun onAnimationCancel(animation: Animator) {}
                override fun onAnimationRepeat(animation: Animator) {}
            })
            .start()
        binding.tvProduct.animate()
            .alpha(1f)
            .setDuration(500)
            .start()
    }

    private fun checkIfAgreePrivacy() {
        if (DemoHelper.getInstance().dataModel.isAgreeAgreement().not()) {
            showPrivacyDialog()
        } else {
            checkSDKValid()
        }
    }

    private fun checkSDKValid() {
        if (DemoHelper.getInstance().hasAppKey.not()) {
            showAlertDialog(R.string.em_splash_not_appkey)
        } else {
            if (DemoHelper.getInstance().isSDKInited().not()) {
                showAlertDialog(R.string.em_splash_not_init)
            } else {
                loginSDK()
            }
        }
    }

    private fun showPrivacyDialog() {
        DemoAgreementDialogFragment.Builder(mContext as AppCompatActivity)
            .setTitle(R.string.em_login_dialog_title)
            .setOnConfirmClickListener(
                R.string.em_login_dialog_confirm,
                object : DemoDialogFragment.OnConfirmClickListener {
                    override fun onConfirmClick(view: View?) {
                        DemoHelper.getInstance().dataModel.setAgreeAgreement(true)
                        DemoHelper.getInstance().initSDK()
                        checkSDKValid()
                    }
                })
            .setOnCancelClickListener(
                R.string.em_login_dialog_cancel,
                object : DemoDialogFragment.OnCancelClickListener {
                    override fun onCancelClick(view: View?) {
                        exitProcess(1)
                    }
                })
            .show()
    }

    private fun showAlertDialog(@StringRes title: Int) {
        SimpleDialog.Builder(mContext)
            .setTitle(getString(title))
            .setPositiveButton(getString(R.string.em_confirm)) {
                exitProcess(1)
            }
            .dismissNegativeButton()
            .show()
    }

    private fun loginSDK() {
        lifecycleScope.launch {
            model.loginData()
                .catchChatException { e ->
                    ChatLog.e("TAG", "error message = " + e.description)
                    LoginActivity.startAction(mContext)
                    finish()
                }
                .stateIn(lifecycleScope, SharingStarted.WhileSubscribed(5000), false)
                .collect {
                    if (it) {
                        DemoHelper.getInstance().dataModel.initDb()
                        startActivity(Intent(mContext, MainActivity::class.java))
                        finish()
                    }
                }
        }
    }
}