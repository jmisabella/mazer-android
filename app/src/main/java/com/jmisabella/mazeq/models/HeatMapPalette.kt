package com.jmisabella.mazeq.models

data class HeatMapPalette(
    val name: String,
    val shades: List<String>
)

val allPalettes: List<HeatMapPalette> = listOf(
    turquoisePalette,
    greenSeaPalette,
    emeraldPalette,
    nephritisPalette,
    peterRiverPalette,
    belizeHolePalette,
    amethystPalette,
    wisteriaPalette,
    sunflowerPalette,
    orangePalette,
    carrotPalette,
    pumpkinPalette,
    alizarinPalette,
    pomegranatePalette,
    cloudsPalette,
    silverPalette,
    concretePalette,
    asbestosPalette,
    wetAsphaltPalette,
    midnightBluePalette
)

val turquoisePalette = HeatMapPalette("Turquoise", listOf(
    "#e8f8f5", "#d1f2eb", "#a3e4d7", "#76d7c4", "#48c9b0",
    "#1abc9c", "#17a589", "#148f77", "#117864", "#0e6251"
))

// ... (add the rest similarly; e.g., greenSeaPalette = HeatMapPalette("Green Sea", listOf(...)))
// Truncated for brevity; copy the arrays from Swift and wrap in listOf().