package com.zoewave.probase.gotmind.features.mindwave

import kotlinx.coroutines.CoroutineScope

class ClassicMindWaveEngine(
    scope: CoroutineScope,
    onGameOver: (Int, Int) -> Unit
) : BaseMindWaveEngine(scope, onGameOver) {

    override fun createInitialGrid(): List<Node> {
        return List(16) { Node(id = it) }
    }
}
