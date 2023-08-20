package nl.chimpgamer.ultimatejqmessages.paper.models

import java.util.UUID

data class User(
    val uuid: UUID,
    var playerName: String,
    var joinMessage: JoinQuitMessage?,
    var quitMessage: JoinQuitMessage?,
    var customJoinMessage: String?,
    var customQuitMessage: String?,
    var showJoinQuitMessages: Boolean
)
