import korlibs.io.util.*
import korlibs.memory.Platform

//val skipIOTest get() = OS.isJsBrowser
val skipIOTest get() = Platform.isJs || Platform.isAndroid
val doIOTest get() = !skipIOTest
