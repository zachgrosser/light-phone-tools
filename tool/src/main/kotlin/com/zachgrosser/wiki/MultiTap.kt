package com.zachgrosser.wiki

// Old-school keypad typing (2 = abc, 3 = def…) for keypads that send digits, like the Light Flip's
// probably will. Pressing the same key again quickly cycles the letter; holding it types the digit.
// No Android code in here, so it can be unit tested.
internal class MultiTap(private val timeoutMillis: Long = DEFAULT_TIMEOUT_MILLIS) {

    sealed interface Edit {
        val char: Char

        data class Insert(override val char: Char) : Edit

        // Swap the character before the cursor (the letter the last press typed).
        data class Replace(override val char: Char) : Edit
    }

    private var lastDigit = NONE
    private var lastPressMillis = 0L
    private var letterIndex = 0
    private var lastPressCycled = false

    // If the cursor has moved since, the next press starts a new letter.
    private var cursorAfterEdit = NONE

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

    // The last key is being held: swap what it typed for its digit. After 2, 2, hold 2 that's
    // "b2", not "2".
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

    fun reset() {
        lastDigit = NONE
        cursorAfterEdit = NONE
    }

    companion object {
        const val DEFAULT_TIMEOUT_MILLIS = 1_000L
        private const val NONE = -1

        // Each key ends its cycle on its own digit, like most phones.
        private val KEYPAD = listOf(
            " 0", ".,'-?!1", "abc2", "def3", "ghi4", "jkl5", "mno6", "pqrs7", "tuv8", "wxyz9",
        )

        fun lettersFor(digit: Int): String {
            require(digit in 0..9) { "Not a keypad digit: $digit" }
            return KEYPAD[digit]
        }
    }
}
