package net.runelite.client.plugins.skillingoutfit;

import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.ItemContainerChanged;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.chat.ChatMessageBuilder;
import net.runelite.client.chat.ChatMessageManager;
import net.runelite.client.chat.QueuedMessage;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.game.ItemManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;

import javax.inject.Inject;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.function.Consumer;

@PluginDescriptor(
        name = "Skilling Outfit Tracker",
        description = "Tracks skilling outfits obtained (inventory, bank, or equipped)",
        tags = {"skilling", "outfit", "tracking"}
)
@Slf4j
public class SkillingOutfitPlugin extends Plugin
{
    @Inject private Client client;
    @Inject private SkillingOutfitTracker tracker;
    @Inject private ItemManager itemManager;
    @Inject private ClientToolbar clientToolbar;
    @Inject private ClientThread clientThread;
    @Inject private ChatMessageManager chatMessageManager;
    @Inject private SkillingOutfitConfig config;
    @Inject private ConfigManager configManager;
    @Inject private EventBus eventBus;

    private NavigationButton navButton;
    private boolean notifyOnNew;
    private SkillingOutfitWindow popoutWindow;
    private SkillingOutfitPanel panel;
    private final String configGroup = "skillingoutfit";

    // ===== Patterns for minigame tracking =====
    private static final Pattern CONTRACT_PATTERN = Pattern.compile(
            "You have completed <col=[0-9a-f]+>(\\d+)</col> contracts with a total of <col=[0-9a-f]+>(\\d+)</col> points\\."
    );
    // Farming Not Completed
    private static final Pattern TITHE_FARM_PATTERN = Pattern.compile(
            "You now have <col=[0-9a-f]+>(\\d+)</col> reward points\\."
    );
    //Foundry Not Completed
    private static final Pattern FOUNDRY_PATTERN = Pattern.compile(
            "You now have <col=[0-9a-f]+>(\\d+)</col> Foundry Reputation\\."
    );
    private static final Pattern TEMPOROSS_PATTERN = Pattern.compile(
            "Your Tempoross kill count is: <col=[0-9a-f]+>(\\d+)</col>\\."
    );
    // Hunter Not Completed
    private static final Pattern HUNTER_PATTERN = Pattern.compile(
            "You have <col=[0-9a-f]+>(\\d+)</col> completed hunter rumours\\."
    );
    // Wintertodt Not Completed?
    private static final Pattern WINTERTODT_PATTERN = Pattern.compile(
            "You have received <col=[0-9a-f]+>(\\d+)</col> Wintertodt supply crates\\."
    );

    // ===== Startup =====
    @Override
    protected void startUp()
    {
        panel = new SkillingOutfitPanel(client, itemManager, tracker, config, configManager, this, clientThread);

        configManager.setConfiguration("skillingoutfit", "enablePopoutConfigMode", "false");

        BufferedImage icon = resizeIcon(itemManager.getImage(24554, 1, true), 16, 16);

        navButton = NavigationButton.builder()
                .tooltip("Skilling Outfit Tracker")
                .icon(icon)
                .priority(1)
                .panel(panel)
                .build();

        clientToolbar.addNavigation(navButton);

        notifyOnNew = config.notifyOnNew();

        clientThread.invokeLater(() -> {
            // 1️⃣ Load previously obtained items
            tracker.loadObtainedItems();

            // 2️⃣ Update caches
            tracker.updateInventoryCache();
            tracker.updateEquipmentCache();
            tracker.updateBankCache();

            // 3️⃣ Delay until next tick to ensure bank is loaded
            clientThread.invokeLater(() -> {
                // Merge caches into obtained items
                tracker.updateOwnedItemsFromCaches();

                // Update ownedCache for UI
                tracker.updateOwnedItems();

                // Save back to config
                tracker.saveObtainedItems();

                // Refresh panel
                safeUpdatePanel(panel::updateAllCaches);

                // Debug
                System.out.println("[SOT] [startUp] Obtained items on login: " + tracker.getObtainedItems());
            });
        });


        // Load minigame stats
        loadMinigameStat("mahoganyContracts", tracker::setCarpenterContracts, 0);
        loadMinigameStat("mahoganyPoints", tracker::setCarpenterPoints, 0);
        loadMinigameStat("farmingPoints", tracker::setFarmingPoints, 0);
        loadMinigameStat("foundryPoints", tracker::setFoundryPoints, 0);
        loadMinigameStat("temporossPoints", tracker::setTemporossPoints, 0);
        loadMinigameStat("hunterRumors", tracker::setHunterRumors, 0);
        loadMinigameStat("wintertodtCrates", tracker::setWintertodtCrates, 0);
    }




    // ===== Shutdown =====
    @Override
    protected void shutDown()
    {
        if (navButton != null) clientToolbar.removeNavigation(navButton);
        if (popoutWindow != null)
        {
            popoutWindow.dispose();
            popoutWindow = null;
        }
    }

