package com.ardatech.evas

import io.sellmair.evas.Event
import io.sellmair.evas.State
import io.sellmair.evas.collectEvents
import io.sellmair.evas.launchState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

data class UsernameState(
    val username: String = "emilys"
) : State {
    companion object Key : State.Key<UsernameState> {
        override val default: UsernameState = UsernameState()
    }
}

fun CoroutineScope.launchUsernameState() = launchState(UsernameState, Dispatchers.Main.immediate) {
    UsernameState.default.emit()

    collectEvents<UsernameChangedEvent> { event ->
        UsernameState(username = event.username).emit()
    }
}

data class UsernameChangedEvent(val username: String) : Event