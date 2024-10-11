package app.vitune.android.ui.screens.settings

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SnapshotMutationPolicy
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.vitune.android.Database
import app.vitune.android.DatabaseInitializer
import app.vitune.android.LocalPlayerServiceBinder
import app.vitune.android.R
import app.vitune.android.preferences.AppearancePreferences
import app.vitune.android.preferences.DataPreferences
import app.vitune.android.preferences.PlayerPreferences
import app.vitune.android.query
import app.vitune.android.service.PlayerMediaBrowserService
import app.vitune.android.service.PrecacheService
import app.vitune.android.ui.components.themed.SecondaryTextButton
import app.vitune.android.ui.components.themed.SliderDialog
import app.vitune.android.ui.components.themed.SliderDialogBody
import app.vitune.android.ui.screens.Route
import app.vitune.android.ui.screens.logsRoute
import app.vitune.android.utils.findActivity
import app.vitune.android.utils.intent
import app.vitune.android.utils.isIgnoringBatteryOptimizations
import app.vitune.android.utils.smoothScrollToBottom
import app.vitune.android.utils.toast
import app.vitune.core.ui.utils.isAtLeastAndroid12
import app.vitune.core.ui.utils.isAtLeastAndroid6
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import kotlin.system.exitProcess

@SuppressLint("BatteryLife")
@Route
@Composable
fun OtherSettings() {
    val context = LocalContext.current
    val binder = LocalPlayerServiceBinder.current
    val uriHandler = LocalUriHandler.current

    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()

    var isAndroidAutoEnabled by remember {
        val component = ComponentName(context, PlayerMediaBrowserService::class.java)
        val disabledFlag = PackageManager.COMPONENT_ENABLED_STATE_DISABLED
        val enabledFlag = PackageManager.COMPONENT_ENABLED_STATE_ENABLED

        mutableStateOf(
            value = context.packageManager.getComponentEnabledSetting(component) == enabledFlag,
            policy = object : SnapshotMutationPolicy<Boolean> {
                override fun equivalent(a: Boolean, b: Boolean): Boolean {
                    context.packageManager.setComponentEnabledSetting(
                        component,
                        if (b) enabledFlag else disabledFlag,
                        PackageManager.DONT_KILL_APP
                    )
                    return a == b
                }
            }
        )
    }

    var isIgnoringBatteryOptimizations by remember {
        mutableStateOf(context.isIgnoringBatteryOptimizations)
    }

    val activityResultLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
        onResult = { isIgnoringBatteryOptimizations = context.isIgnoringBatteryOptimizations }
    )

    val queriesCount by remember {
        Database.queriesCount().distinctUntilChanged()
    }.collectAsState(initial = 0)

    SettingsCategoryScreen(
        title = stringResource(R.string.other),
        scrollState = scrollState
    ) {
        SettingsGroup(title = stringResource(R.string.android_auto)) {
            SwitchSettingsEntry(
                title = stringResource(R.string.android_auto),
                text = stringResource(R.string.android_auto_description),
                isChecked = isAndroidAutoEnabled,
                onCheckedChange = { isAndroidAutoEnabled = it }
            )

            AnimatedVisibility(visible = isAndroidAutoEnabled) {
                SettingsDescription(text = stringResource(R.string.android_auto_warning))
            }
        }
        SettingsGroup(title = stringResource(R.string.search_history)) {
            SwitchSettingsEntry(
                title = stringResource(R.string.pause_search_history),
                text = stringResource(R.string.pause_search_history_description),
                isChecked = DataPreferences.pauseSearchHistory,
                onCheckedChange = { DataPreferences.pauseSearchHistory = it }
            )

            AnimatedVisibility(visible = !(DataPreferences.pauseSearchHistory && queriesCount == 0)) {
                SettingsEntry(
                    title = stringResource(R.string.clear_search_history),
                    text = if (queriesCount > 0) stringResource(
                        R.string.format_clear_search_history_amount,
                        queriesCount
                    )
                    else stringResource(R.string.empty_history),
                    onClick = { query(Database::clearQueries) },
                    isEnabled = queriesCount > 0
                )
            }
        }
        SettingsGroup(title = stringResource(R.string.playlists)) {
            SwitchSettingsEntry(
                title = stringResource(R.string.auto_sync_playlists),
                text = stringResource(R.string.auto_sync_playlists_description),
                isChecked = DataPreferences.autoSyncPlaylists,
                onCheckedChange = { DataPreferences.autoSyncPlaylists = it }
            )
        }
        
        SettingsGroup(title = stringResource(R.string.quick_picks)) {
            EnumValueSelectorSettingsEntry(
                title = stringResource(R.string.quick_picks_source),
                selectedValue = DataPreferences.quickPicksSource,
                onValueSelect = { DataPreferences.quickPicksSource = it },
                valueText = { it.displayName() }
            )

            SwitchSettingsEntry(
                title = stringResource(R.string.quick_picks_cache),
                text = stringResource(R.string.quick_picks_cache_description),
                isChecked = DataPreferences.shouldCacheQuickPicks,
                onCheckedChange = { DataPreferences.shouldCacheQuickPicks = it }
            )
        }

        
        SettingsGroup(title = stringResource(R.string.service_lifetime)) {
            AnimatedVisibility(visible = !isIgnoringBatteryOptimizations) {
                SettingsDescription(
                    text = stringResource(R.string.service_lifetime_warning),
                    important = true
                )
            }

            if (isAtLeastAndroid12) SettingsDescription(
                text = stringResource(R.string.service_lifetime_warning_android_12)
            )

            SettingsEntry(
                title = stringResource(R.string.ignore_battery_optimizations),
                text = if (isIgnoringBatteryOptimizations) stringResource(R.string.ignoring_battery_optimizations)
                else stringResource(R.string.ignore_battery_optimizations_action),
                onClick = {
                    if (!isAtLeastAndroid6) return@SettingsEntry

                    try {
                        activityResultLauncher.launch(
                            Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                                data = Uri.parse("package:${context.packageName}")
                            }
                        )
                    } catch (e: ActivityNotFoundException) {
                        try {
                            activityResultLauncher.launch(Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS))
                        } catch (e: ActivityNotFoundException) {
                            context.toast(context.getString(R.string.no_battery_optimization_settings_found))
                        }
                    }
                },
                isEnabled = !isIgnoringBatteryOptimizations
            )

            AnimatedVisibility(!isAtLeastAndroid12 || isIgnoringBatteryOptimizations) {
                SwitchSettingsEntry(
                    title = stringResource(R.string.invincible_service),
                    text = stringResource(R.string.invincible_service_description),
                    isChecked = PlayerPreferences.isInvincibilityEnabled,
                    onCheckedChange = { PlayerPreferences.isInvincibilityEnabled = it }
                )
            }

            

            
