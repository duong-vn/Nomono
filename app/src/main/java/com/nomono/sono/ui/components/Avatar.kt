package com.nomono.sono.ui.components

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import android.util.LruCache
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private val AvatarPalette = listOf(
    Color(0xFF00696D),
    Color(0xFF5B5BD6),
    Color(0xFF7B3F9E),
    Color(0xFFB0476D),
    Color(0xFFC05B2E),
    Color(0xFF2E7D32),
    Color(0xFF1565C0),
    Color(0xFF6D4C41),
    Color(0xFF00838F),
    Color(0xFF455A64),
)

fun avatarColorFor(name: String): Color =
    AvatarPalette[(name.hashCode() and Int.MAX_VALUE) % AvatarPalette.size]

private fun initialOf(name: String): String =
    name.trim().firstOrNull()?.uppercaseChar()?.toString() ?: "?"

// Cache decoded avatars so scrolling doesn't re-decode the same uri on every
// recomposition. Count-based (32 entries); each entry is already downscaled to ≤512px.
private val avatarCache = object : LruCache<String, ImageBitmap>(32) {
    override fun sizeOf(key: String, value: ImageBitmap): Int = 1
}

private fun cachedAvatar(uri: String): ImageBitmap? = synchronized(avatarCache) {
    avatarCache.get(uri)
}

private fun cacheAvatar(uri: String, image: ImageBitmap) = synchronized(avatarCache) {
    avatarCache.put(uri, image)
}

@Composable
fun Avatar(
    name: String,
    uri: String?,
    size: Dp,
) {
    val context = LocalContext.current
    // IO-bound decode must not run on the main thread (produceState uses Main).
    val image by produceState<ImageBitmap?>(initialValue = uri?.let(::cachedAvatar), uri) {
        value = if (uri == null) {
            null
        } else {
            cachedAvatar(uri) ?: withContext(Dispatchers.IO) {
                loadAvatarBitmap(context, uri)?.also { cacheAvatar(uri, it) }
            }
        }
    }

    val background = avatarColorFor(name)
    val initial = initialOf(name)

    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(background),
        contentAlignment = Alignment.Center,
    ) {
        if (image != null) {
            Image(
                bitmap = image!!,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
        } else {
            Text(
                text = initial,
                color = Color.White,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
            )
        }
    }
}

private fun loadAvatarBitmap(context: Context, uriString: String): ImageBitmap? = runCatching {
    val uri = Uri.parse(uriString)
    val resolver = context.contentResolver
    val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        val source = ImageDecoder.createSource(resolver, uri)
        ImageDecoder.decodeBitmap(source) { decoder, info, _ ->
            decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
            val w = info.size.width
            val h = info.size.height
            if (w > 0 && h > 0 && (w > 512 || h > 512)) {
                val scale = minOf(512f / w, 512f / h)
                decoder.setTargetSize((w * scale).toInt(), (h * scale).toInt())
            }
        }
    } else {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }
        var sampleSize = 1
        while (bounds.outWidth / sampleSize > 512 || bounds.outHeight / sampleSize > 512) {
            sampleSize *= 2
        }
        val options = BitmapFactory.Options().apply { inSampleSize = sampleSize }
        resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, options) }
    }
    bitmap?.asImageBitmap()
}.getOrNull()
