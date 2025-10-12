package net.runelite.client.plugins.skillingoutfit;

import lombok.Getter;
import lombok.Setter;
import net.runelite.api.Client;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;
import net.runelite.api.InventoryID;
import net.runelite.api.widgets.Widget;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;

import javax.inject.Inject;
import javax.swing.*;
import java.util.*;

@Getter
@Setter
public class SkillingOutfitTracker
{
    private int carpenterPoints = 0;
    private int carpenterContracts = 0;
    private int farmingPoints = 0;
    private int foundryPoints = 0;
    private int temporossPoints = 0;
    private int hunterRumors = 0;
    private int wintertodtCrates = 0;
    private int animaBark;

    private final Map<Integer, Boolean> ownedCache = new HashMap<>();
    private final Set<Integer> obtainedItems = new HashSet<>();
    private final Map<Integer, Integer> inventoryCostCache = new HashMap<>();
    private final Map<Integer, Integer> equipmentCostCache = new HashMap<>();
    private final Map<Integer, Integer> bankCostCache = new HashMap<>();

    private final Client client;
    private final ClientThread clientThread;
    private final ConfigManager configManager;
    private final String configGroup = "skillingoutfit";

    private Map<Integer, Integer> inventoryCacheSnapshot = new HashMap<>();
    private Map<Integer, Integer> equipmentCacheSnapshot = new HashMap<>();
    private Map<Integer, Integer> bankCacheSnapshot = new HashMap<>();

    @Setter
    private SkillingOutfitPanel panel;

    @Inject
    public SkillingOutfitTracker(Client client, ClientThread clientThread, ConfigManager configManager)
    {
        this.client = client;
        this.clientThread = clientThread;
        this.configManager = configManager;

        loadObtainedItems();
        loadBankCache();
    }

    // ======== UPDATE ALL CACHES ========
    public void updateAllCaches() {
        updateInventoryCache();
        updateEquipmentCache();
        updateBankCache();
    }

    public void updateInventoryCache()
    {
        // Clear previous cache
        inventoryCostCache.clear();

        // Ensure this runs on the client thread
        clientThread.invoke(() ->
        {
            ItemContainer inv = client.getItemContainer(InventoryID.INVENTORY);
            if (inv != null)
            {
                for (Item item : inv.getItems())
                {
                    int id = item.getId();
                    int qty = item.getQuantity();
                    inventoryCostCache.put(id, qty);
                }
            }
            // Update snapshot for safe panel/thread reading
            inventoryCacheSnapshot = new HashMap<>(inventoryCostCache);

        });
    }

    public void updateEquipmentCache()
    {
        equipmentCostCache.clear();

        ItemContainer equipment = client.getItemContainer(InventoryID.EQUIPMENT);
        if (equipment != null)
        {
            for (Item item : equipment.getItems())
            {
                equipmentCostCache.put(item.getId(), item.getQuantity());
            }
        }

        equipmentCacheSnapshot = new HashMap<>(equipmentCostCache);
    }

    public void updateBankCache()
    {
        ItemContainer bank = client.getItemContainer(InventoryID.BANK);
        if (bank != null)
        {
            Map<Integer, Integer> newBankSnapshot = new HashMap<>();
            for (Item item : bank.getItems())
                newBankSnapshot.put(item.getId(), item.getQuantity());

            bankCacheSnapshot = newBankSnapshot;

            // Save to config immediately
            saveBankCache();
        }
    }

    public void saveBankCache()
    {
        if (bankCacheSnapshot.isEmpty())
            return;

        StringBuilder sb = new StringBuilder();
        for (Map.Entry<Integer, Integer> entry : bankCacheSnapshot.entrySet())
        {
            sb.append(entry.getKey()).append(":").append(entry.getValue()).append(",");
        }
        if (sb.length() > 0)
            sb.setLength(sb.length() - 1); // remove trailing comma

        configManager.setConfiguration(configGroup, "bankCache", sb.toString());

        System.out.println("[SOT] Saved bank cache: " + sb.toString());
    }

    public void loadBankCache()
    {
        String saved = configManager.getConfiguration(configGroup, "bankCache");
        if (saved == null || saved.isEmpty())
            return;

        Map<Integer, Integer> loaded = new HashMap<>();
        for (String pair : saved.split(","))
        {
            String[] parts = pair.split(":");
            if (parts.length != 2)
                continue;
            try
            {
                int itemId = Integer.parseInt(parts[0]);
                int qty = Integer.parseInt(parts[1]);
                loaded.put(itemId, qty);
            }
            catch (NumberFormatException ignored) {}
        }

        bankCacheSnapshot = loaded;
        System.out.println("[SOT] Loaded bank cache: " + loaded);
    }

    // ======== TOTAL COST ITEMS COMBINED ========
    public int getTotalCostItem(int costId)
    {
        int inv = inventoryCacheSnapshot.getOrDefault(costId, 0);
        int equip = equipmentCacheSnapshot.getOrDefault(costId, 0);
        int bank = bankCacheSnapshot.getOrDefault(costId, 0);

        return inv + equip + bank;
    }


