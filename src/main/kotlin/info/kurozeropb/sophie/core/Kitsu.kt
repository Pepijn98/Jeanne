package info.kurozeropb.sophie.core

// Probably worth making this into a dependency like I did for C#
object Kitsu {
    const val baseUrl = "https://kitsu.io/api/edge"

    data class Anime(
            val data: ArrayList<AnimeData>
    )

    data class AnimeData(
            val id: String,
            val type: String,
            val links: AnimeLinks,
            val attributes: AnimeAttributes
    )

    data class AnimeLinks(
            val self: String
    )

    data class AnimeAttributes(
            val createdAt: String,
            val updatedAt: String,
            val slug: String,
            val synopsis: String,
            val coverImageTopOffset: Int,
            val titles: AnimeTitles,
            val canonicalTitle: String,
            val abbreviatedTitles: ArrayList<String>? = null,
            val averageRating: String? = null,
            val ratingFrequencies: AnimeRatingFrequencies,
            val userCount: Int,
            val favoritesCount: Int,
            val startDate: String? = null,
            val endDate: String? = null,
            val popularityRank: Int,
            val ratingRank: Int? = null,
            val ageRating: String? = null,
            val ageRatingGuide: String? = null,
            val subtype: String,
            val status: String,
            val tba: String? = null,
            val posterImage: AnimePosterImage,
            val coverImage: AnimeCoverImage? = null,
            val episodeCount: Int? = null,
            val episodeLength: Int? = null,
            val youtubeVideoId: String? = null,
            val showType: String,
            val nsfw: Boolean
    )

    data class AnimeTitles(
            val en: String? = null,
            val en_jp: String,
            val ja_jp: String? = null
    )

    data class AnimeRatingFrequencies(
            val `2`: String = "0",
            val `3`: String = "0",
            val `4`: String = "0",
            val `5`: String = "0",
            val `6`: String = "0",
            val `7`: String = "0",
            val `8`: String = "0",
            val `9`: String = "0",
            val `10`: String = "0",
            val `11`: String = "0",
            val `12`: String = "0",
            val `13`: String = "0",
            val `14`: String = "0",
            val `15`: String = "0",
            val `16`: String = "0",
            val `17`: String = "0",
            val `18`: String = "0",
            val `19`: String = "0",
            val `20`: String = "0"
    )

    data class AnimePosterImage(
            val tiny: String,
            val small: String,
            val medium: String,
            val large: String,
            val original: String
    )

    data class AnimeCoverImage(
            val tiny: String,
            val small: String,
            val large: String,
            val original: String
    )

    data class Manga(
            val data: ArrayList<MangaData>
    )

    data class MangaData(
            val id: String,
            val type: String,
            val links: MangaLinks,
            val attributes: MangaAttributes
    )

    data class MangaLinks(
            val self: String
    )

    data class MangaAttributes(
            val createdAt: String,
            val updatedAt: String,
            val slug: String,
            val synopsis: String,
            val coverImageTopOffset: Int,
            val titles: MangaTitles,
            val canonicalTitle: String,
            val abbreviatedTitles: ArrayList<String>? = null,
            val averageRating: String? = null,
            val ratingFrequencies: MangaRatingFrequencies,
            val userCount: Int,
            val favoritesCount: Int,
            val startDate: String? = null,
            val endDate: String? = null,
            val popularityRank: Int,
            val ratingRank: Int? = null,
            val ageRating: String? = null,
            val ageRatingGuide: String? = null,
            val subtype: String,
            val status: String,
            val tba: String? = null,
            val posterImage: MangaPosterImage,
            val coverImage: MangaCoverImage? = null,
            val chapterCount: Int? = null,
            val volumeCount: Int? = null,
            val serialization: String,
            val mangaType: String
    )

    data class MangaTitles(
            val en: String? = null,
            val en_jp: String,
            val ja_jp: String? = null
    )

    data class MangaRatingFrequencies(
            val `2`: String = "0",
            val `3`: String = "0",
            val `4`: String = "0",
            val `5`: String = "0",
            val `6`: String = "0",
            val `7`: String = "0",
            val `8`: String = "0",
            val `9`: String = "0",
            val `10`: String = "0",
            val `11`: String = "0",
            val `12`: String = "0",
            val `13`: String = "0",
            val `14`: String = "0",
            val `15`: String = "0",
            val `16`: String = "0",
            val `17`: String = "0",
            val `18`: String = "0",
            val `19`: String = "0",
            val `20`: String = "0"
    )

    data class MangaPosterImage(
            val tiny: String,
            val small: String,
            val medium: String,
            val large: String,
            val original: String
    )

    data class MangaCoverImage(
            val tiny: String,
            val small: String,
            val large: String,
            val original: String
    )
}