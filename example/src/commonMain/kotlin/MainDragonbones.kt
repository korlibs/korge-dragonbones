import com.dragonbones.event.EventObject
import korlibs.korge.dragonbones.KorgeDbFactory
import korlibs.datastructure.WeakPropertyThis
import korlibs.image.bitmap.mipmaps
import korlibs.image.format.readBitmap
import korlibs.io.async.*
import korlibs.io.file.std.resourcesVfs
import korlibs.io.serialization.json.Json
import korlibs.korge.input.MouseEvents
import korlibs.korge.input.mouse
import korlibs.korge.scene.Scene
import korlibs.korge.scene.SceneContainer
import korlibs.korge.scene.sceneContainer
import korlibs.korge.time.delay
import korlibs.korge.ui.*
import korlibs.korge.view.*
import korlibs.math.geom.MPoint
import korlibs.math.random.get
import korlibs.memory.Buffer
import korlibs.time.milliseconds
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.awaitAll
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin
import kotlin.random.Random

class MainDragonbones : Scene() {
    lateinit var buttonContainer: Container
    lateinit var mySceneContainer: SceneContainer

    override suspend fun SContainer.sceneMain() {
        scale = 0.9
        //delay(1000.milliseconds)
        //mySceneContainer.changeToDisablingButtons<HelloWorldScene>()
        disablingButtons { mySceneContainer.changeTo({ EyeTrackingScene() }) }
    }

    override suspend fun SContainer.sceneInit() {
        //addEventListener<MouseEvent> {
        //	println("MouseEvent: ${views.nativeWidth},${views.nativeHeight} :: ${views.virtualWidth},${views.virtualHeight} :: $it")
        //}

        mySceneContainer = sceneContainer(views) {
            this.x = views.virtualWidth.toDouble() * 0.5
            this.y = views.virtualHeight.toDouble() * 0.5
        }
        buttonContainer = this
        uiButton("Hello").clicked {
            println("Hello")
            launchImmediately { disablingButtons { mySceneContainer.changeTo({ HelloWorldScene() }) } }
        }.position(8, views.virtualHeight - 48)
        //this += Button("Classic") { mySceneContainer.changeToDisablingButtons<ClassicDragonScene>() }.position(108, views.virtualHeight - 48)
        uiButton("Eye Tracking").clicked {
            println("Eye Tracking")
            launchImmediately { disablingButtons { mySceneContainer.changeTo({ EyeTrackingScene() }) } }
        }.position(200, views.virtualHeight - 48)
        uiButton("Skin Changing").clicked {
            println("Skin Changing")
            launchImmediately { disablingButtons { mySceneContainer.changeTo({ SkinChangingScene() }) } }
        }.position(600, views.virtualHeight - 48)
    }

    inline fun <T> disablingButtons(block: () -> T): T {
        for (child in buttonContainer.children.filterIsInstance<UIButton>()) {
            //println("DISABLE BUTTON: $child")
            child.enabled = false
        }
        try {
            return block()
        } finally {
            for (child in buttonContainer.children.filterIsInstance<UIButton>()) {
                //println("ENABLE BUTTON: $child")
                child.enabled = true
            }
        }
    }

    abstract class BaseDbScene : Scene() {
        val res get() = resourcesVfs
        val factory = KorgeDbFactory()
    }

