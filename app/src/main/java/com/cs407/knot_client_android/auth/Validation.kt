package com.cs407.knot_client_android.auth

import androidx.annotation.StringRes
import com.cs407.knot_client_android.R

data class PasswordResult(
    val isValid: Boolean,
    @StringRes val errorMessageRes: Int? = null
)

data class UsernameResult(
    val isValid: Boolean,
    @StringRes val errorMessageRes: Int? = null
)

fun checkUsername(username: String): UsernameResult {
    if (username.isEmpty()) {
        return UsernameResult(
            isValid = false,
            errorMessageRes = R.string.error_username_empty
        )
    }

    return UsernameResult(isValid = true)
}

fun checkPassword(password: String): PasswordResult {
    if (password.isEmpty()) {
        return PasswordResult(
            isValid = false,
            errorMessageRes = R.string.error_password_empty
        )
    }

    if (password.length < 5) {
        return PasswordResult(
            isValid = false,
            errorMessageRes = R.string.error_password_short
        )
    }

    val hasDigit = Regex("\\d+").containsMatchIn(password)
    val hasLowercase = Regex("[a-z]+").containsMatchIn(password)
    val hasUppercase = Regex("[A-Z]+").containsMatchIn(password)

    if (hasLowercase && hasUppercase && hasDigit) {
        return PasswordResult(isValid = true)
    }

    return PasswordResult(
        isValid = false,
        errorMessageRes = R.string.error_password_requirements
    )
}