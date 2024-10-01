package com.ardatech.evas

import android.os.Bundle
import android.os.StrictMode
import android.os.StrictMode.ThreadPolicy
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.ardatech.evas.ui.theme.EvasTheme
import io.sellmair.evas.Events
import io.sellmair.evas.States
import io.sellmair.evas.compose.EvasLaunching
import io.sellmair.evas.compose.composeValue
import io.sellmair.evas.compose.installEvas
import io.sellmair.evas.compose.rememberEvasCoroutineScope
import io.sellmair.evas.emit
import kotlinx.coroutines.launch


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        StrictMode.setThreadPolicy(ThreadPolicy.Builder().permitAll().build())

        enableEdgeToEdge()

        val events = Events()
        val states = States()

        lifecycleScope.launch(events + states) {
            launchAppStates()
        }

        setContent {
            EvasTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Column(
                        modifier = Modifier
                            .padding(innerPadding)
                            .padding(horizontal = 10.dp)
                    ) {
                        installEvas(events, states) {
                            val tabSelected : UserLoginState = when(UserLoginState.composeValue() ?: return@installEvas){
                                is UserLoginState.LoggedIn -> {
                                    (UserLoginState.composeValue() as? UserLoginState.LoggedIn)!!
                                }

                                is UserLoginState.LoggingIn -> {
                                    UserLoginState.LoggingIn
                                }

                                is UserLoginState.NotLoggedIn -> {
                                    UserLoginState.NotLoggedIn()
                                }
                            }

                            AnimatedContent(
                                targetState = tabSelected,
                                transitionSpec = {
                                    fadeIn(animationSpec = tween(650)) togetherWith fadeOut(animationSpec = tween(400))
                                }, label = "Main"
                            ){ targetState ->
                                when(targetState){
                                    is UserLoginState.LoggedIn -> MainScreen()
                                    is UserLoginState.LoggingIn -> LoadingScreen()
                                    is UserLoginState.NotLoggedIn -> LoginUi()
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MainScreen() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val userState = UserLoginState.composeValue() as? UserLoginState.LoggedIn ?: return
        Text("You are logged in as ${userState.userData.firstName}")
        Spacer(Modifier.height(16.dp))

        Button(
            modifier = Modifier.fillMaxWidth()
                .padding(32.dp),
            onClick = EvasLaunching {
                UsernameChangedEvent("").emit()
                PasswordChangedEvent("").emit()
                UserLogoutEvent.emit()
            }
        ) {
            Text("Logout")
        }
    }
}

@Composable
fun LoadingScreen() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator()
    }
}

@Composable
fun LoginUi() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Greeting(
            name = "Android",
            modifier = Modifier
        )
        Spacer(modifier = Modifier.height(8.dp))
        LoginErrorText()
        Spacer(modifier = Modifier.height(8.dp))
        UsernameTextField()
        Spacer(modifier = Modifier.height(8.dp))
        PasswordTextField()
        Spacer(modifier = Modifier.height(8.dp))
        LoginButton()
    }
}

@Composable
fun UsernameTextField() {
    val focusManager = LocalFocusManager.current

    val usernameState = UsernameState.composeValue()

    TextField(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("UsernameTextField"),
        value = usernameState.username,
        label = { Text("Username") },
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Text
        ),
        keyboardActions = KeyboardActions(
            onNext = { focusManager.moveFocus(FocusDirection.Down) }
        ),
        singleLine = true,
        onValueChange = EvasLaunching { value ->
            UsernameChangedEvent(value).emit()
        },
    )
}

@Composable
fun PasswordTextField() {
    val focusManager = LocalFocusManager.current
    val passwordState = PasswordState.composeValue()
    var passwordVisible by remember { mutableStateOf(false) }
    val coroutineScope = rememberEvasCoroutineScope()

    TextField(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("PasswordTextField"),
        value = passwordState.password,
        label = { Text("Password") },
        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Password,
            imeAction = ImeAction.Done
        ),
        keyboardActions = KeyboardActions(
            onDone = { coroutineScope.launch { LoginClickedEvent.emit() } },
            onNext = {
                focusManager.moveFocus(FocusDirection.Down)
            }),
        trailingIcon = {
            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                Icon(
                    imageVector = if (passwordVisible) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                    contentDescription = if (passwordVisible) "Hide password" else "Show password"
                )
            }
        },
        onValueChange = EvasLaunching { value ->
            PasswordChangedEvent(value).emit()
        }
    )
}

@Composable
fun LoginButton() {
    val usernameState = UsernameState.composeValue()
    val passwordState = PasswordState.composeValue()
    val userLoginState = UserLoginState.composeValue()

    Button(onClick = EvasLaunching {
        LoginClickedEvent.emit()
    }, enabled = usernameState.username.isNotEmpty() &&
            passwordState.password.isNotEmpty() &&
            userLoginState is UserLoginState.NotLoggedIn) {
        Text("Login")
    }
}

@Composable
private fun LoginErrorText(modifier: Modifier = Modifier) {
    val notLoggedIn = UserLoginState.composeValue() as? UserLoginState.NotLoggedIn ?: return
    val error = notLoggedIn.error ?: return
    if (error.isEmpty()) return

    Text(
        modifier = modifier.testTag("LoginErrorText"),
        text = "Login failed: $error",
        color = MaterialTheme.colorScheme.error,
        fontWeight = FontWeight.Bold
    )

    Spacer(modifier = Modifier.height(16.dp))
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}