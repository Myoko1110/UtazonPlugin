package work.utakatanet.utazonplugin.post;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.ShulkerBox;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.scheduler.BukkitRunnable;
import work.utakatanet.utazonplugin.UtazonPlugin;
import work.utakatanet.utazonplugin.data.ProductItem;
import work.utakatanet.utazonplugin.data.ReturnStockList;
import work.utakatanet.utazonplugin.util.DatabaseHelper;
import work.utakatanet.utazonplugin.util.ItemStackHelper;
import work.utakatanet.utazonplugin.util.MailboxHelper;

import java.io.OutputStreamWriter;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.UUID;

import static work.utakatanet.utazonplugin.post.detectOrder.allowBlocks;
import static work.utakatanet.utazonplugin.post.detectOrder.getHttpConnection;

public class detectReturn extends BukkitRunnable {

    private static final UtazonPlugin plugin = UtazonPlugin.plugin;

    @Override
    public void run() {

        ArrayList<ReturnStockList> returnStock = DatabaseHelper.getReturnStock();
        boolean isExist = false;

        for (ReturnStockList i : returnStock) {

            LocalDateTime deliveryTime = i.deliveryAt;
            LocalDateTime now = LocalDateTime.now();

            if (deliveryTime.isBefore(now)) {
                isExist = true;

                int itemId = i.itemID;
                int amount = i.amount;
                UUID uuid = i.uuid;

                ProductItem item = DatabaseHelper.getItemStack(itemId);
                if (item == null) {
                    try {
                        HttpURLConnection connection = getHttpConnection("/post/returnstock/item_notfound/");

                        OutputStreamWriter out = new OutputStreamWriter(connection.getOutputStream(), StandardCharsets.UTF_8);
                        out.write(String.format("uuid=%s&id=%s", uuid, i.id));
                        out.flush();
                        out.close();

                        connection.connect();
                        int status = connection.getResponseCode();

                        if (status != HttpURLConnection.HTTP_OK) {
                            plugin.getLogger().warning("WebのUtazonに接続できませんでした");
                        }

                        DatabaseHelper.completeReturnStock(i.id);

                    }catch (ConnectException e){
                        plugin.getLogger().warning("WebのUtazonに接続できませんでした");
                    }catch (Exception e) {
                        e.printStackTrace();
                    }
                    return;
                }

                ItemStack itemStack = ItemStackHelper.decodeItemStack(item);

                ArrayList<ItemStack> itemStacks = new ArrayList<>();
                for (int j = 0; j < amount; j++) {
                    itemStacks.add(itemStack);
                }
                ArrayList<ArrayList<ItemStack>> firstSplit = splitArrayList(itemStacks, 729);

                ArrayList<ArrayList<ArrayList<ItemStack>>> secondSplit = new ArrayList<>();
                for (ArrayList<ItemStack> subList : firstSplit) {
                    ArrayList<ArrayList<ItemStack>> subSplit = splitArrayList(subList, 27);
                    secondSplit.add(subSplit);
                }

                ArrayList<ItemStack> returnShulker = new ArrayList<>();
                for (int firstBox = 0; firstBox < secondSplit.size(); firstBox++) {
                    ArrayList<ItemStack> childShulker = new ArrayList<>();
                    for (ArrayList<ItemStack> secondBox : secondSplit.get(firstBox)) {
                        ItemStack shulkerBox = new ItemStack(Material.BROWN_SHULKER_BOX);
                        BlockStateMeta meta = (BlockStateMeta) shulkerBox.getItemMeta();

                        if (meta != null) {
                            ShulkerBox shulkerBoxMeta = (ShulkerBox) meta.getBlockState();
                            for (ItemStack shulkerBoxItem : secondBox) {
                                shulkerBoxMeta.getInventory().addItem(shulkerBoxItem);
                            }

                            meta.setDisplayName(ChatColor.DARK_PURPLE + "Utazonからの在庫返却");

                            meta.setBlockState(shulkerBoxMeta);
                            shulkerBox.setItemMeta(meta);
                        }
                        childShulker.add(shulkerBox);
                    }

                    ItemStack shulkerBox = new ItemStack(Material.BROWN_SHULKER_BOX);
                    BlockStateMeta meta = (BlockStateMeta) shulkerBox.getItemMeta();

                    if (meta != null) {
                        int shulkerAmount = firstBox + 1;

                        String title = org.bukkit.ChatColor.AQUA + "Utazonからの在庫返却";
                        if (firstSplit.size() > 1) {
                            title += " #" + shulkerAmount;
                        }

                        meta.setDisplayName(title);

                        ShulkerBox shulkerBoxMeta = (ShulkerBox) meta.getBlockState();
                        for (ItemStack shulkerBoxItem : childShulker) {
                            shulkerBoxMeta.getInventory().addItem(shulkerBoxItem);
                        }
                        meta.setBlockState(shulkerBoxMeta);
                        shulkerBox.setItemMeta(meta);
                    }
                    returnShulker.add(shulkerBox);
                }

                World world = Bukkit.getWorld("world");

                Location chestLocation = new Location(world, 299, 67, -339);
                Block block = chestLocation.getBlock();

                boolean breakOccur = false;
                for (Material material : allowBlocks) {
                    if (block.getType() == material) {
                        breakOccur = true;
                        break;
                    }
                }

                if (!breakOccur) {
                    if (i.error == null || !i.error.equals("MailboxNotfound")) {
                        try {
                            HttpURLConnection connection = getHttpConnection("/post/returnstock/mailbox_notfound/");

                            OutputStreamWriter out = new OutputStreamWriter(connection.getOutputStream(), StandardCharsets.UTF_8);
                            out.write(String.format("uuid=%s&id=%s", uuid, i.id));
                            out.flush();
                            out.close();

                            connection.connect();
                            int status = connection.getResponseCode();

                            if (status != HttpURLConnection.HTTP_OK) {
                                plugin.getLogger().warning("WebのUtazonに接続できませんでした");
                            }

                            DatabaseHelper.errorReturnStock(i.id, "MailboxNotfound");

                        }catch (ConnectException e){
                            plugin.getLogger().warning("WebのUtazonに接続できませんでした");
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                    return;
                }

                boolean result = MailboxHelper.postItem(chestLocation, returnShulker);
                if (!result) {
                    if (i.error == null || !i.error.equals("MailboxFull")) {
                        try {
                            HttpURLConnection connection = getHttpConnection("/post/returnstock/mailbox_full/");

                            OutputStreamWriter out = new OutputStreamWriter(connection.getOutputStream(), StandardCharsets.UTF_8);
                            out.write(String.format("uuid=%s&id=%s", uuid, i.id));
                            out.flush();
                            out.close();

                            connection.connect();
                            int status = connection.getResponseCode();

                            if (status != HttpURLConnection.HTTP_OK) {
                                plugin.getLogger().warning("WebのUtazonに接続できませんでした");
                            }

                            DatabaseHelper.errorReturnStock(i.id, "MailboxFull");

                        }catch (ConnectException e){
                            plugin.getLogger().warning("WebのUtazonに接続できませんでした");
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                    return;
                }

                try {
                    HttpURLConnection connection = getHttpConnection("/post/returnstock_complete/");

                    OutputStreamWriter out = new OutputStreamWriter(connection.getOutputStream(), StandardCharsets.UTF_8);
                    out.write(String.format("uuid=%s&id=%s", uuid, i.id));
                    out.flush();
                    out.close();

                    connection.connect();
                    int status = connection.getResponseCode();

                    if (status != HttpURLConnection.HTTP_OK) {
                        plugin.getLogger().warning("WebのUtazonに接続できませんでした");
                    }

                    DatabaseHelper.completeReturnStock(i.id);

                }catch (ConnectException e){
                    plugin.getLogger().warning("WebのUtazonに接続できませんでした");
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }

        if (isExist) {
            plugin.getLogger().info("アイテムの在庫を返却しました");
        }
    }


    public static <T> ArrayList<ArrayList<T>> splitArrayList(ArrayList<T> list, int chunkSize) {
        ArrayList<ArrayList<T>> splitList = new ArrayList<>();
        for (int i = 0; i < list.size(); i += chunkSize) {
            int end = Math.min(list.size(), i + chunkSize);
            splitList.add(new ArrayList<>(list.subList(i, end)));
        }
        return splitList;
    }

}
