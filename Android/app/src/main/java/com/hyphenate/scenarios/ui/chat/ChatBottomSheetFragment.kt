package com.hyphenate.scenarios.ui.chat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.hyphenate.easeui.base.EaseBaseSheetFragmentDialog
import com.hyphenate.easeui.feature.chat.EaseChatFragment
import com.hyphenate.scenarios.R
import com.hyphenate.scenarios.databinding.DemoChatBottomSheetLayoutBinding

class ChatBottomSheetFragment(
    private val conversationId: String
) : EaseBaseSheetFragmentDialog<DemoChatBottomSheetLayoutBinding>() {

    override fun getViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): DemoChatBottomSheetLayoutBinding {
        return DemoChatBottomSheetLayoutBinding.inflate(inflater, container, false)
    }

    private var fragment:Fragment? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding?.root?.let { setOnApplyWindowInsets(it) }
        val chatFragment = ChatFragment()
        chatFragment.hideMenu()
        fragment = EaseChatFragment.Builder(conversationId)
            .useTitleBar(true)
            .enableTitleBarPressBack(true)
            .setTitleBarBackPressListener { dismiss() }
            .setCustomFragment(chatFragment).build()
        fragment?.let { replaceFragment(it) }

    }

    private fun replaceFragment(fragment: Fragment) {
        childFragmentManager.beginTransaction()
            .replace(R.id.fl_fragment_container, fragment)
            .commit()
    }

    override fun onStart() {
        super.onStart()
        // 设置 BottomSheet 的高度
        binding?.flFragmentContainer?.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, (resources.displayMetrics.heightPixels * 3 / 4)
        )
        // 禁用滑动
        val bottomSheet = dialog?.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
        val behavior = BottomSheetBehavior.from(bottomSheet!!)
        behavior.isDraggable = false
        behavior.state = BottomSheetBehavior.STATE_EXPANDED // 确保底部抽屉是展开状态
    }

}