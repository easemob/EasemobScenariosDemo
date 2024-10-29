package com.hyphenate.scenarios.callkit.gift

import android.app.Activity
import android.content.Context
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.widget.PopupWindow
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding

abstract class CommonPopupWindow<V : View?, Binding : ViewDataBinding> : PopupWindow {
    private val context: Context? = null
    private var onDismissListener: OnDismissListener?

    private constructor(context: Context, builder: ViewDataBindingBuilder<Binding>) {
        intercept.intercept()
        contentView = builder.mBinding?.root
        width = builder.mWidth
        height = builder.mHeight
        isOutsideTouchable = builder.mOutsideTouchable
        setBackgroundDrawable(builder.mBackground)
        isFocusable = builder.mFocusable
        /*
         * 结合showAtLocation使用精准定位，需设置mClippingEnabled为false,否则当内容过多时会移位，比如设置在某
         * 个控件底下内容过多时popupwindow会上移
         */isClippingEnabled = builder.mClippingEnabled
        animationStyle = builder.mAnimationStyle
        onDismissListener = builder.onDismissListener
        super.setOnDismissListener {
            if (context is Activity) {
                val lp = context.window.attributes
                if (lp != null && lp.alpha != 1.0f) {
                    lp.alpha = 1.0f /* 0.0~1.0*/
                    context.window.attributes = lp
                }
            }
            if (onDismissListener != null) {
                onDismissListener!!.onDismiss()
            }
            builder.mBinding?.unbind()
        }
    }

    private constructor(context: Context, builder: ViewBuilder<V>) : super(context) {
        intercept.intercept()
        contentView = builder.view
        width = builder.mWidth
        height = builder.mHeight
        isOutsideTouchable = builder.mOutsideTouchable
        setBackgroundDrawable(builder.mBackground)
        isFocusable = builder.mFocusable
        /*
         * 结合showAtLocation使用精准定位，需设置mClippingEnabled为false,否则当内容过多时会移位，比如设置在某
         * 个控件底下内容过多时popupwindow会上移
         */isClippingEnabled = builder.mClippingEnabled
        animationStyle = builder.mAnimationStyle
        onDismissListener = builder.onDismissListener
        super.setOnDismissListener {
            if (context is Activity) {
                val lp = context.window.attributes
                if (lp != null && lp.alpha != 1.0f) {
                    lp.alpha = 1.0f /* 0.0~1.0*/
                    context.window.attributes = lp
                }
            }
            if (onDismissListener != null) {
                onDismissListener!!.onDismiss()
            }
        }
    }

    override fun showAsDropDown(anchor: View, xoff: Int, yoff: Int) {
        if (contentView != null) {
            val context = contentView.context
            if (context is Activity) {
                val lp = context.window.attributes
                if (lp != null && lp.alpha != alpha) {
                    lp.alpha = alpha
                    context.window.attributes = lp
                }
            }
        }
        intercept.showBefore()
        super.showAsDropDown(anchor, xoff, yoff)
        intercept.showAfter()
    }

    override fun showAtLocation(parent: View, gravity: Int, x: Int, y: Int) {
        if (contentView != null) {
            val context = contentView.context
            if (context is Activity) {
                val lp = context.window.attributes
                if (lp != null && lp.alpha != alpha) {
                    lp.alpha = alpha
                    context.window.attributes = lp
                }
            }
        }
        intercept.showBefore()
        super.showAtLocation(parent, gravity, x, y)
        intercept.showAfter()
    }

    override fun setOnDismissListener(onDismissListener: OnDismissListener) {
        this.onDismissListener = onDismissListener
    }

    abstract val alpha: Float
    abstract val instance: CommonPopupWindow<*, *>?
    abstract val intercept: InterceptTransform<*>

    class ViewDataBindingBuilder<Binding : ViewDataBinding> {
        var mBinding: Binding? = null
        var mWidth = 0
        var mHeight = 0
        var mOutsideTouchable = false
        var mEvent: ViewEvent<Binding>? = null
        var mBackground: Drawable? = null
        var mFocusable = false

        /**
         * 设置窗口弹出时背景的透明度
         * 0f（透明）~1.0f（正常）
         * 设置了alpha时需要在onDismiss恢复窗口的alpha至默认值即1.0f
         */
        private var alpha = 1.0f

        /**
         * 结合showAtLocation使用精准定位，需设置mClippingEnabled为false,否则当内容过多时会移位，比如设置在某
         * 个控件底下内容过多时popupwindow会上移
         */
        var mClippingEnabled = true
        var mOnShowBefore: OnShowBefore<Binding>? = null
        var mOnShowAfter: OnShowAfter<Binding>? = null
        var mAnimationStyle = -1
        var onDismissListener: OnDismissListener? = null
        fun layoutId(context: Context?, layoutId: Int): ViewDataBindingBuilder<Binding> {
            mBinding = DataBindingUtil.inflate(LayoutInflater.from(context), layoutId, null, false)
            return this
        }

        fun viewDataBinding(mBinding: Binding): ViewDataBindingBuilder<Binding> {
            this.mBinding = mBinding
            return this
        }

        fun width(width: Int): ViewDataBindingBuilder<Binding> {
            mWidth = width
            return this
        }

        fun height(height: Int): ViewDataBindingBuilder<Binding> {
            mHeight = height
            return this
        }

        fun outsideTouchable(outsideTouchable: Boolean): ViewDataBindingBuilder<Binding> {
            mOutsideTouchable = outsideTouchable
            return this
        }

        fun backgroundDrawable(background: Drawable?): ViewDataBindingBuilder<Binding> {
            mBackground = background
            return this
        }

