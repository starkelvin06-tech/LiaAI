package com.Lia.assistant.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.Lia.assistant.data.ApiKeyStore
import com.Lia.assistant.data.LanguagePreference
import com.Lia.assistant.data.Personality
import com.Lia.assistant.ui.FlavorRoutes
import com.Lia.assistant.ui.state.NovaAppState
import com.Lia.assistant.ui.theme.IndigoMid
import com.Lia.assistant.ui.theme.IndigoNight
import com.Lia.assistant.ui.theme.IndigoSurface
import com.Lia.assistant.ui.theme.Marigold
import com.Lia.assistant.ui.theme.PearlWhite

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen(
    appState: NovaAppState,
    navController: NavController
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager()

    var apiKeyText by remember { mutableStateOf("") }
    var apiKeyVisible by remember { mutableStateOf(false) }
    var keySavedFeedback by remember { mutableStateOf(ApiKeyStore.hasKey(context)) }

    var assistantNameInput by remember { mutableStateOf(appState.assistantName) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Settings",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = { navController.popBackStack() },
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = IndigoNight
                )
            )
        },
        containerColor = IndigoNight
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // 1. API Key Section
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = IndigoMid),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterAlignment) {
                            Icon(imageVector = Icons.Default.Key, contentDescription = null, tint = Marigold)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Gemini Live API Key",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                            )
                        }

                        Text(
                            "Required for real-time full-duplex speech. Stored securely on your device only.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )

                        OutlinedTextField(
                            value = apiKeyText,
                            onValueChange = { apiKeyText = it },
                            placeholder = { Text(if (keySavedFeedback) "Key is saved (paste to update)" else "AIzaSy...") },
                            modifier = Modifier.fillMaxWidth(),
                            visualTransformation = if (apiKeyVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                            trailingIcon = {
                                IconButton(
                                    onClick = { apiKeyVisible = !apiKeyVisible },
                                    modifier = Modifier.size(48.dp)
                                ) {
                                    Icon(
                                        imageVector = if (apiKeyVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = "Toggle visibility"
                                    )
                                }
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = IndigoSurface,
                                unfocusedContainerColor = IndigoSurface,
                                focusedBorderColor = Marigold,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline
                            )
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterAlignment
                        ) {
                            if (keySavedFeedback) {
                                Row(verticalAlignment = Alignment.CenterAlignment) {
                                    Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = Marigold, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Key configured", style = MaterialTheme.typography.labelMedium, color = Marigold)
                                }
                            } else {
                                Spacer(modifier = Modifier.width(1.dp))
                            }

                            Button(
                                onClick = {
                                    if (apiKeyText.isNotBlank()) {
                                        ApiKeyStore.saveKey(context, apiKeyText)
                                        apiKeyText = ""
                                        keySavedFeedback = true
                                        focusManager.clearFocus()
                                    }
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Marigold, contentColor = IndigoNight),
                                modifier = Modifier.height(48.dp)
                            ) {
                                Text("Save Key", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // 2. Assistant Identity
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = IndigoMid),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterAlignment) {
                            Icon(imageVector = Icons.Default.Person, contentDescription = null, tint = Marigold)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Assistant Name",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(verticalAlignment = Alignment.CenterAlignment) {
                            OutlinedTextField(
                                value = assistantNameInput,
                                onValueChange = { assistantNameInput = it },
                                modifier = Modifier.weight(1f),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = IndigoSurface,
                                    unfocusedContainerColor = IndigoSurface,
                                    focusedBorderColor = Marigold
                                )
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            Button(
                                onClick = {
                                    appState.applyAssistantName(assistantNameInput, scope)
                                    focusManager.clearFocus()
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Marigold, contentColor = IndigoNight),
                                modifier = Modifier.height(48.dp)
                            ) {
                                Text("Update")
                            }
                        }
                    }
                }
            }

            // 3. 10 Personalities Selection
            item {
                Text(
                    "Personality (10 Modes)",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(8.dp))

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Personality.entries.forEach { p ->
                        val isSelected = appState.personality == p
                        FilterChip(
                            selected = isSelected,
                            onClick = { appState.applyPersonality(p, scope) },
                            label = { Text("${p.icon} ${p.displayName}") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Marigold,
                                selectedLabelColor = IndigoNight,
                                containerColor = IndigoMid,
                                labelColor = PearlWhite
                            ),
                            modifier = Modifier.height(48.dp)
                        )
                    }
                }

                Text(
                    text = appState.personality.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            // 4. Language Selection
            item {
                Text(
                    "Language Mode",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(8.dp))

                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = IndigoMid),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        LanguagePreference.entries.forEach { lang ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .padding(horizontal = 8.dp),
                                verticalAlignment = Alignment.CenterAlignment
                            ) {
                                RadioButton(
                                    selected = appState.language == lang,
                                    onClick = { appState.applyLanguage(lang, scope) }
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(lang.displayName, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
                                    Text(lang.description, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }
            }

            // 5. Gemini Voice Model Selection
            item {
                Text(
                    "Voice Synthesis Model",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(8.dp))

                val voices = listOf("Aoede", "Charon", "Fenrir", "Kore", "Puck")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    voices.forEach { v ->
                        val isSelected = appState.voiceName.equals(v, ignoreCase = true)
                        FilterChip(
                            selected = isSelected,
                            onClick = { appState.applyVoiceName(v, scope) },
                            label = { Text(v) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Marigold,
                                selectedLabelColor = IndigoNight,
                                containerColor = IndigoMid,
                                labelColor = PearlWhite
                            ),
                            modifier = Modifier.height(48.dp)
                        )
                    }
                }
            }

            // 6. Appearance & Motion
            item {
                Text(
                    "Display & Motion",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(8.dp))

                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = IndigoMid),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterAlignment
                        ) {
                            Column {
                                Text("Dark Theme", style = MaterialTheme.typography.bodyLarge)
                                Text("Indigo Night palette", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Switch(
                                checked = appState.themeMode == "DARK",
                                onCheckedChange = { appState.applyThemeMode(if (it) "DARK" else "LIGHT") },
                                colors = SwitchDefaults.colors(checkedThumbColor = Marigold)
                            )
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterAlignment
                        ) {
                            Column {
                                Text("Reduced Motion", style = MaterialTheme.typography.bodyLarge)
                                Text("Disables 3D orb pulsations", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Switch(
                                checked = appState.reducedMotion,
                                onCheckedChange = { appState.applyReducedMotion(it) },
                                colors = SwitchDefaults.colors(checkedThumbColor = Marigold)
                            )
                        }
                    }
                }
            }

            // 7. Flavor Routes (Direct Accessibility Screen Row)
            item {
                FlavorRoutes.accessibilitySettingsRow(navController = navController)
            }

            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}
