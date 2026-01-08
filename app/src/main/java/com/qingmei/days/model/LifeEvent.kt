package com.qingmei.days.model

import java.util.UUID

data class LifeEvent(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val date: String,
    val color: Long,
    val type: Int = 0,
    val description: String = "",
    val imageUri: String? = null,
    val isTop: Boolean = false,
)