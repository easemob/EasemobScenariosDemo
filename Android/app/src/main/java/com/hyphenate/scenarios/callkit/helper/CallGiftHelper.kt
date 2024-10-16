package com.hyphenate.scenarios.callkit.helper

import android.app.Activity
import android.content.Context
import android.os.AsyncTask
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.fragment.app.FragmentActivity
import com.google.gson.Gson
import com.hyphenate.easeui.common.ChatLog
import com.hyphenate.scenarios.bean.DefaultGifts
import com.hyphenate.scenarios.bean.GiftEntityProtocol
import org.libpag.PAGFile
import org.libpag.PAGView
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.URL
import java.nio.charset.Charset
import java.security.MessageDigest

object CallGiftHelper {
    const val TAG = "CallGiftHelper"
    private var giftList: MutableList<GiftEntityProtocol> = mutableListOf()
    private var mPAGView: PAGView? = null

    fun initGiftData(context: Context,useAssets: Boolean = true){
        try {
            val assetManager = context.assets
            val jsonFile = "giftEntity.json"
            val inputStream = assetManager.open(jsonFile)
            val size = inputStream.available()
            val buffer = ByteArray(size)
            inputStream.read(buffer)
            inputStream.close()
            val jsonString = String(buffer, Charset.forName("UTF-8"))
            val gson = Gson()
            val defaultList = gson.fromJson(jsonString, Array<DefaultGifts>::class.java).toList()
            defaultList.let {
                giftList = it[0].gifts.toMutableList()
            }
            if (!useAssets){
                if (defaultList.isNotEmpty()){
                    downloadEffectResource(context,defaultList)
                }
            }
        }catch (e: Exception){
            ChatLog.e(TAG,"init gift error")
        }
    }
    fun getDefaultGifts():List<GiftEntityProtocol>{
        ChatLog.d(TAG,"getDefaultGifts ${giftList.size}")
        return giftList
    }

    fun clearGiftInfo(){
        giftList.clear()
    }

    fun getGiftsByPage(page: Int): List<GiftEntityProtocol> {
        val base = 4
        val index = page * base
        val gifts: MutableList<GiftEntityProtocol> = ArrayList<GiftEntityProtocol>()
        val data: List<GiftEntityProtocol> = getDefaultGifts()
        for (i in 1..data.size) {
            if (index < i && i <= base + page * base) {
                gifts.add(data[i - 1])
            }
        }
        return gifts
    }

