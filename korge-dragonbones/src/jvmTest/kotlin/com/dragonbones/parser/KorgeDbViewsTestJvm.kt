package com.dragonbones.parser

import korlibs.korge.dragonbones.*
import korlibs.korge.view.*
import korlibs.image.bitmap.Bitmap32
import korlibs.image.format.*
import korlibs.io.async.*
import korlibs.io.file.std.*
import korlibs.io.serialization.json.*
import kotlin.test.*

class KorgeDbViewsTestJvm {
	@Test
	fun test() = suspendTest {
		val factory = KorgeDbFactory()
		val data = factory.parseDragonBonesData(Json.parseFast(resourcesVfs["Dragon/Dragon_ske.json"].readString())!!)
		val atlas = factory.parseTextureAtlasData(
			Json.parseFast(resourcesVfs["Dragon/Dragon_tex.json"].readString())!!,
			resourcesVfs["Dragon/Dragon_tex.png"].readBitmap().toBMP32()
		)
		val armatureDisplay = factory.buildArmatureDisplay("Dragon", "Dragon")!!.position(100, 100)
		armatureDisplay.dbUpdate()
		factory.clock.advanceTime(0.1)
		//armatureDisplay.dump()
		//println("--------------------")
		//armatureDisplay.dump()
	}

    @Test
    fun testOutOfBoundsSample() = suspendTest {
        val factory = KorgeDbFactory()
        val skeJsonData = resourcesVfs["503/503_ske.json"].readString()
        val texJsonData = resourcesVfs["503/503_tex.json"].readString()

        val dragonBonesData = Json.parseFast(skeJsonData) ?: throw Exception("Parse ske data error!")
        factory.parseDragonBonesData(dragonBonesData)

        val textureAtlasData = Json.parseFast(texJsonData) ?: throw Exception("Parse tex data error!")
        factory.parseTextureAtlasData(textureAtlasData, Bitmap32(1, 1))
    }
}
