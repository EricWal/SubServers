package net.ME1312.SubServer.GUI;

import net.ME1312.SubServer.Libraries.Version.Version;
import net.ME1312.SubServer.SubAPI;
import net.ME1312.SubServer.SubPlugin;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Arrays;
import java.util.Iterator;

/**
 * GUI Renderer Class
 * 
 * @author ME1312
 *
 */
public class SubGUI implements Listener {
	protected boolean closeWindow = false;
	
	private ItemStack block = null;
	private ItemMeta  blockMeta = null;
	private Inventory inv = null;
	private SubPlugin SubPlugin;
	
	protected SubGUI(SubPlugin SubPlugin) {
		this.SubPlugin = SubPlugin;
	}
	
	/**
	 * Opens The GUI
	 * 
	 * @param player The Player Opening the GUI
	 * @param page The Page Number
	 * @param server The Server Name (if not null, opens Selection Window)
	 */
	
	@SuppressWarnings("deprecation")
	public SubGUI(Player player, int page, String server, SubPlugin SubPlugin) {
		this.SubPlugin = SubPlugin;
		inv = Bukkit.createInventory(null, 27, ChatColor.DARK_GREEN + SubPlugin.lang.get("Lang.GUI.Server-List-Title").replace("$Int$", Integer.toString(page + 1)));
		
		if (server == null) {
			int i = 0;
			int min = (page * 18);
			int max = (min + 17);
			block = null;
			blockMeta = null;
			for(Iterator<String> str = SubPlugin.SubServers.iterator(); str.hasNext(); ) {
			    String item = str.next();
			    if (SubAPI.getSubServer(item).Enabled) {
			    	if (SubPlugin.SubServers.indexOf(item) >= min && SubPlugin.SubServers.indexOf(item) <= max) {
			    		if (SubAPI.getSubServer(item).Temporary) {
			    			block = new ItemStack(289);
			    			blockMeta = block.getItemMeta();
			    			blockMeta.setDisplayName(ChatColor.YELLOW + item);
			    			blockMeta.setLore(Arrays.asList(ChatColor.DARK_GREEN + SubPlugin.lang.get("Lang.GUI.Online"), ChatColor.GRAY + SubPlugin.lang.get("Lang.GUI.Temp-Server")));
			    			block.setItemMeta(blockMeta);
			    		} else if (SubAPI.getSubServer(item).isRunning()) {
			    			block = new ItemStack(Material.GLOWSTONE_DUST);
			    			blockMeta = block.getItemMeta();
			    			blockMeta.setDisplayName(ChatColor.YELLOW + item);
			    			blockMeta.setLore(Arrays.asList(ChatColor.DARK_GREEN + SubPlugin.lang.get("Lang.GUI.Online")));
			    			block.setItemMeta(blockMeta);
			    		} else {
			    			block = new ItemStack(Material.REDSTONE);
			    			blockMeta = block.getItemMeta();
			    			blockMeta.setDisplayName(ChatColor.YELLOW + item);
			    			blockMeta.setLore(Arrays.asList(ChatColor.DARK_RED + SubPlugin.lang.get("Lang.GUI.Offline")));
			    			block.setItemMeta(blockMeta);
			    		}
			    		inv.setItem(i, block);
			    		block = null;
			    		blockMeta = null;
			    		i++;
			    	}
			    }
			}
			
			block = new ItemStack(Material.ENCHANTED_BOOK);
	    	blockMeta = block.getItemMeta();
	    	blockMeta.setDisplayName(ChatColor.GRAY + SubPlugin.Plugin.getName() + " v" + SubPlugin.Plugin.getDescription().getVersion());
	    	blockMeta.setLore(Arrays.asList("\u00A9 ME1312 EPIC 2015", "", ChatColor.DARK_AQUA + SubPlugin.lang.get("Lang.GUI.Sub-Help-Book").split("\\|\\|\\|")[0], ChatColor.DARK_AQUA + SubPlugin.lang.get("Lang.GUI.Sub-Help-Book").split("\\|\\|\\|")[1]));
	    	block.setItemMeta(blockMeta);
	    	inv.setItem(18, block);
	    	block = null;
	    	blockMeta = null;
	    	
	    	if (SubPlugin.SubServers.size() > max) {
	    		block = new ItemStack(Material.IRON_INGOT);
	    		blockMeta = block.getItemMeta();
	    		blockMeta.setDisplayName(ChatColor.DARK_GREEN + SubPlugin.lang.get("Lang.GUI.Next"));
	    		block.setItemMeta(blockMeta);
	    		inv.setItem(23, block);
	    		block = null;
	    		blockMeta = null;
	    	}
	    	
	    	if (min != 0) {
	    		block = new ItemStack(Material.IRON_INGOT);
	    		blockMeta = block.getItemMeta();
	    		blockMeta.setDisplayName(ChatColor.DARK_GREEN + SubPlugin.lang.get("Lang.GUI.Back"));
	    		block.setItemMeta(blockMeta);
	    		inv.setItem(21, block);
	    		block = null;
	    		blockMeta = null;
	    	}
	    	
	    	if (SubPlugin.MCVersion.compareTo(new Version("1.8")) >= 0) {
	    		block = new ItemStack(166);
	    	} else {
	    		block = new ItemStack(Material.REDSTONE_BLOCK);
	    	}
	    	blockMeta = block.getItemMeta();
	    	blockMeta.setDisplayName(ChatColor.DARK_RED + SubPlugin.lang.get("Lang.GUI.Exit"));
	    	block.setItemMeta(blockMeta);
	    	inv.setItem(26, block);
	    	block = null;
	    	blockMeta = null;
	    	
	    	player.openInventory(inv);
			inv = null;
		} else if (SubPlugin.SubServers.contains(server) || server.equalsIgnoreCase("~Proxy")) {
			openServerWindow(player, server);
		}
	}
	
