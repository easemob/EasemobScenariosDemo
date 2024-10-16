package com.hyphenate.scenarios.callkit.gift

import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import com.hyphenate.easeui.common.HorizontalPageLayoutManager
import com.hyphenate.easeui.common.extensions.dpToPx
import com.hyphenate.easeui.common.helper.PagingScrollHelper
import com.hyphenate.easeui.interfaces.OnItemClickListener
import com.hyphenate.scenarios.R
import com.hyphenate.scenarios.base.BaseInitFragment
import com.hyphenate.scenarios.bean.GiftEntityProtocol
import com.hyphenate.scenarios.callkit.adapter.GiftListAdapter
import com.hyphenate.scenarios.callkit.interfaces.OnGiftConfirmClickListener
import com.hyphenate.scenarios.callkit.helper.CallGiftHelper

class LiveGiftListFragment : BaseInitFragment(), OnItemClickListener {
    private var rvList: RecyclerView? = null
    private var adapter: GiftListAdapter? = null
    private var giftBean: GiftEntityProtocol? = null
    private var listener: OnGiftConfirmClickListener? = null
    private var position = 0
    override fun initArgument() {
        super.initArgument()
        val data = arguments
        if (null != data) position = data.getInt("position")
    }

    override val layoutId: Int
         get() = R.layout.demo_gift_fragment_list_layout

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        rvList = findViewById<RecyclerView>(R.id.rv_list)
        val snapHelper = PagingScrollHelper()
        val manager = HorizontalPageLayoutManager(1, 4)
        rvList?.setHasFixedSize(true)
        rvList?.layoutManager = manager

        //设置item 间距
        val itemDecoration = DividerItemDecoration(
            context, DividerItemDecoration.VERTICAL
        )
        val drawable = GradientDrawable()
        context?.let {
            drawable.setSize(3.dpToPx(it), 0)
        }
        itemDecoration.setDrawable(drawable)
        rvList?.addItemDecoration(itemDecoration)
        adapter = GiftListAdapter()
        adapter?.hideEmptyView(true)
        rvList?.adapter = adapter
        snapHelper.setUpRecycleView(rvList)
        snapHelper.updateLayoutManger()
        snapHelper.scrollToPosition(0)
        rvList?.isHorizontalScrollBarEnabled = true
    }

    override fun initListener() {
        super.initListener()
        adapter?.setOnItemClickListener(this)
    }

    override fun onResume() {
        super.onResume()
        adapter?.let {
            it.setData(CallGiftHelper.getGiftsByPage(position).toMutableList())
            if (it.mData.isNullOrEmpty().not()){
                it.setSelectedPosition(0)
                listener?.onFirstItem(it.getItem(0))
            }
        }
    }

    override fun onItemClick(view: View?, position: Int) {
        giftBean = adapter?.getItem(position)
        giftBean?.let { bean->
            val checked: Boolean = bean.isChecked
            bean.isChecked = !checked
            if (bean.isChecked) {
                adapter?.setSelectedPosition(position)
            } else {
                adapter?.setSelectedPosition(-1)
            }
            listener?.let {
                it.onConfirmClick(view, bean)
            }
        }

    }

    fun setOnItemSelectClickListener(listener: OnGiftConfirmClickListener?) {
        this.listener = listener
    }
}