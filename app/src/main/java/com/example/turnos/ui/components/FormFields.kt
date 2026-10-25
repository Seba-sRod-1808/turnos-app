package com.example.turnos.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.outlined.Chair
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.turnos.ui.theme.*

@Composable
fun BrandMark(modifier: Modifier = Modifier, size: Int = 112) {
    Box(
        modifier.size(size.dp).clip(CircleShape).background(TurnosOrangeSoft),
        contentAlignment = Alignment.Center,
    ) {
        Icon(Icons.Outlined.Chair, contentDescription = null, tint = TurnosOrange, modifier = Modifier.size((size * 0.57f).dp))
        Icon(
            Icons.Filled.ContentCut, contentDescription = null, tint = TurnosOrange,
            modifier = Modifier.size((size * 0.23f).dp).align(Alignment.TopEnd).offset(x = (-14).dp, y = 14.dp),
        )
    }
}

@Composable
fun LabeledTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = label,
    error: String? = null,
    enabled: Boolean = true,
    trailingIcon: ImageVector? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Next,
) {
    Column(modifier) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = InkSecondary, modifier = Modifier.padding(bottom = 6.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder) },
            trailingIcon = if (trailingIcon != null) {
                { Icon(trailingIcon, null, tint = TurnosOrange) }
            } else null,
            isError = error != null,
            supportingText = if (error != null) {
                { Text(error) }
            } else null,
            enabled = enabled,
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType, imeAction = imeAction),
            shape = MaterialTheme.shapes.small,
            colors = turnosFieldColors(),
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
fun PasswordField(
    label: String,
    value: String,
    visible: Boolean,
    onValueChange: (String) -> Unit,
    onToggleVisibility: () -> Unit,
    modifier: Modifier = Modifier,
    error: String? = null,
    enabled: Boolean = true,
    imeAction: ImeAction = ImeAction.Done,
) {
    Column(modifier) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = InkSecondary, modifier = Modifier.padding(bottom = 6.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(label) },
            singleLine = true,
            enabled = enabled,
            isError = error != null,
            supportingText = if (error != null) {
                { Text(error) }
            } else null,
            visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = onToggleVisibility) {
                    Icon(
                        if (visible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                        contentDescription = if (visible) "Ocultar contraseña" else "Mostrar contraseña",
                        tint = InkSecondary,
                    )
                }
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = imeAction),
            shape = MaterialTheme.shapes.small,
            colors = turnosFieldColors(),
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
fun <T> SegmentedSelector(
    options: List<Pair<T, String>>,
    selected: T,
    onSelect: (T) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Row(modifier.fillMaxWidth().clip(MaterialTheme.shapes.small).background(NeutralSoft).padding(4.dp)) {
        options.forEach { (value, label) ->
            val isSelected = value == selected
            Box(
                Modifier
                    .weight(1f)
                    .clip(MaterialTheme.shapes.small)
                    .background(if (isSelected) SurfaceWhite else NeutralSoft)
                    .clickable(enabled = enabled) { onSelect(value) }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    label,
                    style = MaterialTheme.typography.titleSmall,
                    color = if (isSelected) TurnosOrange else InkSecondary,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

@Composable
fun TextLink(text: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Text(
        text,
        style = MaterialTheme.typography.bodySmall,
        color = TurnosOrange,
        modifier = modifier.clickable(onClick = onClick).padding(vertical = 6.dp),
    )
}

@Composable
fun LoadingPrimaryButton(text: String, isLoading: Boolean, onClick: () -> Unit, enabled: Boolean = true) {
    if (isLoading) {
        Box(Modifier.fillMaxWidth().height(48.dp), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = TurnosOrange, modifier = Modifier.size(28.dp))
        }
    } else {
        PrimaryButton(text, onClick = onClick, enabled = enabled)
    }
}

@Composable
fun turnosFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = TurnosOrange,
    unfocusedBorderColor = DividerGray,
    focusedLabelColor = TurnosOrange,
    cursorColor = TurnosOrange,
    focusedPlaceholderColor = InkMuted,
    unfocusedPlaceholderColor = InkMuted,
)
