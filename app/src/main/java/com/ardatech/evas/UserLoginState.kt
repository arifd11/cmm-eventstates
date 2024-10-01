package com.ardatech.evas

import io.sellmair.evas.State

sealed class UserLoginState : State {
    companion object Key : State.Key<UserLoginState?> {
        override val default: UserLoginState? = null
    }

    data class NotLoggedIn(val error: String? = null) : UserLoginState()
    data object LoggingIn : UserLoginState()
    data class LoggedIn(val userData: UserDataResponse) : UserLoginState()
}
