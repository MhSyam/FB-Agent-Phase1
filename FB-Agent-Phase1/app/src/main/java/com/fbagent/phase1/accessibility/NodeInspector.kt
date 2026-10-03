package com.fbagent.phase1.accessibility

import android.view.accessibility.AccessibilityNodeInfo

/**
 * Reads Facebook's accessibility tree without depending on Facebook view IDs/classes.
 * The root is owned by Android and is intentionally NOT recycled here.
 * Child nodes obtained by getChild() are recycled after use.
 */
object NodeInspector {
    private const val MAX_TEXT_CHARS = 12_000
    private const val MAX_DEPTH = 40

    fun extract(root: AccessibilityNodeInfo?): String {
        if (root == null) return ""
        val out = StringBuilder()
        walk(root, out, 0)
        return out.toString().trim()
    }

    fun containsAny(root: AccessibilityNodeInfo?, terms: Collection<String>): Boolean {
        if (root == null || terms.isEmpty()) return false
        val wanted = terms.map { it.lowercase() }
        return containsAnyRecursive(root, wanted, 0)
    }

    private fun containsAnyRecursive(node: AccessibilityNodeInfo, terms: List<String>, depth: Int): Boolean {
        if (depth > MAX_DEPTH) return false
        if (matches(node.text?.toString(), terms) || matches(node.contentDescription?.toString(), terms)) return true
        for (i in 0 until node.childCount) {
            val child = try { node.getChild(i) } catch (_: RuntimeException) { null } ?: continue
            try {
                if (containsAnyRecursive(child, terms, depth + 1)) return true
            } finally {
                child.recycle()
            }
        }
        return false
    }

    private fun walk(node: AccessibilityNodeInfo, out: StringBuilder, depth: Int) {
        if (depth > MAX_DEPTH || out.length >= MAX_TEXT_CHARS) return

        append(node.text?.toString(), out)
        append(node.contentDescription?.toString(), out)

        for (i in 0 until node.childCount) {
            if (out.length >= MAX_TEXT_CHARS) break
            val child = try { node.getChild(i) } catch (_: RuntimeException) { null } ?: continue
            try {
                walk(child, out, depth + 1)
            } finally {
                child.recycle()
            }
        }
    }

    private fun append(value: String?, out: StringBuilder) {
        val text = value?.trim().orEmpty()
        if (text.isNotEmpty()) out.append(text).append('\n')
    }

    private fun matches(value: String?, terms: List<String>): Boolean {
        val text = value?.lowercase().orEmpty()
        return text.isNotEmpty() && terms.any { text.contains(it) }
    }
}
