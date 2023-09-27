package work.utakatanet.utazonplugin.util;

import org.bukkit.Location;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.block.Hopper;
import org.bukkit.block.ShulkerBox;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;

public class MailboxHelper {
    public static boolean postItem(Location location, ArrayList<ItemStack> itemStack) {
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
