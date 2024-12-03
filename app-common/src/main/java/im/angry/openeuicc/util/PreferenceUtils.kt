package im.angry.openeuicc.util

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import androidx.fragment.app.Fragment
import im.angry.openeuicc.OpenEuiccApplication
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "prefs")

val Context.preferenceRepository: PreferenceRepository
    get() = (applicationContext as OpenEuiccApplication).appContainer.preferenceRepository

val Fragment.preferenceRepository: PreferenceRepository
    get() = requireContext().preferenceRepository

object PreferenceKeys {
    // ---- Profile Notifications ----
    val NOTIFICATION_DOWNLOAD = booleanPreferencesKey("notification_download")
    val NOTIFICATION_DELETE = booleanPreferencesKey("notification_delete")
    val NOTIFICATION_SWITCH = booleanPreferencesKey("notification_switch")

    // ---- Advanced ----
    val DISABLE_SAFEGUARD_REMOVABLE_ESIM = booleanPreferencesKey("disable_safeguard_removable_esim")
    val VERBOSE_LOGGING = booleanPreferencesKey("verbose_logging")

    // ---- Developer Options ----
    val DEVELOPER_OPTIONS_ENABLED = booleanPreferencesKey("developer_options_enabled")
    val EXPERIMENTAL_DOWNLOAD_WIZARD = booleanPreferencesKey("experimental_download_wizard")
    val UNFILTERED_PROFILE_LIST = booleanPreferencesKey("unfiltered_profile_list")
    val IGNORE_TLS_CERTIFICATE = booleanPreferencesKey("ignore_tls_certificate")
}

class PreferenceRepository(private val context: Context) {
    // Expose flows so that we can also handle default values
    // ---- Profile Notifications ----
    val notificationDownloadFlow = bindFlow(PreferenceKeys.NOTIFICATION_DOWNLOAD, true)
    val notificationDeleteFlow = bindFlow(PreferenceKeys.NOTIFICATION_DELETE, true)
    val notificationSwitchFlow = bindFlow(PreferenceKeys.NOTIFICATION_SWITCH, false)

    // ---- Advanced ----
    val disableSafeguardFlow = bindFlow(PreferenceKeys.DISABLE_SAFEGUARD_REMOVABLE_ESIM, false)
    val verboseLoggingFlow = bindFlow(PreferenceKeys.VERBOSE_LOGGING, false)

    // ---- Developer Options ----
    val developerOptionsEnabledFlow = bindFlow(PreferenceKeys.DEVELOPER_OPTIONS_ENABLED, false)
    val experimentalDownloadWizardFlow = bindFlow(PreferenceKeys.EXPERIMENTAL_DOWNLOAD_WIZARD, false)
    val unfilteredProfileListFlow = bindFlow(PreferenceKeys.UNFILTERED_PROFILE_LIST, false)
    val ignoreTLSCertificateFlow = bindFlow(PreferenceKeys.IGNORE_TLS_CERTIFICATE, false)

    private fun <T> bindFlow(key: Preferences.Key<T>, defaultValue: T): Flow<T> =
        context.dataStore.data.map { it[key] ?: defaultValue }

    suspend fun <T> updatePreference(key: Preferences.Key<T>, value: T) =
        context.dataStore.edit { it[key] = value }
}