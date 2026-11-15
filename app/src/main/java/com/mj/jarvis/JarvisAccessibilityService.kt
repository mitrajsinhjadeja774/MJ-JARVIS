package com.mj.jarvis

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityNodeInfo

/**
 * User-enabled accessibility bridge for explicit JARVIS UI commands.
 * It does not listen to the microphone or run commands on its own.
 */
class JarvisAccessibilityService : AccessibilityService() {
    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
    }

    override fun onInterrupt() {}

    override fun onDestroy() {
        if (instance === this) instance = null
        super.onDestroy()
    }

    private fun runUiCommand(raw: String): Boolean {
        val command = raw.trim().lowercase()
        return when {
            command == "back" || command == "go back" -> performGlobalAction(GLOBAL_ACTION_BACK)
            command == "home" || command == "go home" -> performGlobalAction(GLOBAL_ACTION_HOME)
            command == "recents" || command == "recent apps" -> performGlobalAction(GLOBAL_ACTION_RECENTS)
            command == "scroll down" -> rootInActiveWindow?.let { scroll(it, forward = true) } ?: false
            command == "scroll up" -> rootInActiveWindow?.let { scroll(it, forward = false) } ?: false
            command.startsWith("tap ") -> clickText(command.removePrefix("tap ").trim())
            else -> false
        }
    }

    private fun scroll(node: AccessibilityNodeInfo, forward: Boolean): Boolean {
        if (node.isScrollable) {
            val action = if (forward) AccessibilityNodeInfo.ACTION_SCROLL_FORWARD
                         else AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD
            if (node.performAction(action)) return true
        }
        for (index in 0 until node.childCount) {
            val child = node.getChild(index) ?: continue
            if (scroll(child, forward)) return true
        }
        return false
    }

    private fun clickText(text: String): Boolean {
        val root = rootInActiveWindow ?: return false
        val matches = root.findAccessibilityNodeInfosByText(text)
        for (match in matches) {
            var node: AccessibilityNodeInfo? = match
            while (node != null) {
                if (node.isClickable && node.performAction(AccessibilityNodeInfo.ACTION_CLICK)) return true
                node = node.parent
            }
        }
        return false
    }

    companion object {
        @Volatile private var instance: JarvisAccessibilityService? = null

        fun execute(command: String): Boolean = instance?.runUiCommand(command) ?: false
        fun isEnabled(): Boolean = instance != null
    }
}
