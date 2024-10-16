package com.hyphenate.scenarios.callkit.helper

import android.os.CountDownTimer
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object CountdownTimerHelper {

    // 倒计时 15 秒未收到回复 视为超时
    private var countDownTimer: CountDownTimer? = null
    private var isRunning = false

    // 启动倒计时
    fun startCountdown(onTick: (Long) -> Unit, onFinish: () -> Unit) {
        if (isRunning) {
            Log.d("CountdownTimerManager", "Countdown is already running.")
            return
        }
        CoroutineScope(Dispatchers.Main).launch {
            isRunning = true

            countDownTimer = object : CountDownTimer(15000, 1000) {
                override fun onTick(millisUntilFinished: Long) {
                    onTick(millisUntilFinished / 1000) // 每秒回调
                }

                override fun onFinish() {
                    isRunning = false
                    onFinish() // 倒计时结束时的回调
                }
            }.start()
        }
    }

    // 取消倒计时
    fun cancelCountdown() {
        countDownTimer?.cancel()
        countDownTimer = null
        isRunning = false
    }

    // 检查倒计时是否在运行
    fun isCountdownRunning(): Boolean {
        return isRunning
    }
}