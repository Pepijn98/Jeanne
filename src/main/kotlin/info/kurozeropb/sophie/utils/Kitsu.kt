package info.kurozeropb.sophie.utils

import com.github.kittinunf.fuel.core.ResponseDeserializable
import com.google.gson.Gson

// Probably worth making this into a dependency like I did for C#
object Kitsu {
    const val baseUrl = "https://kitsu.io/api/edge"

    // Anime deserialization
    data class Anime(
            val data: ArrayList<AnimeData>
    ) {
        class Deserializer : ResponseDeserializable<Anime> {
            override fun deserialize(content: String): Anime? = Gson().fromJson(content, Anime::class.java)
        }
    }

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
            val abbreviatedTitles: ArrayList<String>,
            val averageRating: String,
            val ratingFrequencies: AnimeRatingFrequencies,
            val userCount: Int,
            val favoritesCount: Int,
            val startDate: String?,
            val endDate: String?,
            val popularityRank: Int,
            val ratingRank: Int,
            val ageRating: String,
            val ageRatingGuide: String,
            val subtype: String,
            val status: String,
            val tba: String,
            val posterImage: AnimePosterImage,
            val coverImage: AnimeCoverImage,
            val episodeCount: Int,
            val episodeLength: Int,
            val youtubeVideoId: String,
            val showType: String,
            val nsfw: Boolean
    )

    data class AnimeTitles(
            val en: String,
            val en_jp: String,
            val ja_jp: String
    )

    data class AnimeRatingFrequencies(
            val `2`: String,
            val `3`: String,
            val `4`: String,
            val `5`: String,
            val `6`: String,
            val `7`: String,
            val `8`: String,
            val `9`: String,
            val `10`: String,
            val `11`: String,
            val `12`: String,
            val `13`: String,
            val `14`: String,
            val `15`: String,
            val `16`: String,
            val `17`: String,
            val `18`: String,
            val `19`: String,
            val `20`: String
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

    // Manga deserialization
    data class Manga(
            val data: ArrayList<MangaData>
    ) {
        class Deserializer : ResponseDeserializable<Manga> {
            override fun deserialize(content: String): Manga? = Gson().fromJson(content, Manga::class.java)
        }
    }

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
            val abbreviatedTitles: ArrayList<String>,
            val averageRating: String,
            val ratingFrequencies: MangaRatingFrequencies,
            val userCount: Int,
            val favoritesCount: Int,
            val startDate: String?,
            val endDate: String?,
            val popularityRank: Int,
            val ratingRank: Int,
            val ageRating: String,
            val ageRatingGuide: String,
            val subtype: String,
            val status: String,
            val tba: String,
            val posterImage: MangaPosterImage,
            val coverImage: MangaCoverImage,
            val chapterCount: Int,
            val volumeCount: Int,
            val serialization: String,
            val mangaType: String
    )

    data class MangaTitles(
            val en: String,
            val en_jp: String,
            val ja_jp: String
    )

    data class MangaRatingFrequencies(
            val `2`: String,
            val `3`: String,
            val `4`: String,
            val `5`: String,
            val `6`: String,
            val `7`: String,
            val `8`: String,
            val `9`: String,
            val `10`: String,
            val `11`: String,
            val `12`: String,
            val `13`: String,
            val `14`: String,
            val `15`: String,
            val `16`: String,
            val `17`: String,
            val `18`: String,
            val `19`: String,
            val `20`: String
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