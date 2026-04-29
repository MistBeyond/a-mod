@file:Suppress("DEPRECATION")

package com.mistbeyond.examplemod.data.provider

import com.mistbeyond.examplemod.Ids
import com.mistbeyond.examplemod.block.Blocks
import com.mistbeyond.examplemod.integration.jade.ModJadeIds
import com.mistbeyond.examplemod.item.Items
import net.minecraft.data.PackOutput
import net.minecraft.resources.Identifier
import net.minecraft.util.Util
import net.minecraft.world.item.BlockItem
import net.neoforged.neoforge.common.data.LanguageProvider
import org.apache.commons.lang3.text.WordUtils

class ModLanguageProvider(output: PackOutput) : LanguageProvider(output, Ids.MODID, "en_us") {
    override fun addTranslations() {
        add("itemGroup.examplemod", "Example Tab")
        add("examplemod.overlaymessage.wrench.rotation_failed", "Rotation failed on %s axis")
        addGui()
        addConfig()

        // may crash
        addBlocksAndItems()
        addJadeIds()
    }

    private fun addBlocksAndItems() {
        for (holder in Blocks.BLOCKS.entries) {
            this.addBlock(holder, English.byId(holder.id, English::capitalize))
        }

        for (holder in Items.ITEMS.entries) {
            if (holder.get() !is BlockItem) {
                this.addItem(holder, English.byId(holder.id, English::capitalize))
            }
        }
    }

    private fun addJadeIds() {
        for (id in ModJadeIds.IDENTIFIERS) {
            add("config.jade.plugin_${id.toLanguageKey()}", English.byId(id, English::capitalize))
        }
    }

    private fun addConfig() {
        fun cfgKey(key: String): String = "${Ids.MODID}.configuration.$key"
        add(cfgKey("title"), "Example Mod Configs")
        add(cfgKey("section.examplemod.common.toml"), "Example Mod Configs")
        add(cfgKey("section.examplemod.common.toml.title"), "Example Mod Configs")
        add(cfgKey("items"), "Item List")
        add(cfgKey("logDirtBlock"), "Log Dirt Block")
        add(cfgKey("magicNumber"), "Magic Number")
        add(cfgKey("magicNumberIntroduction"), "Magic Number Text")
        add(cfgKey("allow"), "Magic Number Text")
        add(cfgKey("allowEnergyConversion"), "Allow Energy Conversion")
    }

    private fun addGui() {
        // gui.modid.xxxxxx
        fun guiKey(idPath: String) = Util.makeDescriptionId("gui", Ids.thisMod(idPath))
        addByKeyString(guiKey("category/crushing"))
        add(guiKey("energy/consumption"), "Consumption: %s")
        add(guiKey("energy/voltage"), "Voltage: %s (%s)")
        add(guiKey("energy/voltage_tier"), "Voltage Tier: %s")
        add(guiKey("energy/current"), "Current: %s")
        add(guiKey("energy/duration"), "Duration: %s")
        add(guiKey("energy/power"), "Power: %s")
        add(guiKey("energy/eu"), "%s / %s")
    }

    private fun addByKeyString(key: String) {
        add(key, English.byKey(key, English::capitalize))
    }

    object English {
        fun byId(id: Identifier, formatter: (String) -> String): String {
            return byKey(Util.makeDescriptionId("", id), formatter)
        }

        fun byKey(key: String, formatter: (String) -> String): String {
            val delimiter = key.lastIndexOf(".")
            return formatter(key.substring(delimiter + 1).replace("_", " "))
        }

        /**
         * The wrapper of [WordUtils.capitalizeFully]}
         * 
         */
        fun capitalize(string: String): String {
            return WordUtils.capitalizeFully(string)
        }
    }
}
