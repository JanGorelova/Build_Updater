package com.uploader.dao.dto

data class ProductInfo(
    val name: String,
    val version: String,
    val buildNumber: String,
    val productCode: String,
    val dataDirectoryName: String,
    val svgIconPath: String,
    val launch: List<LaunchData>
) {
    data class LaunchData(
        val os: String,
        val launcherPath: String,
        val javaExecutablePath: String,
        val vmOptionsFilePath: String,
        val startupWmClass: String
    )
}
