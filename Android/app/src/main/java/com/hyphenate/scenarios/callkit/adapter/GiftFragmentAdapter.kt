package com.hyphenate.scenarios.callkit.adapter

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.hyphenate.scenarios.bean.GiftEntityProtocol
import com.hyphenate.scenarios.callkit.gift.LiveGiftListFragment
import com.hyphenate.scenarios.callkit.helper.CallGiftHelper
import com.hyphenate.scenarios.callkit.interfaces.OnGiftConfirmClickListener
import kotlin.math.roundToInt

class GiftFragmentAdapter(fragment: FragmentActivity) :
    FragmentStateAdapter(fragment) {
    private var listener: OnVpFragmentItemListener? = null
    private val list: List<GiftEntityProtocol> = CallGiftHelper.getDefaultGifts()

    override fun createFragment(position: Int): Fragment {
        val fragment = LiveGiftListFragment()
        val args = Bundle()
        args.putInt("position", position)
        fragment.arguments = args
        fragment.setOnItemSelectClickListener(object : OnGiftConfirmClickListener {
            override fun onConfirmClick(view: View?, bean: GiftEntityProtocol?) {
                listener?.onVpFragmentItem(position, bean)
            }

            override fun onFirstItem(firstBean: GiftEntityProtocol?) {
                listener?.onFirstData(firstBean)
            }
        })
        //添加参数
        return fragment
    }

    override fun getItemCount(): Int {
        if (list.size % 4 == 0){
            return (list.size / 4)
        }
        return (list.size / 4 + 0.5f).roundToInt()
    }

    fun setOnVpFragmentItemListener(listener: OnVpFragmentItemListener?) {
        this.listener = listener
    }

    interface OnVpFragmentItemListener {
        fun onVpFragmentItem(position: Int, bean: Any?)
        fun onFirstData(bean: GiftEntityProtocol?)
    }
}