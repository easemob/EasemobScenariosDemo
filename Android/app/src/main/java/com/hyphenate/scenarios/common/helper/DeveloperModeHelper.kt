package com.hyphenate.scenarios.common.helper

import com.hyphenate.scenarios.DemoHelper

object DeveloperModeHelper {
    fun isRequestToAppServer():Boolean{
        val developerMode = DemoHelper.getInstance().dataModel.isDeveloperMode()
        return !developerMode
    }
}