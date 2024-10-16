package com.hyphenate.scenarios.viewmodel

import androidx.lifecycle.viewModelScope
import com.hyphenate.easeui.viewmodel.EaseBaseViewModel
import com.hyphenate.scenarios.interfaces.IMainRequest
import com.hyphenate.scenarios.interfaces.IMainResultView
import com.hyphenate.scenarios.repository.ChatClientRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class MainViewModel: EaseBaseViewModel<IMainResultView>(), IMainRequest {
    private val chatRepository by lazy { ChatClientRepository() }
    companion object{
        const val TAG = "MainViewModel"
    }
    override fun getUnreadMessageCount() {
        viewModelScope.launch {
            flow {
                emit(chatRepository.getAllUnreadMessageCount())
            }
            .map {
                if (it <= 0) {
                    null
                } else if (it > 99) {
                    "99+"
                } else {
                    it.toString()
                }
            }
            .flowOn(Dispatchers.Main)
            .collectLatest {
                view?.getUnreadCountSuccess(it)
            }
        }
    }

}