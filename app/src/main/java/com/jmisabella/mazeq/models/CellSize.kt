package com.jmisabella.mazeq.models

enum class CellSize(val value: Int) {
    TINY(11), SMALL(12), MEDIUM(13), LARGE(14);

    val label: String
        get() = when (this) {
            TINY -> "Tiny"
            SMALL -> "Small"
            MEDIUM -> "Medium"
            LARGE -> "Large"
        }
}