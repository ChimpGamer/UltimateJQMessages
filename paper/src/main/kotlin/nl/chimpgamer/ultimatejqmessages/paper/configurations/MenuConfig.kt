package nl.chimpgamer.ultimatejqmessages.paper.configurations

import dev.dejvokep.boostedyaml.YamlDocument
import dev.dejvokep.boostedyaml.settings.dumper.DumperSettings
import dev.dejvokep.boostedyaml.settings.general.GeneralSettings
import dev.dejvokep.boostedyaml.settings.loader.LoaderSettings
import dev.dejvokep.boostedyaml.settings.updater.UpdaterSettings
import nl.chimpgamer.ultimatejqmessages.paper.UltimateJQMessagesPlugin
import java.io.File

abstract class MenuConfig(plugin: UltimateJQMessagesPlugin, val file: File) {
    var config: YamlDocument

    init {
        val inputStream = plugin.getResource("menus/" + file.name)
        val generalSettings = GeneralSettings.builder()
            .setDefaultString(null)
            .setDefaultObject(null)
            .setKeyFormat(GeneralSettings.KeyFormat.STRING)
            .setUseDefaults(false)
            .build()

        config = if (inputStream != null) {
            YamlDocument.create(file, inputStream, generalSettings, LoaderSettings.DEFAULT, DumperSettings.DEFAULT, UpdaterSettings.DEFAULT)
        } else {
            YamlDocument.create(file, generalSettings, LoaderSettings.DEFAULT, DumperSettings.DEFAULT, UpdaterSettings.DEFAULT)
        }
    }
}