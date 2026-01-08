package com.qingmei.days.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEventDialog(
    initialTitle: String = "",
    initialDate: String = "",
    initialType: Int = 0,
    initialColor: Long = 0xFFF48FB1,
    initialDesc: String = "",
    onDismiss: () -> Unit,
    onConfirm: (String, String, Int, Long, String) -> Unit
) {
    var title by remember { mutableStateOf(initialTitle) }
    var dateString by remember { mutableStateOf(initialDate) }
    var isAnniversary by remember { mutableStateOf(initialType == 1) }
    var selectedColorHex by remember { mutableStateOf(initialColor) }
    var description by remember { mutableStateOf(initialDesc) }

    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()

    val colorPalette = listOf(
        0xFFF48FB1,
        0xFF80DEEA,
        0xFFFBC02D,
        0xFFA5D6A7,
        0xFFCE93D8
    )

    val qingCyanMain = Color(0xFF00BCD4)
    val qingCyanDeep = Color(0xFF006064)
    val meiPink = Color(0xFFEC407A)

    LaunchedEffect(datePickerState.selectedDateMillis) {
        datePickerState.selectedDateMillis?.let { millis ->
            val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            dateString = formatter.format(Date(millis))
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        title = {
            Text(
                text = if (initialTitle.isEmpty()) "种下一个日子" else "修改日子",
                color = qingCyanDeep
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {

                // 标题
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("标题", color = Color.Black) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = qingCyanMain,
                        focusedLabelColor = qingCyanMain,
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black,
                        cursorColor = Color.Black
                    )
                )

                // 日期
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = dateString,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("日期", color = Color.Black) },
                        trailingIcon = {
                            Icon(Icons.Default.DateRange, null, tint = qingCyanMain)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = qingCyanMain,
                            focusedLabelColor = qingCyanMain,
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.Black
                        )
                    )
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .clickable { showDatePicker = true }
                    )
                }

                // 描述
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("描述 / 心情", color = Color.Black) },
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = qingCyanMain,
                        focusedLabelColor = qingCyanMain,
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black,
                        cursorColor = Color.Black
                    )
                )

                // 类型
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isAnniversary) "类型: 🌸 纪念日" else "类型: ⏳ 倒数日",
                        color = qingCyanDeep
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Switch(
                        checked = isAnniversary,
                        onCheckedChange = { isAnniversary = it },
                        colors = SwitchDefaults.colors(
                            checkedTrackColor = meiPink,
                            uncheckedTrackColor = Color.LightGray
                        )
                    )
                }

                // 颜色选择
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    colorPalette.forEach { colorHex ->
                        val isSelected = selectedColorHex == colorHex
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color(colorHex))
                                .border(
                                    if (isSelected) 3.dp else 0.dp,
                                    qingCyanDeep,
                                    CircleShape
                                )
                                .clickable { selectedColorHex = colorHex },
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Icon(
                                    Icons.Default.Check,
                                    null,
                                    tint = qingCyanDeep,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotEmpty() && dateString.isNotEmpty()) {
                        onConfirm(
                            title,
                            dateString,
                            if (isAnniversary) 1 else 0,
                            selectedColorHex,
                            description
                        )
                    }
                },
                enabled = title.isNotEmpty() && dateString.isNotEmpty(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = meiPink,
                    contentColor = Color.White
                )
            ) {
                Text("确定")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(contentColor = Color.Gray)
            ) {
                Text("取消")
            }
        }
    )

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("确定", color = qingCyanDeep)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("取消", color = Color.Gray)
                }
            },
            colors = DatePickerDefaults.colors(containerColor = Color.White)
        ) {
            DatePicker(
                state = datePickerState,
                colors = DatePickerDefaults.colors(
                    containerColor = Color.White,
                    headlineContentColor = Color.Black,
                    titleContentColor = Color.Black,
                    weekdayContentColor = Color.Black,
                    dayContentColor = Color.Black,
                    selectedDayContainerColor = qingCyanMain,
                    selectedDayContentColor = Color.White,
                    todayContentColor = qingCyanMain,
                    todayDateBorderColor = qingCyanMain
                )
            )
        }
    }
}
