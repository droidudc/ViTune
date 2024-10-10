package app.vitune.android.ui.screens.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.vitune.android.R
import app.vitune.android.preferences.AppearancePreferences
import app.vitune.android.preferences.PlayerPreferences
import app.vitune.android.ui.screens.Route
import app.vitune.android.utils.currentLocale
import app.vitune.android.utils.findActivity
import app.vitune.android.utils.startLanguagePicker
import app.vitune.core.ui.BuiltInFontFamily
import app.vitune.core.ui.ColorMode
import app.vitune.core.ui.ColorSource
import app.vitune.core.ui.Darkness
import app.vitune.core.ui.LocalAppearance
import app.vitune.core.ui.ThumbnailRoundness
import app.vitune.core.ui.googleFontsAvailable
import app.vitune.core.ui.utils.isAtLeastAndroid13

@Route
@Composable
fun AppearanceSettings() = with(AppearancePreferences) {
    val (colorPalette) = LocalAppearance.current
    val context = LocalContext.current
    val isDark = isSystemInDarkTheme()

    SettingsCategoryScreen(title = stringResource(R.string.appearance)) {
        SettingsGroup(title = stringResource(R.string.colors)) {
            EnumValueSelectorSettingsEntry(
                title = stringResource(R.string.color_source),
                selectedValue = colorSource,
                onValueSelect = { colorSource = it },
                valueText = { it.nameLocalized }
            )
            EnumValueSelectorSettingsEntry(
                title = stringResource(R.string.color_mode),
                selectedValue = colorMode,
                onValueSelect = { colorMode = it },
                valueText = { it.nameLocalized }
            )
            AnimatedVisibility(visible = colorMode == ColorMode.Dark || (colorMode == ColorMode.System && isDark)) {
                EnumValueSelectorSettingsEntry(
                    title = stringResource(R.string.darkness),
                    selectedValue = darkness,
                    onValueSelect = { darkness = it },
                    valueText = { it.nameLocalized }
                )
            }
        }
        
        
        SettingsGroup(title = stringResource(R.string.player)) {
            SwitchSettingsEntry(
                title = stringResource(R.string.previous_button_while_collapsed),
                text = stringResource(R.string.previous_button_while_collapsed_description),
                isChecked = PlayerPreferences.isShowingPrevButtonCollapsed,
                onCheckedChange = { PlayerPreferences.isShowingPrevButtonCollapsed = it }
            )

            SwitchSettingsEntry(
                title = stringResource(R.string.swipe_horizontally_to_close),
                text = stringResource(R.string.swipe_horizontally_to_close_description),
                isChecked = PlayerPreferences.horizontalSwipeToClose,
                onCheckedChange = { PlayerPreferences.horizontalSwipeToClose = it }
            )

            

            SwitchSettingsEntry(
                title = stringResource(R.string.swipe_to_remove_item),
                text = stringResource(R.string.swipe_to_remove_item_description),
                isChecked = PlayerPreferences.horizontalSwipeToRemoveItem,
                onCheckedChange = { PlayerPreferences.horizontalSwipeToRemoveItem = it }
            )

            SwitchSettingsEntry(
                title = stringResource(R.string.lyrics_keep_screen_awake),
                text = stringResource(R.string.lyrics_keep_screen_awake_description),
                isChecked = PlayerPreferences.lyricsKeepScreenAwake,
                onCheckedChange = { PlayerPreferences.lyricsKeepScreenAwake = it }
            )

            SwitchSettingsEntry(
                title = stringResource(R.string.lyrics_show_system_bars),
                text = stringResource(R.string.lyrics_show_system_bars_description),
                isChecked = PlayerPreferences.lyricsShowSystemBars,
                onCheckedChange = { PlayerPreferences.lyricsShowSystemBars = it }
            )

            SwitchSettingsEntry(
                title = stringResource(R.string.pip),
                text = stringResource(R.string.pip_description),
                isChecked = autoPip,
                onCheckedChange = { autoPip = it }
            )
        }
        SettingsGroup(title = stringResource(R.string.songs)) {
            SwitchSettingsEntry(
                title = stringResource(R.string.swipe_to_hide_song),
                text = stringResource(R.string.swipe_to_hide_song_description),
                isChecked = swipeToHideSong,
                onCheckedChange = { swipeToHideSong = it }
            )
            AnimatedVisibility(
                visible = swipeToHideSong,
                label = ""
            ) {
                SwitchSettingsEntry(
                    title = stringResource(R.string.swipe_to_hide_song_confirm),
                    text = stringResource(R.string.swipe_to_hide_song_confirm_description),
                    isChecked = swipeToHideSongConfirm,
                    onCheckedChange = { swipeToHideSongConfirm = it }
                )
            }
            SwitchSettingsEntry(
                title = stringResource(R.string.hide_explicit),
                text = stringResource(R.string.hide_explicit_description),
                isChecked = hideExplicit,
                onCheckedChange = { hideExplicit = it }
            )
        }
    }
}

val ColorSource.nameLocalized
    @Composable get() = stringResource(
        when (this) {
            ColorSource.Default -> R.string.color_source_default
            ColorSource.Dynamic -> R.string.color_source_dynamic
            ColorSource.MaterialYou -> R.string.color_source_material_you
        }
    )

val ColorMode.nameLocalized
    @Composable get() = stringResource(
        when (this) {
            ColorMode.System -> R.string.color_mode_system
            ColorMode.Light -> R.string.color_mode_light
            ColorMode.Dark -> R.string.color_mode_dark
        }
    )

val Darkness.nameLocalized
    @Composable get() = stringResource(
        when (this) {
            Darkness.Normal -> R.string.darkness_normal
            Darkness.AMOLED -> R.string.darkness_amoled
            Darkness.PureBlack -> R.string.darkness_pureblack
        }
    )

val ThumbnailRoundness.nameLocalized
    @Composable get() = stringResource(
        when (this) {
            ThumbnailRoundness.None -> R.string.none
            ThumbnailRoundness.Light -> R.string.thumbnail_roundness_light
            ThumbnailRoundness.Medium -> R.string.thumbnail_roundness_medium
            ThumbnailRoundness.Heavy -> R.string.thumbnail_roundness_heavy
            ThumbnailRoundness.Heavier -> R.string.thumbnail_roundness_heavier
            ThumbnailRoundness.Heaviest -> R.string.thumbnail_roundness_heaviest
        }
    )
