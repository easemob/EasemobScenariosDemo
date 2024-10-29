package com.hyphenate.scenarios.callkit.widget

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.icu.text.MeasureFormat
import android.icu.util.Measure
import android.icu.util.MeasureUnit
import android.net.Uri
import android.os.SystemClock
import android.text.format.DateUtils
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.widget.Chronometer
import androidx.annotation.InspectableProperty
import androidx.appcompat.widget.AppCompatTextView
import com.hyphenate.scenarios.R
import java.util.Formatter
import java.util.IllegalFormatException
import java.util.Locale

class CallChronometer @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) :
    AppCompatTextView(context, attrs, defStyleAttr) {
    /**
     * A callback that notifies when the chronometer has incremented on its own.
     */
    interface OnChronometerTickListener {
        /**
         * Notification that the chronometer has changed.
         */
        fun onChronometerTick(chronometer: CallChronometer?)
    }

    private var mBase: Long = 0
    private var mNow: Long = 0 // the currently displayed time
    private var mVisible = false
    private var mStarted = false
    private var mRunning = false
    private var mLogged = false
    private var mFormat: String? = null
    private var mFormatter: Formatter? = null
    private var mFormatterLocale: Locale? = null
    private val mFormatterArgs = arrayOfNulls<Any>(1)
    private var mFormatBuilder: StringBuilder? = null
    /**
     * @return The listener (may be null) that is listening for chronometer change
     * events.
     */
    /**
     * Sets the listener to be called when the chronometer changes.
     *
     * @param listener The listener.
     */
    var onChronometerTickListener: OnChronometerTickListener? = null
    private val mRecycle = StringBuilder(8)
    private var mCountDown = false
    var costSeconds: Long = 0
        private set

    private fun init() {
        mBase = SystemClock.elapsedRealtime()
        updateText(mBase)
    }

    @get:InspectableProperty
    var isCountDown: Boolean
        /**
         * @return whether this view counts down
         *
         * @see .setCountDown
         */
        get() = mCountDown
        /**
         * Set this view to count down to the base instead of counting up from it.
         *
         * @param countDown whether this view should count down
         *
         * @see .setBase
         */
        set(countDown) {
            mCountDown = countDown
            updateText(SystemClock.elapsedRealtime())
        }
    val isTheFinalCountDown: Boolean
        /**
         * @return whether this is the final countdown
         */
        get() {
            try {
                context.startActivity(
                    Intent(Intent.ACTION_VIEW, Uri.parse("https://youtu.be/9jK-NcRmVcw"))
                        .addCategory(Intent.CATEGORY_BROWSABLE)
                        .addFlags(
                            Intent.FLAG_ACTIVITY_NEW_DOCUMENT
                                    or Intent.FLAG_ACTIVITY_LAUNCH_ADJACENT
                        )
                )
                return true
            } catch (e: Exception) {
                return false
            }
        }
    var base: Long
        /**
         * Return the base time as set through [.setBase].
         */
        get() = mBase
        /**
         * Set the time that the count-up timer is in reference to.
         *
         * @param base Use the [SystemClock.elapsedRealtime] time base.
         */
        set(base) {
            mBase = base
            dispatchChronometerTick()
            updateText(SystemClock.elapsedRealtime())
        }

    var format: String?
        /**
         * Returns the current format string as set through [.setFormat].
         */
        get() = mFormat
        /**
         * Sets the format string used for display.  The Chronometer will display
         * this string, with the first "%s" replaced by the current timer value in
         * "MM:SS" or "H:MM:SS" form.
         *
         * If the format string is null, or if you never call setFormat(), the
         * Chronometer will simply display the timer value in "MM:SS" or "H:MM:SS"
         * form.
         *
         * @param format the format string.
         */
        set(format) {
            mFormat = format
            if (format != null && mFormatBuilder == null) {
                mFormatBuilder = StringBuilder(format.length * 2)
            }
        }

    /**
     * Start counting up.  This does not affect the base as set from [.setBase], just
     * the view display.
     *
     * Chronometer works by regularly scheduling messages to the handler, even when the
     * Widget is not visible.  To make sure resource leaks do not occur, the user should
     * make sure that each start() call has a reciprocal call to [.stop].
     */
    fun start() {
        mStarted = true
        updateRunning()
    }

    /**
     * Stop counting up.  This does not affect the base as set from [.setBase], just
     * the view display.
     *
     * This stops the messages to the handler, effectively releasing resources that would
     * be held as the chronometer is running, via [.start].
     */
    fun stop() {
        mStarted = false
        updateRunning()
    }

    /**
     * The same as calling [.start] or [.stop].
     * @hide pending API council approval
     */
    fun setStarted(started: Boolean) {
        mStarted = started
        updateRunning()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        mVisible = false
        updateRunning()
    }

    override fun onWindowVisibilityChanged(visibility: Int) {
        //continue when view is hidden
        var visibility = visibility
        visibility = VISIBLE
        super.onWindowVisibilityChanged(visibility)
        mVisible = visibility == VISIBLE
        updateRunning()
    }

    override fun onVisibilityChanged(changedView: View, visibility: Int) {
        super.onVisibilityChanged(changedView, visibility)
        updateRunning()
    }

    @Synchronized
    private fun updateText(now: Long) {
        mNow = now
        Log.e(TAG, "now: " + mNow + " mBase: " + mBase + " cost: " + (mNow - mBase))
        var seconds = if (mCountDown) mBase - now else now - mBase
        seconds /= 1000
        var negative = false
        if (seconds < 0) {
            seconds = -seconds
            negative = true
        }
        costSeconds = seconds

        var text = DateUtils.formatElapsedTime(mRecycle, seconds)

        if (negative) {
            text = resources.getString(R.string.negative_duration, text)
        }
        if (mFormat != null) {
            val loc = Locale.getDefault()
            if (mFormatter == null || loc != mFormatterLocale) {
                mFormatterLocale = loc
                mFormatter = Formatter(mFormatBuilder, loc)
            }
            mFormatBuilder?.setLength(0)
            mFormatterArgs[0] = text
            try {
                mFormatter?.format(mFormat, *mFormatterArgs)
                text = mFormatBuilder.toString()
            } catch (ex: IllegalFormatException) {
                if (!mLogged) {
                    Log.w(
                        TAG,
                        "Illegal format string: $mFormat"
                    )
                    mLogged = true
                }
            }
        }
        setText(text)
    }

    private fun updateRunning() {
        val running = mVisible && mStarted && isShown
        if (running != mRunning) {
            if (running) {
                updateText(SystemClock.elapsedRealtime())
                dispatchChronometerTick()
                postDelayed(mTickRunnable, 1000)
            } else {
                removeCallbacks(mTickRunnable)
            }
            mRunning = running
        }
    }

    private fun formatMillisToHHMMSS(millis: Long): String {
        val totalSeconds = (millis / 1000).toInt()
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60
        return String.format("%02d:%02d:%02d", hours, minutes, seconds)
    }

    private val mTickRunnable: Runnable = object : Runnable {
        override fun run() {
            if (mRunning) {
                updateText(SystemClock.elapsedRealtime())
                dispatchChronometerTick()
                postDelayed(this, 1000)
            }
        }
    }

    fun dispatchChronometerTick() {
        if (onChronometerTickListener != null) {
            onChronometerTickListener!!.onChronometerTick(this)
        }
    }
    /**
     * Initialize with standard view layout information and style.
     * Sets the base to the current time.
     */
    /**
     * Initialize this Chronometer object.
     * Sets the base to the current time.
     */
    /**
     * Initialize with standard view layout information.
     * Sets the base to the current time.
     */
    init {
        val a = context.obtainStyledAttributes(
            attrs, R.styleable.EMChronometer, defStyleAttr, 0
        )
        format = a.getString(R.styleable.EMChronometer_format)
        isCountDown = a.getBoolean(R.styleable.EMChronometer_countDown, false)
        a.recycle()
        init()
    }

    @SuppressLint("GetContentDescriptionOverride")
    override fun getContentDescription(): CharSequence {
        return formatDuration(mNow - mBase)
    }

    override fun getAccessibilityClassName(): CharSequence {
        return Chronometer::class.java.name
    }

    companion object {
        private val TAG = "Chronometer"
        private val MIN_IN_SEC = 60
        private val HOUR_IN_SEC = MIN_IN_SEC * 60
        private fun formatDuration(ms: Long): String {
            var duration = (ms / DateUtils.SECOND_IN_MILLIS).toInt()
            if (duration < 0) {
                duration = -duration
            }
            var h = 0
            var m = 0
            if (duration >= HOUR_IN_SEC) {
                h = duration / HOUR_IN_SEC
                duration -= h * HOUR_IN_SEC
            }
            if (duration >= MIN_IN_SEC) {
                m = duration / MIN_IN_SEC
                duration -= m * MIN_IN_SEC
            }
            val s = duration
            val measures = ArrayList<Measure>()
            if (h > 0) {
                measures.add(Measure(h, MeasureUnit.HOUR))
            }
            if (m > 0) {
                measures.add(Measure(m, MeasureUnit.MINUTE))
            }
            measures.add(Measure(s, MeasureUnit.SECOND))
            return MeasureFormat.getInstance(Locale.getDefault(), MeasureFormat.FormatWidth.WIDE)
                .formatMeasures(*measures.toTypedArray())
        }
    }
}