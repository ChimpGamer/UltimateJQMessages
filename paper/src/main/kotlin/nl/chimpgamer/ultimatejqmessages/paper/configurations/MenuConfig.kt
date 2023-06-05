package nl.chimpgamer.ultimatetags.configurations

import dev.dejvokep.boostedyaml.YamlDocument
import nl.chimpgamer.ultimatetags.UltimateTagsPlugin
import java.io.File

abstract class MenuConfig(plugin: UltimateTagsPlugin, val file: File) {
    var config: YamlDocument

    init {
        val inputStream = plugin.getResource("menus" + File.separator + file.name)
        config = if (inputStream != null) {
            YamlDocument.create(file, inputStream)
        } else {
            YamlDocument.create(file)
        }
    }
}