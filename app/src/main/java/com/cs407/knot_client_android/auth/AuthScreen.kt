package com.cs407.knot_client_android.auth

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cs407.knot_client_android.R
import com.cs407.knot_client_android.ui.theme.Knot_client_androidTheme
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun AuthScreen() {
    val lilac = Color(0xFFF1EAFF)
    val accent = Color(0xFF7F67FF)
    var isRegister by remember { mutableStateOf(false) }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    var statusMessageRes by remember { mutableStateOf<Int?>(null) }

    Scaffold(
        containerColor = lilac
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(lilac)
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp, vertical = 32.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(96.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.8f)),
                            contentAlignment = Alignment.Center
                        ) {
                            GlyphIcon(
                                type = GlyphType.Profile,
                                tint = accent,
                                modifier = Modifier.size(56.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = if (isRegister) "Create your account" else "Welcome back",
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                        color = Color(0xFF2E2750)
                    )
                    Text(
                        text = if (isRegister) "Sign up to join your friends" else "Sign in to continue",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF7B749C)
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    SurfaceToggle(
                        isRegister = isRegister,
                        accent = accent,
                        onToggle = { isRegister = it }
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    ElevatedCard(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 6.dp),
                        colors = CardDefaults.elevatedCardColors(containerColor = Color.White.copy(alpha = 0.92f))
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            AuthTextField(
                                value = username,
                                onValueChange = {
                                    username = it
                                    statusMessageRes = null
                                },
                                label = "Username",
                                leadingGlyph = GlyphType.Profile
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            AuthTextField(
                                value = password,
                                onValueChange = {
                                    password = it
                                    statusMessageRes = null
                                },
                                label = "Password",
                                leadingGlyph = GlyphType.Lock,
                                isPassword = true,
                                isPasswordVisible = passwordVisible,
                                onVisibilityToggle = { passwordVisible = !passwordVisible }
                            )
                            if (isRegister) {
                                Spacer(modifier = Modifier.height(16.dp))
                                AuthTextField(
                                    value = confirmPassword,
                                    onValueChange = {
                                        confirmPassword = it
                                        statusMessageRes = null
                                    },
                                    label = "Confirm Password",
                                    leadingGlyph = GlyphType.Lock,
                                    isPassword = true,
                                    isPasswordVisible = confirmPasswordVisible,
                                    onVisibilityToggle = { confirmPasswordVisible = !confirmPasswordVisible }
                                )
                            }
                            statusMessageRes?.let { messageRes ->
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = stringResource(id = messageRes),
                                    color = Color(0xFFD32F2F),
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.fillMaxWidth(),
                                    textAlign = TextAlign.Center
                                )
                            }
                            Spacer(modifier = Modifier.height(24.dp))
                            Button(
                                onClick = {
                                    val usernameResult = checkUsername(username)
                                    statusMessageRes = when {
                                        !usernameResult.isValid -> usernameResult.errorMessageRes
                                        else -> {
                                            val passwordResult = checkPassword(password)
                                            when {
                                                !passwordResult.isValid -> passwordResult.errorMessageRes
                                                isRegister && password != confirmPassword -> R.string.error_password_mismatch
                                                else -> null
                                            }
                                        }
                                    }
                                    if (statusMessageRes == null) {
                                        // TODO: Handle successful auth action
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = accent),
                                shape = RoundedCornerShape(18.dp)
                            ) {
                                Text(
                                    text = if (isRegister) "Create Account" else "Log In",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(28.dp))
                    QuickActionsRow(accent = accent)
                }

                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = if (isRegister) "Already have an account?" else "Need a new account?",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF7B749C),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    TextButton(
                        onClick = { isRegister = !isRegister },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = if (isRegister) "Switch to Log In" else "Switch to Sign Up",
                            color = accent,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SurfaceToggle(
    isRegister: Boolean,
    accent: Color,
    onToggle: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White.copy(alpha = 0.9f), RoundedCornerShape(32.dp))
            .padding(6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ToggleChip(
            text = "Log In",
            selected = !isRegister,
            accent = accent,
            modifier = Modifier.weight(1f)
        ) { onToggle(false) }
        ToggleChip(
            text = "Sign Up",
            selected = isRegister,
            accent = accent,
            modifier = Modifier.weight(1f)
        ) { onToggle(true) }
    }
}

@Composable
private fun QuickActionsRow(accent: Color) {
    val actions = listOf(
        GlyphType.Profile to "Profile",
        GlyphType.Invite to "Invite",
        GlyphType.Settings to "Settings",
        GlyphType.Help to "Help"
    )
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        actions.forEach { (glyph, label) ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    modifier = Modifier.size(56.dp),
                    shape = CircleShape,
                    color = Color.White.copy(alpha = 0.95f),
                    shadowElevation = 6.dp
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clickable { },
                        contentAlignment = Alignment.Center
                    ) {
                        GlyphIcon(
                            type = glyph,
                            tint = accent,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    color = Color(0xFF7B749C)
                )
            }
        }
    }
}

@Composable
private fun ToggleChip(
    text: String,
    selected: Boolean,
    accent: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .background(if (selected) accent else Color.Transparent)
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (selected) Color.White else Color(0xFF7B749C),
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun AuthTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    leadingGlyph: GlyphType,
    isPassword: Boolean = false,
    isPasswordVisible: Boolean = false,
    onVisibilityToggle: (() -> Unit)? = null
) {
    val iconTint = Color(0xFF7B749C)
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        singleLine = true,
        leadingIcon = {
            GlyphIcon(
                type = leadingGlyph,
                tint = iconTint,
                modifier = Modifier.size(22.dp)
            )
        },
        trailingIcon = if (isPassword && onVisibilityToggle != null) {
            {
                IconButton(onClick = onVisibilityToggle) {
                    GlyphIcon(
                        type = if (isPasswordVisible) GlyphType.VisibilityOn else GlyphType.VisibilityOff,
                        tint = iconTint,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        } else null,
        visualTransformation = if (isPassword && !isPasswordVisible) {
            PasswordVisualTransformation()
        } else {
            VisualTransformation.None
        },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    )
}

private enum class GlyphType {
    Profile,
    Invite,
    Settings,
    Help,
    Lock,
    VisibilityOn,
    VisibilityOff
}

@Composable
private fun GlyphIcon(
    type: GlyphType,
    tint: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val strokeWidth = size.minDimension * 0.12f
        val center = Offset(size.width / 2f, size.height / 2f)
        when (type) {
            GlyphType.Profile -> {
                val headRadius = size.minDimension * 0.26f
                drawCircle(
                    color = tint,
                    radius = headRadius,
                    center = Offset(center.x, size.height * 0.32f)
                )
                drawRoundRect(
                    color = tint,
                    topLeft = Offset(center.x - size.width * 0.3f, size.height * 0.55f),
                    size = Size(size.width * 0.6f, size.height * 0.35f),
                    cornerRadius = CornerRadius(size.width * 0.3f, size.width * 0.3f)
                )
            }
            GlyphType.Invite -> {
                val body = Path().apply {
                    moveTo(size.width * 0.18f, size.height * 0.68f)
                    lineTo(size.width * 0.78f, size.height * 0.5f)
                    lineTo(size.width * 0.18f, size.height * 0.32f)
                    close()
                }
                drawPath(body, tint)
                drawLine(
                    color = tint,
                    start = Offset(size.width * 0.35f, size.height * 0.48f),
                    end = Offset(size.width * 0.5f, size.height * 0.58f),
                    strokeWidth = strokeWidth,
                    cap = StrokeCap.Round
                )
            }
            GlyphType.Settings -> {
                val radius = size.minDimension * 0.22f
                for (angle in 0 until 360 step 90) {
                    val rad = angle * PI / 180f
                    val start = Offset(
                        center.x + cos(rad).toFloat() * radius * 1.6f,
                        center.y + sin(rad).toFloat() * radius * 1.6f
                    )
                    val end = Offset(
                        center.x + cos(rad).toFloat() * radius * 2.1f,
                        center.y + sin(rad).toFloat() * radius * 2.1f
                    )
                    drawLine(
                        color = tint,
                        start = start,
                        end = end,
                        strokeWidth = strokeWidth,
                        cap = StrokeCap.Round
                    )
                }
                drawCircle(
                    color = tint,
                    radius = radius,
                    center = center,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round)
                )
                drawCircle(
                    color = tint,
                    radius = radius * 0.55f,
                    center = center
                )
            }
            GlyphType.Help -> {
                drawArc(
                    color = tint,
                    startAngle = 200f,
                    sweepAngle = 140f,
                    useCenter = false,
                    topLeft = Offset(size.width * 0.25f, size.height * 0.2f),
                    size = Size(size.width * 0.5f, size.height * 0.55f),
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
                drawLine(
                    color = tint,
                    start = Offset(center.x, size.height * 0.6f),
                    end = Offset(center.x, size.height * 0.72f),
                    strokeWidth = strokeWidth,
                    cap = StrokeCap.Round
                )
                drawCircle(
                    color = tint,
                    radius = strokeWidth / 2f,
                    center = Offset(center.x, size.height * 0.84f)
                )
            }
            GlyphType.Lock -> {
                val bodyWidth = size.width * 0.6f
                val bodyHeight = size.height * 0.4f
                drawRoundRect(
                    color = tint,
                    topLeft = Offset(center.x - bodyWidth / 2f, size.height * 0.5f),
                    size = Size(bodyWidth, bodyHeight),
                    cornerRadius = CornerRadius(size.width * 0.1f, size.width * 0.1f)
                )
                drawArc(
                    color = tint,
                    startAngle = 210f,
                    sweepAngle = 120f,
                    useCenter = false,
                    topLeft = Offset(center.x - bodyWidth / 2f, size.height * 0.18f),
                    size = Size(bodyWidth, bodyHeight),
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
                drawCircle(
                    color = tint.copy(alpha = 0.3f),
                    radius = strokeWidth * 0.7f,
                    center = Offset(center.x, size.height * 0.66f)
                )
            }
            GlyphType.VisibilityOn -> {
                drawEyeShape(tint = tint, strokeWidth = strokeWidth, hideStrike = false)
            }
            GlyphType.VisibilityOff -> {
                drawEyeShape(tint = tint, strokeWidth = strokeWidth, hideStrike = true)
            }
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawEyeShape(
    tint: Color,
    strokeWidth: Float,
    hideStrike: Boolean
) {
    val centerY = size.height / 2f
    val path = Path().apply {
        moveTo(size.width * 0.1f, centerY)
        quadraticBezierTo(size.width / 2f, centerY - size.height * 0.3f, size.width * 0.9f, centerY)
        quadraticBezierTo(size.width / 2f, centerY + size.height * 0.3f, size.width * 0.1f, centerY)
    }
    drawPath(
        path = path,
        color = tint.copy(alpha = 0.15f)
    )
    drawPath(
        path = path,
        color = tint,
        style = Stroke(
            width = strokeWidth,
            cap = StrokeCap.Round,
            join = StrokeJoin.Round
        )
    )
    drawCircle(
        color = tint,
        radius = size.minDimension * 0.18f,
        center = Offset(size.width / 2f, centerY)
    )
    if (hideStrike) {
        drawLine(
            color = tint,
            start = Offset(size.width * 0.2f, size.height * 0.75f),
            end = Offset(size.width * 0.8f, size.height * 0.25f),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun AuthScreenPreview() {
    Knot_client_androidTheme {
        AuthScreen()
    }
}