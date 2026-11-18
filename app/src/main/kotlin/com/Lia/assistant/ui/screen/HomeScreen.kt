package com.Lia.assistant.ui.screen

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.Lia.assistant.service.VoiceForegroundService
import com.Lia.assistant.ui.orb.LiaOrb
import com.Lia.assistant.ui.state.NovaAppState
import com.Lia.assistant.ui.theme.IndigoNight
import com.Lia.assistant.ui.theme.Marigold
import com.Lia.assistant.voice.NovaOrbState
import com.Lia.assistant.voice.VoiceSessionManager

@Composable
fun HomeScreen(
    appState: NovaAppState,
    navController: NavController
) {
    val context = LocalContext.current

    val orbState by VoiceSessionManager.state.collectAsStateWithLifecycle()
    val amplitude by VoiceSessionManager.amplitude.collectAsStateWithLifecycle()
    val isActive by VoiceSessionManager.isActive.collectAsStateWithLifecycle()
    val statusText by VoiceSessionManager.statusText.collectAsStateWithLifecycle()
    val isMuted by VoiceSessionManager.muted.collectAsStateWithLifecycle()
    val lastError by VoiceSessionManager.lastError.collectAsStateWithLifecycle()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(IndigoNight)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterAlignment,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterAlignment
            ) {
                Column {
                    Text(
                        text = appState.assistantName,
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    )
                    Text(
                        text = "${appState.personality.icon} ${appState.personality.displayName} · ${appState.language.displayName}",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }

                IconButton(
                    onClick = { navController.navigate("settings") },
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
            }

            // Central 3D Glowing Orb
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                LiaOrb(
                    state = orbState,
                    amplitude = amplitude,
                    reducedMotion = appState.reducedMotion,
                    modifier = Modifier.size(280.dp)
                )
            }

            // Bottom Status & Controls
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterAlignment
            ) {
                // Status Text Pill
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    modifier = Modifier.padding(bottom = 20.dp)
                ) {
                    Text(
                        text = statusText,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.Medium,
                            fontSize = 15.sp,
                            color = if (orbState == NovaOrbState.ERROR) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
                        textAlign = TextAlign.Center
                    )
                }

                // Error Details Banner
                AnimatedVisibility(visible = lastError != null && orbState == NovaOrbState.ERROR) {
                    lastError?.let { err ->
                        Text(
                            text = err,
                            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.error),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                    }
                }

                // Control Buttons (min 48dp touch targets)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterAlignment
                ) {
                    if (isActive) {
                        // Mute Button
                        OutlinedButton(
                            onClick = { VoiceSessionManager.toggleMute() },
                            modifier = Modifier
                                .size(56.dp),
                            shape = CircleShape,
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = if (isMuted) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Icon(
                                imageVector = if (isMuted) Icons.Default.MicOff else Icons.Default.Mic,
                                contentDescription = if (isMuted) "Unmute" else "Mute"
                            )
                        }

                        Spacer(modifier = Modifier.width(20.dp))

                        // Stop Button
                        Button(
                            onClick = {
                                context.startService(VoiceForegroundService.stopIntent(context))
                            },
                            modifier = Modifier
                                .height(56.dp)
                                .weight(1f),
                            shape = RoundedCornerShape(28.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Icon(imageVector = Icons.Default.Stop, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Stop Voice", style = MaterialTheme.typography.titleMedium)
                        }
                    } else {
                        // Start Button
                        Button(
                            onClick = {
                                context.startForegroundService(VoiceForegroundService.startIntent(context))
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = RoundedCornerShape(28.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Marigold,
                                contentColor = IndigoNight
                            )
                        ) {
                            Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Start Voice Mode", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                        }
                    }
                }
            }
        }

        // Floating Action Button for Typed Chat
        FloatingActionButton(
            onClick = { navController.navigate("chat") },
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
                .size(56.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Chat,
                contentDescription = "Open Typed Chat"
            )
        }
    }
}
