package com.hyphenate.scenarios.callkit.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import coil.load
import com.hyphenate.easeui.base.EaseBaseRecyclerViewAdapter
import com.hyphenate.scenarios.R
import com.hyphenate.scenarios.bean.GiftEntityProtocol

class GiftListAdapter: EaseBaseRecyclerViewAdapter<GiftEntityProtocol>() {
    private var selectedPosition:Int = -1
    override fun getViewHolder(parent: ViewGroup, viewType: Int): ViewHolder<GiftEntityProtocol> {
        return GiftViewHolder(LayoutInflater.from(mContext)
            .inflate(R.layout.demo_gift_list_item_layout, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder<GiftEntityProtocol>, position: Int) {
        if (holder is GiftViewHolder){
            holder.selectedPosition = selectedPosition
        }
        super.onBindViewHolder(holder, position)
    }

    class GiftViewHolder(itemView: View): ViewHolder<GiftEntityProtocol>(itemView) {
        var selectedPosition = -1
        private var mContext:Context? = null
        private var ivGift: ImageView? = null
        private var tvGiftName: TextView? = null
        private var price: TextView? = null
        override fun initView(itemView: View?) {
            this.mContext = itemView?.context
            this.ivGift = findViewById(R.id.iv_gift)
            this.tvGiftName = findViewById(R.id.tv_gift_name)
            this.price = findViewById(R.id.price)
        }

        override fun setData(item: GiftEntityProtocol?, position: Int) {
            Log.e("GiftListAdapter", "setData: $position")
            ivGift?.load(item?.giftIcon)
            tvGiftName?.text = item?.giftName
            price?.text = item?.giftPrice
            if (selectedPosition == position) {
                item?.isChecked = true
                mContext?.let {
                    itemView.background =
                        ContextCompat.getDrawable(it, R.drawable.bg_gift_selected_shape)
                }
            } else {
                item?.isChecked = false
                itemView.background = null
            }
        }
    }

    fun setSelectedPosition(position: Int) {
        selectedPosition = position
        notifyDataSetChanged()
    }


}