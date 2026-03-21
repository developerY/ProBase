package com.zoewave.probase.photodo.mobile.ui.components

import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.zoewave.probase.photodo.mobile.ui.navigation.PhotoTodoRoute
import com.zoewave.probase.photodo.mobile.ui.navigation.topLevelRoutes
import com.zoewave.probase.photodo.mobile.ui.theme.ProBaseTheme

@Composable
fun PhotoTodoBottomBar(
    currentRoute: PhotoTodoRoute,
    onNavigateTo: (PhotoTodoRoute) -> Unit
) {
    NavigationBar {
        topLevelRoutes.forEach { route ->
            NavigationBarItem(
                // ✅ THE FIX: Compare the class types, not the data payload!
                // This tells the bar: "If I am looking at ANY TasksList, highlight the Tasks tab!"
                selected = currentRoute::class == route::class,
                onClick = { onNavigateTo(route) },
                icon = { Icon(imageVector = route.icon, contentDescription = route.title) },
                label = { Text(route.title) }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PhotoTodoBottomBarPreview() {
    ProBaseTheme {
        PhotoTodoBottomBar(currentRoute = PhotoTodoRoute.Home, onNavigateTo = {})
    }
}