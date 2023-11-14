package com.noirsonora.onboarding_presentation.welcome

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.noirsonora.core.navigation.Route
import com.noirsonora.core.util.UiEvent
import com.noirsonora.core_ui.LocalDimensions

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun WelcomeScreen(
    onNavigate: (UiEvent.Navigate) -> Unit
) {
    val pages = listOf(
        OnboardingPage.FirstPage,
        OnboardingPage.SecondPage,
        OnboardingPage.ThirdPage
    )
    val pagerState = rememberPagerState()

    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        HorizontalPager(
            pageCount = pages.count(),
            state = pagerState,
            verticalAlignment = Alignment.Top
        ) { position ->
            PagerScreen(
                onBoardingPage = pages[position]
            )
        }
        Row(
            modifier = Modifier
                .wrapContentHeight()
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(LocalDimensions.current.spaceExtraLarge),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.Bottom
        ) {
            repeat(pages.count()) { iteration ->
                val color =
                    if (pagerState.currentPage == iteration) Color.DarkGray else Color.LightGray
                Box(
                    modifier = Modifier
                        .padding(LocalDimensions.current.default)
                        .clip(CircleShape)
                        .background(color)
                        .size(LocalDimensions.current.sizeMedium)
                )
            }
        }
        FinishButton(modifier = Modifier, pagerState = pagerState) {
            onNavigate(UiEvent.Navigate(Route.LOGIN))
        }
    }
}

@Composable
fun PagerScreen(
    onBoardingPage: OnboardingPage
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            modifier = Modifier
                .fillMaxWidth(0.5f)
                .fillMaxHeight(0.6f),
            painter = painterResource(
                id = onBoardingPage.image
            ),
            // TODO(): Review stringResource
            contentDescription = "Onboarding image"
        )
        Text(
            modifier = Modifier
                .fillMaxWidth(),
            text = stringResource(id = onBoardingPage.title),
            fontSize = MaterialTheme.typography.headlineLarge.fontSize,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = LocalDimensions.current.spaceExtraLarge)
                .padding(top = LocalDimensions.current.spaceExtraLarge),
            text = stringResource(onBoardingPage.description),
            fontSize = MaterialTheme.typography.headlineSmall.fontSize,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FinishButton(
    modifier: Modifier,
    pagerState: PagerState,
    onClick: () -> Unit
) {
    val lastPageIndex = 2
    Row(
        modifier = modifier
            .padding(horizontal = LocalDimensions.current.spaceExtraLarge)
            .fillMaxWidth(),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.End
    ) {
        AnimatedVisibility(
            modifier = Modifier
                .fillMaxWidth(),
            visible = pagerState.currentPage == lastPageIndex
        ) {
            Button(
                modifier = Modifier.wrapContentWidth(),
                onClick = onClick,
                colors = ButtonDefaults.buttonColors(
                    contentColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text(
                    color = MaterialTheme.colorScheme.onPrimary,
                    text = stringResource(
                        id = com.noirsonora.santoro_core.R.string.onboarding_third_screen_button
                    ),
                    fontSize = MaterialTheme.typography.labelLarge.fontSize
                )
            }
        }
    }
}

@Composable
@Preview
fun FirstOnBoardingScreenPreview() {
    Column(modifier = Modifier.fillMaxSize()) {
        PagerScreen(onBoardingPage = OnboardingPage.FirstPage)
    }
}

@Composable
@Preview
fun SecondOnBoardingScreenPreview() {
    Column(modifier = Modifier.fillMaxSize()) {
        PagerScreen(onBoardingPage = OnboardingPage.SecondPage)
    }
}

@Composable
@Preview
fun ThirdOnBoardingScreenPreview() {
    Column(modifier = Modifier.fillMaxSize()) {
        PagerScreen(onBoardingPage = OnboardingPage.ThirdPage)
    }
}
