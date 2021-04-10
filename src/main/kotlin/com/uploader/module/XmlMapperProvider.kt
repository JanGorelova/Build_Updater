package com.uploader.module

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.dataformat.xml.XmlMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule

object XmlMapperProvider {
    fun xmlMapper(): XmlMapper {
        val mapper = XmlMapper()
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        mapper.registerModule(KotlinModule())
        mapper.findAndRegisterModules()

        return mapper
    }
}
