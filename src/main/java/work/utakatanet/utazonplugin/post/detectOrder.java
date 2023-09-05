package work.utakatanet.utazonplugin.post;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.bukkit.*;
import org.bukkit.block.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import work.utakatanet.utazonplugin.UtazonPlugin;
import work.utakatanet.utazonplugin.data.OrderList;
import work.utakatanet.utazonplugin.util.DatabaseHelper;
import work.utakatanet.utazonplugin.util.ProductHelper;

import java.io.IOException;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;

public class detectOrder extends BukkitRunnable {

    private static final UtazonPlugin plugin = UtazonPlugin.plugin;
    public static final Material[] allowBlocks = {Material.CHEST, Material.HOPPER, Material.SHULKER_BOX, Material.WHITE_SHULKER_BOX, Material.LIGHT_GRAY_SHULKER_BOX, Material.GRAY_SHULKER_BOX, Material.BLACK_SHULKER_BOX, Material.BROWN_SHULKER_BOX, Material.RED_SHULKER_BOX, Material.ORANGE_SHULKER_BOX, Material.YELLOW_SHULKER_BOX, Material.LIME_SHULKER_BOX, Material.GREEN_SHULKER_BOX, Material.CYAN_SHULKER_BOX, Material.LIGHT_BLUE_SHULKER_BOX, Material.BLUE_SHULKER_BOX, Material.PURPLE_SHULKER_BOX, Material.MAGENTA_SHULKER_BOX, Material.PINK_SHULKER_BOX};

    @Override
    public void run() {
        ArrayList<OrderList> order = DatabaseHelper.getOrder();

        for (OrderList i : order) {

            LocalDateTime deliveryTime = i.deliveryTime;
            LocalDateTime now = LocalDateTime.now();

            if (deliveryTime.isBefore(now)) {

                UUID uuid = i.uuid;
                int[][] orderItem = i.orderItem;
                OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);


                World world = Bukkit.getWorld("world");
                if (world == null) {
                    plugin.getLogger().warning("ワールドが見つかりませんでした");
                    return;
                }

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
                    plugin.getLogger().warning(player.getName() + "のポストが見つかりませんでした");
                    if (i.error == null || !i.error.equals("MailboxNotFound")){
                        try {
                            HttpURLConnection connection = getHttpConnection("/post/mailbox_notfound", player, i.orderID);
                            connection.getResponseCode();

                            DatabaseHelper.errorOrder(i.orderID, "MailboxNotFound");

                        }catch (ConnectException e){
                            plugin.getLogger().warning("WebのUtazonに接続できませんでした");
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                    return;
                }


                ArrayList<ItemStack> itemList = new ArrayList<>();

                // 納品書を作成
                ItemStack deliverySlip = new ItemStack(Material.WRITTEN_BOOK);
                BookMeta bookMeta = (BookMeta) deliverySlip.getItemMeta();
                if (bookMeta != null) {
                    bookMeta.setTitle("納品書 #" + i.orderID);
                    bookMeta.setAuthor("Utazon");

                    NumberFormat numberFormat = new DecimalFormat("###,##0.00");

                    ComponentBuilder pageComponents = new ComponentBuilder("           納品書 \n発行 ")
                            .color(ChatColor.RESET)
                            .append("Utazon").color(ChatColor.BLUE).underlined(true)
                            .append(String.format("\n納品日 %s/%s/%s", now.getYear(), now.getMonthValue(), now.getDayOfMonth())).color(ChatColor.RESET).underlined(false)
                            .append("\n" + player.getName() + "様\n\n下記の通り納品いたします\n\n")
                            .append("合計額：$" + numberFormat.format(i.amount) + "\n───────────");


                    for (int[] item : orderItem) {
                        int itemID = item[0];
                        ArrayList<String> infoList = DatabaseHelper.geItemInfo(itemID);

                        if (infoList != null){
                            String itemFormat = getitemList(item, infoList);
                            pageComponents.append(itemFormat);
                        }
                    }

                    BaseComponent[] pageComponentsFormat = pageComponents.create();
                    pageComponentsFormat[1].setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, UtazonPlugin.webHost));


                    bookMeta.spigot().addPage(pageComponentsFormat);
                    deliverySlip.setItemMeta(bookMeta);
                    itemList.add(deliverySlip);
                }


                // アイテムを取得
                for (int[] item : orderItem) {
                    int itemID = item[0];
                    int itemQty = item[1];

                    // アイテム取得処理
                    ItemStack itemStack = ProductHelper.getProductStack(itemID);
                    for (int j = 0; j < itemQty; j++) {
                        itemList.add(itemStack);
                    }
                }
                ArrayList<ItemStack> shulkerList = getShulkerList(i, itemList);


                boolean post = postItem(chestLocation, shulkerList);
                if (post) {
                    DatabaseHelper.completeOrder(i.orderID);
                }else{
                    plugin.getLogger().info(player.getName() + "のポストにアイテムを追加するスペースがありませんでした");

                    if (i.error == null || !i.error.equals("MailboxFull")){
                        try {
                            HttpURLConnection connection = getHttpConnection("/post/mailbox_full", player, i.orderID);
                            connection.getResponseCode();

                            DatabaseHelper.errorOrder(i.orderID, "MailboxFull");

                        }catch (ConnectException e){
                            plugin.getLogger().warning("WebのUtazonに接続できませんでした");
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                }
            }
        }

        if (!order.isEmpty()) {
            plugin.getLogger().info("アイテムを配達しました");
        }
    }

