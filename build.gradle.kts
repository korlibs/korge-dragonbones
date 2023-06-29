import korlibs.korge.gradle.*

plugins {
    //alias(libs.plugins.korge)
    id("com.soywiz.korge") version "4.0.7"
    //id("com.soywiz.korge") version "999.0.0.999"
}

korge {
    id = "com.sample.demodragonbones"

// To enable all targets at once

    //targetAll()

// To enable targets based on properties/environment variables
    //targetDefault()

// To selectively enable targets

    targetJvm()
    targetJs()
    targetDesktop()
    targetDesktopCross()
    targetIos()
    targetAndroid()
    serializationJson()
}

dependencies {
    add("commonMainApi", project(":deps"))
    //add("commonMainApi", project(":korge-dragonbones"))
}

