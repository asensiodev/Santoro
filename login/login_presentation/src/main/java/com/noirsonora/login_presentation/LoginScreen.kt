package com.noirsonora.login_presentation

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import com.noirsonora.core.util.UiEvent
import com.noirsonora.core_ui.LocalDimensions
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onScreenReady: () -> Unit,
    onNavigate: (UiEvent.Navigate) -> Unit
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisibility by remember { mutableStateOf(false) }

    LaunchedEffect(key1 = true) {
        delay(1.seconds)
        onScreenReady()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(LocalDimensions.current.spaceMedium),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = com.noirsonora.santoro_core_ui.R.drawable.letter_s),
            contentDescription = null,
            modifier = Modifier
                .size(LocalDimensions.current.sizeOneHundred)
                .padding(bottom = LocalDimensions.current.spaceMedium)
        )

        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = LocalDimensions.current.spaceSmall),
            label = { Text(text = "Username") },
            leadingIcon = {
                Icon(imageVector = Icons.Default.Person, contentDescription = null)
            }
        )

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = LocalDimensions.current.spaceSmall),
            label = { Text(text = "Password") },
            leadingIcon = {
                Icon(imageVector = Icons.Default.Lock, contentDescription = null)
            },
            trailingIcon = {
                // Toggle password visibility
                val icon =
                    if (passwordVisibility) Icons.Default.FavoriteBorder else Icons.Default.Favorite
                IconButton(
                    onClick = { passwordVisibility = !passwordVisibility },
                    modifier = Modifier.padding(LocalDimensions.current.spaceSmall)
                ) {
                    Icon(imageVector = icon, contentDescription = null)
                }
            },
            visualTransformation = if (passwordVisibility) PasswordVisualTransformation() else VisualTransformation.None,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    println("Username: $username, Password: $password")
                })
        )

        Button(
            onClick = {
                println("Username: $username, Password: $password")
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(LocalDimensions.current.sizeExtraLarge)
        ) {
            Text(text = "Log In")
        }

    }


}

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    LoginScreen(onScreenReady = {}, onNavigate = {})
}