        fun focusable(focusable: Boolean): ViewDataBindingBuilder<Binding> {
            mFocusable = focusable
            return this
        }

        fun alpha(alpha: Float): ViewDataBindingBuilder<Binding> {
            this.alpha = alpha
            return this
        }

        fun clippingEnabled(clippingEnabled: Boolean): ViewDataBindingBuilder<Binding> {
            mClippingEnabled = clippingEnabled
            return this
        }

        fun onShowBefore(showBefore: OnShowBefore<Binding>?): ViewDataBindingBuilder<Binding> {
            mOnShowBefore = showBefore
            return this
        }

        fun onShowAfter(showAfter: OnShowAfter<Binding>?): ViewDataBindingBuilder<Binding> {
            mOnShowAfter = showAfter
            return this
        }

        fun animationStyle(animationStyle: Int): ViewDataBindingBuilder<Binding> {
            mAnimationStyle = animationStyle
            return this
        }

        fun intercept(event: ViewEvent<Binding>?): ViewDataBindingBuilder<Binding> {
            mEvent = event
            return this
        }

        fun onDismissListener(onDismissListener: OnDismissListener?): ViewDataBindingBuilder<Binding> {
            this.onDismissListener = onDismissListener
            return this
        }

        fun <V : View?> build(context: Context): CommonPopupWindow<V, Binding> {
            return object : CommonPopupWindow<V, Binding>(context, this) {
                override val alpha: Float = 1.0f
                override val instance: CommonPopupWindow<V, Binding>
                    get() = this
                override val intercept: InterceptTransform<*>
                    get() = object : InterceptTransform<Binding>() {
                        override fun showBefore() {
                            mOnShowBefore?.showBefore(instance, mBinding)
                        }

                        override fun showAfter() {
                            mOnShowAfter?.showAfter(instance, mBinding)
                        }

                        override fun intercept() {
                            mEvent?.getView(instance, mBinding)
                        }
                    }
            }
        }
    }

    class ViewBuilder<V : View?> {
        var view: V? = null
        var mWidth = 0
        var mHeight = 0
        var mOutsideTouchable = false
        var mEvent: ViewEvent<V>? = null
        var mBackground: Drawable? = null
        var mFocusable = false

        /**
         * 设置窗口弹出时背景的透明度
         * 0f（透明）~1.0f（正常）
         * 设置了alpha时需要在onDismiss恢复窗口的alpha至默认值即1.0f
         */
        var alpha = 1.0f

        /**
         * 结合showAtLocation使用精准定位，需设置mClippingEnabled为false,否则当内容过多时会移位，比如设置在某
         * 个控件底下内容过多时popupwindow会上移
         */
         var mClippingEnabled = true
         var mOnShowBefore: OnShowBefore<V>? = null
         var mOnShowAfter: OnShowAfter<V>? = null
         var mAnimationStyle = -1
         var onDismissListener: OnDismissListener? = null
        fun view(view: V): ViewBuilder<V> {
            this.view = view
            return this
        }

        fun width(width: Int): ViewBuilder<V> {
            mWidth = width
            return this
        }

        fun height(height: Int): ViewBuilder<V> {
            mHeight = height
            return this
        }

        fun outsideTouchable(outsideTouchable: Boolean): ViewBuilder<V> {
            mOutsideTouchable = outsideTouchable
            return this
        }

        fun backgroundDrawable(background: Drawable?): ViewBuilder<V> {
            mBackground = background
            return this
        }

        fun focusable(focusable: Boolean): ViewBuilder<V> {
            mFocusable = focusable
            return this
        }

        fun alpha(alpha: Float): ViewBuilder<V> {
            this.alpha = alpha
            return this
        }

        fun clippingEnabled(clippingEnabled: Boolean): ViewBuilder<V> {
            mClippingEnabled = clippingEnabled
            return this
        }

        fun onShowBefore(showBefore: OnShowBefore<V>): ViewBuilder<V> {
            mOnShowBefore = showBefore
            return this
        }

        fun onShowAfter(showAfter: OnShowAfter<V>): ViewBuilder<V> {
            mOnShowAfter = showAfter
            return this
        }

        fun animationStyle(animationStyle: Int): ViewBuilder<V> {
            mAnimationStyle = animationStyle
            return this
        }

        fun intercept(event: ViewEvent<V>): ViewBuilder<V> {
            mEvent = event
            return this
        }

        fun onDismissListener(onDismissListener: OnDismissListener?): ViewBuilder<V> {
            this.onDismissListener = onDismissListener
            return this
        }

        fun <T : ViewDataBinding> build(context: Context?): CommonPopupWindow<V, T> {
            return object : CommonPopupWindow<V, T>(context!!, this) {
                override val alpha: Float = 1.0f
                override val instance: CommonPopupWindow<V, T>
                    get() = this
                override val intercept: InterceptTransform<*>
                    get() = object : InterceptTransform<V>() {
                        override fun showBefore() {
                            mOnShowBefore?.showBefore(instance, view)
                        }

                        override fun showAfter() {
                            mOnShowAfter?.showAfter(instance, view)
                        }

                        override fun intercept() {
                            mEvent?.getView(instance, view)
                        }
                    }
            }
        }
    }

    interface ViewEvent<T> {
        fun getView(popupWindow: CommonPopupWindow<*, *>?, view: T?)
    }

    abstract class InterceptTransform<V> {
        abstract fun showBefore()
        abstract fun showAfter()
        abstract fun intercept()
    }

    interface OnShowBefore<V> {
        fun showBefore(popupWindow: CommonPopupWindow<*, *>, view: V?)
    }

    interface OnShowAfter<V> {
        fun showAfter(popupWindow: CommonPopupWindow<*, *>, view: V?)
    }
}