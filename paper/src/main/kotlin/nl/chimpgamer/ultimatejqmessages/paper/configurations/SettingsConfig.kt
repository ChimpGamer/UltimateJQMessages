package nl.chimpgamer.ultimatejqmessages.paper.configurations

import dev.dejvokep.boostedyaml.YamlDocument
import dev.dejvokep.boostedyaml.dvs.versioning.BasicVersioning
import dev.dejvokep.boostedyaml.settings.dumper.DumperSettings
import dev.dejvokep.boostedyaml.settings.general.GeneralSettings
import dev.dejvokep.boostedyaml.settings.loader.LoaderSettings
import dev.dejvokep.boostedyaml.settings.updater.UpdaterSettings
import nl.chimpgamer.ultimatejqmessages.paper.UltimateJQMessagesPlugin
import nl.chimpgamer.ultimatejqmessages.paper.models.JoinQuitMessageType

class SettingsConfig(plugin: UltimateJQMessagesPlugin) {
    val config: YamlDocument

    val storageType: String get() = config.getString("storage.type", "sqlite")
    val storageHost: String get() = config.getString("storage.host", "localhost")
    val storagePort: Int get() = config.getInt("storage.port", 3306)
    val storageDatabase: String get() = config.getString("storage.database", "ultimatemobcoins")
    val storageUsername: String get() = config.getString("storage.username", "ultimatemobcoins")
    val storagePassword: String get() = config.getString("storage.password", "ultimatemobcoins")
    val storageProperties: Map<String, String> get() = config.getSection("storage.properties").getStringRouteMappedValues(false).mapValues { it.value.toString() }

    val joinMessagesDefaultMessageNewPlayersOnly: Boolean get() = config.getBoolean("join_messages.default-message.new-players-only")
    val joinMessagesDefaultMessageName: String get() = config.getString("join_messages.default-message.name")
    val joinMessagesCustomMaxLength: Int get() = config.getInt("join_messages.custom.max_length")
    val joinMessagesCooldown: Long get() = config.getLong("join_messages.cooldown")
    val joinMessagesDelay: Long get() = config.getLong("join_messages.delay")
    val joinMessageDisplayNameFormat: String get() = config.getString("join_messages.display-name-format")
    val joinMessagesPrefix: String get() = config.getString("join_messages.prefix")
    val joinMessagesCommandName: String get() = config.getString("join_messages.command.name")
    val joinMessagesCommandAliases: List<String> get() = config.getStringList("join_messages.command.name")

    val quitMessagesDefaultMessageNewPlayersOnly: Boolean get() = config.getBoolean("quit_messages.default-message.new-players-only")
    val quitMessagesDefaultMessageName: String get() = config.getString("quit_messages.default-message.name")
    val quitMessagesCustomMaxLength: Int get() = config.getInt("quit_messages.custom.max_length")
    val quitMessagesCooldown: Long get() = config.getLong("quit_messages.cooldown")
    val quitMessagesDelay: Long get() = config.getLong("quit_messages.delay")
    val quitMessageDisplayNameFormat: String get() = config.getString("quit_messages.display-name-format")
    val quitMessagesPrefix: String get() = config.getString("quit_messages.prefix")
    val quitMessagesCommandName: String get() = config.getString("quit_messages.command.name")
    val quitMessagesCommandAliases: List<String> get() = config.getStringList("quit_messages.command.name")

    fun displayNameFormat(joinQuitMessageType: JoinQuitMessageType): String = if (joinQuitMessageType.isJoin()) joinMessageDisplayNameFormat else quitMessageDisplayNameFormat

    init {
        val file = plugin.dataFolder.resolve("settings.yml")
        val inputStream = plugin.getResource("settings.yml")
        val loaderSettings = LoaderSettings.builder().setAutoUpdate(true).build()
        val updaterSettings = UpdaterSettings.builder().setVersioning(BasicVersioning("config-version")).build()
        config = if (inputStream != null) {
            YamlDocument.create(file, inputStream, GeneralSettings.DEFAULT, loaderSettings, DumperSettings.DEFAULT, updaterSettings)
        } else {
            YamlDocument.create(file, GeneralSettings.DEFAULT, loaderSettings, DumperSettings.DEFAULT, updaterSettings)
        }
    }
}