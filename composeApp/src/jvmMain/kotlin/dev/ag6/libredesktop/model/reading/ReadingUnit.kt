package dev.ag6.libredesktop.model.reading

enum class ReadingUnit(val label: String) {
    MMOL("mmol/L"),
    MG("mg/dL");

    fun format(valueInMgPerDl: Int): String {
        return when (this) {
            MMOL -> String.format("%.1f %s", valueInMgPerDl / 18.0, label)
            MG -> "$valueInMgPerDl $label"
        }
    }

    fun formatValueOnly(valueInMgPerDl: Int): String {
        return when (this) {
            MMOL -> String.format("%.1f", valueInMgPerDl / 18.0)
            MG -> valueInMgPerDl.toString()
        }
    }
}
