package nl.chimpgamer.ultimatejqmessages.paper.models

import java.util.UUID

data class User(
    val uuid: UUID,
    val playerName: String,
    val joinMessage: JoinQuitMessage?,
    val quitMessage: JoinQuitMessage?,
    val customJoinMessage: String?,
    val customQuitMessage: String?,
    val showJoinQuitMessages: Boolean
)
