package com.mundodedescobertas.app.data

/** Configurações locais. Intensidade, volume e velocidade são sempre normalizados. */
data class ChildPreferences(
    val childName: String = "Vivi",
    val masterVolume: Float = .65f,
    val narrationVolume: Float = .75f,
    val effectsVolume: Float = .55f,
    val narrationEnabled: Boolean = true,
    val animationIntensity: Float = .5f,
    val gameSpeed: Float = .75f,
    val difficulty: Difficulty = Difficulty.EASY,
    val reducedStimuli: Boolean = false,
    val calmMode: Boolean = false,
    val vibration: Boolean = false,
    val sessionMinutes: Int = 0,
    val maxElements: Int = 8,
) {
    // Preferências individuais são preservadas ao entrar e sair do Modo Calminho.
    val effectiveAnimation: Float get() = if (reducedStimuli) 0f else if (calmMode) minOf(animationIntensity, .2f) else animationIntensity
    val effectiveNarrationVolume: Float get() = masterVolume * narrationVolume * if (calmMode) .6f else 1f
    val effectiveEffectsVolume: Float get() = masterVolume * effectsVolume * if (calmMode) .5f else 1f
    val effectiveSpeed: Float get() = if (calmMode) minOf(gameSpeed, .5f) else gameSpeed
    val effectiveElements: Int get() = if (reducedStimuli || calmMode) minOf(maxElements, 4) else maxElements

    fun normalized() = copy(
        childName = childName.filterNot { it.isISOControl() }.trim().take(24).ifBlank { "Vivi" },
        masterVolume = masterVolume.safeUnit(), narrationVolume = narrationVolume.safeUnit(),
        effectsVolume = effectsVolume.safeUnit(), animationIntensity = animationIntensity.safeUnit(),
        gameSpeed = if (gameSpeed.isFinite()) gameSpeed.coerceIn(.25f, 1f) else .75f,
        sessionMinutes = if (sessionMinutes in listOf(0, 10, 20, 30, 45, 60)) sessionMinutes else 0,
        maxElements = maxElements.coerceIn(3, 12),
    )
}
private fun Float.safeUnit() = if (isFinite()) coerceIn(0f, 1f) else .5f

enum class Difficulty(val title: String) { EASY("Suave"), MEDIUM("Explorar"), HARD("Desafio") }

/** Verificação simples para separar a área infantil; não é autenticação de segurança. */
data class AdultChallenge(val first: Int, val second: Int) {
    fun accepts(input: String): Boolean = input.trim().toIntOrNull() == first * second
    companion object {
        fun create() = AdultChallenge((3..9).random(), (3..9).random())
    }
}
