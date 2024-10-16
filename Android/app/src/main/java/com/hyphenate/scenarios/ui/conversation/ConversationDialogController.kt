package com.hyphenate.scenarios.ui.conversation

import android.content.Context
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.hyphenate.easeui.EaseIM
import com.hyphenate.easeui.common.dialog.SimpleListSheetDialog
import com.hyphenate.easeui.common.dialog.SimpleSheetType
import com.hyphenate.easeui.interfaces.SimpleListSheetItemClickListener
import com.hyphenate.easeui.model.EaseMenuItem
import com.hyphenate.scenarios.R

class ConversationDialogController(
    private val context: Context,
    private val fragment: Fragment,
    private val conversationId:String?,
) {

    fun showMenuDialog(){
        val context = (context as FragmentActivity)
        conversationId?.let { EaseIM.checkMutedConversationList(it) }
        val mutableListOf = mutableListOf(
            EaseMenuItem(
                menuId = com.hyphenate.easeui.R.id.ease_action_conv_menu_silent,
                order = 0,
                title = context.getString(com.hyphenate.easeui.R.string.ease_conv_menu_item_silent),
                titleColor = ContextCompat.getColor(context, com.hyphenate.easeui.R.color.ease_color_primary)
            ),
            EaseMenuItem(
                menuId = com.hyphenate.easeui.R.id.ease_action_conv_menu_unsilent,
                order = 1,
                title = context.getString(com.hyphenate.easeui.R.string.ease_conv_menu_item_unsilent),
                titleColor = ContextCompat.getColor(context, com.hyphenate.easeui.R.color.ease_color_primary)
            ),
            EaseMenuItem(
                menuId = com.hyphenate.easeui.R.id.ease_action_conv_menu_pin,
                title = context.getString(com.hyphenate.easeui.R.string.ease_conv_menu_item_pin),
                titleColor = ContextCompat.getColor(context, com.hyphenate.easeui.R.color.ease_color_primary)
            ),
            EaseMenuItem(
                menuId = com.hyphenate.easeui.R.id.ease_action_conv_menu_unpin,
                title = context.getString(com.hyphenate.easeui.R.string.ease_conv_menu_item_unpin),
                titleColor = ContextCompat.getColor(context, com.hyphenate.easeui.R.color.ease_color_primary)
            ),
            EaseMenuItem(
                menuId = R.id.action_conversation_menu_clear,
                title = context.getString(R.string.em_conversation_menu_clear),
                titleColor = ContextCompat.getColor(context, com.hyphenate.easeui.R.color.ease_color_primary)
            ),
            EaseMenuItem(
                menuId = com.hyphenate.easeui.R.id.ease_action_conv_menu_delete,
                title = context.getString(com.hyphenate.easeui.R.string.ease_conv_menu_item_delete),
                titleColor = ContextCompat.getColor(context, com.hyphenate.easeui.R.color.ease_color_primary)
            ),
        )

        val dialog = SimpleListSheetDialog(
            context = context,
            itemList = mutableListOf,
            type = SimpleSheetType.ITEM_LAYOUT_DIRECTION_CENTER)

        dialog.setSimpleListSheetItemClickListener(object : SimpleListSheetItemClickListener {
            override fun onItemClickListener(position: Int, menu: EaseMenuItem) {
                dialog.dismiss()
                when(menu.menuId){
                    com.hyphenate.easeui.R.id.ease_action_conv_menu_silent -> {

                    }
                    com.hyphenate.easeui.R.id.ease_action_conv_menu_unsilent -> {

                    }
                    com.hyphenate.easeui.R.id.ease_action_conv_menu_pin -> {

                    }
                    com.hyphenate.easeui.R.id.ease_action_conv_menu_unpin -> {

                    }
                    R.id.action_conversation_menu_clear -> {

                    }
                    com.hyphenate.easeui.R.id.ease_action_conv_menu_delete -> {

                    }
                    else -> {}
                }
            }
        })
        context.supportFragmentManager.let { dialog.show(it,"conversation_action_menu") }
    }
}