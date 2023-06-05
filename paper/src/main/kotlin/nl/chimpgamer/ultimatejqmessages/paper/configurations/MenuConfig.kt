package nl.chimpgamer.ultimatejqmessages.paper.configurations

import dev.dejvokep.boostedyaml.YamlDocument
import nl.chimpgamer.ultimatejqmessages.paper.UltimateJQMessagesPlugin
import java.io.File

abstract class MenuConfig(plugin: UltimateJQMessagesPlugin, val file: File) {
    var config: YamlDocument

    init {
        val inputStream = plugin.getResource("menus/" + file.name)
        config = if (inputStream != null) {
            YamlDocument.create(file, inputStream)
        } else {
            YamlDocument.create(file)
        }
    }
}