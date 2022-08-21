package me.mical.orehardness

import org.bukkit.Material
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.Damageable
import taboolib.common.platform.Plugin
import taboolib.common.platform.event.SubscribeEvent
import taboolib.module.configuration.Config
import taboolib.module.configuration.Configuration
import taboolib.module.nms.getI18nName
import taboolib.platform.util.sendActionBar

object OreHardness : Plugin() {

    @Config
    lateinit var conf: Configuration

    private val oreMap = hashMapOf<Material, Int>()

    override fun onEnable() {
        conf.getKeys(false).forEach {
            if (Material.getMaterial(it) != null) {
                oreMap.putIfAbsent(Material.getMaterial(it)!!, conf.getInt(it, 1))
            }
        }
    }

    @SubscribeEvent
    fun e(e: BlockBreakEvent) {
        if (oreMap.containsKey(e.block.type)) {
            val durability = oreMap[e.block.type] ?: return
            val item = e.player.itemInUse ?: return
            val meta = item.itemMeta!!
            val name = ItemStack(e.block.type).getI18nName()
            if (meta is Damageable) {
                if (item.type.maxDurability.toInt() - meta.damage < durability) {
                    meta.damage = item.type.maxDurability.toInt()
                    item.itemMeta = meta
                    e.player.sendActionBar("&e你尝试开采 &b$name, 但你的工具报废了也没能挖掉该方块...")
                    e.isCancelled = true
                } else {
                    meta.damage = meta.damage + durability
                    item.itemMeta = meta
                    if (durability in (1..3)) {
                        e.player.sendActionBar("&e你开采的 &b$name &e矿物硬度适中, 消耗了你${durability}格耐久.")
                    } else if (durability in 4..7) {
                        e.player.sendActionBar("&e你开采的 &b$name &e矿物稍微有点硬, 消耗了你${durability}格耐久.")
                    } else {
                        e.player.sendActionBar("&e你开采的 &b$name &e矿物硬度过大! 消耗了你${durability}格耐久.")
                    }
                }
            }
        }
    }
}