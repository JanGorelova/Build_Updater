package com.uploader.provider

object Constants {
    const val UPDATES_URL = "https://www.jetbrains.com/updates/updates.xml"
    const val productInfoFileName = "product-info.json"

    const val PYCHARM = "PyCharm"
    const val WEBSTORM = "WebStorm"
    const val IDEA = "IntelliJ IDEA"

    val productNameToUrl = mapOf(
        IDEA to "idea/ideaIU",
//        "CLion" to "cpp/CLion",
//        "DataGrip" to "datagrip/datagrip",
//        "GoLand" to "datagrip/datagrip",
//        "JetBrains DS" to "datagrip/datagrip",
//        "MPS" to "datagrip/datagrip",
//        "PhpStorm" to "datagrip/datagrip",
        PYCHARM to "python/pycharm-professional",
//        "PyCharm Edu" to "datagrip/datagrip",
//        "Rider" to "datagrip/datagrip",
//        "Rider for Unreal Engine" to "datagrip/datagrip",
//        "RubyMine" to "datagrip/datagrip",
        WEBSTORM to "webstorm/WebStorm"
    )

    val supportedCodes = mapOf(
        "CLion" to listOf("CL"),
        "DataGrip" to listOf("DB"),
        "DataGrip" to listOf("DB"),
        "GoLand" to listOf("GO"),
        IDEA to listOf("IC", "IU"),
        "IntelliJ IDEA Edu" to listOf("IE"),
        "MPS" to listOf("MPS"),
        "PhpStorm" to listOf("PS"),
        PYCHARM to listOf("PC", "PCA", "PY", "PYA"),
        "PyCharm Edu" to listOf("PE"),
        "Rider" to listOf("RD"),
        "Rider for Unreal Engine" to listOf("RDCPPP"),
        "RubyMine" to listOf("RM"),
        WEBSTORM to listOf("WS"),
    )

    fun getProductNameByProductCode(productCode: String) =
        supportedCodes.entries
            .first { productCode in it.value }
            .key
}
