package com.jmisabella.mazer.models

data class HeatMapPalette(
    val name: String,
    val shades: List<String>
)

val turquoisePalette = HeatMapPalette("Turquoise", listOf(
    "#e8f8f5", "#d1f2eb", "#a3e4d7", "#76d7c4", "#48c9b0",
    "#1abc9c", "#17a589", "#148f77", "#117864", "#0e6251"
))

val greenSeaPalette = HeatMapPalette("Green Sea", listOf(
    "#e8f6f3", "#d0ece7", "#a2d9ce", "#73c6b6", "#45b39d",
    "#16a085", "#138d75", "#117a65", "#0e6655", "#0b5345"
))

val emeraldPalette = HeatMapPalette("Emerald", listOf(
    "#eafaf1", "#d5f5e3", "#abebc6", "#82e0aa", "#58d68d",
    "#2ecc71", "#28b463", "#239b56", "#1d8348", "#186a3b"
))

val nephritisPalette = HeatMapPalette("Nephritis", listOf(
    "#e9f7ef", "#d4efdf", "#a9dfbf", "#7dcea0", "#52be80",
    "#27ae60", "#229954", "#1e8449", "#196f3d", "#145a32"
))

val peterRiverPalette = HeatMapPalette("Peter River", listOf(
    "#ebf5fb", "#d6eaf8", "#aed6f1", "#85c1e9", "#5dade2",
    "#3498db", "#2e86c1", "#2874a6", "#21618c", "#1b4f72"
))

val belizeHolePalette = HeatMapPalette("Belize Hole", listOf(
    "#eaf2f8", "#d4e6f1", "#a9cce3", "#7fb3d5", "#5499c7",
    "#2980b9", "#2471a3", "#1f618d", "#1a5276", "#154360"
))

val amethystPalette = HeatMapPalette("Amethyst", listOf(
    "#f5eef8", "#ebdef0", "#d7bde2", "#c39bd3", "#af7ac5",
    "#9b59b6", "#884ea0", "#76448a", "#633974", "#512e5f"
))

val wisteriaPalette = HeatMapPalette("Wisteria", listOf(
    "#f4ecf7", "#e8daef", "#d2b4de", "#bb8fce", "#a569bd",
    "#8e44ad", "#7d3c98", "#6c3483", "#5b2c6f", "#4a235a"
))

val sunflowerPalette = HeatMapPalette("Sunflower", listOf(
    "#fef9e7", "#fcf3cf", "#f9e79f", "#f7dc6f", "#f4d03f",
    "#f1c40f", "#d4ac0d", "#b7950b", "#9a7d0a", "#7d6608"
))

val orangePalette = HeatMapPalette("Orange", listOf(
    "#fef5e7", "#fdebd0", "#fad7a0", "#f8c471", "#f5b041",
    "#f39c12", "#d68910", "#b9770e", "#9c640c", "#7e5109"
))

val carrotPalette = HeatMapPalette("Carrot", listOf(
    "#fdf2e9", "#fae5d3", "#f5cba7", "#f0b27a", "#eb984e",
    "#e67e22", "#ca6f1e", "#af601a", "#935116", "#784212"
))

val pumpkinPalette = HeatMapPalette("Pumpkin", listOf(
    "#fbeee6", "#f6ddcc", "#edbb99", "#e59866", "#dc7633",
    "#d35400", "#ba4a00", "#a04000", "#873600", "#6e2c00"
))

val alizarinPalette = HeatMapPalette("Alizarin", listOf(
    "#fdedec", "#fadbd8", "#f5b7b1", "#f1948a", "#ec7063",
    "#e74c3c", "#cb4335", "#b03a2e", "#943126", "#78281f"
))

val pomegranatePalette = HeatMapPalette("Pomegranate", listOf(
    "#f9ebea", "#f2d7d5", "#e6b0aa", "#d98880", "#cd6155",
    "#c0392b", "#a93226", "#922b21", "#7b241c", "#641e16"
))

val cloudsPalette = HeatMapPalette("Clouds", listOf(
    "#fdfefe", "#fbfcfc", "#f7f9f9", "#f4f6f7", "#f0f3f4",
    "#ecf0f1", "#d0d3d4", "#b3b6b7", "#979a9a", "#7b7d7d"
))

val silverPalette = HeatMapPalette("Silver", listOf(
    "#f8f9f9", "#f2f3f4", "#e5e7e9", "#d7dbdd", "#cacfd2",
    "#bdc3c7", "#a6acaf", "#909497", "#797d7f", "#626567"
))

val concretePalette = HeatMapPalette("Concrete", listOf(
    "#f4f6f6", "#eaeded", "#d5dbdb", "#bfc9ca", "#aab7b8",
    "#95a5a6", "#839192", "#717d7e", "#5f6a6a", "#4d5656"
))

val asbestosPalette = HeatMapPalette("Asbestos", listOf(
    "#f2f4f4", "#e5e8e8", "#ccd1d1", "#b2babb", "#99a3a4",
    "#7f8c8d", "#707b7c", "#616a6b", "#515a5a", "#424949"
))

val wetAsphaltPalette = HeatMapPalette("Wet Asphalt", listOf(
    "#ebedef", "#d6dbdf", "#aeb6bf", "#85929e", "#5d6d7e",
    "#34495e", "#2e4053", "#283747", "#212f3c", "#1b2631"
))

val midnightBluePalette = HeatMapPalette("Midnight Blue", listOf(
    "#eaecee", "#d5d8dc", "#abb2b9", "#808b96", "#566573",
    "#2c3e50", "#273746", "#212f3d", "#1c2833", "#17202a"
))

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


