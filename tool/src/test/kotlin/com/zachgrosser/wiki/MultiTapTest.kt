package com.zachgrosser.wiki

import kotlin.test.Test
import kotlin.test.assertEquals

class MultiTapTest {

    /** A keypad typing into a text box, with a clock that only moves when told to. */
    private class Keypad {
        private val multiTap = MultiTap(timeoutMillis = 1_000)
        private val text = StringBuilder()
        private var cursor = 0
        private var now = 0L

        val typed: String get() = text.toString()

        fun tap(vararg digits: Int) = digits.forEach { digit ->
            now += 100
            applyEdit(multiTap.press(digit, now, cursor))
        }

        fun hold(digit: Int) {
            now += 100
            applyEdit(multiTap.press(digit, now, cursor))
            multiTap.hold(digit, cursor).forEach(::applyEdit)
        }

        fun pause() {
            now += 1_500
        }

        fun backspace() {
            text.deleteCharAt(cursor - 1)
            cursor--
        }

        private fun applyEdit(edit: MultiTap.Edit) {
            when (edit) {
                is MultiTap.Edit.Insert -> text.insert(cursor++, edit.char)
                is MultiTap.Edit.Replace -> text.setCharAt(cursor - 1, edit.char)
            }
        }
    }

    @Test
    fun `pressing a key again quickly moves to its next letter`() {
        val keypad = Keypad()
        keypad.tap(2)
        assertEquals("a", keypad.typed)
        keypad.tap(2)
        assertEquals("b", keypad.typed)
        keypad.tap(2, 2)
        assertEquals("2", keypad.typed)
        keypad.tap(2)
        assertEquals("a", keypad.typed, "wraps around after the digit")
    }

    @Test
    fun `a pause or a different key starts the next letter`() {
        val keypad = Keypad()
        keypad.tap(9)
        keypad.tap(4, 4, 4)
        keypad.tap(5, 5)
        keypad.pause()
        keypad.tap(4, 4, 4)
        assertEquals("wiki", keypad.typed)
    }

    @Test
    fun `the same letter twice needs a pause between`() {
        val keypad = Keypad()
        keypad.tap(6, 6, 6)
        keypad.pause()
        keypad.tap(6, 6, 6)
        assertEquals("oo", keypad.typed)
    }

    @Test
    fun `0 types a space and 1 punctuation`() {
        val keypad = Keypad()
        keypad.tap(5, 5, 5)
        keypad.tap(0)
        keypad.tap(1, 1, 1)
        assertEquals("l '", keypad.typed)
    }

    @Test
    fun `holding a key types its digit`() {
        val keypad = Keypad()
        keypad.hold(1)
        keypad.hold(9)
        keypad.hold(8)
        keypad.hold(4)
        assertEquals("1984", keypad.typed)
    }

    @Test
    fun `holding after a letter types the digit after it`() {
        val keypad = Keypad()
        keypad.tap(7, 7)
        keypad.hold(7)
        assertEquals("q7", keypad.typed, "the held third press types 7 after the q, instead of r")
    }

    @Test
    fun `after the cursor moves, a press starts a new letter`() {
        val keypad = Keypad()
        keypad.tap(2)
        keypad.tap(3)
        keypad.backspace()
        keypad.tap(3)
        assertEquals("ad", keypad.typed, "doesn't turn the deleted d into e")
    }

    @Test
    fun `every key ends with its digit`() {
        for (digit in 0..9) {
            assertEquals('0' + digit, MultiTap.lettersFor(digit).last())
        }
    }
}
