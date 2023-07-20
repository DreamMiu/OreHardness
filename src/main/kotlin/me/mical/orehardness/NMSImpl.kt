package me.mical.orehardness

import net.minecraft.network.protocol.game.PacketPlayOutEntityStatus
import org.bukkit.craftbukkit.v1_19_R1.entity.CraftPlayer
import org.bukkit.entity.Player
import taboolib.module.nms.MinecraftVersion
import taboolib.module.nms.dataSerializerBuilder
import taboolib.module.nms.sendPacket

/**
 * OreHardness
 * me.mical.orehardness.NMSImpl
 *
 * @author Mical
 * @since 2023/7/20 14:37
 */
class NMSImpl : NMS() {

    override fun sendAnimation(player: Player) {
        if (MinecraftVersion.isUniversal) {
            player.sendPacket(NMSPacketPlayOutEntityStatus(dataSerializerBuilder {
                writeInt((player as CraftPlayer19).entityId)
                writeByte(47)
            }.build() as NMSPacketDataSerializer))
        } else {
            player.sendPacket(NMS16PacketPlayOutEntityStatus().also {
                it.a(dataSerializerBuilder {
                    writeInt((player as CraftPlayer16).entityId)
                    writeByte(47)
                }.build() as NMS16PacketDataSerializer)
            })
        }
    }
}