	/**
	 * Opens Selection Window
	 * 
	 * @param player The Player Opening The GUI
	 * @param server The Server Name (Required)
	 */
	@SuppressWarnings("deprecation")
	protected void openServerWindow(Player player, String server) {
		if (server != null) {
			inv = Bukkit.createInventory(null, 27, ChatColor.DARK_GREEN + SubPlugin.lang.get("Lang.GUI.Server-Admin-Title") + ChatColor.YELLOW + server);

			if (SubAPI.getSubServer(server).isRunning()) {
				block = new ItemStack(Material.EMERALD_BLOCK);
		    	blockMeta = block.getItemMeta();
		    	blockMeta.setDisplayName(ChatColor.GRAY + SubPlugin.lang.get("Lang.GUI.Start"));
		    	if (!player.hasPermission("SubServer.Command.start." + server) && !player.hasPermission("SubServer.Command.start.*")) {
		    		blockMeta.setLore(Arrays.asList(ChatColor.DARK_RED + SubPlugin.lang.get("Lang.GUI.Already-Running"), ChatColor.DARK_RED + SubPlugin.lang.get("Lang.GUI.Permission-Error")));
		    	} else {
		    		blockMeta.setLore(Arrays.asList(ChatColor.DARK_RED + SubPlugin.lang.get("Lang.GUI.Already-Running")));
		    	}
		    	block.setItemMeta(blockMeta);
		    	inv.setItem(1, block);
		    	block = null;
		    	blockMeta = null;
		    	
		    	block = new ItemStack(Material.REDSTONE_BLOCK);
		    	blockMeta = block.getItemMeta();
		    	blockMeta.setDisplayName(ChatColor.RED + SubPlugin.lang.get("Lang.GUI.Stop"));
		    	if (!player.hasPermission("SubServer.Command.stop." + server) && !player.hasPermission("SubServer.Command.stop.*")) {
		    		blockMeta.setLore(Arrays.asList(ChatColor.DARK_RED + SubPlugin.lang.get("Lang.GUI.Permission-Error")));
		    	}
		    	block.setItemMeta(blockMeta);
		    	inv.setItem(3, block);
		    	block = null;
		    	blockMeta = null;
		    	
		    	block = new ItemStack(Material.GRAVEL);
		    	blockMeta = block.getItemMeta();
		    	blockMeta.setDisplayName(ChatColor.DARK_RED + SubPlugin.lang.get("Lang.GUI.Terminate"));
		    	if (!player.hasPermission("SubServer.Command.kill." + server) && !player.hasPermission("SubServer.Command.kill.*")) {
		    		blockMeta.setLore(Arrays.asList(ChatColor.DARK_RED + SubPlugin.lang.get("Lang.GUI.Permission-Error")));
		    	}
		    	block.setItemMeta(blockMeta);
		    	inv.setItem(5, block);
		    	block = null;
		    	blockMeta = null;
		    	
		    	block = new ItemStack(356);
		    	blockMeta = block.getItemMeta();
		    	blockMeta.setDisplayName(ChatColor.AQUA + SubPlugin.lang.get("Lang.GUI.Send-CMD"));
		    	if (!player.hasPermission("SubServer.Command.send." + server) && !player.hasPermission("SubServer.Command.send.*")) {
		    		blockMeta.setLore(Arrays.asList(ChatColor.DARK_RED + SubPlugin.lang.get("Lang.GUI.Permission-Error")));
		    	}
		    	block.setItemMeta(blockMeta);
		    	inv.setItem(7, block);
		    	block = null;
		    	blockMeta = null;
		    	
		    	if (!SubAPI.getSubServer(server).Temporary) {
		    		block = new ItemStack(Material.GLOWSTONE_DUST);
		    		blockMeta = block.getItemMeta();
		    		blockMeta.setDisplayName(ChatColor.DARK_GREEN + SubPlugin.lang.get("Lang.GUI.Online"));
		    		if ((player.hasPermission("SubServer.Command.teleport." + server) || player.hasPermission("SubServer.Command.teleport.*")) && !server.equalsIgnoreCase("~Proxy")) {
		    			blockMeta.setLore(Arrays.asList(ChatColor.GRAY + "Click to Teleport"));
		    		}
		    		block.setItemMeta(blockMeta);
		    		inv.setItem(22, block);
		    		block = null;
		    		blockMeta = null;
		    	} else {
		    		block = new ItemStack(289);
		    		blockMeta = block.getItemMeta();
		    		blockMeta.setDisplayName(ChatColor.DARK_GREEN + SubPlugin.lang.get("Lang.GUI.Online"));
		    		if ((player.hasPermission("SubServer.Command.teleport." + server) || player.hasPermission("SubServer.Command.teleport.*")) && !server.equalsIgnoreCase("~Proxy")) {
		    			blockMeta.setLore(Arrays.asList(ChatColor.GRAY + SubPlugin.lang.get("Lang.GUI.Temp-Server"), ChatColor.GRAY + "Click to Teleport"));
		    		} else {
		    			blockMeta.setLore(Arrays.asList(ChatColor.GRAY + SubPlugin.lang.get("Lang.GUI.Temp-Server")));
		    		}
		    		block.setItemMeta(blockMeta);
		    		inv.setItem(22, block);
		    		block = null;
		    		blockMeta = null;
		    	}
			} else {
				if (!SubAPI.getSubServer(server).Temporary) {
					block = new ItemStack(Material.EMERALD_BLOCK);
					blockMeta = block.getItemMeta();
					blockMeta.setDisplayName(ChatColor.DARK_GREEN + SubPlugin.lang.get("Lang.GUI.Start"));
					if (!player.hasPermission("SubServer.Command.start." + server) && !player.hasPermission("SubServer.Command.start.*")) {
						blockMeta.setLore(Arrays.asList(ChatColor.DARK_RED + SubPlugin.lang.get("Lang.GUI.Permission-Error")));
					}
					block.setItemMeta(blockMeta);
					inv.setItem(1, block);
					block = null;
					blockMeta = null;
				} else {
					block = new ItemStack(Material.EMERALD_BLOCK);
					blockMeta = block.getItemMeta();
					blockMeta.setDisplayName(ChatColor.GRAY + SubPlugin.lang.get("Lang.GUI.Start"));
					if (player.hasPermission("SubServer.Command.start." + server) && player.hasPermission("SubServer.Command.start.*")) {
						blockMeta.setLore(Arrays.asList(ChatColor.DARK_RED + SubPlugin.lang.get("Lang.GUI.Removed")));
					} else {
						blockMeta.setLore(Arrays.asList(ChatColor.DARK_RED + SubPlugin.lang.get("Lang.GUI.Removed"), ChatColor.DARK_RED + SubPlugin.lang.get("Lang.GUI.Permission-Error")));
					}
					block.setItemMeta(blockMeta);
					inv.setItem(1, block);
					block = null;
					blockMeta = null;
				}
		    	
		    	block = new ItemStack(Material.REDSTONE_BLOCK);
		    	blockMeta = block.getItemMeta();
		    	blockMeta.setDisplayName(ChatColor.GRAY + SubPlugin.lang.get("Lang.GUI.Stop"));
		    	if (!player.hasPermission("SubServer.Command.stop." + server) && !player.hasPermission("SubServer.Command.stop.*")) {
		    		blockMeta.setLore(Arrays.asList(ChatColor.DARK_RED + SubPlugin.lang.get("Lang.GUI.Not-Running"), ChatColor.DARK_RED + SubPlugin.lang.get("Lang.GUI.Permission-Error")));
		    	} else {
		    		blockMeta.setLore(Arrays.asList(ChatColor.DARK_RED + SubPlugin.lang.get("Lang.GUI.Not-Running")));
		    	}
		    	block.setItemMeta(blockMeta);
		    	inv.setItem(3, block);
		    	block = null;
		    	blockMeta = null;
		    	
		    	block = new ItemStack(Material.GRAVEL);
		    	blockMeta = block.getItemMeta();
		    	blockMeta.setDisplayName(ChatColor.GRAY + SubPlugin.lang.get("Lang.GUI.Terminate"));
		    	if (!player.hasPermission("SubServer.Command.kill." + server) && !player.hasPermission("SubServer.Command.kill.*")) {
		    		blockMeta.setLore(Arrays.asList(ChatColor.DARK_RED + SubPlugin.lang.get("Lang.GUI.Not-Running"), ChatColor.DARK_RED + SubPlugin.lang.get("Lang.GUI.Permission-Error")));
		    	} else {
		    		blockMeta.setLore(Arrays.asList(ChatColor.DARK_RED + SubPlugin.lang.get("Lang.GUI.Not-Running")));
		    	}
		    	block.setItemMeta(blockMeta);
		    	inv.setItem(5, block);
		    	block = null;
		    	blockMeta = null;
		    	
		    	block = new ItemStack(356);
	    		blockMeta = block.getItemMeta();
	    		blockMeta.setDisplayName(ChatColor.GRAY + SubPlugin.lang.get("Lang.GUI.Send-CMD"));
	    		if (!player.hasPermission("SubServer.Command.send." + server) && !player.hasPermission("SubServer.Command.send.*")) {
	    			blockMeta.setLore(Arrays.asList(ChatColor.DARK_RED + SubPlugin.lang.get("Lang.GUI.Not-Running"), ChatColor.DARK_RED + SubPlugin.lang.get("Lang.GUI.Permission-Error")));
	    		} else {
	    			blockMeta.setLore(Arrays.asList(ChatColor.DARK_RED + SubPlugin.lang.get("Lang.GUI.Not-Running")));
	    		}
	    		block.setItemMeta(blockMeta);
	    		inv.setItem(7, block);
	    		block = null;
	    		blockMeta = null;
	    		
		    	if (!SubAPI.getSubServer(server).Temporary) {
		    		block = new ItemStack(Material.REDSTONE);
		    		blockMeta = block.getItemMeta();
		    		blockMeta.setDisplayName(ChatColor.DARK_RED + SubPlugin.lang.get("Lang.GUI.Offline"));
		    		block.setItemMeta(blockMeta);
		    		inv.setItem(22, block);
		    		block = null;
		    		blockMeta = null;
		    	} else {
		    		block = new ItemStack(289);
		    		blockMeta = block.getItemMeta();
		    		blockMeta.setDisplayName(ChatColor.GRAY + SubPlugin.lang.get("Lang.GUI.Temp-Server"));
		    		blockMeta.setLore(Arrays.asList(ChatColor.DARK_RED + SubPlugin.lang.get("Lang.GUI.Removed")));
		    		block.setItemMeta(blockMeta);
		    		inv.setItem(22, block);
		    		block = null;
		    		blockMeta = null;
		    	}
			}
			block = new ItemStack(Material.ENCHANTED_BOOK);
	    	blockMeta = block.getItemMeta();
	    	blockMeta.setDisplayName(ChatColor.GRAY + SubPlugin.Plugin.getName() + " v" + SubPlugin.Plugin.getDescription().getVersion());
	    	blockMeta.setLore(Arrays.asList("\u00A9 ME1312 EPIC 2015", "", ChatColor.DARK_AQUA + SubPlugin.lang.get("Lang.GUI.Sub-Help-Book").split("\\|\\|\\|")[0], ChatColor.DARK_AQUA + SubPlugin.lang.get("Lang.GUI.Sub-Help-Book").split("\\|\\|\\|")[1]));
	    	block.setItemMeta(blockMeta);
	    	inv.setItem(18, block);
	    	block = null;
	    	blockMeta = null;
			
			block = new ItemStack(Material.IRON_INGOT);
	    	blockMeta = block.getItemMeta();
	    	blockMeta.setDisplayName(ChatColor.YELLOW + SubPlugin.lang.get("Lang.GUI.Back"));
	    	block.setItemMeta(blockMeta);
	    	inv.setItem(25, block);
	    	block = null;
	    	blockMeta = null;
	    	
	    	if (SubPlugin.MCVersion.compareTo(new Version("1.8")) >= 0) {
	    		block = new ItemStack(166);
	    	} else {
	    		block = new ItemStack(Material.REDSTONE_BLOCK);
	    	}
	    	blockMeta = block.getItemMeta();
	    	blockMeta.setDisplayName(ChatColor.DARK_RED + SubPlugin.lang.get("Lang.GUI.Exit"));
	    	block.setItemMeta(blockMeta);
	    	inv.setItem(26, block);
	    	block = null;
	    	blockMeta = null;
	    	
	    	player.openInventory(inv);
	    	inv = null;
		}
	}
	
