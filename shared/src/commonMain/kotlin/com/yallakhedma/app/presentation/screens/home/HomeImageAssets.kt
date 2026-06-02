package com.yallakhedma.app.presentation.screens.home

/**
 * Single source of truth for the home screen's static Firebase Storage images
 * (banner + category icons). The splash screen preloads exactly these URLs into
 * Coil's cache, and the home screen renders the same URLs — the strings must
 * match byte-for-byte so the cache keys line up and the images appear instantly.
 */
object HomeImageAssets {
    private const val BUCKET = "yallakhedma-1b576.firebasestorage.app"
    private const val BASE =
        "https://firebasestorage.googleapis.com/v0/b/$BUCKET/o/"

    /**
     * Cache-busting version. Because the banner/category files keep the same
     * Storage path when you re-upload them, their URL never changes and Coil
     * keeps serving the cached (old) image. Bump this number every time you
     * replace any banner/category image so the URL changes and Coil fetches
     * the new one. Firebase ignores the extra `v` query param server-side.
     */
    private const val ASSET_VERSION = 2

    fun storageUrl(path: String): String =
        BASE + path.trim().replace("/", "%2F") + "?alt=media&v=$ASSET_VERSION"

    /**
     * Resolves an image reference that may be EITHER a full http(s) download
     * URL (e.g. pasted from the Storage console) OR a plain Storage path like
     * "categories/plumbing.png". Returns null for blank input.
     */
    fun imageUrl(reference: String?): String? {
        val ref = reference?.trim().orEmpty()
        if (ref.isEmpty()) return null
        return if (ref.startsWith("http")) ref else storageUrl(ref)
    }

    const val BANNER = "banner/hero.jpg"
    const val CAT_PLUMBING = "categories/plumbing.png"
    const val CAT_ELECTRICAL = "categories/electrical.png"
    const val CAT_TUTORING = "categories/tutoring.png"
    const val CAT_DESIGN = "categories/design.png"
    const val CAT_CLEANING = "categories/cleaning.png"
    const val CAT_MORE = "categories/more.png"

    /** Every static path, used by the splash to warm the cache up front. */
    val allStaticPaths: List<String> = listOf(
        BANNER, CAT_PLUMBING, CAT_ELECTRICAL, CAT_TUTORING,
        CAT_DESIGN, CAT_CLEANING, CAT_MORE,
    )

    /** Resolved URLs for the splash preloader. */
    val preloadUrls: List<String> = allStaticPaths.map { storageUrl(it) }
}