    // ======== OWNED ITEM CHECKS ========
    private boolean isItemOwnedCached(int itemId)
    {
        int invCount = getInventoryCacheSnapshot().getOrDefault(itemId, 0);
        int equipCount = getEquipmentCacheSnapshot().getOrDefault(itemId, 0);
        int bankCount = getBankCacheSnapshot().getOrDefault(itemId, 0);

        return (invCount + equipCount + bankCount) > 0;
    }

    public void updateOwnedItems()
    {
        for (SkillingOutfitData.SkillingOutfitDataEntry outfitEntry : SkillingOutfitData.OUTFITS_DATA.values())
        {
            for (SkillingOutfitItem item : outfitEntry.items.values())
            {
                boolean owned = getTotalCostItem(item.getItemId()) > 0; // use combined total
                ownedCache.put(item.getItemId(), owned);
            }
        }
    }

    // ======== OBTAINED ITEMS ========
    public void addObtainedItem(int itemId)
    {
        if (obtainedItems.add(itemId))  // only save if it’s new
        {
            //PRINTOUT System.out.println("[SOT] [addObtainedItem] Adding new obtained item: " + itemId);
            saveObtainedItems();

            // Update caches and refresh panel
            if (panel != null)
            {
                SwingUtilities.invokeLater(panel::updateAllCaches);
            }
        }
        //PRINTOUT else
        //PRINTOUT  {
            //PRINTOUT System.out.println("[SOT] [addObtainedItem] Item already obtained: " + itemId);
        //PRINTOUT }
    }



    public void saveObtainedItems()
    {
        StringBuilder sb = new StringBuilder();
        for (int id : obtainedItems)
            sb.append(id).append(",");
        if (sb.length() > 0)
            sb.setLength(sb.length() - 1);

        configManager.setConfiguration(configGroup, "obtainedItems", sb.toString());
        //PRINTOUT   System.out.println("[SOT] [saveObtainedItems] Saved obtained items to config: " + sb.toString());
    }

    public void loadObtainedItems()
    {
        String saved = configManager.getConfiguration(configGroup, "obtainedItems");
        if (saved != null && !saved.isEmpty())
        {
            for (String s : saved.split(","))
            {
                try
                {
                    int itemId = Integer.parseInt(s);
                    obtainedItems.add(itemId);
                    //PRINTOUT  System.out.println("[SOT] [loadObtainedItems] Loaded obtained item from config: " + itemId);
                }
                catch (NumberFormatException ignored) {}
            }
        }
        //PRINTOUT else
        //PRINTOUT{
            //PRINTOUT     System.out.println("[SOT] [loadObtainedItems] No obtained items found in config");
            //PRINTOUT }
    }

    public void updateOwnedItemsFromCaches()
    {
        Set<Integer> allItems = new HashSet<>();
        allItems.addAll(getInventoryCacheSnapshot().keySet());
        allItems.addAll(getEquipmentCacheSnapshot().keySet());
        allItems.addAll(getBankCacheSnapshot().keySet());

        boolean anyNew = false;

        for (int itemId : allItems)
        {
            if (obtainedItems.add(itemId)) // only new items
            {
                ownedCache.put(itemId, true);
                anyNew = true;
            }
        }

        if (anyNew)
        {
            saveObtainedItems();   // only save once if there were new items
        }

        // Update cost totals so buildPointsLine shows correct x/x
        refreshCostItemCache();

        // Optionally refresh panel UI
        if (panel != null)
        {
            SwingUtilities.invokeLater(() -> panel.updateAllCaches());
        }
    }


    public void markObtainedFromConfig()
    {
        // Load items saved in config
        loadObtainedItems();

        // Add all previously obtained items to ownedCache so UI knows you own them
        for (int itemId : obtainedItems)
        {
            ownedCache.put(itemId, true);
        }

        // Debug: print all obtained items marked from config
        //PRINTOUT System.out.println("[SOT] [markObtainedFromConfig] Marking obtained items from config: " + obtainedItems);
    }


    // ======== COST ITEM CACHE ========
    private final Map<Integer, Integer> costItemCache = new HashMap<>();

    private int loadCostItem(int costItemId)
    {
        Integer stored = configManager.getRSProfileConfiguration(configGroup, "costItem_" + costItemId, Integer.class);
        return (stored != null) ? stored : 0;
    }

    public void loadAllCostItems()
    {
        for (SkillingOutfitData.SkillingOutfitDataEntry outfitEntry : SkillingOutfitData.OUTFITS_DATA.values())
        {
            for (SkillingOutfitItem item : outfitEntry.items.values())
            {
                int costId = item.getCostItemId();
                costItemCache.put(costId, loadCostItem(costId));
            }
        }
    }

    public void refreshCostItemCache()
    {
        costItemCache.clear();

        for (SkillingOutfitData.SkillingOutfitDataEntry outfitEntry : SkillingOutfitData.OUTFITS_DATA.values())
        {
            for (SkillingOutfitItem item : outfitEntry.items.values())
            {
                int costId = item.getCostItemId();
                int total = getTotalCostItem(costId); // inventory+equipment+bank
                costItemCache.put(costId, total);

                // Debug print
                System.out.println("[SOT] [refreshCostItemCache] Loading cost item: ID=" + costId
                        + ", Name=" + item.getCostText()
                        + ", Total Owned=" + total);
            }
        }
    }

}
