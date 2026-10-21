package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ai.AiKeyStore
import com.example.ai.AiProvider
import com.example.ai.AiSettings
import com.example.model.EditorTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiSettingsDialog(
    theme: EditorTheme,
    settings: AiSettings,
    keyStore: AiKeyStore,
    onSaveSettings: (AiSettings) -> Unit,
    onTestConnection: (AiSettings, (Result<String>) -> Unit) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedProvider by remember { mutableStateOf(settings.provider) }
    var selectedModel by remember { mutableStateOf(settings.selectedModel) }
    var customEndpointUrl by remember { mutableStateOf(settings.customEndpointUrl) }
    var rawApiKey by remember { mutableStateOf(keyStore.getApiKey(selectedProvider) ?: "") }
    var isKeyMasked by remember { mutableStateOf(true) }
    var providerDropdownExpanded by remember { mutableStateOf(false) }
    var modelDropdownExpanded by remember { mutableStateOf(false) }

    var testStatusText by remember { mutableStateOf<String?>(null) }
    var isTestingConnection by remember { mutableStateOf(false) }
    var isSuccessStatus by remember { mutableStateOf(false) }

    // Validation
    val (isValidFormat, formatHint) = remember(selectedProvider, rawApiKey) {
        if (rawApiKey.isEmpty()) true to ""
        else keyStore.validateApiKeyFormat(selectedProvider, rawApiKey)
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = theme.surfaceColor,
            border = androidx.compose.foundation.BorderStroke(1.dp, theme.borderColor),
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
                .testTag("ai_settings_dialog")
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Title
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = theme.primaryColor,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = "AI Assistant Settings",
                        color = theme.onSurfaceColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Provider Picker
                Text(
                    text = "Provider",
                    color = theme.onSurfaceColor,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(6.dp))

                ExposedDropdownMenuBox(
                    expanded = providerDropdownExpanded,
                    onExpandedChange = { providerDropdownExpanded = !providerDropdownExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedProvider.displayName,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = providerDropdownExpanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                            .testTag("ai_provider_picker"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = theme.onSurfaceColor,
                            unfocusedTextColor = theme.onSurfaceColor,
                            focusedBorderColor = theme.primaryColor,
                            unfocusedBorderColor = theme.borderColor
                        ),
                        shape = RoundedCornerShape(10.dp)
                    )

                    ExposedDropdownMenu(
                        expanded = providerDropdownExpanded,
                        onDismissRequest = { providerDropdownExpanded = false },
                        modifier = Modifier.background(theme.surfaceColor)
                    ) {
                        AiProvider.values().forEach { provider ->
                            DropdownMenuItem(
                                text = { Text(provider.displayName, color = theme.onSurfaceColor) },
                                onClick = {
                                    selectedProvider = provider
                                    selectedModel = provider.defaultModel
                                    rawApiKey = keyStore.getApiKey(provider) ?: ""
                                    providerDropdownExpanded = false
                                },
                                modifier = Modifier.testTag("ai_provider_option_${provider.name}")
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Model Picker
                Text(
                    text = "Model",
                    color = theme.onSurfaceColor,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(6.dp))

                ExposedDropdownMenuBox(
                    expanded = modelDropdownExpanded,
                    onExpandedChange = { modelDropdownExpanded = !modelDropdownExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedModel,
                        onValueChange = { selectedModel = it },
                        readOnly = selectedProvider != AiProvider.CUSTOM,
                        trailingIcon = {
                            if (selectedProvider != AiProvider.CUSTOM) {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = modelDropdownExpanded)
                            }
                        },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                            .testTag("ai_model_picker"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = theme.onSurfaceColor,
                            unfocusedTextColor = theme.onSurfaceColor,
                            focusedBorderColor = theme.primaryColor,
                            unfocusedBorderColor = theme.borderColor
                        ),
                        shape = RoundedCornerShape(10.dp)
                    )

                    if (selectedProvider != AiProvider.CUSTOM) {
                        ExposedDropdownMenu(
                            expanded = modelDropdownExpanded,
                            onDismissRequest = { modelDropdownExpanded = false },
                            modifier = Modifier.background(theme.surfaceColor)
                        ) {
                            selectedProvider.models.forEach { model ->
                                DropdownMenuItem(
                                    text = { Text(model, color = theme.onSurfaceColor) },
                                    onClick = {
                                        selectedModel = model
                                        modelDropdownExpanded = false
                                    },
                                    modifier = Modifier.testTag("ai_model_option_$model")
                                )
                            }
                        }
                    }
                }

                if (selectedProvider == AiProvider.CUSTOM) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Endpoint URL",
                        color = theme.onSurfaceColor,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = customEndpointUrl,
                        onValueChange = { customEndpointUrl = it },
                        modifier = Modifier.fillMaxWidth().testTag("ai_custom_endpoint_field"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = theme.onSurfaceColor,
                            unfocusedTextColor = theme.onSurfaceColor,
                            focusedBorderColor = theme.primaryColor,
                            unfocusedBorderColor = theme.borderColor
                        ),
                        shape = RoundedCornerShape(10.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Masked API Key Input with Validation & Android Keystore
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "API Key (Stored in Keystore)",
                        color = theme.onSurfaceColor,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    if (keyStore.hasApiKey(selectedProvider)) {
                        Text(
                            text = "SECURED",
                            color = theme.primaryColor,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = rawApiKey,
                    onValueChange = { rawApiKey = it },
                    placeholder = { Text("Paste ${selectedProvider.displayName} API Key", color = theme.lineNumberColor, fontSize = 13.sp) },
                    visualTransformation = if (isKeyMasked) PasswordVisualTransformation() else VisualTransformation.None,
                    trailingIcon = {
                        IconButton(onClick = { isKeyMasked = !isKeyMasked }) {
                            Icon(
                                imageVector = if (isKeyMasked) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = "Toggle key mask",
                                tint = theme.lineNumberColor,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 52.dp)
                        .testTag("ai_api_key_field"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = theme.onSurfaceColor,
                        unfocusedTextColor = theme.onSurfaceColor,
                        focusedBorderColor = if (!isValidFormat && rawApiKey.isNotEmpty()) Color(0xFFEF4444) else theme.primaryColor,
                        unfocusedBorderColor = if (!isValidFormat && rawApiKey.isNotEmpty()) Color(0xFFEF4444) else theme.borderColor
                    ),
                    shape = RoundedCornerShape(10.dp),
                    isError = !isValidFormat && rawApiKey.isNotEmpty()
                )

                if (formatHint.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = formatHint,
                        color = if (isValidFormat) Color(0xFF10B981) else Color(0xFFEF4444),
                        fontSize = 11.sp
                    )
                }

                // Status message (e.g. from test connection)
                if (testStatusText != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSuccessStatus) Color(0x3310B981) else Color(0x33EF4444))
                            .padding(10.dp)
                    ) {
                        Text(
                            text = testStatusText!!,
                            color = if (isSuccessStatus) Color(0xFF6EE7B7) else Color(0xFFFCA5A5),
                            fontSize = 12.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Actions: Test connection, Remove key
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            if (rawApiKey.isNotEmpty()) {
                                keyStore.saveApiKey(selectedProvider, rawApiKey.trim())
                            }
                            val updatedSettings = settings.copy(
                                provider = selectedProvider,
                                selectedModel = selectedModel,
                                customEndpointUrl = customEndpointUrl
                            )
                            isTestingConnection = true
                            testStatusText = null
                            onTestConnection(updatedSettings) { result ->
                                isTestingConnection = false
                                result.fold(
                                    onSuccess = {
                                        isSuccessStatus = true
                                        testStatusText = it
                                    },
                                    onFailure = {
                                        isSuccessStatus = false
                                        testStatusText = it.localizedMessage ?: it.message
                                    }
                                )
                            }
                        },
                        shape = RoundedCornerShape(8.dp),
                        enabled = !isTestingConnection && (rawApiKey.isNotEmpty() || keyStore.hasApiKey(selectedProvider)),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("ai_test_connection_button")
                    ) {
                        if (isTestingConnection) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.Default.Speed, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Test", fontSize = 12.sp)
                        }
                    }

                    OutlinedButton(
                        onClick = {
                            keyStore.removeApiKey(selectedProvider)
                            rawApiKey = ""
                            testStatusText = "Key removed from secure keystore"
                            isSuccessStatus = true
                        },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF4444)),
                        enabled = keyStore.hasApiKey(selectedProvider) || rawApiKey.isNotEmpty(),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("ai_remove_key_button")
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Remove", fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Save button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(
                        onClick = {
                            if (rawApiKey.isNotEmpty()) {
                                keyStore.saveApiKey(selectedProvider, rawApiKey.trim())
                            }
                            onSaveSettings(
                                settings.copy(
                                    provider = selectedProvider,
                                    selectedModel = selectedModel,
                                    customEndpointUrl = customEndpointUrl
                                )
                            )
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = theme.primaryColor, contentColor = Color.Black),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("ai_save_settings_button")
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Save Settings", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
