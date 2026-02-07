package com.asensiodev.core.designsystem.theme

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ExitToApp
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Help
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.PhotoCamera
import androidx.compose.material.icons.rounded.Public
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.Videocam
import androidx.compose.ui.graphics.vector.ImageVector

object AppIcons {
    val Home: ImageVector = Icons.Rounded.Home
    val HomeOutlined: ImageVector = Icons.Outlined.Home

    val Watched: ImageVector = Icons.Rounded.CheckCircle
    val WatchedOutlined: ImageVector = Icons.Outlined.CheckCircle

    val Watchlist: ImageVector = Icons.Rounded.Bookmark
    val WatchlistOutlined: ImageVector = Icons.Outlined.BookmarkBorder

    val Profile: ImageVector = Icons.Rounded.AccountCircle
    val ProfileOutlined: ImageVector = Icons.Outlined.AccountCircle

    // General UI Actions
    val Search: ImageVector = Icons.Rounded.Search
    val SearchOff: ImageVector = Icons.Filled.SearchOff
    val Add = Icons.Rounded.Add
    val Info = Icons.Filled.Info
    val ExitToApp = Icons.AutoMirrored.Rounded.ExitToApp
    val ChevronRight = Icons.AutoMirrored.Rounded.KeyboardArrowRight
    val ArrowBack = Icons.AutoMirrored.Rounded.ArrowBack
    val Clear = Icons.Filled.Clear
    val Refresh = Icons.Filled.Refresh
    val Star = Icons.Rounded.Star
    val Settings = Icons.Rounded.Settings
    val Help = Icons.Rounded.Help

    // Movie Detail Metadata
    val Duration = Icons.Rounded.Schedule
    val Director = Icons.Rounded.Videocam
    val Country = Icons.Rounded.Public
    val Writer = Icons.Rounded.Edit
    val Music = Icons.Rounded.MusicNote
    val Camera = Icons.Rounded.PhotoCamera
    val Calendar = Icons.Rounded.CalendarToday
}
