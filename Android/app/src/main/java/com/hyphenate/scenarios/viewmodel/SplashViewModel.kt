package com.hyphenate.scenarios.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.hyphenate.scenarios.repository.ChatClientRepository
import kotlinx.coroutines.flow.flow

class SplashViewModel(application: Application) : AndroidViewModel(application) {
    private val mRepository: ChatClientRepository = ChatClientRepository()

    fun loginData() = flow {
        emit(mRepository.loadAllInfoFromHX())
    }

}