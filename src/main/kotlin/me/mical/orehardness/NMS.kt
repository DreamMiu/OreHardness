package me.mical.orehardness

import org.bukkit.entity.Player
import taboolib.module.nms.nmsProxy

/**
 * OreHardness
 * me.mical.orehardness.NMS
 *
 * @author Mical
 * @since 2023/7/20 14:36
 */
abstract class NMS {

    abstract fun sendAnimation(player: Player)

    companion object {

        val INSTANCE by lazy {
            nmsProxy<NMS>()
        }
    }
}