    // ===== Chat Message Parsing =====
    @Subscribe
    public void onChatMessage(ChatMessage event)
    {
        if (event.getType() != ChatMessageType.GAMEMESSAGE) return;

        String message = event.getMessage();

        // Mahogany Homes
        if (handleTwoValueMessage(CONTRACT_PATTERN, message,
                (contracts, points) -> {
                    tracker.setCarpenterContracts(contracts);
                    tracker.setCarpenterPoints(points);
                    persistConfig("mahoganyContracts", contracts);
                    persistConfig("mahoganyPoints", points);
                })) return;

        // Single-value minigames
        if (handleSingleValueMessage(TITHE_FARM_PATTERN, message, tracker::setFarmingPoints, "farmingPoints")) return;
        if (handleSingleValueMessage(FOUNDRY_PATTERN, message, tracker::setFoundryPoints, "foundryPoints")) return;
        if (handleSingleValueMessage(TEMPOROSS_PATTERN, message, tracker::setTemporossPoints, "temporossPoints")) return;
        if (handleSingleValueMessage(HUNTER_PATTERN, message, tracker::setHunterRumors, "hunterRumors")) return;

        // Wintertodt
        if (handleSingleValueMessage(WINTERTODT_PATTERN, message, crates -> {
            if (crates > tracker.getWintertodtCrates()) {
                tracker.setWintertodtCrates(crates);
                persistConfig("wintertodtCrates", crates);
                safeUpdatePanel(panel::updateAllCaches);
            }
        }, null)) return;
    }

    // ===== Item Container Changes =====
    @Subscribe
    public void onItemContainerChanged(ItemContainerChanged event)
    {
        int id = event.getContainerId();
        if (id == InventoryID.INVENTORY.getId()
                || id == InventoryID.EQUIPMENT.getId()
                || id == InventoryID.BANK.getId())
        {
            System.out.println("[SOT] [onItemContainerChanged] ItemContainerChanged triggered for: " + id);

            safeUpdatePanel(() -> {
                panel.updateAllCaches();
                tracker.updateOwnedItemsFromCaches();
            });
        }
    }


    // ===== Game Tick Updates =====
    @Subscribe
    public void onGameTick(GameTick tick)
    {
        tracker.updateAllCaches();
        safeUpdatePanel(panel::updateAllCaches);
    }

    // ===== Game State Changes =====
    @Subscribe
    public void onGameStateChanged(GameStateChanged event)
    {
        if (event.getGameState() == GameState.LOGGED_IN)
        {
            tracker.updateInventoryCache();
            tracker.updateEquipmentCache();
            tracker.updateBankCache();
            tracker.updateOwnedItems();
            safeUpdatePanel(panel::updateAllCaches);
        }
    }

    // ===== Config Changed =====
    @Subscribe
    public void onConfigChanged(ConfigChanged event)
    {
        if (!"skillingoutfit".equals(event.getGroup())) return;

        SwingUtilities.invokeLater(() -> {
            switch (event.getKey())
            {
                case "enablePopoutConfigMode":
                    if (config.enablePopoutConfigMode()) {
                        if (popoutWindow == null || !popoutWindow.isDisplayable())
                            popoutWindow = new SkillingOutfitWindow(config, panel, configManager, eventBus, clientThread);
                    } else if (popoutWindow != null) {
                        popoutWindow.dispose();
                        popoutWindow = null;
                    }
                    break;

                case "notifyOnNew":
                    notifyOnNew = config.notifyOnNew();
                    break;

                default:
                    if (event.getKey().startsWith("display") && panel != null)
                        panel.setupOutfitDisplayMap();
            }
            safeUpdatePanel(panel::refresh);
        });
    }

    // ===== Config Provider =====
    @Provides
    SkillingOutfitConfig provideConfig(ConfigManager configManager)
    {
        return configManager.getConfig(SkillingOutfitConfig.class);
    }

    // ===== Helper Methods =====
    private void safeUpdatePanel(Runnable r)
    {
        if (panel != null) r.run();
    }

    private void persistConfig(String key, int value)
    {
        if (key != null) configManager.setConfiguration("skillingoutfit", key, value);
        safeUpdatePanel(panel::updateAllCaches);
    }

    private void loadMinigameStat(String key, Consumer<Integer> setter, int defaultValue)
    {
        Integer stored = configManager.getConfiguration("skillingoutfit", key, Integer.class);
        setter.accept((stored != null) ? stored : defaultValue);
    }

    private BufferedImage resizeIcon(BufferedImage icon, int width, int height)
    {
        if (icon == null) return null;
        if (icon.getWidth() == width && icon.getHeight() == height) return icon;

        BufferedImage resized = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = resized.createGraphics();
        g2d.drawImage(icon, 0, 0, width, height, null);
        g2d.dispose();
        return resized;
    }

    private boolean handleTwoValueMessage(Pattern pattern, String message, java.util.function.BiConsumer<Integer, Integer> handler)
    {
        Matcher matcher = pattern.matcher(message);
        if (matcher.find() && matcher.groupCount() >= 2)
        {
            handler.accept(Integer.parseInt(matcher.group(1)), Integer.parseInt(matcher.group(2)));
            return true;
        }
        return false;
    }

    private boolean handleSingleValueMessage(Pattern pattern, String message, Consumer<Integer> setter, String configKey)
    {
        Matcher matcher = pattern.matcher(message);
        if (matcher.find())
        {
            int value = Integer.parseInt(matcher.group(1));
            setter.accept(value);
            if (configKey != null) persistConfig(configKey, value);
            safeUpdatePanel(panel::updateAllCaches);
            return true;
        }
        return false;
    }
}
