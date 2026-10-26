package com.mundodedescobertas.app.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** Transações do DataStore evitam perder alterações quando dois controles são usados juntos. */
class PreferencesRepository(private val store: DataStore<Preferences>) {
    val preferences: Flow<ChildPreferences> = store.data.map(::decode)

    suspend fun update(transform: (ChildPreferences) -> ChildPreferences) {
        store.updateData { current ->
            val old = decode(current)
            val p = transform(old).normalized()
            current.toMutablePreferences().apply {
                this[Name] = p.childName; this[Master] = p.masterVolume
                this[Voice] = p.narrationVolume; this[Effects] = p.effectsVolume
                this[Narration] = p.narrationEnabled; this[Animation] = p.animationIntensity
                this[Speed] = p.gameSpeed; this[Level] = p.difficulty.name
                this[Reduced] = p.reducedStimuli; this[Calm] = p.calmMode
                this[Vibration] = p.vibration; this[Session] = p.sessionMinutes
                this[Elements] = p.maxElements
            }
        }
    }

    private fun decode(p: Preferences) = ChildPreferences(
        childName = p[Name] ?: "Vivi", masterVolume = p[Master] ?: .65f,
        narrationVolume = p[Voice] ?: .75f, effectsVolume = p[Effects] ?: .55f,
        narrationEnabled = p[Narration] ?: true, animationIntensity = p[Animation] ?: .5f,
        gameSpeed = p[Speed] ?: .75f,
        difficulty = Difficulty.entries.firstOrNull { it.name == p[Level] } ?: Difficulty.EASY,
        reducedStimuli = p[Reduced] ?: false, calmMode = p[Calm] ?: false,
        vibration = p[Vibration] ?: false, sessionMinutes = p[Session] ?: 0,
        maxElements = p[Elements] ?: 8,
    ).normalized()

    private companion object {
        val Name = stringPreferencesKey("child_name")
        val Master = floatPreferencesKey("master_volume")
        val Voice = floatPreferencesKey("narration_volume")
        val Effects = floatPreferencesKey("effects_volume")
        val Narration = booleanPreferencesKey("narration_enabled")
        val Animation = floatPreferencesKey("animation_intensity")
        val Speed = floatPreferencesKey("game_speed")
        val Level = stringPreferencesKey("difficulty")
        val Reduced = booleanPreferencesKey("reduced_stimuli")
        val Calm = booleanPreferencesKey("calm_mode")
        val Vibration = booleanPreferencesKey("vibration")
        val Session = intPreferencesKey("session_minutes")
        val Elements = intPreferencesKey("max_elements")
    }
}
