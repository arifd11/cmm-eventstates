package com.ardatech.evas

import io.sellmair.evas.Event
import io.sellmair.evas.State
import io.sellmair.evas.collectEvents
import io.sellmair.evas.launchState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

data class PasswordState(val password: String = "emilyspass") : State {
    companion object Key : State.Key<PasswordState> {
        override val default: PasswordState = PasswordState()
    }
}

fun CoroutineScope.launchPasswordState() = launchState(PasswordState, Dispatchers.Main.immediate) {
    PasswordState.default.emit()

    collectEvents<PasswordChangedEvent> { event ->
        PasswordState(event.password).emit()
    }
}

data class PasswordChangedEvent(val password: String) : Event