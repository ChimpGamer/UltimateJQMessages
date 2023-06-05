package nl.chimpgamer.ultimatejqmessages.paper.configurations

import dev.dejvokep.boostedyaml.YamlDocument
import dev.dejvokep.boostedyaml.dvs.versioning.BasicVersioning
import dev.dejvokep.boostedyaml.settings.dumper.DumperSettings
import dev.dejvokep.boostedyaml.settings.general.GeneralSettings
import dev.dejvokep.boostedyaml.settings.loader.LoaderSettings
import dev.dejvokep.boostedyaml.settings.updater.UpdaterSettings
import nl.chimpgamer.ultimatejqmessages.paper.UltimateJQMessagesPlugin

class MessagesConfig(plugin: UltimateJQMessagesPlugin) {
    val config: YamlDocument

    val joinMessageReset: String get() = config.getString("join_message.reset")
    val joinMessageResetOther: String get() = config.getString("join_message.reset_other")
    val joinMessageSet: String get() = config.getString("join_message.set")
    val joinMessageCreateMissingPlaceholder: String get() = config.getString("join_message.create.missing_placeholder")

    val joinMessageCreateCustomChat: String get() = config.getString("join_message.create.custom.chat")
    val joinMessageCreateCustomTitle: String get() = config.getString("join_message.create.custom.title")
    val joinMessageCreateCustomSetChat: String get() = config.getString("join_message.create.custom_set.chat")
    val joinMessageCreateCustomSetTitle: String get() = config.getString("join_message.create.custom_set.title")
    val joinMessagesCreateCustomTooLong: String get() = config.getString("join_message.create.custom_too_long")

    val quitMessageReset: String get() = config.getString("quit_message.reset")
    val quitMessageResetOther: String get() = config.getString("quit_message.reset_other")
    val quitMessageSet: String get() = config.getString("quit_message.set")
    val quitMessageCreateMissingPlaceholder: String get() = config.getString("quit_message.create.missing_placeholder")

    val quitMessageCreateCustomChat: String get() = config.getString("quit_message.create.custom.chat")
    val quitMessageCreateCustomTitle: String get() = config.getString("quit_message.create.custom.title")
    val quitMessageCreateCustomSetChat: String get() = config.getString("quit_message.create.custom_set.chat")
    val quitMessageCreateCustomSetTitle: String get() = config.getString("quit_message.create.custom_set.title")
    val quitMessagesCreateCustomTooLong: String get() = config.getString("quit_message.create.custom_too_long")

    val noPermission: String get() = config.getString("noPermission")

    init {
        val file = plugin.dataFolder.resolve("messages.yml")
        val inputStream = plugin.getResource("messages.yml")
        val loaderSettings = LoaderSettings.builder().setAutoUpdate(true).build()
        val updaterSettings = UpdaterSettings.builder().setVersioning(BasicVersioning("config-version")).build()
        config = if (inputStream != null) {
            YamlDocument.create(file, inputStream, GeneralSettings.DEFAULT, loaderSettings, DumperSettings.DEFAULT, updaterSettings)
        } else {
            YamlDocument.create(file, GeneralSettings.DEFAULT, loaderSettings, DumperSettings.DEFAULT, updaterSettings)
        }
    }
}