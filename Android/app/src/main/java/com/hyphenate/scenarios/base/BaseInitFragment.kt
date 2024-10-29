package com.hyphenate.scenarios.base

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import androidx.annotation.IdRes
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment

abstract class BaseInitFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val layoutId = layoutId
        var view: View? = null
        view = if (layoutId != 0) {
            inflater.inflate(layoutId, container, false)
        } else {
            getContentView(inflater, container, savedInstanceState)
        }
        initArgument()
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView(savedInstanceState)
        initViewModel()
        initListener()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initData()
    }

    protected open val layoutId: Int
        /**
         * Return the layout ID
         * @return
         */
        protected get() = 0

    /**
     * Return the layout view
     * @return
     * @param inflater
     * @param container
     * @param savedInstanceState
     */
    protected fun getContentView(
        inflater: LayoutInflater?,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return null
    }

    /**
     * Initialize the params
     */
    protected open fun initArgument() {}

    /**
     * Initialize the views
     * @param savedInstanceState
     */
    protected open fun initView(savedInstanceState: Bundle?) {}

    /**
     * Initialize the viewmodels
     */
    protected fun initViewModel() {}

    /**
     * Initialize the listeners
     */
    protected open fun initListener() {}

    /**
     * Initialize the data
     */
    protected open fun initData() {}

    /**
     * hide keyboard
     */
    protected fun hideKeyboard() {
        activity?.let { ac->
            if (ac.window?.attributes?.softInputMode != WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN) {
                ac.currentFocus?.let {
                    val inputManager = ac.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    inputManager.hideSoftInputFromWindow(
                        it.windowToken,
                        InputMethodManager.HIDE_NOT_ALWAYS
                    )
                }
            }
        }
    }

    /**
     * Call it after [.onCreateView]
     * @param id
     * @param <T>
     * @return
    </T> */
    protected fun <T : View?> findViewById(@IdRes id: Int): T? {
        return view?.findViewById(id)
    }
}
