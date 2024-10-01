package com.ardatech.evas

import com.ardatech.evas.UserLoginState.LoggedIn
import com.ardatech.evas.UserLoginState.LoggingIn
import com.ardatech.evas.UserLoginState.NotLoggedIn
import com.google.gson.Gson
import io.sellmair.evas.Event
import io.sellmair.evas.collectEventsAsync
import io.sellmair.evas.events
import io.sellmair.evas.flow
import io.sellmair.evas.launchState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.withContext

fun CoroutineScope.launchUserLoginState() = launchState(UserLoginState, Dispatchers.Main.immediate) {
    val passwordStateFlow = events<PasswordChangedEvent>().map { it.password }
        .stateIn(this, SharingStarted.Eagerly, null)

    NotLoggedIn().emit()

    collectEventsAsync<LoginClickedEvent> {
        val usernameState = UsernameState.flow().value
        val password = passwordStateFlow.value ?: return@collectEventsAsync
        LoggingIn.emit()
        withContext(Dispatchers.IO){
            apiInterface().login(usernameState.username, password)
                .let { response ->
                    val userData = response.body!!.string()
                    if (response.isSuccessful) {
                        val data = Gson().fromJson(userData, UserDataResponse::class.java)
                        LoggedIn(data).emit()
                    } else {
                        val error = Gson().fromJson(userData, UserDataErrorResponse::class.java)
                        NotLoggedIn(error.message).emit()
                    }
                }
        }
    }

    collectEventsAsync<UserLogoutEvent> {
        // Delete Saved User Session Here
        LoggingIn.emit()
        apiInterface().logout().let {
            if (it){
                NotLoggedIn().emit()
            }
        }
    }

}

data object LoginClickedEvent : Event

data object UserLogoutEvent : Event