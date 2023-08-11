package nl.chimpgamer.ultimatejqmessages.paper.models

data class JoinQuitMessage(
    val id: Int?,
    val name: String,
    val type: JoinQuitMessageType,
    val message: String
)

enum class JoinQuitMessageType {
    JOIN,
    QUIT
}