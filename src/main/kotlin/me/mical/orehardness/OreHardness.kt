package me.mical.orehardness

import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.enchantments.Enchantment
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.Damageable
import org.bukkit.inventory.meta.ItemMeta
import taboolib.common.platform.Plugin
import taboolib.common.platform.ProxyCommandSender
import taboolib.common.platform.command.command
import taboolib.common.platform.event.SubscribeEvent
import taboolib.common5.Mirror
import taboolib.common5.mirrorNow
import taboolib.module.configuration.Config
import taboolib.module.configuration.Configuration
import taboolib.module.nms.getI18nName
import taboolib.platform.util.asLangText
import taboolib.platform.util.modifyMeta
import taboolib.platform.util.sendActionBar
import java.util.concurrent.ConcurrentHashMap

object OreHardness : Plugin() {

    @Config
    lateinit var conf: Configuration
        private set

    private val oreMap = ConcurrentHashMap<Material, Int>()

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
                val name = ItemStack(e.block.type).getI18nName(e.player)
                var hasEnchantment = false
                if (item.type.name.contains("PICKAXE")) {
                    item.modifyMeta<ItemMeta> {
                        this as Damageable
                        if (item.enchantments.containsKey(Enchantment.DURABILITY)) {
                            val level = item.getEnchantmentLevel(Enchantment.DURABILITY)
                            durability = (durability / level)
                            if (level > 1) {
                                hasEnchantment = true
                            }
                        }
                        if (item.type.maxDurability.toInt() - damage < durability) {
                            // FIXME: 这样有些简单粗暴, 我更希望的是有原版工具坏掉的动画.
                            e.player.playSound(e.player.location, Sound.ENTITY_ITEM_BREAK, 1f, 1f)
                            NMS.INSTANCE.sendAnimation(e.player)
                            item.amount = 0
                            // damage = item.type.maxDurability.toInt()
                            e.player.sendActionBar(e.player.asLangText("message-broken", name))
                            e.isCancelled = true
                        } else {
                            damage += durability
                            when (durability) {
                                in 1..3 -> e.player.sendActionBar(e.player.asLangText("message-1", name, durability) + if (hasEnchantment) e.player.asLangText("enchantment") else "")
                                in 4..6 -> e.player.sendActionBar(e.player.asLangText("message-2", name, durability) + if (hasEnchantment) e.player.asLangText("enchantment") else "")
                                else -> e.player.sendActionBar(e.player.asLangText("message-3", name, durability) + if (hasEnchantment) e.player.asLangText("enchantment") else "")
                            }
                        }
                    }
                }
            }
        }
    }
}