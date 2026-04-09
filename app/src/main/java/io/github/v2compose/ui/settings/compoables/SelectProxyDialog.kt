package io.github.v2compose.ui.settings.compoables

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowDropDown
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.PopupProperties
import io.github.v2compose.shared.bean.ProxyInfo
import io.github.v2compose.shared.bean.ProxyType
import io.github.v2compose.util.InetValidator
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import v2compose.shared.generated.resources.Res
import v2compose.shared.generated.resources.ok
import v2compose.shared.generated.resources.settings_proxy
import v2compose.shared.generated.resources.settings_proxy_direct
import v2compose.shared.generated.resources.settings_proxy_hostOrIp
import v2compose.shared.generated.resources.settings_proxy_hostOrIp_empty
import v2compose.shared.generated.resources.settings_proxy_hostOrIp_format_error
import v2compose.shared.generated.resources.settings_proxy_http
import v2compose.shared.generated.resources.settings_proxy_port
import v2compose.shared.generated.resources.settings_proxy_port_empty
import v2compose.shared.generated.resources.settings_proxy_port_error
import v2compose.shared.generated.resources.settings_proxy_socks
import v2compose.shared.generated.resources.settings_proxy_system

@Composable
fun SelectProxyDialog(
    proxyInfo: ProxyInfo, onDismiss: () -> Unit, onProxySelected: (ProxyInfo) -> Unit
) {
    val context = LocalContext.current

    var proxyType by remember(proxyInfo) { mutableStateOf(proxyInfo.type) }
    var proxyAddress by remember(proxyInfo) { mutableStateOf(proxyInfo.address) }
    var proxyPort by remember(proxyInfo) { mutableStateOf(proxyInfo.port.toString()) }

    val inputEnabled =
        remember(proxyType) { proxyType != ProxyType.System && proxyType != ProxyType.Direct }

    var proxyAddressError by remember { mutableStateOf<StringResource?>(null) }
    var proxyPortError by remember { mutableStateOf<StringResource?>(null) }

    val adressFocusRequester = remember { FocusRequester() }
    val portFocusRequester = remember { FocusRequester() }

    val checkProxyInfo = {
        while (true) {
            if (proxyType == ProxyType.Direct || proxyType == ProxyType.System) {
                onProxySelected(ProxyInfo(proxyType))
                break
            }

            val address = proxyAddress.trim()
            if (address.isEmpty()) {
                proxyAddressError = Res.string.settings_proxy_hostOrIp_empty
                break
            }
            if (!InetValidator.isValidHostOrIp(address)) {
                proxyAddressError = Res.string.settings_proxy_hostOrIp_format_error
                break
            }

            val port = proxyPort.trim()
            val portInt = port.toIntOrNull() ?: -1
            if (port.isEmpty()) {
                proxyPortError = Res.string.settings_proxy_port_empty
                break
            }
            if (!InetValidator.isValidInetPort(portInt)) {
                proxyPortError = Res.string.settings_proxy_port_error
                break
            }
            onProxySelected(ProxyInfo(proxyType, address, portInt))
            break
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(Res.string.settings_proxy)) },
        text = {
            Column {
                SelectProxyType(proxyType, onProxyTypeSelected = { proxyType = it })
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = TextFieldValue(proxyAddress, TextRange(proxyAddress.length)),
                    onValueChange = {
                        proxyAddress = it.text
                        proxyAddressError = null
                    },
                    modifier = Modifier.focusRequester(adressFocusRequester),
                    label = { Text(stringResource(Res.string.settings_proxy_hostOrIp)) },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Uri,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(onNext = { portFocusRequester.requestFocus() }),
                    isError = proxyAddressError != null,
                    supportingText = { proxyAddressError?.let { Text(stringResource(it)) } },
                    singleLine = true,
                    enabled = inputEnabled,
                )
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = TextFieldValue(proxyPort, TextRange(proxyPort.length)),
                    onValueChange = {
                        proxyPort = it.text
                        proxyPortError = null
                    },
                    modifier = Modifier.focusRequester(portFocusRequester),
                    label = { Text(stringResource(Res.string.settings_proxy_port)) },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(onDone = { checkProxyInfo() }),
                    isError = proxyPortError != null,
                    supportingText = { proxyPortError?.let { Text(stringResource(it)) } },
                    singleLine = true,
                    enabled = inputEnabled,
                )

                LaunchedEffect(true) {
                    adressFocusRequester.requestFocus()
                }
            }
        },
        confirmButton = {
            TextButton(onClick = checkProxyInfo) {
                Text(stringResource(Res.string.ok))
            }
        })
}

@Composable
private fun SelectProxyType(proxyType: ProxyType, onProxyTypeSelected: (ProxyType) -> Unit) {
    var showProxyTypeDropdown by remember { mutableStateOf(false) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { showProxyTypeDropdown = true }
            .padding(start = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(stringResource(proxyType.titleRes))
        Spacer(modifier = Modifier.weight(1f))
        IconButton(onClick = { showProxyTypeDropdown = true }) {
            Icon(Icons.Rounded.ArrowDropDown, "drop down")
        }

        DropdownMenu(
            expanded = showProxyTypeDropdown,
            onDismissRequest = { showProxyTypeDropdown = false },
            properties = PopupProperties(focusable = false),
        ) {
            listOf(ProxyType.Direct, ProxyType.Http, ProxyType.Socks).forEach {
                DropdownMenuItem(
                    text = {
                        Text(stringResource(it.titleRes))
                    },
                    onClick = {
                        showProxyTypeDropdown = false
                        onProxyTypeSelected(it)
                    },
                )
            }
        }
    }
}

val ProxyType.titleRes: StringResource
    get() = when (this) {
        ProxyType.System -> Res.string.settings_proxy_system
        ProxyType.Direct -> Res.string.settings_proxy_direct
        ProxyType.Http -> Res.string.settings_proxy_http
        ProxyType.Socks -> Res.string.settings_proxy_socks
    }
