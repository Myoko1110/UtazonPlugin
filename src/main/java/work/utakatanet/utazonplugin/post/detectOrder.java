package work.utakatanet.utazonplugin.post;

import org.bukkit.*;
import org.bukkit.block.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import work.utakatanet.utazonplugin.UtazonPlugin;
import work.utakatanet.utazonplugin.data.OrderList;
import work.utakatanet.utazonplugin.util.DatabaseHelper;
import work.utakatanet.utazonplugin.util.ProductHelper;

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
                    return;
                }


                ArrayList<ItemStack> itemList = new ArrayList<>();

                // 納品書を作成
                ItemStack deliverySlip = new ItemStack(Material.WRITTEN_BOOK);
                BookMeta bookMeta = (BookMeta) deliverySlip.getItemMeta();
                if (bookMeta != null){
                    bookMeta.setTitle("納品書 #" + i.orderID);
                    bookMeta.setAuthor("Utazon");

                    NumberFormat numberFormat = new DecimalFormat("###,##0.00");
                    String value = String.format(
                            "           納品書\n              発行 Utazon\n納品日 %s/%s/%s\n\n下記の通り納品いたします\n\n合計額：$%s\n────────────",
                            now.getYear(), now.getMonthValue(), now.getDayOfMonth(), numberFormat.format(i.amount));

                    StringBuilder itemInfo = new StringBuilder();
                    for (int[] item: orderItem){
                        int itemID = item[0];
                        ArrayList<Object> infoList = DatabaseHelper.geItemInfo(itemID);
                        if (infoList != null){
                            String itemFormat = "\n・" + infoList.get(0).toString();
                            itemInfo.append(itemFormat);
                        }
                    }

                    value += itemInfo;

                    bookMeta.addPage(value);
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


                postItem(chestLocation, shulkerList);
                DatabaseHelper.completeOrder(i.orderID);
            }
        }
    }

    public void postItem(Location location, ArrayList<ItemStack> itemStack) {
        try {
            BlockState b = location.getBlock().getState();

            if (b instanceof Chest) {
                Chest chest = (Chest) b;
                for (ItemStack i : itemStack) {
                    chest.getInventory().addItem(i);
                }

            } else if (b instanceof ShulkerBox) {
                ShulkerBox shulkerBox = (ShulkerBox) b;
                for (ItemStack i : itemStack) {
                    shulkerBox.getInventory().addItem(i);
                }

            } else if (b instanceof Hopper) {
                Hopper hopper = (Hopper) b;
                for (ItemStack i : itemStack) {
                    hopper.getInventory().addItem(i);
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }

    }
}
