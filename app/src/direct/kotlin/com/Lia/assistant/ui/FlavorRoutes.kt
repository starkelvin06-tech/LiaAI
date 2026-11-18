package com.Lia.assistant.ui

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.Lia.assistant.accessibility.NovaAccessibilityService

object FlavorRoutes {
    @Composable
    fun accessibilitySettingsRow(navController: NavController) {
        val context = LocalContext.current
        val isEnabled = NovaAccessibilityService.isEnabled(context)

        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
        ListItem(
            headlineContent = {
                Text(
                    text = "Screen Control (Accessibility)",
                    style = MaterialTheme.typography.titleMedium
                )
            },
            supportingContent = {
                Text(
                    text = if (isEnabled) "Active and enabled ✓" else "Not enabled — tap to open Android settings",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            leadingContent = {
                Icon(
                    imageVector = Icons.Default.Visibility,
                    contentDescription = null,
                    tint = if (isEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            trailingContent = {
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            colors = ListItemDefaults.colors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 56.dp)
                .clickable {
                    val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(intent)
                }
        )
    }
}