    @NotNull
    private static ArrayList<ItemStack> getShulkerList(OrderList i, ArrayList<ItemStack> itemList) {
        ItemStack[] itemStacks = new ItemStack[itemList.size()];
        itemStacks = itemList.toArray(itemStacks);

        // 必要なシュルカーボックスを計算
        int requiredBox = (int) Math.ceil((double) itemStacks.length / 27);

        // シュルカーボックス数に応じ、アイテムを追加
        ArrayList<ItemStack> shulkerList = new ArrayList<>();
        for (int j = 1; j <= requiredBox; j++) {
            ItemStack shulker = new ItemStack(Material.BROWN_SHULKER_BOX);
            ItemMeta itemMeta = shulker.getItemMeta();

            if (itemMeta != null) {
                itemMeta.setDisplayName("Utazonからのお届け物");

                ArrayList<String> lores = new ArrayList<>();
                lores.add("注文番号: " + i.orderID);
                itemMeta.setLore(lores);

                shulker.setItemMeta(itemMeta);
            }

            BlockStateMeta bsm = (BlockStateMeta) shulker.getItemMeta();
            ShulkerBox box = (ShulkerBox) bsm.getBlockState();
            Inventory inv = box.getInventory();

            int fromIndex = 27 * (j - 1);
            int toIndex = 27 * j;

            ItemStack[] itemStackDst = Arrays.copyOfRange(itemStacks, fromIndex, toIndex);
            inv.setContents(itemStackDst);

            bsm.setBlockState(box);
            shulker.setItemMeta(bsm);

            shulkerList.add(shulker);

        }
        return shulkerList;
    }

    @NotNull
    private static String getitemList(int[] item, ArrayList<String> infoList) {
        String itemName = infoList.get(0);
        String itemPrice = infoList.get(1);

        String itemFormat;
        if (infoList.get(0).length() > 11) {
            itemFormat = "\n・" + itemName.substring(0, 10) + "…"  + "\n　$" + itemPrice  + " ×" + item[1];
        } else {
            itemFormat = "\n・" + itemName + "\n　$" + itemPrice  + " ×" + item[1];
        }
        return itemFormat;
    }

    @NotNull
    private static HttpURLConnection getHttpConnection(String path, OfflinePlayer player, String orderID) throws IOException {
        URL url = new URL(new URL(UtazonPlugin.webHost), path);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("pass", UtazonPlugin.webPass);
        connection.setRequestProperty("mcuuid", player.getUniqueId().toString());
        connection.setRequestProperty("orderid", orderID);
        return connection;
    }

    public boolean postItem(Location location, ArrayList<ItemStack> itemStack) {
        try {
            BlockState b = location.getBlock().getState();

            int emptySlots = 0;

            if (b instanceof Chest chest) {
                for (ItemStack item : chest.getInventory().getContents()) {
                    if (item == null) {
                        emptySlots++;
                    }
                }
                if (emptySlots < itemStack.size()) {
                    return false;
                }

                for (ItemStack i : itemStack) {
                    chest.getInventory().addItem(i);
                }

            } else if (b instanceof ShulkerBox shulkerBox) {
                for (ItemStack item : shulkerBox.getInventory().getContents()) {
                    if (item == null) {
                        emptySlots++;
                    }
                }
                if (emptySlots < itemStack.size()) {
                    return false;
                }

                for (ItemStack i : itemStack) {
                    shulkerBox.getInventory().addItem(i);
                }

            } else if (b instanceof Hopper hopper) {
                for (ItemStack item : hopper.getInventory().getContents()) {
                    if (item == null) {
                        emptySlots++;
                    }
                }
                if (emptySlots < itemStack.size()) {
                    return false;
                }

                for (ItemStack i : itemStack) {
                    hopper.getInventory().addItem(i);
                }
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
