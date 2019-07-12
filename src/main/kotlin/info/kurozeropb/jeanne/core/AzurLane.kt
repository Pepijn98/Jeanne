package info.kurozeropb.jeanne.core

object AzurLane {
    const val baseUrl = "https://api.kurozeropb.info/v1/azurlane"

    data class ShipNames(val en: String? = null, val cn: String? = null, val jp: String? = null, val kr: String? = null)
    data class ShipSkin(val title: String? = null, val image: String? = null)
    data class ShipStars(val value: String? = null, val count: Int)
    data class Ship(
            val wikiUrl: String,
            val id: String? = null,
            val names: ShipNames,
            val thumbnail: String,
            val chibi: String,
            val skins: List<ShipSkin>,
            val buildTime: String? = null,
            val rarity: String,
            val stars: ShipStars,
            val `class`: String? = null,
            val nationality: String? = null,
            val nationalityShort: String? = null,
            val hullType: String? = null
    )

    data class ShipResponse(
            val statusCode: Int,
            val statusMessage: String,
            val ship: Ship
    )

    // data class Construction()
}