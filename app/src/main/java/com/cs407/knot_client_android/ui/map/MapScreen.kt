package com.cs407.knot_client_android.ui.map

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import androidx.compose.material3.Text
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun MapScreen(
    navController: NavHostController
) {
  Text(text = "Map Screen")
}

@Preview(showBackground = true)
@Composable
fun MapScreenPreview() {
    MapScreen(navController = rememberNavController())
}
