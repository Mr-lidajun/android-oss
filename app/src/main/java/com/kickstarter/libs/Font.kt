package com.kickstarter.libs

import android.content.res.AssetManager
import android.graphics.Typeface
import java.lang.RuntimeException

interface Font {
    fun maisonNeueBookTypeface(): Typeface
    fun materialIconsTypeface(): Typeface
    fun sansSerifLightTypeface(): Typeface
    fun sansSerifTypeface(): Typeface
    fun ssKickstarterTypeface(): Typeface?
}
class FontImpl(assetManager: AssetManager) : Font {
    private val maisonNeueBookTypeface: Typeface
    private val materialIconsTypeface: Typeface
    private val sansSerifLightTypeface: Typeface
    private val sansSerifTypeface: Typeface
    private var ssKickstarterTypeface: Typeface? = null

    override fun maisonNeueBookTypeface(): Typeface {
        return maisonNeueBookTypeface
    }

    override fun materialIconsTypeface(): Typeface {
        return materialIconsTypeface
    }

    override fun sansSerifLightTypeface(): Typeface {
        return sansSerifLightTypeface
    }

    override fun sansSerifTypeface(): Typeface {
        return sansSerifTypeface
    }

    override fun ssKickstarterTypeface(): Typeface? {
        return ssKickstarterTypeface
    }

    init {
        maisonNeueBookTypeface =
            Typeface.createFromAsset(assetManager, "fonts/maison-neue-book.ttf")
        materialIconsTypeface =
            Typeface.createFromAsset(assetManager, "fonts/MaterialIcons-Regular.ttf")
        sansSerifLightTypeface = Typeface.create("sans-serif-light", Typeface.NORMAL)
        sansSerifTypeface = Typeface.create("sans-serif", Typeface.NORMAL)
        try {
            ssKickstarterTypeface =
                Typeface.createFromAsset(assetManager, "fonts/ss-kickstarter.otf")
        } catch (e: RuntimeException) {
            ssKickstarterTypeface = materialIconsTypeface
        }
    }
}
