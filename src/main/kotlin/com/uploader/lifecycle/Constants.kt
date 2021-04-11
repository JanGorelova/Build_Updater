package com.uploader.lifecycle

object Constants {
    const val UPDATES_URL = "https://www.jetbrains.com/updates/updates.xml"

    const val PYCHARM = "PyCharm"
    const val WEBSTORM = "WebStorm"
    const val IDEA = "IntelliJ IDEA"
    const val IDEA_EDU = "IntelliJ IDEA Edu"
    const val CLION = "CLion"
    const val DATA_GRIP = "DataGrip"
    const val GO_LAND = "GoLand"
    const val PHP_STORM = "PhpStorm"
    const val PYCHARM_EDU = "PyCharm Edu"
    const val RIDER = "Rider"
    const val RUBY_MINE = "RubyMine"
    const val MPS = "MPS"

    val productNameToUrl = mapOf(
        IDEA to "idea/ideaIU",
        IDEA_EDU to "idea/ideaIE",
        CLION to "cpp/CLion",
        DATA_GRIP to "datagrip/datagrip",
        GO_LAND to "go/goland",
        // todo is that same as pycharm?
//        "JetBrains DS" to "?/?",
        MPS to "mps/{version}/MPS",
        PHP_STORM to "webide/PhpStorm",
        PYCHARM to "python/pycharm-professional",
        PYCHARM_EDU to "python/pycharm-edu",
        RIDER to "rider/JetBrains.Rider",
        RUBY_MINE to "ruby/RubyMine",
        WEBSTORM to "webstorm/WebStorm"
    )

    private val supportedCodes = mapOf(
        CLION to listOf("CL"),
        DATA_GRIP to listOf("DB"),
        GO_LAND to listOf("GO"),
        IDEA to listOf("IC", "IU"),
        IDEA_EDU to listOf("IE"),
        MPS to listOf("MPS"),
        PHP_STORM to listOf("PS"),
        PYCHARM to listOf("PC", "PCA", "PY", "PYA"),
        PYCHARM_EDU to listOf("PE"),
        RIDER to listOf("RD"),
        RUBY_MINE to listOf("RM"),
        WEBSTORM to listOf("WS"),
    )

    fun getProductNameByProductCode(productCode: String) =
        supportedCodes.entries
            .firstOrNull { productCode in it.value }?.key ?: error("Unknown product code specified: $productCode")
}
