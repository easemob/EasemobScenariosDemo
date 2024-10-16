package com.hyphenate.scenarios.interfaces

import android.app.Activity

interface ActivityState {
    /**
     * 得到当前Activity
     * @return
     */
    fun current(): Activity?

    /**
     * 得到Activity集合
     * @return
     */
    val activityList: List<Activity?>?

    /**
     * 任务栈中Activity的总数
     * @return
     */
    fun count(): Int

    /**
     * 判断应用是否处于前台，即是否可见
     * @return
     */
    val isFront: Boolean
}