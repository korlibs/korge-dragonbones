package com.dragonbones.util

import com.soywiz.korim.bitmap.*
import com.soywiz.korma.geom.*

internal var MPoint.xf: Float
    get() = this.x.toFloat()
    set(value) {
        this.x = value.toDouble()
    }

internal var MPoint.yf: Float
    get() = this.y.toFloat()
    set(value) {
        this.y = value.toDouble()
    }

// http://pixijs.download/dev/docs/PIXI.Texture.html#Texture
internal fun BitmapSliceCompat(
    bmp: Bitmap,
    frame: MRectangle,
    orig: MRectangle,
    trim: MRectangle,
    rotated: Boolean,
    name: String = "unknown"
) = bmp.sliceWithSize(frame.toInt(), name, if (rotated) ImageOrientation.ROTATE_90 else ImageOrientation.ORIGINAL)

fun <T : Bitmap> T.sliceWithSize(
    rect: MRectangleInt,
    name: String? = null,
    imageOrientation: ImageOrientation = ImageOrientation.ORIGINAL
): BitmapSlice<T> = slice(MRectangleInt(rect.x, rect.y, rect.width, rect.height), name, imageOrientation)
