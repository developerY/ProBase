package com.zoewave.probase.photodo.mobile.ui.components

import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.zoewave.probase.photodo.mobile.core.ui.PhotoDoTheme
import com.zoewave.probase.photodo.model.navigation.PhotoTodoRoute
import com.zoewave.probase.photodo.model.navigation.topLevelRoutes


@Composable
fun PhotoTodoBottomBar(
    currentRoute: PhotoTodoRoute,
    navTo: (PhotoTodoRoute) -> Unit
) {
    NavigationBar {
        topLevelRoutes.forEach { route ->
            NavigationBarItem(
                // ✅ THE FIX: Compare the class types, not the data payload!
                // This tells the bar: "If I am looking at ANY TasksList, highlight the Tasks tab!"
                selected = currentRoute::class == route::class,
                onClick = { navTo(route) },
                icon = { Icon(imageVector = route.icon, contentDescription = route.title) },
                label = { Text(route.title) }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PhotoTodoBottomBarPreview() {
    PhotoDoTheme {
        PhotoTodoBottomBar(currentRoute = topLevelRoutes.first(), navTo = {})
    }
}