package app.vitune.android.ui.screens.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.ui.res.stringResource
import app.vitune.android.Database
import app.vitune.android.R
import app.vitune.android.handleUrl
import app.vitune.android.models.SearchQuery
import app.vitune.android.models.toUiMood
import app.vitune.android.preferences.DataPreferences
import app.vitune.android.preferences.UIStatePreferences
import app.vitune.android.query
import app.vitune.android.ui.components.themed.Scaffold
import app.vitune.android.ui.screens.GlobalRoutes
import app.vitune.android.ui.screens.Route
import app.vitune.android.ui.screens.albumRoute
import app.vitune.android.ui.screens.artistRoute
import app.vitune.android.ui.screens.builtInPlaylistRoute
import app.vitune.android.ui.screens.builtinplaylist.BuiltInPlaylistScreen
import app.vitune.android.ui.screens.localPlaylistRoute
import app.vitune.android.ui.screens.localplaylist.LocalPlaylistScreen
import app.vitune.android.ui.screens.mood.MoodScreen
import app.vitune.android.ui.screens.mood.MoreAlbumsScreen
import app.vitune.android.ui.screens.mood.MoreMoodsScreen
import app.vitune.android.ui.screens.moodRoute
import app.vitune.android.ui.screens.pipedPlaylistRoute
import app.vitune.android.ui.screens.playlistRoute
import app.vitune.android.ui.screens.search.SearchScreen
import app.vitune.android.ui.screens.searchResultRoute
import app.vitune.android.ui.screens.searchRoute
import app.vitune.android.ui.screens.settingsRoute
import app.vitune.android.utils.toast
import app.vitune.compose.persist.PersistMapCleanup
import app.vitune.compose.routing.Route0
import app.vitune.compose.routing.RouteHandler

private val moreMoodsRoute = Route0("moreMoodsRoute")
private val moreAlbumsRoute = Route0("moreAlbumsRoute")

@Route
@Composable
fun HomeScreen(
    navController: NavController,
    screenIndex: Int
) {
    val screens = listOf(
        Screen.Home,
        Screen.Songs,
        Screen.Artists,
        Screen.Albums,
        Screen.Playlists
    )
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = {
                    Text(text = stringResource(id = screens[screenIndex].resourceId))
                },
                actions = {
                    TooltipIconButton(
                        description = R.string.search,
                        onClick = { navController.navigate(route = "search") },
                        icon = Icons.Outlined.Search,
                        inTopBar = true
                    )

                    TooltipIconButton(
                        description = R.string.settings,
                        onClick = { navController.navigate(route = "settings") },
                        icon = Icons.Outlined.Settings,
                        inTopBar = true
                    )
                },
                scrollBehavior = scrollBehavior
            )
        }
    ) { paddingValues ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = paddingValues.calculateTopPadding())
        ) {
            when (screenIndex) {
                0 -> QuickPicks(
                    onAlbumClick = { browseId -> navController.navigate(route = "album/$browseId") },
                    onArtistClick = { browseId -> navController.navigate(route = "artist/$browseId") },
                    onPlaylistClick = { browseId -> navController.navigate(route = "playlist/$browseId") }
                )

                1 -> HomeSongs(
                    onGoToAlbum = { browseId -> navController.navigate(route = "album/$browseId") },
                    onGoToArtist = { browseId -> navController.navigate(route = "artist/$browseId") }
                )

                2 -> HomeArtistList(
                    onArtistClick = { artist -> navController.navigate(route = "artist/${artist.id}") }
                )

                3 -> HomeAlbums(
                    onAlbumClick = { album -> navController.navigate(route = "album/${album.id}") }
                )

                4 -> HomePlaylists(
                    onBuiltInPlaylist = { playlistIndex -> navController.navigate(route = "builtInPlaylist/$playlistIndex") },
                    onPlaylistClick = { playlist -> navController.navigate(route = "localPlaylist/${playlist.id}") }
                )
            }
        }
    }
}
