package com.cs407.knot_client_android.auth

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
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cs407.knot_client_android.ui.theme.Knot_client_androidTheme

@Composable
fun AuthScreen() {
    val lilac = Color(0xFFF1EAFF)
    val accent = Color(0xFF7F67FF)
    var isRegister by remember { mutableStateOf(false) }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

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
                            Icon(
                                painter = painterResource(id = android.R.drawable.sym_def_app_icon),
                                contentDescription = null,
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
                                onValueChange = { username = it },
                                label = "Username",
                                leadingIcon = android.R.drawable.ic_menu_edit
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            AuthTextField(
                                value = password,
                                onValueChange = { password = it },
                                label = "Password",
                                leadingIcon = android.R.drawable.ic_lock_idle_lock,
                                isPassword = true
                            )
                            if (isRegister) {
                                Spacer(modifier = Modifier.height(16.dp))
                                AuthTextField(
                                    value = confirmPassword,
                                    onValueChange = { confirmPassword = it },
                                    label = "Confirm Password",
                                    leadingIcon = android.R.drawable.ic_lock_idle_lock,
                                    isPassword = true
                                )
                            }
                            Spacer(modifier = Modifier.height(24.dp))
                            Button(
                                onClick = { /* TODO: Handle auth action */ },
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
        android.R.drawable.ic_menu_myplaces to "Profile",
        android.R.drawable.ic_menu_send to "Invite",
        android.R.drawable.ic_menu_manage to "Settings",
        android.R.drawable.ic_menu_help to "Help"
    )
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        actions.forEach { (iconRes, label) ->
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
                        Icon(
                            painter = painterResource(id = iconRes),
                            contentDescription = label,
                            tint = accent
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
    leadingIcon: Int,
    isPassword: Boolean = false
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        singleLine = true,
        leadingIcon = {
            Icon(
                painter = painterResource(id = leadingIcon),
                contentDescription = null,
                tint = Color(0xFF7B749C)
            )
        },
        visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    )
}

@Preview(showBackground = true)
@Composable
private fun AuthScreenPreview() {
    Knot_client_androidTheme {
        AuthScreen()
    }
}