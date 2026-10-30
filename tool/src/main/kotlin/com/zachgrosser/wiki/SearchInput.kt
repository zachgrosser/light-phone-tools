package com.zachgrosser.wiki

import android.view.KeyCharacterMap
import android.view.KeyEvent
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.delete
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.thelightphone.lp3Keyboard.ui.SpecialKey
import com.thelightphone.lp3Keyboard.ui.lightOsRemap
import com.thelightphone.lp3Keyboard.ui.viewmodel.Lp3KeyboardViewModel
import com.thelightphone.lp3Keyboard.ui.viewmodel.Lp3RepeatableKeyboardCallback
import com.thelightphone.sdk.ui.LightBarButton
import com.thelightphone.sdk.ui.LightBottomBar
import com.thelightphone.sdk.ui.LightIcons
import com.thelightphone.sdk.ui.LightThemeTokens
import com.thelightphone.sdk.ui.designVerticalPxToDp
import com.thelightphone.sdk.ui.gridUnitsAsDp
import com.thelightphone.sdk.ui.keyboard.LightEmbeddedLp3Keyboard

// Three ways to type into the same TextFieldState: Light's on-screen keyboard, a hardware
// keyboard, or a phone keypad (multi-tap).

private const val UNDERLINE_THICKNESS_PX = 3f

@Composable
fun searchButton(onSearch: () -> Unit) = LightBarButton.LightIcon(
    icon = LightIcons.SEARCH,
    onClick = onSearch,
    contentDescription = strings().search,
)

