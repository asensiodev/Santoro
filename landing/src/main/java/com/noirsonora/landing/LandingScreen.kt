package com.noirsonora.landing

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun LandingScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            modifier = Modifier
                .fillMaxWidth(0.6f)
                .fillMaxHeight(0.8f),
            painter = painterResource(
                id = com.noirsonora.santoro_core_ui.R.drawable.letter_s
            ),
            contentDescription = "Onboarding image"
        )

        ElevatedButton(
            onClick = { /*TODO*/ }
        )
        {
            Text(
                stringResource(
                    id = com.noirsonora.santoro_core.R.string.landing_button
                ),
                fontSize = MaterialTheme.typography.headlineSmall.fontSize
            )
        }

    }

}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun LandingScreenPreview() {
    LandingScreen()
}