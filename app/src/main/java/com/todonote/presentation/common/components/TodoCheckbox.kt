package com.todonote.presentation.common.components

import android.view.HapticFeedbackConstants
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import com.todonote.presentation.theme.*

@Composable
fun TodoCheckbox(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val view = LocalView.current
    Checkbox(
        checked = checked,
        onCheckedChange = { isChecked ->
            if (isChecked) {
                view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
            }
            onCheckedChange(isChecked)
        },
        modifier = modifier,
        colors = CheckboxDefaults.colors(
            checkedColor = PriorityMedium,
            uncheckedColor = PriorityLow.copy(alpha = 0.6f)
        )
    )
}
