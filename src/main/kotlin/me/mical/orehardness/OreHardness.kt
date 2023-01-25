package me.mical.orehardness

import dev.lone.itemsadder.api.CustomStack
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.Damageable
import taboolib.common.platform.Plugin
import taboolib.common.platform.ProxyCommandSender
import taboolib.common.platform.command.command
import taboolib.common.platform.event.SubscribeEvent
import taboolib.common5.Mirror
import taboolib.common5.mirrorNow
import taboolib.module.chat.colored
import taboolib.module.configuration.Config
import taboolib.module.configuration.Configuration
import taboolib.module.nms.getI18nName
import taboolib.platform.util.modifyMeta
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

        command("orehardness") {
            literal("report") {
                execute<ProxyCommandSender> { user, _, _ ->
                    Mirror.report(user)
                }
            }
        }
    }

    @SubscribeEvent
    fun e(e: BlockBreakEvent) {
        mirrorNow("OreHardness:Handler:BlockBreak") {
            if (oreMap.containsKey(e.block.type)) {
                var durability = oreMap[e.block.type] ?: return@mirrorNow
                val item = e.player.equipment?.itemInMainHand ?: return@mirrorNow
                val name = ItemStack(e.block.type).getI18nName()
                var hasEnchantment = false
                val customStack = CustomStack.byItemStack(item)
                if (customStack != null) {
                    if (customStack.id.lowercase().contains("pickaxe")) {
                        val view = durability
                        if (item.enchantments.containsKey(Enchantment.DURABILITY)) {
                            val level = item.getEnchantmentLevel(Enchantment.DURABILITY)
                            durability = (durability / level)
                            if (level > 1) {
                                hasEnchantment = true
                            }
                        }
                        if (customStack.durability < durability) {
                            // FIXME: 这样有些简单粗暴, 我更希望的是有原版工具坏掉的动画.
                            item.amount = 0
                            e.player.sendActionBar("&e你尝试开采 &b$name, &e但你的工具报废了也没能挖掉该方块...".colored())
                            e.isCancelled = true
                        } else {
                            durability -= 1
                            customStack.durability -= durability
                            when (view) {
                                in 1..3 -> e.player.sendActionBar("&e你开采的 &b$name &e矿物硬度适中, 消耗了你${view}格耐久. ${if (hasEnchantment) "&b(耐久减免)" else ""}".colored())
                                in 4..7 -> e.player.sendActionBar("&e你开采的 &b$name &e矿物稍微有点硬, 消耗了你${view}格耐久. ${if (hasEnchantment) "&b(耐久减免)" else ""}".colored())
                                else -> e.player.sendActionBar("&e你开采的 &b$name &e矿物硬度过大! 消耗了你${view}格耐久. ${if (hasEnchantment) "&b(耐久减免)" else ""}".colored())
                            }
                        }
                    }
                } else {
                    if (item.type.name.contains("PICKAXE")) {
                        item.modifyMeta<Damageable> {
                            if (item.enchantments.containsKey(Enchantment.DURABILITY)) {
                                val level = item.getEnchantmentLevel(Enchantment.DURABILITY)
                                durability = (durability / level)
                                if (level > 1) {
                                    hasEnchantment = true
                                }
                            }
                            if (item.type.maxDurability.toInt() - damage < durability) {
                                // FIXME: 这样有些简单粗暴, 我更希望的是有原版工具坏掉的动画.
                                item.amount = 0
                                e.player.sendActionBar("&e你尝试开采 &b$name, &e但你的工具报废了也没能挖掉该方块...".colored())
                                e.isCancelled = true
                            } else {
                                damage += durability
                                when (durability) {
                                    in 1..3 -> e.player.sendActionBar("&e你开采的 &b$name &e矿物硬度适中, 消耗了你${durability}格耐久. ${if (hasEnchantment) "&b(耐久减免)" else ""}".colored())
                                    in 4..6 -> e.player.sendActionBar("&e你开采的 &b$name &e矿物稍微有点硬, 消耗了你${durability}格耐久. ${if (hasEnchantment) "&b(耐久减免)" else ""}".colored())
                                    else -> e.player.sendActionBar("&e你开采的 &b$name &e矿物硬度过大! 消耗了你${durability}格耐久. ${if (hasEnchantment) "&b(耐久减免)" else ""}".colored())
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}