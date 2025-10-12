package net.runelite.client.plugins.skillingoutfit;

import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.Player;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.game.ItemManager;
import net.runelite.client.ui.PluginPanel;
import net.runelite.api.ChatMessageType;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BooleanSupplier;
import java.util.stream.Collectors;

public class SkillingOutfitPanel extends PluginPanel
{
    private final Client client;
    private final ItemManager itemManager;
    private final SkillingOutfitTracker tracker;
    private final SkillingOutfitConfig config;
    private final ConfigManager configManager;
    private final SkillingOutfitPlugin plugin;
    private final ClientThread clientThread;

    private final Map<Integer, BufferedImage> itemSprites = new HashMap<>();
    private final Map<Integer, Rectangle> iconBounds = new HashMap<>();
    private final Map<String, Rectangle> outfitBounds = new HashMap<>();
    private final Map<String, BooleanSupplier> outfitDisplayMap = new HashMap<>();
    private final Map<String, Integer> remainingCounts = new HashMap<>();

    private final Map<Integer, Integer> inventoryCacheSnapshot = new ConcurrentHashMap<>();
    private final Map<Integer, Integer> equipmentCacheSnapshot = new ConcurrentHashMap<>();
    private final Map<Integer, Integer> bankCacheSnapshot = new ConcurrentHashMap<>();

    private JPanel innerPanel;
    private int hoveredItemId = -1;
    private String hoveredOutfitName = null;

    private static final Map<String, Color> SKILL_COLORS = Map.ofEntries(
            Map.entry("Agility", new Color(0x33FF33)),
            Map.entry("Construction", new Color(0xCC9966)),
            Map.entry("Farming", new Color(0x33CC33)),
            Map.entry("Firemaking", new Color(0xFF3300)),
            Map.entry("Fishing", new Color(0x3399FF)),
            Map.entry("Hunter", new Color(0x66CC00)),
            Map.entry("Mining", new Color(0x999999)),
            Map.entry("Prayer", new Color(0xFFFFFF)),
            Map.entry("Runecraft", new Color(0x996633)),
            Map.entry("Smithing", new Color(0xCCCCCC)),
            Map.entry("Thieving", new Color(0xFFFF00)),
            Map.entry("Woodcutting", new Color(0x996633))
    );

    public SkillingOutfitPanel(Client client, ItemManager itemManager, SkillingOutfitTracker tracker,
                               SkillingOutfitConfig config, ConfigManager configManager,
                               SkillingOutfitPlugin plugin, ClientThread clientThread)
    {
        this.client = client;
        this.itemManager = itemManager;
        this.tracker = tracker;
        this.config = config;
        this.configManager = configManager;
        this.plugin = plugin;
        this.clientThread = clientThread; // now works correctly

        setBackground(new Color(40, 40, 40));
        setLayout(new BorderLayout());

        innerPanel = new JPanel() {
            @Override
            public Dimension getPreferredSize() {
                return calculatePreferredSize();
            }

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                paintItems((Graphics2D) g);
            }
        };
        innerPanel.setBackground(new Color(40, 40, 40));

        preloadSprites();
        setupOutfitDisplayMap();