	protected static boolean stopLoader = false;
	@SuppressWarnings("deprecation")
	private static ItemStack invBlock = new ItemStack(168, 1, (short) 1);
	
	/**
	 * Opens Loader Animation
	 * 
	 * NOTE: Set stopLoader to false to stop loading.
	 * 
	 * @param player Player opening the GUI
	 * @param done Callback Method Name
	 */
	@SuppressWarnings("deprecation")
	protected void openLoader(final Player player, final String args, final String done) {
		final Inventory inventory = Bukkit.createInventory(null, 9, SubPlugin.lang.get("Lang.GUI.Loading"));
		
		if (SubPlugin.MCVersion.compareTo(new Version("1.8")) < 0) invBlock = new ItemStack(35, 1, (short) 9);
    	ItemMeta invBlockMeta = invBlock.getItemMeta();
    	invBlockMeta.setDisplayName("");
    	invBlock.setItemMeta(invBlockMeta);
    	
		final ItemStack Block = new ItemStack(Material.DIAMOND_BLOCK);
    	ItemMeta BlockMeta = Block.getItemMeta();
    	BlockMeta.setDisplayName("");
    	Block.setItemMeta(BlockMeta);
    	
    	inventory.setItem(0, invBlock);
    	inventory.setItem(1, invBlock);
    	inventory.setItem(2, invBlock);
    	inventory.setItem(3, invBlock);
    	inventory.setItem(4, invBlock);
    	inventory.setItem(5, invBlock);
    	inventory.setItem(6, invBlock);
    	inventory.setItem(7, invBlock);
    	inventory.setItem(8, invBlock);
    	
    	new BukkitRunnable() {
    		@Override
			public void run() {
				do {
					try {
						Thread.sleep(125);
						player.closeInventory();
						inventory.setItem(0, Block);
						player.openInventory(inventory);
						if (stopLoader == false) {
							Thread.sleep(75);
							player.closeInventory();
							inventory.setItem(1, Block);
							player.openInventory(inventory);
							if (stopLoader == false) {
								Thread.sleep(75);
								player.closeInventory();
								inventory.setItem(2, Block);
								player.openInventory(inventory);
								if (stopLoader == false) {
									Thread.sleep(75);
									player.closeInventory();
									inventory.setItem(0, invBlock);
									inventory.setItem(3, Block);
									player.openInventory(inventory);
									if (stopLoader == false) {
										Thread.sleep(75);
										player.closeInventory();
										inventory.setItem(1, invBlock);
										inventory.setItem(4, Block);
										player.openInventory(inventory);
										if (stopLoader == false) {
											Thread.sleep(75);
											player.closeInventory();
											inventory.setItem(2, invBlock);
											inventory.setItem(5, Block);
											player.openInventory(inventory);
											if (stopLoader == false) {
												Thread.sleep(75);
												player.closeInventory();
												inventory.setItem(3, invBlock);
												inventory.setItem(6, Block);
												player.openInventory(inventory);
												if (stopLoader == false) {
													Thread.sleep(75);
													player.closeInventory();
													inventory.setItem(4, invBlock);
													inventory.setItem(7, Block);
													player.openInventory(inventory);
													if (stopLoader == false) {
														Thread.sleep(75);
														player.closeInventory();
														inventory.setItem(5, invBlock);
														inventory.setItem(8, Block);
														player.openInventory(inventory);
														if (stopLoader == false) {
															Thread.sleep(75);
															player.closeInventory();
															inventory.setItem(6, invBlock);
															player.openInventory(inventory);
															if (stopLoader == false) {
																Thread.sleep(75);
																player.closeInventory();
																inventory.setItem(7, invBlock);
																player.openInventory(inventory);
																if (stopLoader == false) {
																	Thread.sleep(75);
																	player.closeInventory();
																	inventory.setItem(8, invBlock);
																	player.openInventory(inventory);
																	if (stopLoader == false) Thread.sleep(125);
																}
															}
														}
													}
												}
											}
										}
									}
								}
							}
						}
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				} while (stopLoader == false);
				
				stopLoader = false;
				player.closeInventory();
				if (done.equalsIgnoreCase("openServerWindow")) {
					new SubGUI(player, 0, args, SubPlugin);
				} else if (done.equalsIgnoreCase("openSelectionWindow")) {
					new SubGUI(player, Integer.parseInt(args), null, SubPlugin);
				}
			}
		}.runTaskAsynchronously(SubPlugin.Plugin);
	}
	
	/**
	 * 
	 * 
	 * @param player Player Opening GUI
	 * @param server Server Name
	 */
	protected void openSentCommand(Player player, String server) {
		inv = Bukkit.createInventory(null, 18, ChatColor.DARK_GREEN + SubPlugin.lang.get("Lang.GUI.Success") + ChatColor.YELLOW + server);
		block = new ItemStack(Material.EMERALD_BLOCK);
    	blockMeta = block.getItemMeta();
    	blockMeta.setDisplayName(ChatColor.GREEN + SubPlugin.lang.get("Lang.GUI.OK"));
    	block.setItemMeta(blockMeta);
    	inv.setItem(5, block);
    	inv.setItem(4, block);
    	inv.setItem(3, block);
    	inv.setItem(14, block);
    	inv.setItem(13, block);
    	inv.setItem(12, block);
		player.openInventory(inv);
		block = null;
		blockMeta = null;
		inv = null;
	}
}
