package io.github.v2compose.ui.login.twostep

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.v2compose.ui.common.CloseButton
import io.github.v2compose.ui.common.HtmlContent
import io.github.v2compose.ui.common.LoadMore
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import v2compose.shared.generated.resources.Res
import v2compose.shared.generated.resources.login
import v2compose.shared.generated.resources.tfa_code
import v2compose.shared.generated.resources.two_step_login

@Composable
fun TwoStepLoginScreenRoute(
    onCloseClick: () -> Unit,
    viewModel: TwoStepLoginViewModel = koinViewModel()
) {
    val loginState by viewModel.login.collectAsStateWithLifecycle()
    val twoStepLoginUiState by viewModel.twoStepLoginUiState.collectAsStateWithLifecycle()

    TwoStepLoginScreen(
        loginState = loginState,
        twoStepLoginUiState = twoStepLoginUiState,
        onCloseClick = onCloseClick,
        onLoginClick = viewModel::loginNextStep,
        onRetryClick = viewModel::fetchTwoStepLoginInfo
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TwoStepLoginScreen(
    loginState: LoginState,
    twoStepLoginUiState: TwoStepLoginUiState,
    onCloseClick: () -> Unit,
    onLoginClick: (String) -> Unit,
    onRetryClick: () -> Unit,
) {

    Scaffold(topBar = {
        CenterAlignedTopAppBar(title = {
            Text(
                stringResource(Res.string.two_step_login),
                style = MaterialTheme.typography.titleLarge
            )
        }, navigationIcon = {
            CloseButton(onClick = onCloseClick)
        })
    }) {
        Box(
            modifier = Modifier
                .padding(it)
                .fillMaxSize()
        ) {

            when (twoStepLoginUiState) {
                is TwoStepLoginUiState.Success -> TwoStepLoginContent(
                    loginState = loginState,
                    twoStepLoginUiState = twoStepLoginUiState,
                    onLoginClick = onLoginClick
                )

                else -> LoadMore(
                    hasError = twoStepLoginUiState is TwoStepLoginUiState.Error,
                    error = if (twoStepLoginUiState is TwoStepLoginUiState.Error) twoStepLoginUiState.error else null,
                    onRetryClick = onRetryClick
                )
            }

        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TwoStepLoginContent(
    loginState: LoginState,
    twoStepLoginUiState: TwoStepLoginUiState.Success,
    onLoginClick: (String) -> Unit
) {
    val focusRequester = remember { FocusRequester() }

    var code by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            twoStepLoginUiState.twoStepLoginInfo.title,
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = code,
            onValueChange = { code = it },
            label = { Text(stringResource(Res.string.tfa_code)) },
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(onDone = {
                if (code.isNotBlank()) {
                    onLoginClick(code)
                }
            }),
            singleLine = true,
        )
        Spacer(modifier = Modifier.height(32.dp))
        LoginButton(
            loginState = loginState,
            enabled = code.isNotBlank(),
            onLoginClick = { onLoginClick(code) }
        )
        Spacer(modifier = Modifier.height(8.dp))
        HtmlContent(
            content = twoStepLoginUiState.twoStepLoginInfo.problem,
            textStyle = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.error),
            modifier = Modifier.fillMaxWidth()
        )
    }


    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
}

@Composable
private fun LoginButton(
    loginState: LoginState,
    enabled: Boolean,
    onLoginClick: () -> Unit
) {
    val isLoading = loginState is LoginState.Loading
    Button(
        onClick = onLoginClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp),
        enabled = enabled && !isLoading,
        colors = ButtonDefaults.buttonColors(
            disabledContainerColor = if (isLoading) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(
                alpha = 0.12f
            ),
        )
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = MaterialTheme.colorScheme.onPrimary,
                strokeWidth = 2.dp
            )
        } else {
            Text(
                stringResource(Res.string.login),
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}