// Not a text field: typing comes from the keyboards. Tapping just moves the cursor.
@Composable
internal fun SearchBox(state: TextFieldState, onTap: () -> Unit, modifier: Modifier = Modifier) {
    val colors = LightThemeTokens.colors
    val style = lightStyle(LightThemeTokens.typography.copy)
    val scroll = rememberScrollState()
    var layout by remember { mutableStateOf<TextLayoutResult?>(null) }
    var viewportWidth by remember { mutableIntStateOf(0) }
    val currentOnTap by rememberUpdatedState(onTap)
    val isEmpty = state.text.isEmpty()
    val cursor = state.selection.min.coerceIn(0, state.text.length)
    val cursorWidth = 2.dp

    LaunchedEffect(cursor, layout, viewportWidth) {
        val textLayout = layout ?: return@LaunchedEffect
        if (isEmpty || viewportWidth == 0) {
            scroll.scrollTo(0)
            return@LaunchedEffect
        }
        val x = textLayout.getCursorRect(cursor.coerceAtMost(textLayout.layoutInput.text.length)).left.toInt()
        val margin = viewportWidth / 8
        when {
            x < scroll.value + margin -> scroll.scrollTo((x - margin).coerceAtLeast(0))
            x > scroll.value + viewportWidth - margin -> scroll.scrollTo(x - viewportWidth + margin)
        }
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .onSizeChanged { viewportWidth = it.width }
                .pointerInput(state) {
                    detectTapGestures { position ->
                        val textLayout = layout
                        if (textLayout != null && state.text.isNotEmpty()) {
                            val offset = textLayout.getOffsetForPosition(position + Offset(scroll.value.toFloat(), 0f))
                            state.edit { selection = TextRange(offset.coerceIn(0, length)) }
                        }
                        currentOnTap()
                    }
                },
        ) {
            Box(modifier = Modifier.horizontalScroll(scroll, enabled = false)) {
                BasicText(
                    text = if (isEmpty) strings().searchPlaceholder else state.text.toString(),
                    style = style.copy(color = if (isEmpty) colors.contentSecondary else colors.content),
                    maxLines = 1,
                    softWrap = false,
                    onTextLayout = { layout = it },
                )
                layout?.let { textLayout ->
                    // The layout can be a keystroke behind the text.
                    val rect = textLayout.getCursorRect(
                        if (isEmpty) 0 else cursor.coerceAtMost(textLayout.layoutInput.text.length),
                    )
                    Box(
                        modifier = Modifier
                            .offset { IntOffset(rect.left.toInt(), rect.top.toInt()) }
                            .width(cursorWidth)
                            .height(with(LocalDensity.current) { rect.height.toDp() })
                            .background(colors.content),
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(0.5f.gridUnitsAsDp()))
        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .height(UNDERLINE_THICKNESS_PX.designVerticalPxToDp())
                .background(colors.content),
        )
    }
}

@Composable
internal fun SearchKeyboard(keyboard: Lp3KeyboardViewModel<*>, onSearch: () -> Unit) {
    LightEmbeddedLp3Keyboard(
        viewModel = keyboard,
        additionalBottomHeight = 5f.gridUnitsAsDp(),
        bottomBar = { LightBottomBar(items = listOf(searchButton(onSearch))) },
    )
}

// Adapted from the SDK's TextInputKeyboardCallback (MIT), which tools can't use directly.
internal class SearchKeyboardCallback(
    private val state: TextFieldState,
    private val onSearch: () -> Unit,
    private val onHaptic: () -> Unit,
) : Lp3RepeatableKeyboardCallback {

    override fun onKeyPressed(code: Int) = onHaptic()

    override fun onSpecialKeyPressed(key: SpecialKey) {
        onHaptic()
        if (key == SpecialKey.Space) state.insertAtCursor(" ")
    }

    override fun onKeyReleased(code: Int) = state.insertAtCursor(String(Character.toChars(code)))

    override fun onSpecialKeyReleased(key: SpecialKey) {
        when (key) {
            SpecialKey.Backspace -> state.deleteBeforeCursor()
            SpecialKey.Return -> onSearch()
            else -> Unit
        }
    }

    override fun onKeyLongPressed(code: Int) = Unit

    override fun onSpecialKeyLongPressed(key: SpecialKey) {
        if (key == SpecialKey.Backspace) state.deleteWordBeforeCursor()
    }

    override fun onKeyRepeated(code: Int) = onKeyReleased(code)

    override fun onSpecialKeyRepeated(specialKey: SpecialKey) {
        if (specialKey == SpecialKey.Space) state.insertAtCursor(" ")
    }

    override fun onSubmitWord(word: CharSequence) = state.insertAtCursor(word.toString())
}

// Bluetooth/USB keyboards and phone keypads. Anything that isn't for typing (volume, the Light
// Phone III's buttons) is left for LightOS.
internal class HardwareTyping(
    private val state: TextFieldState,
    private val onSearch: () -> Unit,
    private val onTyped: () -> Unit,
) {
    private sealed interface Key {
        data object Delete : Key
        data object Search : Key
        data class Text(val text: String) : Key
        data class Keypad(val digit: Int) : Key
    }

    private val multiTap = MultiTap()

    fun isTypingKey(event: KeyEvent): Boolean = classify(event) != null

    // Claims the key up as well as the key down. Light's keyboard types on key up, so otherwise
    // every character would arrive twice.
    fun onKeyEvent(event: KeyEvent): Boolean {
        val key = classify(event) ?: return false
        if (event.action != KeyEvent.ACTION_DOWN) return true
        onTyped()
        when (key) {
            Key.Delete -> {
                multiTap.reset()
                state.deleteBeforeCursor()
            }
            Key.Search -> if (event.repeatCount == 0) {
                multiTap.reset()
                onSearch()
            }
            is Key.Text -> {
                multiTap.reset()
                state.insertAtCursor(key.text)
            }
            is Key.Keypad -> when (event.repeatCount) {
                0 -> applyEdit(multiTap.press(key.digit, event.eventTime, state.selection.min))
                1 -> multiTap.hold(key.digit, state.selection.min).forEach(::applyEdit)
                else -> Unit
            }
        }
        return true
    }

    private fun applyEdit(edit: MultiTap.Edit) = when (edit) {
        is MultiTap.Edit.Insert -> state.insertAtCursor(edit.char.toString())
        is MultiTap.Edit.Replace -> state.replaceBeforeCursor(edit.char)
    }

    private fun classify(event: KeyEvent): Key? {
        // LightOS remaps some external keyboard keys to the Light Phone III's buttons. Undo that.
        val keyCode = lightOsRemap(event)
        val fromKeypad = event.isFromPhoneKeypad()
        return when {
            keyCode == KeyEvent.KEYCODE_DEL -> Key.Delete
            keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.KEYCODE_NUMPAD_ENTER -> Key.Search
            fromKeypad && keyCode in KeyEvent.KEYCODE_0..KeyEvent.KEYCODE_9 ->
                Key.Keypad(keyCode - KeyEvent.KEYCODE_0)
            // * and # vary by phone, so leave them alone.
            fromKeypad -> null
            event.isCtrlPressed || event.isMetaPressed -> null
            else -> printableText(event, keyCode)?.let { Key.Text(it) }
        }
    }

    private fun printableText(event: KeyEvent, keyCode: Int): String? {
        val codePoint = if (keyCode == event.keyCode) {
            event.unicodeChar
        } else {
            runCatching { KeyCharacterMap.load(event.deviceId).get(keyCode, event.metaState) }.getOrDefault(0)
        }
        if (codePoint == 0 || (codePoint and KeyCharacterMap.COMBINING_ACCENT) != 0) return null
        if (Character.isISOControl(codePoint)) return null
        return String(Character.toChars(codePoint))
    }

    private fun KeyEvent.isFromPhoneKeypad(): Boolean =
        when (runCatching { keyCharacterMap.keyboardType }.getOrNull()) {
            KeyCharacterMap.NUMERIC, KeyCharacterMap.PREDICTIVE -> true
            else -> false
        }
}


internal fun TextFieldState.insertAtCursor(text: String) = edit {
    val start = selection.min
    replace(start, selection.max, text)
    selection = TextRange(start + text.length)
}

internal fun TextFieldState.replaceBeforeCursor(char: Char) = edit {
    val end = selection.min
    if (end == 0) {
        replace(0, 0, char.toString())
        selection = TextRange(1)
    } else {
        replace(end - 1, end, char.toString())
        selection = TextRange(end)
    }
}

internal fun TextFieldState.deleteBeforeCursor() = edit {
    if (selection.min != selection.max) {
        val start = selection.min
        delete(start, selection.max)
        selection = TextRange(start)
        return@edit
    }
    val end = selection.min
    if (end == 0) return@edit
    // Surrogate pairs (most emoji) are two chars.
    val count = if (end >= 2 && Character.isLowSurrogate(charAt(end - 1))) 2 else 1
    delete(end - count, end)
    selection = TextRange(end - count)
}

internal fun TextFieldState.deleteWordBeforeCursor() = edit {
    val end = selection.min
    val before = asCharSequence().subSequence(0, end)
    val trimmed = before.trimEnd()
    val start = trimmed.indexOfLast { it.isWhitespace() } + 1
    if (start >= end) return@edit
    delete(start, end)
    selection = TextRange(start)
}
