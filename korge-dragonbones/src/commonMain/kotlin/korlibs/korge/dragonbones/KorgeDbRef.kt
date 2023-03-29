package korlibs.korge.dragonbones

import korlibs.korge.render.*
import korlibs.korge.view.*
import korlibs.io.file.*

class KorgeDbRef() : Container(), ViewLeaf, ViewFileRef by ViewFileRef.Mixin() {
    override suspend fun forceLoadSourceFile(views: Views, currentVfs: VfsFile, sourceFile: String?) {
        baseForceLoadSourceFile(views, currentVfs, sourceFile)
        removeChildren()
        val view = currentVfs["$sourceFile"].readDbSkeletonAndAtlas(views.dragonbonsFactory).buildFirstArmatureDisplay(views.dragonbonsFactory)
        if (view != null) {
            addChild(view)
        }
    }

    override fun renderInternal(ctx: RenderContext) {
        this.lazyLoadRenderInternal(ctx, this)
        super.renderInternal(ctx)
    }

    //var sourceFileDb: VfsFile?
    //    get() = sourceFile
    //    set(value) {
    //
    //    }

    //override fun buildDebugComponent(views: Views, container: UiContainer) {
    //    container.uiCollapsibleSection("DragonBones") {
    //        uiEditableValue(::sourceFile, kind = UiTextEditableValue.Kind.FILE(views.currentVfs) {
    //            it.extensionLC == "dbbin" || it.baseName.endsWith("_ske.json")
    //        })
    //    }
    //    super.buildDebugComponent(views, container)
    //}
}
