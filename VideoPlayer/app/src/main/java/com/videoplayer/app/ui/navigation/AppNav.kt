package com.videoplayer.app.ui.navigation

import android.content.Intent
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.*
import com.videoplayer.app.data.model.VideoItem
import com.videoplayer.app.ui.home.HomeScreen
import com.videoplayer.app.ui.library.LibraryScreen
import com.videoplayer.app.ui.player.PlayerActivity
import com.videoplayer.app.ui.settings.SettingsScreen
import com.videoplayer.app.ui.theme.*
import com.videoplayer.app.ui.vault.VaultScreen
import com.videoplayer.app.viewmodel.LibraryViewModel
import com.videoplayer.app.viewmodel.VaultViewModel

private enum class Tab(val route: String, val label: String, val icon: ImageVector) {
    Home("home", "Home", Icons.Outlined.Home),
    Library("library", "Library", Icons.Outlined.VideoLibrary),
    Vault("vault", "Vault", Icons.Outlined.Lock),
    Settings("settings", "Settings", Icons.Outlined.Settings)
}

@Composable
fun AppNav() {
    val nav = rememberNavController()
    val ctx = LocalContext.current
    val libVm: LibraryViewModel = viewModel()
    val vaultVm: VaultViewModel = viewModel()
    LaunchedEffect(Unit) { libVm.refresh() }

    fun play(list: List<VideoItem>, index: Int) {
        ctx.startActivity(Intent(ctx, PlayerActivity::class.java)
            .putStringArrayListExtra(PlayerActivity.EXTRA_URIS, ArrayList(list.map { it.uri.toString() }))
            .putExtra(PlayerActivity.EXTRA_INDEX, index)
            .putExtra(PlayerActivity.EXTRA_IDS, list.map { it.id }.toLongArray()))
    }
    fun go(route: String) = nav.navigate(route) { launchSingleTop = true; popUpTo("home") }

    Scaffold(
        containerColor = VpBlack,
        bottomBar = {
            NavigationBar(containerColor = VpSurface) {
                val current = nav.currentBackStackEntryAsState().value?.destination?.route
                Tab.values().forEach { t ->
                    NavigationBarItem(
                        selected = current == t.route, onClick = { go(t.route) },
                        icon = { Icon(t.icon, t.label) }, label = { Text(t.label) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = VpLightBlue, selectedTextColor = VpLightBlue,
                            indicatorColor = VpLightBlue.copy(alpha = .15f),
                            unselectedIconColor = VpMuted, unselectedTextColor = VpMuted)
                    )
                }
            }
        }
    ) { pad ->
        NavHost(nav, startDestination = Tab.Home.route, modifier = Modifier.padding(pad)) {
            composable(Tab.Home.route) {
                HomeScreen(libVm, ::play, onSearch = { go("library") }, onVault = { go("vault") },
                    onSeeAllLibrary = { go("library") }, onOpenFolder = { libVm.selectFolder(it); go("library") })
            }
            composable(Tab.Library.route) { LibraryScreen(libVm, ::play) }
            composable(Tab.Vault.route) { VaultScreen(vaultVm) }
            composable(Tab.Settings.route) { SettingsScreen() }
        }
    }
}
