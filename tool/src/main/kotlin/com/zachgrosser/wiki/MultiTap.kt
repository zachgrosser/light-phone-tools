package com.zachgrosser.wiki

/**
 * Multi-tap typing for a phone keypad (2 = abc, 3 = def, …), for phones like the Light Flip whose
 * keypad sends digits. Press a key once for its first letter; press it again within
 * [timeoutMillis] to change that letter to the next one. Hold a key to type its digit.
 *
 * This is plain logic so it can be tested without a phone: the caller passes each key press and
 * where the cursor is, and applies the returned [Edit] to the text.
 */
internal class MultiTap(private val timeoutMillis: Long = DEFAULT_TIMEOUT_MILLIS) {

    sealed interface Edit {
        val char: Char

        /** Type [char] at the cursor. */
        data class Insert(override val char: Char) : Edit

        /** Change the character just before the cursor (the letter the last press typed) to [char]. */
        data class Replace(override val char: Char) : Edit
    }

    private var lastDigit = NONE
    private var lastPressMillis = 0L
    private var letterIndex = 0
    private var lastPressCycled = false

    /** Where the cursor was after the last edit. If it has moved since, the next press starts a new letter. */
    private var cursorAfterEdit = NONE

    /** A key was pressed at [timeMillis] (any steady clock) with the cursor at [cursor]. */
    fun press(digit: Int, timeMillis: Long, cursor: Int): Edit {
        val letters = lettersFor(digit)
        val cycling = digit == lastDigit &&
            cursor == cursorAfterEdit &&
            timeMillis - lastPressMillis in 0..timeoutMillis
        lastDigit = digit
        lastPressMillis = timeMillis
        lastPressCycled = cycling
        return if (cycling) {
            letterIndex = (letterIndex + 1) % letters.length
            cursorAfterEdit = cursor
            Edit.Replace(letters[letterIndex])
        } else {
            letterIndex = 0
            cursorAfterEdit = cursor + 1
            Edit.Insert(letters[0])
        }
    }

    /**
     * The key just pressed is being held down: type its digit instead of what that press did.
     * If the press started a new letter, the digit takes its place; if it changed the previous
     * letter (2, 2, hold 2), that letter goes back and the digit follows it ("b2").
     */
    fun hold(digit: Int, cursor: Int): List<Edit> {
        val digitChar = '0' + digit
        val afterThisKey = digit == lastDigit && cursor == cursorAfterEdit
        val letters = lettersFor(digit)
        val previousLetter = if (afterThisKey && lastPressCycled) {
            letters[(letterIndex - 1 + letters.length) % letters.length]
        } else {
            null
        }
        reset()
        return when {
            !afterThisKey -> listOf(Edit.Insert(digitChar))
            previousLetter == null -> listOf(Edit.Replace(digitChar))
            else -> listOf(Edit.Replace(previousLetter), Edit.Insert(digitChar))
        }
    }

    /** Something else changed the text; the next press starts a new letter. */
    fun reset() {
        lastDigit = NONE
        cursorAfterEdit = NONE
    }

    companion object {
        const val DEFAULT_TIMEOUT_MILLIS = 1_000L
        private const val NONE = -1

        /** The characters each key cycles through, ending with its digit (as on most phones). */
        private val KEYPAD = listOf(
            " 0", ".,'-?!1", "abc2", "def3", "ghi4", "jkl5", "mno6", "pqrs7", "tuv8", "wxyz9",
        )

        fun lettersFor(digit: Int): String {
            require(digit in 0..9) { "Not a keypad digit: $digit" }
            return KEYPAD[digit]
        }
    }
}
