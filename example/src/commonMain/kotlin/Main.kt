import korlibs.korge.*
import korlibs.korge.scene.*

suspend fun main() = Korge(forceRenderEveryFrame = false).start {
    sceneContainer().changeTo({ MainDragonbones() })
}
