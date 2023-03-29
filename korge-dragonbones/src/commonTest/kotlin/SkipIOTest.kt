import korlibs.io.util.*

//val skipIOTest get() = OS.isJsBrowser
val skipIOTest get() = OS.isJs || OS.isAndroid
val doIOTest get() = !skipIOTest