    fun showGiftAction(
        context: Context,
        gift: GiftEntityProtocol?,
        pagView: PAGView? = null,
        useAssets:Boolean = true,
        onError:(error:String?)-> Unit = {}
    ){
        try {
            gift?.giftEffect?.let {giftEffect->
                if (giftEffect.isNotEmpty()){
                    // 从 assets 目录加载 .pag 文件
                    if (useAssets){
                        val assetManager = context.assets
                        val pagFile = PAGFile.Load(assetManager,giftEffect)
                        if (pagView == null){
                            mPAGView = createPagView(context)
                            mPAGView?.composition = pagFile
                            mPAGView?.bringToFront()
                            mPAGView?.setScaleMode(3)
                            mPAGView?.setRepeatCount(1)
                            mPAGView?.play()
                        }else{
                            pagView.composition = pagFile
                            pagView.play()
                        }
                    }else{
                        effectAnimation(context,gift)
                    }
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
            onError.invoke(e.message)
        }
    }

    fun stopGiftAction(){
        mPAGView?.let {
            if ( it.isPlaying ){
                it.stop()
            }
        }
    }

    private fun effectAnimation(context: Context,gift: GiftEntityProtocol) {
        val path = filePath(context,gift.effectMD5 ?: "")
        if (path.isNullOrEmpty() || !File(path).exists()) {
            return
        }
        if (mPAGView == null){
            val pagView = PAGView(context)
            pagView.layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT)
            pagView.elevation = 3F
            val decorView = when (context) {
                is Activity -> context.window?.decorView
                is FragmentActivity -> context.window?.decorView
                else -> null
            }
            val contentView = decorView as? ViewGroup
            contentView?.addView(pagView)
            pagView.addListener(object: PAGView.PAGViewListener {
                override fun onAnimationStart(p0: PAGView?) {
                    pagView.visibility = View.VISIBLE
                    ChatLog.d(TAG, "gift pag: play start")
                }
                override fun onAnimationEnd(p0: PAGView?) {
                    pagView.visibility = View.INVISIBLE
                    ChatLog.d(TAG, "gift pag: play end")
                }
                override fun onAnimationCancel(p0: PAGView?) {
                    pagView.visibility = View.INVISIBLE
                    ChatLog.d(TAG, "gift pag: play cancel")
                }
                override fun onAnimationRepeat(p0: PAGView?) {}
                override fun onAnimationUpdate(p0: PAGView?) {}
            })
            mPAGView = pagView
        }
        val file = PAGFile.Load(path)
        mPAGView?.bringToFront()
        mPAGView?.setScaleMode(3)
        mPAGView?.composition = file
        mPAGView?.setRepeatCount(1)
        mPAGView?.play()
    }

   private fun createPagView(context: Context):PAGView{
        val pagView = PAGView(context)
        pagView.layoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT)
        pagView.elevation = 3F
        val decorView = when (context) {
            is Activity -> context.window?.decorView
            is FragmentActivity -> context.window?.decorView
            else -> null
        }
        val contentView = decorView as? ViewGroup
        contentView?.addView(pagView)
        pagView.addListener(object: PAGView.PAGViewListener {
            override fun onAnimationStart(p0: PAGView?) {
                pagView.visibility = View.VISIBLE
                ChatLog.d(TAG, "gift pag: play start")
            }
            override fun onAnimationEnd(p0: PAGView?) {
                pagView.visibility = View.INVISIBLE
                ChatLog.d(TAG, "gift pag: play end")
            }
            override fun onAnimationCancel(p0: PAGView?) {
                pagView.visibility = View.INVISIBLE
                ChatLog.d(TAG, "gift pag: play cancel")
            }
            override fun onAnimationRepeat(p0: PAGView?) {}
            override fun onAnimationUpdate(p0: PAGView?) {}
        })
        return pagView
    }


    private fun downloadEffectResource(context: Context,tabs: List<DefaultGifts>) {
        tabs.forEach { tab ->
            tab.gifts.forEach { gift ->
                val url = gift.giftEffect
                val savePath = filePath(context,gift.effectMD5 ?: "")
                savePath?.let {
                    val file = File(it)
                    if (!file.exists() && url != null){
                        val task = NetworkTask(url, savePath)
                        task.execute()
                    }
                }
            }
        }
    }

    private fun filePath(context: Context,fileName: String): String? {
        return if (fileName.isEmpty()) {
            null
        } else {
            val dir = File(context.cacheDir,"giftEffects")
            if (!dir.exists()){
                dir.mkdirs()
            }
            val file = dir.resolve("$fileName.pag")
            return file.absoluteFile.toString()
        }
    }

     fun calculateMD5(input: String): String {
        val md = MessageDigest.getInstance("MD5")
        val digest = md.digest(input.toByteArray())
        return digest.joinToString("") { "%02x".format(it) }
    }

    private class NetworkTask constructor(
        val url: String,
        val path: String,
    ) : AsyncTask<Void, Void, String>() {
        override fun doInBackground(vararg params: Void?): String {
            val inputStream = URL(url).openStream()
            val outputStream = FileOutputStream(File(path))
            inputStream.use { input ->
                outputStream.use { output ->
                    input.copyTo(output)
                }
            }
            return ""
        }
    }

    fun giftRest(){
        mPAGView = null
    }


}