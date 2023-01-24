import com.soywiz.korge.gradle.*

plugins {
    //alias(libs.plugins.korge)
    id("com.soywiz.korge") version "4.0.0-alpha-2"
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
    targetAndroidDirect()
    serializationJson()
}

dependencies {
    add("commonMainApi", project(":deps"))
    //add("commonMainApi", project(":korge-dragonbones"))
}

