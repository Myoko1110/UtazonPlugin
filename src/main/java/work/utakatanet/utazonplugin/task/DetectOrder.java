package work.utakatanet.utazonplugin.task;

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
import work.utakatanet.utazonplugin.util.MailboxHelper;
import work.utakatanet.utazonplugin.util.ProductHelper;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.UUID;

public class DetectOrder extends BukkitRunnable {

    private static final UtazonPlugin plugin = UtazonPlugin.plugin;
    public static final Material[] allowBlocks = {Material.CHEST, Material.HOPPER, Material.SHULKER_BOX, Material.WHITE_SHULKER_BOX, Material.LIGHT_GRAY_SHULKER_BOX, Material.GRAY_SHULKER_BOX, Material.BLACK_SHULKER_BOX, Material.BROWN_SHULKER_BOX, Material.RED_SHULKER_BOX, Material.ORANGE_SHULKER_BOX, Material.YELLOW_SHULKER_BOX, Material.LIME_SHULKER_BOX, Material.GREEN_SHULKER_BOX, Material.CYAN_SHULKER_BOX, Material.LIGHT_BLUE_SHULKER_BOX, Material.BLUE_SHULKER_BOX, Material.PURPLE_SHULKER_BOX, Material.MAGENTA_SHULKER_BOX, Material.PINK_SHULKER_BOX};

    @Override
    public void run() {
        ArrayList<OrderList> order = DatabaseHelper.getOrder();
        boolean isExist = false;

        for (OrderList i : order) {

            LocalDateTime now = LocalDateTime.now();

            /* 配達時間になったら*/
            if (i.deliversAt.isBefore(now)) {
                isExist = true;

                // 注文情報を取得
                UUID uuid = i.uuid;
                Map<String, Integer> orderItem = i.orderItem;
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
                    try {
                        HttpURLConnection connection = getHttpConnection("/post/mailbox_notfound/");

                        OutputStreamWriter out = new OutputStreamWriter(connection.getOutputStream(), StandardCharsets.UTF_8);
                        out.write(String.format("uuid=%s&orderid=%s", player.getUniqueId(), i.orderID));
                        out.flush();
                        out.close();

                        connection.connect();
                        int status = connection.getResponseCode();

                        if (status != HttpURLConnection.HTTP_OK) {
                            plugin.getLogger().warning("WebのUtazonに接続できませんでした");
                        }

                        DatabaseHelper.errorOrder(i.orderID, "MailboxNotFound");

                    }catch (ConnectException e){
                        plugin.getLogger().warning("WebのUtazonに接続できませんでした");
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                    return;
                }


                /* 納品書を作成 */
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


                    for (Map.Entry<String, Integer> entry : orderItem.entrySet()) {
                        int itemID = Integer.parseInt(entry.getKey());
                        ArrayList<String> infoList = DatabaseHelper.getItemInfo(itemID);

                        if (infoList != null){
                            String itemFormat = getitemList(entry.getValue(), infoList);
                            pageComponents.append(itemFormat);
                        }
                    }

                    BaseComponent[] pageComponentsFormat = pageComponents.create();
                    pageComponentsFormat[1].setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, UtazonPlugin.webEmbedHost));


                    bookMeta.spigot().addPage(pageComponentsFormat);
                    deliverySlip.setItemMeta(bookMeta);
                    itemList.add(deliverySlip);
                }


                /* 配達する商品をまとめる */
                for (Map.Entry<String, Integer> entry : orderItem.entrySet()) {
                    int itemID = Integer.parseInt(entry.getKey());
                    int itemQty = entry.getValue();

                    // アイテム取得処理
                    ItemStack itemStack = ProductHelper.getProductStack(itemID);
                    for (int j = 0; j < itemQty; j++) {
                        itemList.add(itemStack);
                    }
                }
                ArrayList<ItemStack> shulkerList = getShulkerList(i, itemList);

                /* 配達 */
                boolean post = MailboxHelper.postItem(chestLocation, shulkerList);
                if (post) {
                    DatabaseHelper.completeOrder(i.orderID);
                    try {
                        HttpURLConnection connection = getHttpConnection("/post/order_complete/");

                        OutputStreamWriter out = new OutputStreamWriter(connection.getOutputStream(), StandardCharsets.UTF_8);
                        out.write(String.format("uuid=%s&orderid=%s", player.getUniqueId(), i.orderID));
                        out.flush();
                        out.close();

                        connection.connect();
                        int status = connection.getResponseCode();

                        if (status != HttpURLConnection.HTTP_OK){
                            plugin.getLogger().warning("WebのUtazonに接続できませんでした");
                        }

                    }catch (ConnectException e){
                        plugin.getLogger().warning("WebのUtazonに接続できませんでした");
                    }catch (Exception e){
                        e.printStackTrace();
                    }

                }else{
                    plugin.getLogger().info(player.getName() + "のポストにアイテムを追加するスペースがありませんでした");

                    try {
                        HttpURLConnection connection = getHttpConnection("/post/mailbox_full/");

                        OutputStreamWriter out = new OutputStreamWriter(connection.getOutputStream(), StandardCharsets.UTF_8);
                        out.write(String.format("uuid=%s&orderid=%s", player.getUniqueId(), i.orderID));
                        out.flush();
                        out.close();

                        connection.connect();
                        int status = connection.getResponseCode();

                        if (status != HttpURLConnection.HTTP_OK){
                            plugin.getLogger().warning("WebのUtazonに接続できませんでした");
                        }

                        DatabaseHelper.errorOrder(i.orderID, "MailboxFull");

                    }catch (ConnectException e){
                        plugin.getLogger().warning("WebのUtazonに接続できませんでした");
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }

            /* 発送時間になったら */
            } else if (i.shipsAt.isBefore(now)){
                if (!i.dmSent) {
                    try {
                        HttpURLConnection connection = getHttpConnection("/post/ship_complete/");

                        OutputStreamWriter out = new OutputStreamWriter(connection.getOutputStream(), StandardCharsets.UTF_8);
                        out.write(String.format("uuid=%s&orderid=%s", i.uuid, i.orderID));
                        out.flush();
                        out.close();

                        connection.connect();
                        int status = connection.getResponseCode();

                        if (status != HttpURLConnection.HTTP_OK) {
                            plugin.getLogger().warning("WebのUtazonに接続できませんでした");
                        }

                        DatabaseHelper.completeDMSent(i.orderID);

                    } catch (ConnectException e) {
                        plugin.getLogger().warning("WebのUtazonに接続できませんでした");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        if (isExist) {
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
                itemMeta.setDisplayName(ChatColor.AQUA + "Utazonからのお届け物");

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
    private static String getitemList(int amount, ArrayList<String> infoList) {
        String itemName = infoList.get(0);
        String itemPrice = infoList.get(1);

        String itemFormat;
        if (infoList.get(0).length() > 11) {
            itemFormat = "\n・" + itemName.substring(0, 10) + "…"  + "\n　$" + itemPrice  + " ×" + amount;
        } else {
            itemFormat = "\n・" + itemName + "\n　$" + itemPrice  + " ×" + amount;
        }
        return itemFormat;
    }

    @NotNull
    public static HttpURLConnection getHttpConnection(String path) throws IOException {
        URL url = new URL(new URL(UtazonPlugin.webHost), path);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setDoOutput(true);
        connection.setInstanceFollowRedirects(false);
        connection.setRequestMethod("POST");
        connection.setRequestProperty("pass", UtazonPlugin.webPass);
        return connection;
    }
}