    class HelloWorldScene : BaseDbScene() {
        val SCALE = 1.6
        override suspend fun SContainer.sceneInit() {
            //val skeDeferred = async { Json.parseFast(res["mecha_1002_101d_show/mecha_1002_101d_show_ske.json"].readString())!! }
            val skeDeferred = async { Buffer(res["mecha_1002_101d_show/mecha_1002_101d_show_ske.dbbin"].readBytes()) }
            val texDeferred = async { res["mecha_1002_101d_show/mecha_1002_101d_show_tex.json"].readString() }
            val imgDeferred = async { res["mecha_1002_101d_show/mecha_1002_101d_show_tex.png"].readBitmap().mipmaps() }

            val data = factory.parseDragonBonesData(skeDeferred.await())
            val atlas = factory.parseTextureAtlasData(Json.parseFast(texDeferred.await())!!, imgDeferred.await())

            val armatureDisplay = factory.buildArmatureDisplay("mecha_1002_101d")!!.position(0, 300).scale(SCALE)

            //armatureDisplay.animation.play("walk")
            println(armatureDisplay.animation.animationNames)
            //armatureDisplay.animation.play("jump")
            armatureDisplay.animation.play("idle")
            //scaleView(512, 512) {
            this += armatureDisplay
            //}
        }
    }

    class ClassicDragonScene : BaseDbScene() {
        override suspend fun SContainer.sceneInit() {
            //val scale = 0.3
            val scale = 0.8
            val ske = async { res["Dragon/Dragon_ske.json"].readString() }
            val tex = async { res["Dragon/Dragon_tex.json"].readString() }
            val img = async { res["Dragon/Dragon_tex.png"].readBitmap() }

            val data = factory.parseDragonBonesData(Json.parseFast(ske.await())!!)

            val atlas = factory.parseTextureAtlasData(
                Json.parseFast(tex.await())!!,
                img.await()
            )
            val armatureDisplay = factory.buildArmatureDisplay("Dragon", "Dragon")!!.position(0, 200).scale(scale)
            armatureDisplay.animation.play("walk")
            println(armatureDisplay.animation.animationNames)
            //armatureDisplay.animation.play("jump")
            //armatureDisplay.animation.play("fall")
            this += armatureDisplay
        }
    }

    // @TODO: Remove in next KorGE version
    val MouseEvents.exit by WeakPropertyThis<MouseEvents, Signal<MouseEvents>> {
        Signal()
    }

    class EyeTrackingScene : BaseDbScene() {
        val scale = 0.46
        var totalTime = 0.0

