package net.okocraft.noeasyplace;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Waterlogged;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;

class PlayerListener implements Listener {

    private static final Component ERROR_MESSAGE = Component.text("easyplacemodeでのブロック設置は利用できません。", NamedTextColor.RED);

    private final Map<UUID, BreakHistory> broken = new ConcurrentHashMap<>();

    @EventHandler
    private void onPlayerQuit(PlayerQuitEvent event) {
        broken.remove(event.getPlayer().getUniqueId());
    }

    @EventHandler
    private void onBlockBreak(@NotNull BlockBreakEvent event) {
        broken.put(
                event.getPlayer().getUniqueId(),
                new BreakHistory(System.currentTimeMillis(), event.getBlock().getLocation())
        );
    }

    @EventHandler(ignoreCancelled = true)
    private void onBlockPlace(@NotNull BlockPlaceEvent event) {
        // BE cannot use easyplacemode
        if (event.getPlayer().getUniqueId().getMostSignificantBits() == 0L) {
            return;
        }

        // player may use easyplacemode when block and against-block are same
        if (!event.getBlock().equals(event.getBlockAgainst())) {
            return;
        }

        if (!event.getBlockReplacedState().getType().isAir()) {
            return;
        }

        switch (event.getBlock().getType()) {
            case POWDER_SNOW -> {
                return;
            }
            case LILY_PAD -> {
                Block downBlock = event.getBlock().getRelative(BlockFace.DOWN);
                if (downBlock.getType() == Material.WATER || downBlock.getType() == Material.ICE) {
                    return;
                } else if (downBlock.getBlockData() instanceof Waterlogged waterlogged && waterlogged.isWaterlogged()) {
                    return;
                }
            }
            case FROGSPAWN -> {
                Block downBlock = event.getBlock().getRelative(BlockFace.DOWN);
                if (downBlock.getType() == Material.WATER) {
                    return;
                }
            }
        }

        BreakHistory bh = broken.get(event.getPlayer().getUniqueId());
        if (bh != null && bh.checkLocation(event.getBlock().getLocation()) && bh.time + 30L > System.currentTimeMillis()) {
            return;
        }

        event.setCancelled(true);
        event.getPlayer().sendMessage(ERROR_MESSAGE);
    }
}
