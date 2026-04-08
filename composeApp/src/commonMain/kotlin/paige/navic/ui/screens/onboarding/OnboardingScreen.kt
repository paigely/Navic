package paige.navic.ui.screens.onboarding

import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import paige.navic.ui.screens.onboarding.pages.LoginScreen

@Composable
fun OnboardingScreen() {
	Scaffold { innerPadding ->
		LoginScreen(
			innerPadding = innerPadding
		)
	}
}
