package io.github.v2compose.ui.error

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import v2compose.shared.generated.resources.Res
import v2compose.shared.generated.resources.open_in_browser
import v2compose.shared.generated.resources.unsupported_path_action_home
import v2compose.shared.generated.resources.unsupported_path_message
import v2compose.shared.generated.resources.unsupported_path_route
import v2compose.shared.generated.resources.unsupported_path_title

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnsupportedRouteScreen(
    route: String,
    onBackClick: () -> Unit,
    onNavigateHomeClick: () -> Unit,
    onOpenInBrowserClick: (String) -> Unit,
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = null,
                        )
                    }
                },
                title = {
                    Text(stringResource(Res.string.unsupported_path_title))
                },
            )
        },
    ) { innerPadding ->
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp),
        ) {
            Text(
                text = stringResource(Res.string.unsupported_path_message),
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = stringResource(Res.string.unsupported_path_route, route.ifBlank { "/" }),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = onNavigateHomeClick,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(Res.string.unsupported_path_action_home))
            }
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedButton(
                onClick = { onOpenInBrowserClick(route) },
                enabled = route.isNotBlank(),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(Res.string.open_in_browser))
            }
        }
    }
}
