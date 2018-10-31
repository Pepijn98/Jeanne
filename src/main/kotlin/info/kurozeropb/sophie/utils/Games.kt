package info.kurozeropb.sophie.utils

import info.kurozeropb.sophie.PlayingGame
import net.dv8tion.jda.core.entities.Game

object Games {
    val list = listOf(
            PlayingGame("with Senpai", Game.GameType.DEFAULT),
            PlayingGame("with my master", Game.GameType.DEFAULT),
            PlayingGame("anime", Game.GameType.WATCHING),
            PlayingGame("secret things", Game.GameType.WATCHING),
            PlayingGame("with your feelings", Game.GameType.DEFAULT),
            PlayingGame("https://sophiebot.info", Game.GameType.WATCHING),
            PlayingGame("with %USERSIZE% users", Game.GameType.DEFAULT),
            PlayingGame("in %GUILDSIZE% servers", Game.GameType.DEFAULT),
            PlayingGame("%GUILDSIZE% servers", Game.GameType.WATCHING),
            PlayingGame("%USERSIZE% users", Game.GameType.WATCHING),
            PlayingGame("music", Game.GameType.LISTENING)
    )
}