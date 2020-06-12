package com.darkender.plugins.manhuntcompass;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.WorldType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.UUID;

public class ManhuntCompass extends JavaPlugin implements Listener
{
    @Override
    public void onEnable()
    {
        getServer().getPluginManager().registerEvents(this, this);
    }
    
    private boolean canTrack(Player hunter, Player hunted)
    {
        return hunted != null && hunted.getWorld().equals(hunter.getWorld()) && hunter.getWorld().getWorldType() == WorldType.NORMAL;
    }
    
    private Player getNext(Player hunter, UUID current)
    {
        Player found = null;
        boolean currentFlag = false;
        for(Player player : Bukkit.getOnlinePlayers())
        {
            if((found == null || current == null) && !player.getUniqueId().equals(hunter.getUniqueId()))
            {
                found = player;
            }
    
            if(currentFlag && !player.getUniqueId().equals(hunter.getUniqueId()))
            {
                found = player;
                break;
            }
            else if(player.getUniqueId().equals(current))
            {
                currentFlag = true;
            }
        }
        
        return found;
    }
    
    @EventHandler
    public void onInteract(PlayerInteractEvent event)
    {
        if(event.getHand() == EquipmentSlot.HAND)
        {
            if(event.getItem() != null && event.getItem().getType() == Material.COMPASS)
            {
                List<MetadataValue> values = event.getPlayer().getMetadata("hunting");
                UUID current = null;
                if(values.size() > 0)
                {
                    current = UUID.fromString(values.get(0).asString());
                }
                
                if(event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK)
                {
                    if(current != null)
                    {
                        Player next = Bukkit.getPlayer(current);
                        if(next != null)
                        {
                            event.getPlayer().sendMessage(ChatColor.GOLD + "Tracking: " + next.getName() + " (" +
                                    Math.floor(next.getLocation().distance(event.getPlayer().getLocation())) + " blocks)");
                            event.getPlayer().setCompassTarget(next.getLocation());
                        }
                    }
                }
                else
                {
                    Player next = getNext(event.getPlayer(), current);
    
                    if(canTrack(event.getPlayer(), next))
                    {
                        event.getPlayer().setMetadata("hunting", new FixedMetadataValue(this, next.getUniqueId().toString()));
                        event.getPlayer().sendMessage(ChatColor.GOLD + "Now tracking: " + next.getName() + " (" +
                                Math.floor(next.getLocation().distance(event.getPlayer().getLocation())) + " blocks)");
                        event.getPlayer().setCompassTarget(next.getLocation());
                    }
                }
            }
        }
    }
    
    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event)
    {
        event.getDrops().removeIf(itemStack -> itemStack != null && itemStack.getType() == Material.COMPASS);
        event.getEntity().setMetadata("compass-died", new FixedMetadataValue(this, true));
    }
    
    @EventHandler
    public void onRespawn(PlayerRespawnEvent event)
    {
        if(event.getPlayer().hasMetadata("compass-died"))
        {
            event.getPlayer().removeMetadata("compass-died", this);
            event.getPlayer().getInventory().addItem(new ItemStack(Material.COMPASS, 1));
        }
    }
}
