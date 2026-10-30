package com.zachgrosser.wiki

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.thelightphone.sdk.SealedLightActivity
import com.thelightphone.sdk.SimpleLightScreen
import com.thelightphone.sdk.ui.LightScrollView
import com.thelightphone.sdk.ui.LightText
import com.thelightphone.sdk.ui.LightTextVariant
import com.thelightphone.sdk.ui.LightTopBar
import com.thelightphone.sdk.ui.LightTopBarCenter
import com.thelightphone.sdk.ui.gridUnitsAsDp
import com.thelightphone.sdk.ui.lightClickable

class SettingsScreen(sealedActivity: SealedLightActivity) : SimpleLightScreen<Unit>(sealedActivity) {
    @Composable
    override fun Content() {
        WikiTheme {
            SettingsContent(
                onBack = { goBack() },
                onLanguage = { navigateTo({ LanguageScreen(it) }) },
                onAbout = { navigateTo({ AboutScreen(it) }) },
            )
        }
    }
}

class LanguageScreen(sealedActivity: SealedLightActivity) : SimpleLightScreen<Unit>(sealedActivity) {
    @Composable
    override fun Content() {
        WikiTheme {
            LanguageContent(
                onBack = { goBack() },
                onChoose = { language ->
                    LanguageSetting.choose(lightContext.filesDir, language)
                    goBack()
                },
            )
        }
    }
}

class AboutScreen(sealedActivity: SealedLightActivity) : SimpleLightScreen<Unit>(sealedActivity) {
    @Composable
    override fun Content() {
        WikiTheme { AboutContent(onBack = { goBack() }) }
    }
}

@Composable
internal fun SettingsContent(onBack: () -> Unit, onLanguage: () -> Unit, onAbout: () -> Unit) {
    val strings = strings()
    Column(modifier = Modifier.fillMaxSize()) {
        LightTopBar(
            leftButton = backButton(onBack),
            center = LightTopBarCenter.Text(strings.settings),
            modifier = Modifier.padding(bottom = 0.5f.gridUnitsAsDp()),
        )
        Column(modifier = Modifier.padding(horizontal = 1f.gridUnitsAsDp())) {
            SettingRow(label = strings.language, value = LocalLanguage.current.nativeName, onClick = onLanguage)
            SettingRow(label = null, value = strings.about, onClick = onAbout)
        }
    }
}

// Like the rows in LightOS's own Settings: a small label over the current value.
@Composable
private fun SettingRow(label: String?, value: String, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .lightClickable(onClick = onClick)
            .padding(vertical = 0.75f.gridUnitsAsDp()),
    ) {
        label?.let { LightText(text = it, variant = LightTextVariant.Detail) }
        LightText(text = value, variant = LightTextVariant.Copy)
    }
}

@Composable
internal fun LanguageContent(onBack: () -> Unit, onChoose: (Language) -> Unit) {
    Column(modifier = Modifier.fillMaxSize()) {
        LightTopBar(
            leftButton = backButton(onBack),
            center = LightTopBarCenter.Text(strings().language),
            modifier = Modifier.padding(bottom = 0.5f.gridUnitsAsDp()),
        )
        LightScrollView(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(start = 1f.gridUnitsAsDp()),
        ) {
            Language.entries.forEach { language ->
                LightText(
                    text = language.nativeName,
                    variant = LightTextVariant.Copy,
                    modifier = Modifier
                        .fillMaxWidth()
                        .lightClickable { onChoose(language) }
                        .padding(vertical = 0.75f.gridUnitsAsDp()),
                )
            }
        }
    }
}

// Only the intro is translated. The rest is credits and policies, kept in English.
@Composable
internal fun AboutContent(onBack: () -> Unit) {
    val unit = 1f.gridUnitsAsDp()
    val strings = strings()
    val language = LocalLanguage.current
    Column(modifier = Modifier.fillMaxSize()) {
        LightTopBar(leftButton = backButton(onBack), center = LightTopBarCenter.Text(strings.about))
        LightScrollView(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(start = unit),
        ) {
            AboutSection(null, strings.aboutIntro)
            AboutSection(
                "Where articles come from",
                "Every article is written by Wikipedia's volunteer contributors. Wiki downloads it " +
                    "from Wikipedia (${language.host}), which is run by the Wikimedia Foundation, when you open it.\n\n" +
                    "To fit this screen, Wiki leaves out images, tables, infoboxes, citations and some " +
                    "formatting, and only links to other articles. The end of each article gives its " +
                    "address on Wikipedia, where you can see the full version and everyone who wrote it.",
            )
            AboutSection(
                "License",
                "Wikipedia's text is available under the ${Attribution.LICENSE_NAME} " +
                    "(${Attribution.LICENSE_SHORT}): ${Attribution.LICENSE_URL}\n\n" +
                    "You may share and adapt it if you credit Wikipedia's contributors and share your " +
                    "changes under the same license. When an article includes text from another free " +
                    "source, Wiki shows the article's credit for it. Short quotations from other works " +
                    "belong to their authors.",
            )
            AboutSection(
                "Accuracy",
                "Anyone can edit Wikipedia, so an article may be incomplete, out of date or wrong. " +
                    "Check anything important elsewhere. Wikipedia's disclaimer: ${Attribution.DISCLAIMER_URL}\n\n" +
                    "A saved article is a copy from the day you saved it. For the latest version, " +
                    "remove the star, go back, and open the article again.",
            )
            AboutSection(
                "Privacy",
                "When you search or open an article, Wiki asks Wikipedia for it directly. The " +
                    "Wikimedia Foundation receives what you searched for or opened and your phone's " +
                    "internet (IP) address, as with any website. Its privacy policy applies: " +
                    "${Attribution.PRIVACY_POLICY_URL}\n\n" +
                    "Wiki has no ads, analytics or tracking and sends nothing anywhere else. Saved " +
                    "articles stay on this phone.",
            )
            AboutSection("Trademark", Attribution.TRADEMARK_NOTICE)
            AboutSection(
                "Open source",
                "Wiki is free and open source under the MIT License: ${Attribution.SOURCE_CODE_URL}\n\n" +
                    "To report a problem or suggest a change: ${Attribution.ISSUES_URL}\n\n" +
                    "Version ${BuildConfig.VERSION_NAME}",
            )
            Spacer(Modifier.height(unit * 2f))
        }
    }
}

@Composable
private fun AboutSection(title: String?, body: String) {
    val unit = 1f.gridUnitsAsDp()
    title?.let {
        LightText(
            text = it,
            variant = LightTextVariant.Subheading,
            modifier = Modifier.padding(top = unit, bottom = unit * 0.25f),
        )
    }
    LightText(
        text = body,
        variant = LightTextVariant.Paragraph,
        modifier = Modifier.padding(bottom = unit * 0.5f),
    )
}
