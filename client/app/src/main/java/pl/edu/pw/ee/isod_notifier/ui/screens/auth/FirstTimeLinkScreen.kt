package pl.edu.pw.ee.isod_notifier.ui.screens.auth

import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavController

@Composable
fun FirstTimeLinkScreen(navController: NavController) {
    Text("Link Service")
    Button(onClick = { navController.popBackStack() }) {
        Text("Go to home screen")
    }
}