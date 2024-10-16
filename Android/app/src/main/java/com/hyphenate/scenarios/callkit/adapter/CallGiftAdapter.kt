package com.hyphenate.scenarios.callkit.adapter

import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.google.android.material.textview.MaterialTextView
import com.hyphenate.easeui.EaseIM
import com.hyphenate.easeui.common.ChatMessage
import com.hyphenate.easeui.widget.EaseImageView
import com.hyphenate.scenarios.R
import com.hyphenate.scenarios.callkit.extensions.parseGiftInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CallGiftAdapter(private val items: MutableList<ChatMessage>) :
    RecyclerView.Adapter<CallGiftAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val mUsername: AppCompatTextView = view.findViewById(R.id.tv_user_name)
        val mTvContent: AppCompatTextView = view.findViewById(R.id.tv_content)
        val mIvAvatar: EaseImageView = view.findViewById(R.id.iv_avatar)
        val mIvGiftIcon: AppCompatImageView = view.findViewById(R.id.iv_gift_icon)
        val mTvCount: MaterialTextView = view.findViewById(R.id.tv_count)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.demo_call_gift_list_item_layout, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val message = items[position]
        val gift = message.parseGiftInfo()
        holder.itemView.context?.let {
            // 加载自定义字体
            val typeface: Typeface? = Typeface.createFromAsset(it.assets, "RobotoNumbersVF.ttf")
            holder.mTvCount.typeface = typeface
        }
        gift?.let {
            holder.mTvContent.text = "赠送${it.giftName}"
            holder.mTvCount.text = "x${it.giftCount}"
            holder.mIvGiftIcon.load(it.giftIcon)
        }
        EaseIM.getUserProvider()?.getUser(message.from)?.let {
            holder.mUsername.text = it.getNotEmptyName()?:it.id
            holder.mIvAvatar.load(it.avatar)
        }
    }

    override fun getItemCount() = items.size

    fun addItem(item: ChatMessage) {
        items.add(item)
        notifyItemInserted(items.size - 1)
    }

    fun getData():MutableList<ChatMessage>{
        return items
    }

    fun removeAll() {
        CoroutineScope(Dispatchers.Main).launch {
            notifyItemRangeRemoved(0, items.size)
            items.clear()
        }
    }

}