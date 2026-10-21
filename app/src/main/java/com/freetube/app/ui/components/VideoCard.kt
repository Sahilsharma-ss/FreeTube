package com.freetube.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.BookmarkAdded
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.freetube.app.ui.theme.*

@Composable
fun VideoCard(
    title: String,
    channelName: String,
    thumbnailUrl: String,
    isBookmarked: Boolean = false,
    onCardClick: () -> Unit,
    onBookmarkClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCardClick() },
        colors = CardDefaults.cardColors(containerColor = BlackCard),
        shape  = RoundedCornerShape(12.dp)
    ) {
        Column {
            // Thumbnail
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
                    .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                    .background(BlackElevated)
            ) {
                AsyncImage(
                    model              = thumbnailUrl,
                    contentDescription = title,
                    contentScale       = ContentScale.Crop,
                    modifier           = Modifier.fillMaxSize()
                )
                // Duration badge placeholder
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(6.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(BlackSurface.copy(alpha = 0.85f))
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    Text("4:32", style = MaterialTheme.typography.labelSmall, color = White)
                }
            }

            // Info row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp),
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text     = title,
                        style    = MaterialTheme.typography.titleSmall,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        color    = White
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text  = channelName,
                        style = MaterialTheme.typography.bodySmall,
                        color = WhiteMuted
                    )
                }
                IconButton(onClick = onBookmarkClick) {
                    Icon(
                        imageVector        = if (isBookmarked) Icons.Filled.BookmarkAdded
                                             else             Icons.Filled.BookmarkAdd,
                        contentDescription = "Bookmark",
                        tint               = if (isBookmarked) FTRed else WhiteMuted
                    )
                }
            }
        }
    }
}