        override suspend fun SContainer.sceneInit() {
            try {
                println("EyeTrackingScene[0]")

                val _animationNames = listOf(
                    "PARAM_ANGLE_X", "PARAM_ANGLE_Y", "PARAM_ANGLE_Z",
                    "PARAM_EYE_BALL_X", "PARAM_EYE_BALL_Y",
                    "PARAM_BODY_X", "PARAM_BODY_Y", "PARAM_BODY_Z",
                    "PARAM_BODY_ANGLE_X", "PARAM_BODY_ANGLE_Y", "PARAM_BODY_ANGLE_Z",
                    "PARAM_BREATH"
                )

                val skeDeferred = async { res["shizuku/shizuku_ske.json"].readString() }
                val tex00Deferred = async { res["shizuku/shizuku.1024/texture_00.png"].readBitmap().mipmaps() }
                val tex01Deferred = async { res["shizuku/shizuku.1024/texture_01.png"].readBitmap().mipmaps() }
                val tex02Deferred = async { res["shizuku/shizuku.1024/texture_02.png"].readBitmap().mipmaps() }
                val tex03Deferred = async { res["shizuku/shizuku.1024/texture_03.png"].readBitmap().mipmaps() }

                println("EyeTrackingScene[1]")

                factory.parseDragonBonesData(
                    Json.parseFast(skeDeferred.await())!!,
                    "shizuku"
                )
                println("EyeTrackingScene[2]")
                factory.updateTextureAtlases(
                    arrayOf(
                        tex00Deferred.await(),
                        tex01Deferred.await(),
                        tex02Deferred.await(),
                        tex03Deferred.await()
                    ), "shizuku"
                )
                println("${tex00Deferred.await()}, premultiplied=${tex00Deferred.await().premultiplied}")
                println("EyeTrackingScene[3]")
                val armatureDisplay = factory.buildArmatureDisplay("shizuku", "shizuku")!!
                    .position(0, 300).scale(this@EyeTrackingScene.scale)
                this += armatureDisplay

                //println(armatureDisplay.animation.animationNames)
                println("EyeTrackingScene[4]")
                //armatureDisplay.play("idle_00")
                armatureDisplay.animation.play("idle_00")

                val target = MPoint()
                val ftarget = MPoint()

                mouse {
                    moveAnywhere {
                        ftarget.x = (localMousePos(views).x - armatureDisplay.x) / this@EyeTrackingScene.scale
                        ftarget.y = (localMousePos(views).y - armatureDisplay.y) / this@EyeTrackingScene.scale
                        //println(":" + localMouseXY(views) + ", " + target + " :: ${armatureDisplay.x}, ${armatureDisplay.y} :: ${this@EyeTrackingScene.scale}")
                    }
                    exit {
                        ftarget.x = armatureDisplay.x / this@EyeTrackingScene.scale
                        ftarget.y = (armatureDisplay.y - 650) / this@EyeTrackingScene.scale
                        //println(":onExit:" + " :: $target :: ${armatureDisplay.x}, ${armatureDisplay.y} :: ${this@EyeTrackingScene.scale}")
                    }
                }

                // This job will be automatically destroyed by the SceneContainer
                launchImmediately {
                    val bendRatio = 0.75
                    val ibendRatio = 1.0 - bendRatio
                    while (true) {
                        target.x = (target.x * bendRatio + ftarget.x * ibendRatio)
                        target.y = (target.y * bendRatio + ftarget.y * ibendRatio)
                        delay(16.milliseconds)
                    }
                }

                addUpdater {
                    totalTime += it.milliseconds

                    val armature = armatureDisplay.armature
                    val animation = armatureDisplay.animation
                    val canvas = armature.armatureData.canvas!!

                    var p = 0.0
                    val pX = max(min((target.x - canvas.x) / (canvas.width * 0.5), 1.0), -1.0)
                    val pY = -max(min((target.y - canvas.y) / (canvas.height * 0.5), 1.0), -1.0)
                    for (animationName in _animationNames) {
                        if (!animation.hasAnimation(animationName)) {
                            continue
                        }

                        var animationState = animation.getState(animationName, 1)
                        if (animationState == null) {
                            animationState = animation.fadeIn(animationName, 0.1, 1, 1, animationName)
                            if (animationState != null) {
                                animationState.resetToPose = false
                                animationState.stop()
                            }
                        }

                        if (animationState == null) {
                            continue
                        }

                        when (animationName) {
                            "PARAM_ANGLE_X", "PARAM_EYE_BALL_X" -> p = (pX + 1.0) * 0.5
                            "PARAM_ANGLE_Y", "PARAM_EYE_BALL_Y" -> p = (pY + 1.0) * 0.5
                            "PARAM_ANGLE_Z" -> p = (-pX * pY + 1.0) * 0.5
                            "PARAM_BODY_X", "PARAM_BODY_ANGLE_X" -> p = (pX + 1.0) * 0.5
                            "PARAM_BODY_Y", "PARAM_BODY_ANGLE_Y" -> p = (-pX * pY + 1.0) * 0.5
                            "PARAM_BODY_Z", "PARAM_BODY_ANGLE_Z" -> p = (-pX * pY + 1.0) * 0.5
                            "PARAM_BREATH" -> p = (sin(totalTime / 1000.0) + 1.0) * 0.5
                        }

                        animationState.currentTime = p * animationState.totalTime
                    }
                }
            } catch (e: Throwable) {
                e.printStackTrace()
            }
        }
    }

    class SkinChangingScene : BaseDbScene() {
        val SCALE = 0.42
        val random = Random(0)

