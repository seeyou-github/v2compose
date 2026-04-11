package io.github.v2compose.ui.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.AutofillType
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import io.github.v2compose.Constants
import io.github.v2compose.ui.common.CloseButton
import io.github.v2compose.ui.common.HtmlAlertDialog
import io.github.v2compose.ui.common.autofill
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import v2compose.shared.generated.resources.Res
import v2compose.shared.generated.resources.googleg_standard_color
import v2compose.shared.generated.resources.load_failed
import v2compose.shared.generated.resources.login
import v2compose.shared.generated.resources.login_captcha
import v2compose.shared.generated.resources.login_password
import v2compose.shared.generated.resources.login_username
import v2compose.shared.generated.resources.sign_in_with_google

private const val TAG = "LoginScreen"

@Composable
fun LoginScreenRoute(
    onCloseClick: () -> Unit,
    onSignInWithGoogleClick: (String) -> Unit,
    redirect: String? = null,
    viewModel: LoginViewModel = koinViewModel(),
    loginScreenState: LoginScreenState = rememberLoginScreenState(),
) {
    val loginParamState by viewModel.loginParam.collectAsStateWithLifecycle()
    val loginState by viewModel.login.collectAsStateWithLifecycle()

    LoginScreen(
        loginScreenState = loginScreenState,
        loginParamState = loginParamState,
        loginState = loginState,
        onCloseClick = onCloseClick,
        login = viewModel::login,
        onSignInWithGoogleClick = onSignInWithGoogleClick,
        reloadLoginParam = viewModel::fetchLoginParam
    )
}

