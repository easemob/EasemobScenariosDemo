package com.hyphenate.scenarios

import android.app.Application
import com.hyphenate.scenarios.base.UserActivityLifecycleCallbacks

class DemoApplication: Application() {
    private val mLifecycleCallbacks = UserActivityLifecycleCallbacks()

    override fun onCreate() {
        super.onCreate()
        instance = this
        registerActivityLifecycleCallbacks()
        DemoHelper.getInstance().init(this)
        initSDK()
    }


    private fun registerActivityLifecycleCallbacks() {
        this.registerActivityLifecycleCallbacks(mLifecycleCallbacks)
    }

    fun getLifecycleCallbacks(): UserActivityLifecycleCallbacks {
        return mLifecycleCallbacks
    }

    private fun initSDK() {
        if (DemoHelper.getInstance().dataModel.isAgreeAgreement()) {
            DemoHelper.getInstance().initSDK()
        }
    }

    companion object {
        private lateinit var instance: DemoApplication
        fun getInstance(): DemoApplication {
            return instance
        }
    }

}