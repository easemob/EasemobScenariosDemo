package com.hyphenate.scenarios.callkit.gift

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.hyphenate.scenarios.R

class GiftPopupWindowAdapter(private val context: Context, resource: Int,private val data: Array<String>) :
    ArrayAdapter<String>(context, resource, data) {
    private var listener: OnItemClickListener? = null
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val ss = getItem(position) //获得数据的实例
        val view: View
        val holder: ViewHolder
        if (convertView == null) { //反复利用布局
            view = LayoutInflater.from(getContext())
                .inflate(R.layout.demo_gift_pop_item_layout, null) //加载每个item的布局
            holder = ViewHolder()
            holder.select_count = view.findViewById<TextView>(R.id.select_count) //布局中的TextView实例
            view.tag = holder
        } else {
            view = convertView
            holder = view.tag as ViewHolder
        }
        holder.select_count?.text = ss
        holder.select_count?.setOnClickListener {
            ss?.let { it1 -> listener?.OnItemClick(position, it1) }
        }
        return view
    }

    internal class ViewHolder {
        var select_count: TextView? = null
    }

    fun setOnItemClickListener(listener: OnItemClickListener?) {
        this.listener = listener
    }

    interface OnItemClickListener {
        fun OnItemClick(position: Int, count: String)
    }
}