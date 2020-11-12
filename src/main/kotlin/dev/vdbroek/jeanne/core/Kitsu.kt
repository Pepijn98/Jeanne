package dev.vdbroek.jeanne.core

// Probably worth making this into a dependency like I did for C#
object Kitsu {
    const val baseUrl = "https://kitsu.io/api/edge"

    data class Anime(val data: ArrayList<AnimeData>)
    data class AnimeData(val attributes: AnimeAttributes)
    data class AnimeTitles(val en: String? = null, val en_jp: String? = null, val ja_jp: String? = null)
    data class AnimePosterImage(val tiny: String, val small: String, val medium: String, val large: String, val original: String)
    data class AnimeAttributes(
        val synopsis: String,
        val titles: AnimeTitles,
        val averageRating: String? = null,
        val favoritesCount: Int,
        val startDate: String? = null,
        val endDate: String? = null,
        val ratingRank: Int? = null,
        val status: String,
        val posterImage: AnimePosterImage? = null,
        val episodeCount: Int? = null,
        val showType: String
    )

    data class Manga(val data: ArrayList<MangaData>)
    data class MangaData(val attributes: MangaAttributes)
    data class MangaTitles(val en: String? = null, val en_jp: String? = null, val ja_jp: String? = null)
    data class MangaPosterImage(val tiny: String, val small: String, val medium: String, val large: String, val original: String)
    data class MangaAttributes(
        val synopsis: String,
        val titles: MangaTitles,
        val averageRating: String? = null,
        val favoritesCount: Int,
        val startDate: String? = null,
        val endDate: String? = null,
        val ratingRank: Int? = null,
        val status: String,
        val posterImage: MangaPosterImage? = null,
        val chapterCount: Int? = null,
        val volumeCount: Int? = null,
        val mangaType: String
    )
}