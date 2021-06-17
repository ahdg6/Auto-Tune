package unprotesting.com.github.Commands;

import java.text.DecimalFormat;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui;
import com.github.stefvanschie.inventoryframework.pane.OutlinePane;
import com.github.stefvanschie.inventoryframework.pane.PaginatedPane;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import unprotesting.com.github.Main;
import unprotesting.com.github.Commands.Util.CommandUtil;
import unprotesting.com.github.Config.Config;
import unprotesting.com.github.Data.Ephemeral.Data.TransactionData;
import unprotesting.com.github.Data.Ephemeral.Data.TransactionData.TransactionPositionType;

public class TransactionCommand implements CommandExecutor{

    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String transactions, String[] args) {
        if (!CommandUtil.checkIfSenderPlayer(sender)){return true;}
        return interpretCommand(sender, args);
    }

    private boolean interpretCommand(CommandSender sender, String[] args) {
        CommandUtil.closeInventory(sender);
        ChestGui gui = new ChestGui(6, "Transactions");
        PaginatedPane pages = new PaginatedPane(0, 0, 9, 6);
        List<TransactionData> loans = Main.getCache().getTRANSACTIONS();
        List<OutlinePane> panes = new ArrayList<OutlinePane>();
        List<GuiItem> items = getGuiItemsFromTransactions(loans);
        CommandUtil.loadGuiItemsIntoPane(items, gui, pages, panes, "GRAY_STAINED_GLASS_PANE", sender);
        return true;
    }

    private List<GuiItem> getGuiItemsFromTransactions(List<TransactionData> data){
        List<GuiItem> output = new ArrayList<GuiItem>();
        Collections.sort(data);
        for (TransactionData transaction : data){
            if (transaction.getPosition().equals(TransactionPositionType.BI) || transaction.getPosition().equals(TransactionPositionType.SI)){
                ItemStack item = new ItemStack(Material.matchMaterial(transaction.getItem()), transaction.getAmount());
                ItemMeta meta = item.getItemMeta();
                meta.setDisplayName(ChatColor.GOLD + Integer.toString(transaction.getAmount()) + "x" + " " + transaction.getItem());
                List<String> lore = new ArrayList<String>();
                if (transaction.getPosition().equals(TransactionPositionType.BI)){
                    lore.add(ChatColor.GREEN + "BUY");
                }
                else{
                    lore.add(ChatColor.RED + "SELL");
                }
                output.add(applyMetaToStack(meta, item, transaction, lore));
            }
            else{
                ItemStack item = new ItemStack(Material.ENCHANTED_BOOK);
                ItemMeta meta = item.getItemMeta();
                meta.setDisplayName(ChatColor.GOLD + transaction.getItem());
                List<String> lore = new ArrayList<String>();
                if (transaction.getPosition().equals(TransactionPositionType.BE)){
                    lore.add(ChatColor.GREEN + "BUY");
                }
                else{
                    lore.add(ChatColor.RED + "SELL");
                }
                output.add(applyMetaToStack(meta, item, transaction, lore));
            }
        }
        return output;
    }

    private GuiItem applyMetaToStack(ItemMeta meta, ItemStack item, TransactionData transaction, List<String> lore){
        DecimalFormat df = new DecimalFormat(Config.getNumberFormat());
        OfflinePlayer player = Bukkit.getPlayer(UUID.fromString(transaction.getPlayer()));
        lore.add(ChatColor.WHITE + "Player: " + player.getName());
        lore.add(ChatColor.WHITE + "Price: " + Config.getCurrencySymbol() + df.format(transaction.getPrice()));
        lore.add(ChatColor.WHITE + "Date: " + transaction.getDate().format(formatter));
        meta.setLore(lore);
        item.setItemMeta(meta);
        GuiItem gItem = new GuiItem(item, event ->{
            event.setCancelled(true);
        });
        return gItem;
    }


}
