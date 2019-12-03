package com.boileryao.grabber

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.content.Context
import android.graphics.Path
import android.graphics.PixelFormat
import android.media.AudioManager
import android.view.Gravity
import android.view.LayoutInflater
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.Button
import android.widget.FrameLayout
import java.util.*


/**
 * @author boileryao(姚翔宇)
 */
class GlobalActionBarService : AccessibilityService() {
    private lateinit var contentView: FrameLayout

    override fun onServiceConnected() {
        super.onServiceConnected()
        val wm = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        contentView = FrameLayout(this)

        val lp = WindowManager.LayoutParams()
        lp.type = WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY
        lp.format = PixelFormat.TRANSLUCENT
        lp.flags = lp.flags or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
        lp.width = WindowManager.LayoutParams.WRAP_CONTENT
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT
        lp.gravity = Gravity.TOP

        val inflater = LayoutInflater.from(this)
        inflater.inflate(R.layout.overlapping_action_bar, contentView)
        wm.addView(contentView, lp)

        configButtons()
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        event ?: return
    }

    override fun onInterrupt() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    private fun configButtons() {
        configurePowerButton()
        configureVolumeButton()
        configureScrollButton()
        configureSwipeButton()
    }

    private fun configurePowerButton() {
        contentView.findViewById<Button>(R.id.power).setOnClickListener {
            performGlobalAction(GLOBAL_ACTION_POWER_DIALOG)
        }
    }

    private fun configureVolumeButton() {
        fun changeMusicChanelVolume(direction: Int) {
            val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
            audioManager.adjustStreamVolume(
                AudioManager.STREAM_MUSIC, direction, AudioManager.FLAG_SHOW_UI
            )
        }
        with(contentView.findViewById<Button>(R.id.volume)) {
            // click to increase
            setOnClickListener { changeMusicChanelVolume(AudioManager.ADJUST_RAISE) }
            // long click to decrease
            setOnLongClickListener {
                changeMusicChanelVolume(AudioManager.ADJUST_LOWER)
                /* return */ true
            }
        }
    }

    private fun configureScrollButton() {
        contentView.findViewById<Button>(R.id.scroll).setOnClickListener {
            val scrollableNode = findScrollableNode(rootInActiveWindow) ?: return@setOnClickListener
            scrollableNode.performAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_SCROLL_FORWARD.id)
        }
    }

    private fun configureSwipeButton() {
        contentView.findViewById<Button>(R.id.swipe).setOnClickListener {
            val swipePath = Path()
            swipePath.moveTo(1000f, 1000f)
            swipePath.lineTo(100f, 1000f)
            val gestureBuilder = GestureDescription.Builder()
            gestureBuilder.addStroke(GestureDescription.StrokeDescription(swipePath, 0, 500))
            dispatchGesture(gestureBuilder.build(), null, null)
        }
    }

    private fun findScrollableNode(root: AccessibilityNodeInfo): AccessibilityNodeInfo? {
        val deque = ArrayDeque<AccessibilityNodeInfo>()
        deque.add(root)
        while (!deque.isEmpty()) {
            val node = deque.removeFirst()
            if (node.actionList.contains(AccessibilityNodeInfo.AccessibilityAction.ACTION_SCROLL_FORWARD)) {
                return node
            }
            for (i in 0 until node.childCount) {
                deque.addLast(node.getChild(i))
            }
        }
        return null
    }

    companion object {
        @Suppress("unused")
        private const val TAG = "GlobalActionBarService"
    }
}
