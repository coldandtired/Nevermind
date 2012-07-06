package me.coldandtired.nevermind;

import java.io.File;
import java.util.List;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin implements Listener
{
	List<Integer> blocks_to_remove = null;
	List<Integer> items_to_remove = null;
	
	@SuppressWarnings("unchecked")
	@Override
	public void onEnable() 
	{		
		FileConfiguration config = getConfig();
		blocks_to_remove = config.contains("blocks_to_remove") ? (List<Integer>) config.get("blocks_to_remove") : null;
		items_to_remove = config.contains("items_to_remove") ? (List<Integer>) config.get("items_to_remove") : null;
		if (blocks_to_remove != null && blocks_to_remove.size() == 0) blocks_to_remove = null;
		if (items_to_remove != null && items_to_remove.size() == 0) items_to_remove = null;
		
		if (blocks_to_remove != null || items_to_remove != null)
		{
			getServer().getPluginManager().registerEvents(this, this);
			for (World w : Bukkit.getWorlds())
			{
				for (Chunk c : w.getLoadedChunks()) remove_blocks(c);
			}
			if (items_to_remove != null && items_to_remove.size() != 0)
			{
				for (Player p : getServer().getOnlinePlayers())	remove_items(p.getInventory());
			}
		}
		else
		{
			Logger logger = getLogger();
			File f = new File(this.getDataFolder(), "config.yml");
			if (!f.exists())
			{
				saveDefaultConfig();
				logger.info("No config found - created");
			}
			logger.warning("Nothing in the config - stopping");
			setEnabled(false);
		}
	}
	
	@Override
	public void onDisable() 
	{	
		blocks_to_remove = null;
		items_to_remove = null;
	}
	
	@EventHandler
	public void onChunkLoad(ChunkLoadEvent event)
	{
		remove_blocks(event.getChunk());
	}
	
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event)
	{
		if (items_to_remove != null && items_to_remove.size() != 0) remove_items(event.getPlayer().getInventory());
	}
	
	void remove_blocks(Chunk c)
	{
		World w = c.getWorld();
		if (blocks_to_remove != null && blocks_to_remove.size() != 0)
		{
			for (int x = 0; x < 16; x++)
			{
				for (int z = 0; z < 15; z++)
				{
					for (int y = 0; y < w.getMaxHeight(); y++)
					{
						Block b = c.getBlock(x, y, z);
						int id = b.getTypeId();
						if (id != 0 && blocks_to_remove.contains(id)) b.setTypeId(0);
					}
				}
			}
		}
		if (items_to_remove != null && items_to_remove.size() != 0)
		{
			for (BlockState b : c.getTileEntities())
			{
				if (b.getType() == Material.CHEST) remove_items(((Chest)b).getInventory());
			}
		}
	}
	
	void remove_items(Inventory inv)
	{
		for (ItemStack is : inv.getContents())
		{
			if (is !=null && items_to_remove.contains(is.getTypeId())) inv.remove(is); 
		}
	}
}