@Composable
private fun LoginScreen(
    loginScreenState: LoginScreenState,
    loginParamState: LoginParamState,
    loginState: LoginState,
    onCloseClick: () -> Unit,
    login: (String, String, String) -> Unit,
    onSignInWithGoogleClick: (String) -> Unit,
    reloadLoginParam: () -> Unit,
) {
    val problem = rememberSaveable(loginParamState) {
        if (loginParamState is LoginParamState.Success) loginParamState.data.problem else ""
    }
    HtmlAlertDialog(content = problem)

    Scaffold(
        topBar = { LoginTopBar(onCloseClick = onCloseClick) },
    ) {
        LoginContent(
            loginScreenState = loginScreenState,
            loginParamState = loginParamState,
            loginState = loginState,
            reloadLoginParam = reloadLoginParam,
            login = login,
            onSignInWithGoogleClick = {
                if (loginParamState is LoginParamState.Success) {
                    onSignInWithGoogleClick(loginParamState.data.once)
                }
            },
            modifier = Modifier.padding(it)
        )
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LoginTopBar(onCloseClick: () -> Unit) {
    CenterAlignedTopAppBar(
        title = { Text(stringResource(Res.string.login)) },
        navigationIcon = { CloseButton { onCloseClick() } })
}

@Composable
private fun LoginContent(
    loginScreenState: LoginScreenState,
    loginParamState: LoginParamState,
    loginState: LoginState,
    reloadLoginParam: () -> Unit,
    login: (String, String, String) -> Unit,
    onSignInWithGoogleClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var userName by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var captcha by rememberSaveable { mutableStateOf("") }

    val loginButtonEnabled = remember(userName, password, captcha) {
        userName.isNotEmpty() && password.isNotEmpty() && captcha.isNotEmpty()
    }

    val focusRequesters: List<FocusRequester> = remember {
        List(3) { FocusRequester() }
    }

    val onLoginClick = fun() {
        if (loginScreenState.checkValid(userName, password, captcha)) {
            login(userName, password, captcha)
        }
    }

    Column(
        modifier = modifier.padding(horizontal = 32.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        UserName(
            userName = userName,
            error = loginScreenState.userNameError,
            onValueChanged = {
                userName = it
                loginScreenState.resetUserNameError()
            },
            onNextClick = { focusRequesters[1].requestFocus() },
            modifier = Modifier.focusRequester(focusRequesters[0]),
        )
        Spacer(Modifier.height(8.dp))
        Password(
            password = password,
            error = loginScreenState.passwordError,
            onValueChanged = {
                password = it
                loginScreenState.resetPasswordError()
            },
            onNextClick = { focusRequesters[2].requestFocus() },
            modifier = Modifier.focusRequester(focusRequesters[1]),
        )
        Spacer(Modifier.height(8.dp))
        Captcha(
            text = captcha,
            error = loginScreenState.captchaError,
            loginParamState = loginParamState,
            onValueChanged = {
                captcha = it
                loginScreenState.resetCaptchaError()
            },
            onGoClick = onLoginClick,
            reloadLoginParam = reloadLoginParam,
            modifier = Modifier.focusRequester(focusRequesters[2]),
        )
        Spacer(modifier = Modifier.height(8.dp))
        LoginButton(
            loginState = loginState, enabled = loginButtonEnabled, onLoginClick = onLoginClick
        )
        Spacer(modifier = Modifier.height(16.dp))
        SignInWithGoogle(
            loginParamState = loginParamState,
            onClick = onSignInWithGoogleClick,
            modifier = Modifier.align(Alignment.End)
        )
    }

    LaunchedEffect(true) {
        focusRequesters[0].requestFocus()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun UserName(
    userName: String,
    onValueChanged: (String) -> Unit,
    onNextClick: () -> Unit,
    modifier: Modifier = Modifier,
    error: StringResource? = null,
) {
    OutlinedTextField(
        value = userName,
        onValueChange = onValueChanged,
        label = { Text(stringResource(Res.string.login_username)) },
        supportingText = {
            if (error != null) {
                Text(stringResource(error))
            }
        },
        keyboardOptions = KeyboardOptions(
            imeAction = ImeAction.Next, keyboardType = KeyboardType.Text
        ),
        keyboardActions = KeyboardActions(onNext = { onNextClick() }),
        isError = error != null,
        modifier = modifier
            .fillMaxWidth()
            .autofill(autofillTypes = listOf(AutofillType.Username), onFill = onValueChanged),
        singleLine = true,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Password(
    password: String,
    onValueChanged: (String) -> Unit,
    onNextClick: () -> Unit,
    modifier: Modifier = Modifier,
    error: StringResource? = null,
) {
    val visualTransformation = remember { PasswordVisualTransformation() }
    OutlinedTextField(
        value = password,
        onValueChange = onValueChanged,
        label = { Text(stringResource(Res.string.login_password)) },
        supportingText = {
            if (error != null) {
                Text(stringResource(error))
            }
        },
        keyboardOptions = KeyboardOptions(
            imeAction = ImeAction.Next,
            keyboardType = KeyboardType.Password,
        ),
        keyboardActions = KeyboardActions(onNext = { onNextClick() }),
        isError = error != null,
        modifier = modifier
            .fillMaxWidth()
            .autofill(autofillTypes = listOf(AutofillType.Password), onFill = onValueChanged),
        singleLine = true,
        visualTransformation = visualTransformation,
    )
}

@Composable
private fun Captcha(
    text: String,
    loginParamState: LoginParamState,
    onValueChanged: (String) -> Unit,
    reloadLoginParam: () -> Unit,
    modifier: Modifier = Modifier,
    onGoClick: (() -> Unit)? = null,
    error: StringResource? = null,
) {
    Row(verticalAlignment = Alignment.Top, modifier = modifier) {
        OutlinedTextField(
            value = text,
            onValueChange = onValueChanged,
            label = { Text(stringResource(Res.string.login_captcha)) },
            supportingText = {
                if (error != null) {
                    Text(stringResource(error))
                }
            },
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Go,
                keyboardType = KeyboardType.Text,
                capitalization = KeyboardCapitalization.Characters,
            ),
            keyboardActions = KeyboardActions(onNext = { onGoClick?.invoke() }),
            isError = error != null,
            modifier = Modifier.weight(1f),
            singleLine = true,
        )
        Spacer(Modifier.width(16.dp))
        Box(
            modifier = Modifier
                .weight(1.5f)
                .padding(top = 8.dp)
                .height(52.dp)
        ) {
            when (loginParamState) {
                is LoginParamState.Success -> {
                    val captchaImage =
                        "${Constants.baseUrl}/_captcha?once=${loginParamState.data.once}"
                    AsyncImage(
                        model = captchaImage,
                        contentDescription = "captcha image",
                        modifier = Modifier
                            .fillMaxSize()
                            .clickable { reloadLoginParam() },
                        contentScale = ContentScale.Fit,
                        placeholder = ColorPainter(MaterialTheme.colorScheme.surfaceVariant)
                    )
                }

                is LoginParamState.Loading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    }
                }

                is LoginParamState.Error -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.errorContainer)
                            .clickable { reloadLoginParam() },
                    ) {
                        Text(
                            stringResource(Res.string.load_failed),
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            style = MaterialTheme.typography.labelMedium,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LoginButton(loginState: LoginState, enabled: Boolean, onLoginClick: () -> Unit) {
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

@Composable
private fun SignInWithGoogle(
    loginParamState: LoginParamState, onClick: () -> Unit, modifier: Modifier = Modifier
) {
    OutlinedButton(
        enabled = loginParamState is LoginParamState.Success,
        onClick = onClick,
        modifier = modifier.height(48.dp)
    ) {
        Image(
            painter = painterResource(Res.drawable.googleg_standard_color),
            contentDescription = "google branding",
            modifier = Modifier.size(24.dp)
        )
        Spacer(Modifier.width(8.dp))
        Text(
            stringResource(Res.string.sign_in_with_google),
            style = MaterialTheme.typography.titleMedium,
        )
    }

}