        override suspend fun SContainer.sceneInit() {
            val suitConfigs = listOf(
                listOf(
                    "2010600a", "2010600a_1",
                    "20208003", "20208003_1", "20208003_2", "20208003_3",
                    "20405006",
                    "20509005",
                    "20703016", "20703016_1",
                    "2080100c",
                    "2080100e", "2080100e_1",
                    "20803005",
                    "2080500b", "2080500b_1"
                ),
                listOf(
                    "20106010",
                    "20106010_1",
                    "20208006",
                    "20208006_1",
                    "20208006_2",
                    "20208006_3",
                    "2040600b",
                    "2040600b_1",
                    "20509007",
                    "20703020",
                    "20703020_1",
                    "2080b003",
                    "20801015"
                )
            )

            val deferreds = arrayListOf<Deferred<*>>()

            deferreds += asyncImmediately {
                factory.parseDragonBonesData(
                    Json.parseFast(res["you_xin/body/body_ske.json"].readString())!!
                )
            }
            deferreds += asyncImmediately {
                val atlas = factory.parseTextureAtlasData(
                    Json.parseFast(res["you_xin/body/body_tex.json"].readString())!!,
                    res["you_xin/body/body_tex.png"].readBitmap().mipmaps()
                )
            }

            for ((i, suitConfig) in suitConfigs.withIndex()) {
                for (partArmatureName in suitConfig) {
                    // resource/you_xin/suit1/2010600a/xxxxxx
                    val path = "you_xin/" + "suit" + (i + 1) + "/" + partArmatureName + "/" + partArmatureName
                    val dragonBonesJSONPath = path + "_ske.json"
                    val textureAtlasJSONPath = path + "_tex.json"
                    val textureAtlasPath = path + "_tex.png"
                    //
                    deferreds += asyncImmediately {
                        factory.parseDragonBonesData(Json.parseFast(res[dragonBonesJSONPath].readString())!!)
                        factory.parseTextureAtlasData(
                            Json.parseFast(res[textureAtlasJSONPath].readString())!!,
                            res[textureAtlasPath].readBitmap().mipmaps()
                        )
                    }
                }
            }

            deferreds.awaitAll()

            val armatureDisplay = factory.buildArmatureDisplay("body")!!
                .position(0, 360).scale(SCALE)
            this += armatureDisplay

            println(armatureDisplay.animation.animationNames)
            //armatureDisplay.animation.play("idle_00")
            armatureDisplay.on(EventObject.LOOP_COMPLETE) {
                //println("LOOP!")
                // Random animation index.
                val nextAnimationName = random[armatureDisplay.animation.animationNames]
                armatureDisplay.animation.fadeIn(nextAnimationName, 0.3, 0)
            }
            armatureDisplay.animation.play("idle", 0)
            //armatureDisplay.animation.play("speak")

            for (part in suitConfigs[0]) {
                val partArmatureData = factory.getArmatureData(part)
                factory.replaceSkin(armatureDisplay.armature, partArmatureData!!.defaultSkin!!)
            }
            val _replaceSuitParts = arrayListOf<String>()
            var _replaceSuitIndex = 0

            mouse {
                onUpAnywhere {
                    // This suit has been replaced, next suit.
                    if (_replaceSuitParts.size == 0) {
                        _replaceSuitIndex++

                        if (_replaceSuitIndex >= suitConfigs.size) {
                            _replaceSuitIndex = 0
                        }

                        // Refill the unset parits.
                        for (partArmatureName in suitConfigs[_replaceSuitIndex]) {
                            _replaceSuitParts.add(partArmatureName)
                        }
                    }

                    // Random one part in this suit.
                    val partIndex: Int = floor(random.nextDouble() * _replaceSuitParts.size).toInt()
                    val partArmatureName = _replaceSuitParts[partIndex]
                    val partArmatureData = factory.getArmatureData(partArmatureName)
                    // Replace skin.
                    factory.replaceSkin(armatureDisplay.armature, partArmatureData!!.defaultSkin!!)
                    // Remove has been replaced
                    _replaceSuitParts.removeAt(partIndex)
                }
            }
        }
    }
}
