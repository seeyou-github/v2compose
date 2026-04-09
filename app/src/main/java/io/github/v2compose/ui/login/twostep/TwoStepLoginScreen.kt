package io.github.v2compose.ui.login.twostep

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Done
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
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
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.koin.androidx.compose.koinViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.simform.ssjetpackcomposeprogressbuttonlibrary.SSButtonState
import com.simform.ssjetpackcomposeprogressbuttonlibrary.SSButtonType
import com.simform.ssjetpackcomposeprogressbuttonlibrary.SSJetPackComposeProgressButton
import io.github.v2compose.LocalSnackbarHostState
import io.github.v2compose.network.bean.TwoStepLoginInfo
import io.github.v2compose.ui.common.CloseButton
import io.github.v2compose.ui.common.LoadError
import io.github.v2compose.ui.common.Loading
import v2compose.shared.generated.resources.*

@Composable
fun TwoStepLoginScreenRoute(
    onCloseClick: () -> Unit,
    viewModel: TwoStepLoginViewModel = koinViewModel()
) {
    val context = LocalContext.current
    val snackbarHostState = LocalSnackbarHostState.current

    val twoStepLoginUiState by viewModel.twoStepLoginUiState.collectAsStateWithLifecycle()
    val loginState by viewModel.login.collectAsStateWithLifecycle()

    val problem: String = remember(twoStepLoginUiState) {
        if (twoStepLoginUiState is TwoStepLoginUiState.Success) {
            (twoStepLoginUiState as TwoStepLoginUiState.Success).twoStepLoginInfo.problem
        } else ""
    }

    if (problem.isNotEmpty()) {
        LaunchedEffect(problem) {
            snackbarHostState.showSnackbar(getString(Res.string.captcha_error))
        }
    }

    if (loginState is LoginState.Error) {
        LaunchedEffect(loginState, snackbarHostState) {
            (loginState as LoginState.Error).error?.message?.let {
                snackbarHostState.showSnackbar(it)
            }
            viewModel.resetLoginState()
        }
    }

    TwoStepLoginScreen(
        twoStepLoginUiState = twoStepLoginUiState,
        loginState = loginState,
        onCloseClick = onCloseClick,
        onRetryTwoStepLoginInfo = viewModel::fetchTwoStepLoginInfo,
        onLogin = viewModel::loginNextStep,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TwoStepLoginScreen(
    twoStepLoginUiState: TwoStepLoginUiState,
    loginState: LoginState,
    onCloseClick: () -> Unit,
    onRetryTwoStepLoginInfo: () -> Unit,
    onLogin: (String) -> Unit,
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(Res.string.two_step_login)) },
                navigationIcon = { CloseButton { onCloseClick() } })
        }
    ) {
        Box(modifier = Modifier.padding(it)) {
            when (twoStepLoginUiState) {
                is TwoStepLoginUiState.Success -> {
                    LoginContent(
                        twoStepLoginInfo = twoStepLoginUiState.twoStepLoginInfo,
                        loginState = loginState,
                        onLogin = onLogin
                    )
                }

                is TwoStepLoginUiState.Loading -> {
                    Loading()
                }

                is TwoStepLoginUiState.Error -> {
                    LoadError(
                        error = twoStepLoginUiState.error,
                        onRetryClick = onRetryTwoStepLoginInfo
                    )
                }
            }
        }
    }
}

@Composable
private fun LoginContent(
    twoStepLoginInfo: TwoStepLoginInfo,
    loginState: LoginState,
    onLogin: (String) -> Unit
) {
    val context = LocalContext.current

    val focusRequester = remember { FocusRequester() }
    var code by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<StringResource?>(null) }

    val onLoginClick = {
        if (code.isBlank()) {
            error = Res.string.tfa_code_empty
        } else {
            focusRequester.freeFocus()
            onLogin(code)
        }
    }

    Column(
        modifier = Modifier.padding(horizontal = 32.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            twoStepLoginInfo.title.ifEmpty { stringResource(Res.string.two_step_login_desc) },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )

        Spacer(Modifier.height(8.dp))

        TfaCode(code = code, onValueChanged = {
            code = it
            error = null
        }, onNextClick = onLoginClick, modifier = Modifier.focusRequester(focusRequester), error = error)

        Spacer(Modifier.height(8.dp))

        LoginButton(
            loginState = loginState,
            enabled = code.isNotEmpty(),
            onLoginClick = onLoginClick,
        )

        Spacer(Modifier.height(8.dp))

        Text(
            stringResource(Res.string.two_step_login_tips),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
        )

        LaunchedEffect(focusRequester) {
            focusRequester.requestFocus()
        }
    }
}

@Composable
private fun TfaCode(
    code: String,
    onValueChanged: (String) -> Unit,
    onNextClick: () -> Unit,
    modifier: Modifier = Modifier,
    error: StringResource? = null,
) {
    OutlinedTextField(
        value = code,
        onValueChange = onValueChanged,
        label = { Text(stringResource(Res.string.tfa_code)) },
        supportingText = {
            if (error != null) {
                Text(stringResource(error))
            }
        },
        keyboardOptions = KeyboardOptions(
            imeAction = ImeAction.Done,
            keyboardType = KeyboardType.Number,
        ),
        keyboardActions = KeyboardActions(onDone = { onNextClick() }),
        isError = error != null,
        modifier = modifier.fillMaxWidth(),
        singleLine = true,
    )
}

@Composable
private fun LoginButton(loginState: LoginState, enabled: Boolean, onLoginClick: () -> Unit) {
    val buttonState = remember(loginState) {
        when (loginState) {
            is LoginState.Idle, is LoginState.Error -> SSButtonState.IDLE
            is LoginState.Loading -> SSButtonState.LOADING
        }
    }
    val screenWidthDp = LocalConfiguration.current.screenWidthDp.dp
    SSJetPackComposeProgressButton(
        type = SSButtonType.CIRCLE,
        width = screenWidthDp - 64.dp,
        height = 48.dp,
        onClick = {
            if (buttonState != SSButtonState.LOADING) {
                onLoginClick()
            }
        },
        assetColor = MaterialTheme.colorScheme.onPrimary,
        successIconTintColor = MaterialTheme.colorScheme.onPrimary,
        failureIconTintColor = MaterialTheme.colorScheme.onPrimary,
        buttonState = buttonState,
        enabled = enabled,
        text = stringResource(Res.string.login),
        fontSize = 18.sp,
        fontWeight = FontWeight.SemiBold,
        speedMillis = 400,
        successIconPainter = rememberVectorPainter(image = Icons.Rounded.Done),
        failureIconPainter = rememberVectorPainter(image = Icons.Rounded.Info),
        colors = ButtonDefaults.buttonColors(
            disabledContainerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
            disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
        )
    )
}