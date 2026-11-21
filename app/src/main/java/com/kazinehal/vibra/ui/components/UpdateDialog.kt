package com.kazinehal.vibra.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kazinehal.vibra.update.AppUpdate

/**
 * "Update available" dialog — opens the release page (or the APK asset
 * directly) in the browser; the user installs over the existing app so all
 * local data (favorites, history, downloads) is preserved.
 */
@Composable
fun UpdateDialog(update: AppUpdate, currentVersion: String, onDismiss: () -> Unit) {
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF121114),
        titleContentColor = Color.White,
        textContentColor = Color(0xFFB8B8C0),
        title = {
            Text(
                "Riva v${update.version} is out",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        },
        text = {
            Column {
                Text(
                    "You have v$currentVersion. Update for the latest fixes and features.",
                    fontSize = 13.sp
                )
                if (update.notes.isNotBlank()) {
                    Spacer(Modifier.height(10.dp))
                    Text(
                        update.notes.take(600),
                        fontSize = 12.sp,
                        color = Color(0xFF8A8A94),
                        maxLines = 8
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val url = update.apkUrl ?: update.htmlUrl
                try {
                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                } catch (_: Exception) {
                    // No browser available — nothing else we can do safely.
                }
                onDismiss()
            }) {
                Text("Update", color = Color(0xFFE50914), fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Later", color = Color(0xFF9A9AA3))
            }
        }
    )
}
