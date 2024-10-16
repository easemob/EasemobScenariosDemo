package com.hyphenate.scenarios.repository

import android.content.Context
import com.hyphenate.scenarios.DemoApplication

open class BaseRepository {
    fun getContext(): Context {
        return DemoApplication.getInstance()
    }
}