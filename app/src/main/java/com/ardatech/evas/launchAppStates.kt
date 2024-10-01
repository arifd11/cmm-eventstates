package com.ardatech.evas

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

fun CoroutineScope.launchAppStates() = launch {
    launchUserLoginState()
    launchUsernameState()
    launchPasswordState()
}