        innerPanel.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                hoveredItemId = getItemAt(e.getX(), e.getY());
                hoveredOutfitName = getOutfitAt(e.getX(), e.getY());
                innerPanel.repaint();
            }
        });

        innerPanel.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseClicked(MouseEvent e)
            {
                int itemId = getItemAt(e.getX(), e.getY());
                if (itemId != -1)
                {
                    // Open item wiki
                    openWikiLink("https://oldschool.runescape.wiki/Special:Lookup?utm_source=wiki&type=item&id=" + itemId);
                    return;
                }

                String outfitName = getOutfitAt(e.getX(), e.getY());
                if (outfitName != null)
                {
                    SkillingOutfitData.SkillingOutfitDataEntry entry = SkillingOutfitData.OUTFITS_DATA.get(outfitName);
                    if (entry != null && entry.wikiUrl != null && !entry.wikiUrl.isEmpty())
                    {
                        openWikiLink(entry.wikiUrl);
                    }
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(innerPanel);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);

    }

    private void preloadSprites()
    {
        for (SkillingOutfitData.SkillingOutfitDataEntry entry : SkillingOutfitData.OUTFITS_DATA.values())
            for (int itemId : entry.items.keySet())
                itemSprites.put(itemId, itemManager.getImage(itemId, 1, false));
    }

    public void setupOutfitDisplayMap()
    {
        for (var entry : SkillingOutfitData.OUTFITS_DATA.entrySet())
        {
            String outfitName = entry.getKey();
            String configKey = entry.getValue().configKey;
            outfitDisplayMap.put(outfitName, () -> {
                Object result = configManager.getConfiguration("skillingoutfit", configKey);
                return result != null && Boolean.parseBoolean(result.toString());
            });
        }
    }

    private void paintItems(Graphics2D g)
    {
        // Debug print to verify how many obtained items we have when painting
        //PRINTOUT System.out.println("[SOT] [paintItems] Drawing panel — obtained items: "
        //PRINTOUT       + (tracker != null ? tracker.getObtainedItems().size() : "tracker null"));

        int panelWidth = getWidth();
        int yOffset = config.panelTitleSpacer();
        FontMetrics fm = g.getFontMetrics();

        // Title
        g.setColor(Color.WHITE);
        String title = "Skilling Outfit Tracker";
        g.drawString(title, (panelWidth - fm.stringWidth(title)) / 2, yOffset + fm.getAscent());
        yOffset += fm.getHeight();

        // Display outfit and item collected counts
        yOffset = paintCollectedCounts(g, panelWidth, yOffset, fm);

        Player localPlayer = client.getLocalPlayer();
        boolean isFemale = localPlayer != null && localPlayer.getPlayerComposition() != null && localPlayer.getPlayerComposition().isFemale();

        for (String outfitName : SkillingOutfitData.OUTFITS_DATA.keySet())
        {
            if (!shouldDisplayOutfit(outfitName)) continue;

            SkillingOutfitData.SkillingOutfitDataEntry entry = SkillingOutfitData.OUTFITS_DATA.get(outfitName);
            if (entry == null) continue;

            if (outfitName.equals("Farming - Farmer's Outfit Male") && isFemale) continue;
            if (outfitName.equals("Farming - Farmer's Outfit Female") && !isFemale) continue;

            yOffset = paintOutfit(entry, outfitName, g, panelWidth, yOffset, fm);
        }

        drawItemHoverTooltip(g);
        drawOutfitHoverTooltip(g);
    }

    private int paintCollectedCounts(Graphics2D g, int panelWidth, int yOffset, FontMetrics fm)
    {
        if (config.displayCollectedOutfits())
        {
            int ownedOutfits = (int) SkillingOutfitData.OUTFITS_DATA.values().stream()
                    .filter(entry -> entry.items.values().stream().allMatch(item -> isItemOwnedCached(item.getItemId())))
                    .count();
            int totalOutfits = SkillingOutfitData.OUTFITS_DATA.size();
            String text = "Collected: " + ownedOutfits + " / " + totalOutfits + " Outfits";
            g.setColor(getCollectedColor(ownedOutfits, totalOutfits));
            yOffset += config.outfitTextSpacer();
            g.drawString(text, (panelWidth - fm.stringWidth(text)) / 2, yOffset + fm.getAscent());
            yOffset += fm.getHeight();
        }

        if (config.displayCollectedItems())
        {
            int ownedItems = (int) SkillingOutfitData.OUTFITS_DATA.values().stream()
                    .flatMap(entry -> entry.items.values().stream())
                    .mapToInt(item -> isItemOwnedCached(item.getItemId()) ? 1 : 0)
                    .sum();
            int totalItems = SkillingOutfitData.OUTFITS_DATA.values().stream().mapToInt(entry -> entry.items.size()).sum();
            String text = "Collected: " + ownedItems + " / " + totalItems + " Items";
            g.setColor(getCollectedColor(ownedItems, totalItems));
            yOffset += config.itemTextSpacer();
            g.drawString(text, (panelWidth - fm.stringWidth(text)) / 2, yOffset + fm.getAscent());
            yOffset += fm.getHeight();
        }
        return yOffset;
    }

    private int paintOutfit(SkillingOutfitData.SkillingOutfitDataEntry entry, String outfitName, Graphics2D g, int panelWidth, int yOffset, FontMetrics fm)
    {
        Map<Integer, SkillingOutfitItem> items = entry.items;
        boolean allObtained = items.values().stream().allMatch(item -> isItemOwnedCached(item.getItemId()));
        if (allObtained && !config.showObtainedItems()) return yOffset;

        // Outfit title
        yOffset += config.firstOutfitSpacer();
        Color outfitColor = (entry.primarySkill != null) ? SKILL_COLORS.getOrDefault(entry.primarySkill, Color.CYAN) : Color.CYAN;
        g.setColor(outfitColor);
        int nameWidth = fm.stringWidth(outfitName);
        int outfitX = (panelWidth - nameWidth) / 2;
        g.drawString(outfitName, outfitX, yOffset + fm.getAscent());
        outfitBounds.put(outfitName, new Rectangle(outfitX, yOffset, nameWidth, fm.getHeight()));
        yOffset += fm.getHeight();

        // Points / cost
        yOffset += config.totalNeededTextSpacer();
        String pointsLine = buildPointsLine(entry, items);
        if (!pointsLine.isEmpty())
        {
            g.setColor(Color.LIGHT_GRAY);
            g.drawString(pointsLine, (panelWidth - fm.stringWidth(pointsLine)) / 2, yOffset + fm.getAscent());
            yOffset += fm.getHeight();
        }

        // Icons
        yOffset += config.iconTextSpacer();
        List<Map.Entry<Integer, SkillingOutfitItem>> itemList = items.entrySet().stream()
                .filter(e -> config.showObtainedItems() || !isItemOwnedCached(e.getKey()))
                .collect(Collectors.toList());

        int totalItems = itemList.size();
        int rows = (int) Math.ceil(totalItems / (double) config.maxCols());

        for (int row = 0; row < rows; row++)
        {
            int itemsInRow = Math.min(config.maxCols(), totalItems - row * config.maxCols());
            int rowWidth = itemsInRow * config.iconSize() + (itemsInRow - 1) * config.iconGapSpacing();
            int startX = (panelWidth - rowWidth) / 2;

            for (int col = 0; col < itemsInRow; col++)
            {
                int index = row * config.maxCols() + col;
                var entryItem = itemList.get(index);
                int itemId = entryItem.getKey();
                BufferedImage sprite = itemSprites.get(itemId);
                if (sprite == null) continue;

                int x = startX + col * (config.iconSize() + config.iconGapSpacing());
                int y = yOffset + row * (config.iconSize() + config.iconGapSpacing());
                g.drawImage(sprite, x, y, config.iconSize(), config.iconSize(), null);

                if (isItemOwnedCached(itemId) && config.showObtainedItems())
                {
                    g.setColor(Color.GREEN);
                    g.setStroke(new BasicStroke(2));
                    g.drawRect(x, y, config.iconSize(), config.iconSize());
                }

                iconBounds.put(itemId, new Rectangle(x, y, config.iconSize(), config.iconSize()));
            }
        }
        yOffset += rows * (config.iconSize() + config.iconGapSpacing());

        return yOffset;
    }

    // Updated buildPointsLine to only count remaining unowned items
    private String buildPointsLine(SkillingOutfitData.SkillingOutfitDataEntry entry, Map<Integer, SkillingOutfitItem> items)
    {
        // Skip entirely if the config disables this line
        if (!config.showTotalObtain())
        {
            return "";
        }

        int totalRequired = 0;
        int costItemId = -1;
        String costText = "";

        // Calculate remaining requirements only for items not yet obtained
        for (SkillingOutfitItem item : items.values())
        {
            if (!isItemOwnedCached(item.getItemId()))
            {
                totalRequired += item.getRequirement();
                if (costItemId == -1)
                {
                    costItemId = item.getCostItemId();
                    costText = item.getCostText();
                }
            }
        }

        int totalAvailable = (costItemId != -1) ? tracker.getTotalCostItem(costItemId) : 0;

        switch (entry.primarySkill)
        {
            case "Construction":
                return tracker.getCarpenterPoints() + "/" + totalRequired + " Carpenter Points Owned";
            case "Farming":
                return tracker.getFarmingPoints() + "/" + totalRequired + " Farming Points Owned";
            case "Smithing":
                return tracker.getFoundryPoints() + "/" + totalRequired + " Foundry Reputation Owned";
            case "Firemaking":
                return tracker.getWintertodtCrates() + " Crates Opened";
            case "Fishing":
                return tracker.getTemporossPoints() + " Tempoross Kills";
            case "Hunter":
                return tracker.getHunterRumors() + " Hunter Rumors Completed";
            case "Agility":
                return totalAvailable + "/" + totalRequired + " " + costText + " Owned";
            case "Woodcutting":
                return tracker.getAnimaBark() + "/" + totalRequired +  " " + costText + " Owned";
            default:
                return totalAvailable + "/" + totalRequired + " " + costText + " Owned";
        }
    }



    private int sumRequirements(Map<Integer, SkillingOutfitItem> items)
    {
        return items.values().stream()
                .filter(item -> !isItemOwnedCached(item.getItemId())) // only count unowned items
                .mapToInt(SkillingOutfitItem::getRequirement)
                .sum();
    }

    private boolean shouldDisplayOutfit(String outfitName)
    {
        BooleanSupplier supplier = outfitDisplayMap.get(outfitName);
        return supplier != null && supplier.getAsBoolean();
    }

    private Color getCollectedColor(long owned, long total)
    {
        if (!config.colorTextForCollected()) return Color.WHITE;
        double percent = total == 0 ? 0 : (owned * 100.0 / total);
        if (percent <= 33) return Color.RED;
        else if (percent <= 65) return Color.ORANGE;
        else if (percent < 100) return Color.YELLOW;
        else return Color.GREEN;
    }

    private int getItemAt(int x, int y)
    {
        for (var entry : iconBounds.entrySet())
            if (entry.getValue().contains(x, y)) return entry.getKey();
        return -1;
    }

    private String getOutfitAt(int x, int y)
    {
        for (var entry : outfitBounds.entrySet())
            if (entry.getValue().contains(x, y)) return entry.getKey();
        return null;
    }

    private boolean isItemOwnedCached(int itemId)
    {
        // First, find the SkillingOutfitItem for this ID
        SkillingOutfitItem outfitItem = null;
        outer:
        for (var entry : SkillingOutfitData.OUTFITS_DATA.values())
            for (SkillingOutfitItem i : entry.items.values())
                if (i.getItemId() == itemId)
                {
                    outfitItem = i;
                    break outer;
                }

        // If we have an outfitItem, check both main ID and otherItemIds
        if (outfitItem != null)
        {
            // Combine main ID + other IDs into one stream
            List<Integer> allIds = new ArrayList<>();
            allIds.add(outfitItem.getItemId());
            allIds.addAll(outfitItem.getOtherItemIds());

            for (int id : allIds)
            {
                if (tracker.getObtainedItems().contains(id)) return true;

                int inv = inventoryCacheSnapshot.getOrDefault(id, 0);
                int equip = equipmentCacheSnapshot.getOrDefault(id, 0);
                int bank = bankCacheSnapshot.getOrDefault(id, 0);
                if ((inv + equip + bank) > 0) return true;
            }

            return false;
        }

        // fallback: check only tracker and caches for the single ID
        if (tracker.getObtainedItems().contains(itemId)) return true;
        int inv = inventoryCacheSnapshot.getOrDefault(itemId, 0);
        int equip = equipmentCacheSnapshot.getOrDefault(itemId, 0);
        int bank = bankCacheSnapshot.getOrDefault(itemId, 0);
        return (inv + equip + bank) > 0;
    }


    public void updateAllCaches()
    {
        clientThread.invoke(() -> {
            tracker.updateAllCaches();
            inventoryCacheSnapshot.clear(); inventoryCacheSnapshot.putAll(tracker.getInventoryCacheSnapshot());
            equipmentCacheSnapshot.clear(); equipmentCacheSnapshot.putAll(tracker.getEquipmentCacheSnapshot());
            bankCacheSnapshot.clear(); bankCacheSnapshot.putAll(tracker.getBankCacheSnapshot());
            SwingUtilities.invokeLater(innerPanel::repaint);
        });
    }

    public int getSnapshotTotalCostItem(int itemId)
    {
        return inventoryCacheSnapshot.getOrDefault(itemId, 0)
                + equipmentCacheSnapshot.getOrDefault(itemId, 0)
                + bankCacheSnapshot.getOrDefault(itemId, 0);
    }

    private Dimension calculatePreferredSize()
    {
        int width = 300, height = config.panelTitleSpacer();
        FontMetrics fm = getFontMetrics(getFont());

        if (config.displayCollectedOutfits()) height += config.outfitTextSpacer() + fm.getHeight();
        if (config.displayCollectedItems()) height += config.itemTextSpacer() + fm.getHeight();

        for (String outfitName : SkillingOutfitData.OUTFITS_DATA.keySet())
        {
            if (!shouldDisplayOutfit(outfitName)) continue;
            SkillingOutfitData.SkillingOutfitDataEntry entry = SkillingOutfitData.OUTFITS_DATA.get(outfitName);
            if (entry == null) continue;
            height += config.firstOutfitSpacer() + fm.getHeight() + config.totalNeededTextSpacer() + fm.getHeight() + config.iconTextSpacer();
            int rows = (int) Math.ceil(entry.items.size() / (double) config.maxCols());
            height += rows * (config.iconSize() + config.iconGapSpacing());
        }
        return new Dimension(width, height);
    }

    public void refresh()
    {
        setupOutfitDisplayMap();
        if (innerPanel != null)
        {
            innerPanel.revalidate();
            innerPanel.repaint();
        }
    }

    private void drawItemHoverTooltip(Graphics2D g)
    {
        if (hoveredItemId == -1) return;
        Rectangle iconRect = iconBounds.get(hoveredItemId);
        if (iconRect == null) return;

        SkillingOutfitItem item = null;
        outer:
        for (var entry : SkillingOutfitData.OUTFITS_DATA.values())
            for (SkillingOutfitItem i : entry.items.values())
                if (i.getItemId() == hoveredItemId) { item = i; break outer; }

        if (item == null) return;

        drawTooltip(g, iconRect, List.of(
                item.getName(),
                item.getRequirement() + " " + item.getCostText(),
                "Click To Open Wiki"
        ));
    }

    private void drawOutfitHoverTooltip(Graphics2D g)
    {
        if (hoveredOutfitName == null) return;
        Rectangle outfitRect = outfitBounds.get(hoveredOutfitName);
        if (outfitRect == null) return;

        SkillingOutfitData.SkillingOutfitDataEntry entry = SkillingOutfitData.OUTFITS_DATA.get(hoveredOutfitName);
        if (entry == null) return;

        int owned = (int) entry.items.values().stream().filter(i -> isItemOwnedCached(i.getItemId())).count();
        int total = entry.items.size();
        int totalRequired = entry.items.values().stream().filter(i -> !isItemOwnedCached(i.getItemId())).mapToInt(SkillingOutfitItem::getRequirement).sum();
        int costItemId = entry.items.values().stream().filter(i -> !isItemOwnedCached(i.getItemId())).mapToInt(SkillingOutfitItem::getCostItemId).findFirst().orElse(-1);
        int costAvailable = getSnapshotTotalCostItem(costItemId);
        String costText = entry.items.values().stream().filter(i -> !isItemOwnedCached(i.getItemId())).map(SkillingOutfitItem::getCostText).findFirst().orElse("");

        List<String> lines = new ArrayList<>();
        lines.add("Owned: " + owned + "/" + total + " Items");
        if (totalRequired > 0) lines.add(costAvailable + "/" + totalRequired + " " + costText + " Owned");
        lines.add("Click To Open Wiki");

        drawTooltip(g, outfitRect, lines);
    }

    private void drawTooltip(Graphics2D g, Rectangle rect, List<String> lines)
    {
        FontMetrics fm = g.getFontMetrics();
        int padding = 4;
        int overlayWidth = lines.stream().mapToInt(fm::stringWidth).max().orElse(0) + padding * 2;
        int overlayHeight = lines.size() * fm.getHeight() + padding * 4;

        int x = (getWidth() - overlayWidth) / 2;
        int y = rect.y + rect.height + 5;

        g.setColor(new Color(0, 0, 0, 220));
        g.fillRect(x, y, overlayWidth, overlayHeight);
        g.setColor(Color.WHITE);
        g.drawRect(x, y, overlayWidth, overlayHeight);

        for (int i = 0; i < lines.size(); i++)
            g.drawString(lines.get(i), x + (overlayWidth - fm.stringWidth(lines.get(i))) / 2, y + padding + i * fm.getHeight() + fm.getAscent());
    }

    private void openWikiLink(String url)
    {
        try
        {
            url = url.replace(" ", "_");
            Desktop.getDesktop().browse(URI.create(url));
        }
        catch (Exception e)
        {
            try
            {
                // Fallback: Encode manually
                String encoded = java.net.URLEncoder.encode(url, StandardCharsets.UTF_8);
                Desktop.getDesktop().browse(URI.create(encoded));
            }
            catch (Exception ex)
            {

            }
        }
    }

}
