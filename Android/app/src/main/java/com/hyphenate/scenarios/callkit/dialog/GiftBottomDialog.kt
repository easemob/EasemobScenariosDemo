package com.hyphenate.scenarios.callkit.dialog

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.viewpager2.widget.ViewPager2
import com.hyphenate.easeui.common.extensions.dpToPx
import com.hyphenate.scenarios.R
import com.hyphenate.scenarios.bean.GiftEntityProtocol
import com.hyphenate.scenarios.callkit.adapter.GiftFragmentAdapter
import com.hyphenate.scenarios.callkit.gift.CommonPopupWindow
import com.hyphenate.scenarios.callkit.gift.GiftPopupWindowAdapter
import com.hyphenate.scenarios.callkit.helper.CallGiftHelper
import com.hyphenate.scenarios.databinding.DemoGiftDialogLayoutBinding
import com.hyphenate.scenarios.databinding.DemoGiftPopupwindowLayoutBinding
import kotlin.math.roundToInt

class GiftBottomDialog(
    private val onSendGiftClickListener: ((v:View, bean:GiftEntityProtocol?) -> Unit)?,
) : BaseSheetDialog<DemoGiftDialogLayoutBinding?>(),
    View.OnClickListener {
    private var currentIndex = 0 //当前页面,默认首页
    private var adapter: GiftFragmentAdapter? = null
    private var list: List<GiftEntityProtocol>? = null
    private var GiftNum = 1
    private var giftBean: GiftEntityProtocol? = null
    private var handler: Handler? = null

    override fun getViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): DemoGiftDialogLayoutBinding {
        return DemoGiftDialogLayoutBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding?.root?.let { setOnApplyWindowInsets(it) }
        initView(savedInstanceState)
        initListener()
        initData()
    }

    fun initView(savedInstanceState: Bundle?) {
        activity?.let {
            adapter = GiftFragmentAdapter(it)
            binding?.viewPager?.setAdapter(adapter)
        }
        handler = Handler(Looper.getMainLooper())
    }

    fun initData() {
        list = CallGiftHelper.getDefaultGifts()
        initPoints()
    }

    fun initListener() {
        binding?.viewPager?.offscreenPageLimit = 1
        binding?.viewPager?.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                //当前页卡被选择时,position为当前页数
                binding?.pagerDots?.getChildAt(position)?.isEnabled = false //不可点击
                binding?.pagerDots?.getChildAt(currentIndex)?.isEnabled = true //恢复之前页面状态
                currentIndex = position
                if (currentIndex == 0) {
                    binding?.pagerDots?.getChildAt(currentIndex)?.isEnabled = false
                } else if (currentIndex == 1) { }
            }
        })
        binding?.send?.setOnClickListener(View.OnClickListener { v ->
            if (giftBean?.giftEffect?.isNotEmpty() == true){
                setSendEnable(false)
                // 使用 Handler 在 3 秒后恢复按钮的点击事件
                startCountdown(4)
            }
            onSendGiftClickListener?.invoke(v,giftBean)
        })
        binding?.giftCountLayout?.setOnClickListener(View.OnClickListener { v -> showPop(v) })
        adapter?.setOnVpFragmentItemListener(object : GiftFragmentAdapter.OnVpFragmentItemListener {
            override fun onVpFragmentItem(position: Int, bean: Any?) {
                giftBean = bean as GiftEntityProtocol?
                giftBean?.let {
                    check(it.giftPrice?:"")
                }
                reset()
            }

            override fun onFirstData(bean: GiftEntityProtocol?) {
                giftBean = bean
                giftBean?.let {
                    check(it.giftPrice?:"")
                }
                activity?.let {
                    if (isAdded) {
                        binding?.totalCount?.text = it.getString(
                            R.string.dialog_gift_total_count,
                            bean?.giftPrice
                        )
                    }
                }
                binding?.count?.text = "1"
            }
        })
    }

    private fun isShowPop(isShow: Boolean) {
        if (isShow) {
            binding?.icon?.setImageResource(R.drawable.icon_arrow_down)
        } else {
            binding?.icon?.setImageResource(R.drawable.icon_arrow_up)
        }
    }

    override fun onClick(v: View) {
        binding?.viewPager?.currentItem = v.tag as Int
    }

    private fun initPoints() {
        list?.let {
            if (it.isNotEmpty()){
                if (it.size % 4 == 0){
                    addViewPagerDots(binding?.pagerDots, (it.size / 4 ))
                }else{
                    addViewPagerDots(binding?.pagerDots, (it.size / 4 + 0.5f).roundToInt())
                }
            }
        }
    }

    private fun startCountdown(seconds: Int) {
        var remainingTime = seconds
        binding?.send?.text = "$remainingTime"

        // 定义一个 Runnable 来更新倒计时
        val countdownRunnable = object : Runnable {
            override fun run() {
                remainingTime--
                binding?.send?.text = "$remainingTime"
                if (remainingTime > 0) {
                    handler?.postDelayed(this, 1000) // 每秒更新一次
                } else {
                    // 恢复按钮的点击事件
                    binding?.send?.let {
                        setSendEnable(true)
                        it.text = "发送"
                    }
                }
            }
        }
        // 开始倒计时
        handler?.post(countdownRunnable)
    }

    /**
     * 向一个线性布局里添加小圆点
     * @param llGuideGroup
     * @param count 要添加多少个小圆点
     */
    private fun addViewPagerDots(llGuideGroup: LinearLayoutCompat?, count: Int) {
        context?.let {
            Log.e("addViewPagerDots", "count: $count")
            if (llGuideGroup == null || count < 1 ) return
            val lp: LinearLayoutCompat.LayoutParams = LinearLayoutCompat.LayoutParams(
                5.dpToPx(it),5.dpToPx(it)
            )
            lp.leftMargin = 5.dpToPx(it)
            lp.rightMargin =5.dpToPx(it)
            for (i in 0 until count) {
                val imageView = ImageView(llGuideGroup.context)
                imageView.layoutParams = lp
                imageView.isEnabled = true //设置当前状态为允许点击（可点，灰色）
                imageView.setOnClickListener(this) //设置点击监听
                //额外设置一个标识符，以便点击小圆点时跳转对应页面
                imageView.tag = i //标识符与圆点顺序一致
                imageView.setBackgroundResource(R.drawable.bg_gift_vp_point)
                llGuideGroup.addView(imageView)
            }
        }
    }

    private fun showPop(itemView: View) {
        //Gets the coordinates attached to the view
        val location = IntArray(2)
        itemView.getLocationInWindow(location)
        context?.let {
           CommonPopupWindow.ViewDataBindingBuilder<DemoGiftPopupwindowLayoutBinding>()
                .width(120.dpToPx(it))
                .height(186.dpToPx(it))
                .outsideTouchable(true)
                .focusable(true)
                .clippingEnabled(false)
                .alpha(0.618f)
                .layoutId(it, R.layout.demo_gift_popupwindow_layout)
                .intercept(object : CommonPopupWindow.ViewEvent<DemoGiftPopupwindowLayoutBinding> {
                    override fun getView(
                        popupWindow: CommonPopupWindow<*, *>?,
                        view: DemoGiftPopupwindowLayoutBinding?
                    ) {
                        isShowPop(true)
                        val data = arrayOf("999", "599", "199", "99", "9", "1")
                        val adapter = GiftPopupWindowAdapter(it, 1, data)
                        view?.listView?.adapter = adapter
                        adapter.setOnItemClickListener(object : GiftPopupWindowAdapter.OnItemClickListener {
                            override fun OnItemClick(position: Int, count: String) {
                                if (count.isNotEmpty()){  GiftNum = count.toInt() }
                                giftBean?.let {
                                    val total: Int = GiftNum * it.giftPrice?.toInt()!!
                                    binding?.totalCount?.text = getString(
                                        R.string.dialog_gift_total_count,
                                        total.toString()
                                    )
                                    if (GiftNum >= 1){
                                        it.giftCount = GiftNum
                                        binding?.count?.text = count
                                        //礼物金额大于100的 数量只能选1
                                        if (it.giftPrice.isNullOrEmpty().not()){
                                            if (it.giftPrice!!.toInt() >= 100) {
                                                reset()
                                            }
                                        }
                                    }
                                }
                                popupWindow?.dismiss()
                            }
                        })
                    }
                })
                .onDismissListener { /*每次dismiss都会回调*/
                    isShowPop(false)
                }
                .build<ConstraintLayout>(it)
                .showAtLocation(
                    itemView, Gravity.NO_GRAVITY,
                    location[0] - 60.dpToPx(it) / 3,
                    location[1] - 186.dpToPx(it)
                )
        }
    }

    fun reset() {
        binding?.let {
            it.count.text = "1"
            giftBean?.let { info->
                info.giftCount = 1
                if (!info.isChecked){
                    it.totalCount.text = getString(
                        R.string.dialog_gift_total_count, "0"
                    )
                }else{
                    it.totalCount.text = getString(
                        R.string.dialog_gift_total_count, info.giftPrice
                    )
                }
            }
        }
    }

    fun check(price: String) {
        if (price.isNotEmpty()){
            if (price.toInt() < 100) {
                binding?.let {
                    it.icon.alpha = 1.0f
                    it.count.alpha = 1.0f
                    it.giftCountLayout.isEnabled = true
                }
            } else {
                binding?.let {
                    it.icon.alpha = 0.2f
                    it.count.alpha = 0.2f
                    it.giftCountLayout.isEnabled = false
                }
            }
        }
    }

    fun setSendEnable(enable: Boolean) {
        binding?.send?.isEnabled = enable
    }

}