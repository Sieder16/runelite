package net.runelite.client.plugins.skillingoutfit;

import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.events.ConfigChanged;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;

public class SkillingOutfitWindow extends JFrame
{
    private final SkillingOutfitConfig config;
    private final SkillingOutfitPanel panel;
    private final ConfigManager configManager;
    private final EventBus eventBus;
    private final ClientThread clientThread;

    public SkillingOutfitWindow(SkillingOutfitConfig config, SkillingOutfitPanel panel,
                                ConfigManager configManager, EventBus eventBus, ClientThread clientThread)
    {
        super("Skilling Outfit Tracker - Config");
        this.config = config;
        this.panel = panel;
        this.configManager = configManager;
        this.eventBus = eventBus;
        this.clientThread = clientThread;

        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());
        setSize(400, 600);
        setLocationRelativeTo(null);

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

        // ---------------- Config Options ----------------
        JCheckBox notifyOnNewBox = new JCheckBox("Notify on new item", config.notifyOnNew());
        notifyOnNewBox.addActionListener(e -> setConfigValue("notifyOnNew", notifyOnNewBox.isSelected()));
        content.add(notifyOnNewBox);

        JLabel panelTitleLabel = new JLabel("Panel Title Spacer");
        JSpinner panelTitleSpinner = new JSpinner(new SpinnerNumberModel(config.panelTitleSpacer(), 0, 100, 1));
        panelTitleSpinner.addChangeListener(e -> setConfigValue("panelTitleSpacer", panelTitleSpinner.getValue()));
        content.add(panelTitleLabel);
        content.add(panelTitleSpinner);

        JCheckBox displayOutfitsBox = new JCheckBox("Display Collected Outfits", config.displayCollectedOutfits());
        displayOutfitsBox.addActionListener(e -> setConfigValue("displayCollectedOutfits", displayOutfitsBox.isSelected()));
        content.add(displayOutfitsBox);

        JLabel outfitTextLabel = new JLabel("Outfit Text Spacer");
        JSpinner outfitTextSpinner = new JSpinner(new SpinnerNumberModel(config.outfitTextSpacer(), 0, 50, 1));
        outfitTextSpinner.addChangeListener(e -> setConfigValue("outfitTextSpacer", outfitTextSpinner.getValue()));
        content.add(outfitTextLabel);
        content.add(outfitTextSpinner);

        JCheckBox displayItemsBox = new JCheckBox("Display Collected Items", config.displayCollectedItems());
        displayItemsBox.addActionListener(e -> setConfigValue("displayCollectedItems", displayItemsBox.isSelected()));
        content.add(displayItemsBox);

        JLabel itemTextLabel = new JLabel("Item Text Spacer");
        JSpinner itemTextSpinner = new JSpinner(new SpinnerNumberModel(config.itemTextSpacer(), 0, 50, 1));
        itemTextSpinner.addChangeListener(e -> setConfigValue("itemTextSpacer", itemTextSpinner.getValue()));
        content.add(itemTextLabel);
        content.add(itemTextSpinner);

        JCheckBox colorCollectedBox = new JCheckBox("Color Text For Collected", config.colorTextForCollected());
        colorCollectedBox.addActionListener(e -> setConfigValue("colorTextForCollected", colorCollectedBox.isSelected()));
        content.add(colorCollectedBox);

        JLabel firstOutfitLabel = new JLabel("First Outfit Spacer");
        JSpinner firstOutfitSpinner = new JSpinner(new SpinnerNumberModel(config.firstOutfitSpacer(), 0, 50, 1));
        firstOutfitSpinner.addChangeListener(e -> setConfigValue("firstOutfitSpacer", firstOutfitSpinner.getValue()));
        content.add(firstOutfitLabel);
        content.add(firstOutfitSpinner);

        JCheckBox showTotalBox = new JCheckBox("Show Price To Obtain", config.showTotalObtain());
        showTotalBox.addActionListener(e -> setConfigValue("showTotalObtain", showTotalBox.isSelected()));
        content.add(showTotalBox);

        JLabel totalNeededLabel = new JLabel("Total Needed Text Spacer");
        JSpinner totalNeededSpinner = new JSpinner(new SpinnerNumberModel(config.totalNeededTextSpacer(), 0, 50, 1));
        totalNeededSpinner.addChangeListener(e -> setConfigValue("totalNeededTextSpacer", totalNeededSpinner.getValue()));
        content.add(totalNeededLabel);
        content.add(totalNeededSpinner);

        JLabel iconTextSpacerLabel = new JLabel("Icon Text Spacer");
        JSpinner iconTextSpacerSpinner = new JSpinner(new SpinnerNumberModel(config.iconTextSpacer(), 0, 50, 1));
        iconTextSpacerSpinner.addChangeListener(e -> setConfigValue("iconTextSpacer", iconTextSpacerSpinner.getValue()));
        content.add(iconTextSpacerLabel);
        content.add(iconTextSpacerSpinner);

        JLabel iconSizeLabel = new JLabel("Icon Size");
        JSpinner iconSizeSpinner = new JSpinner(new SpinnerNumberModel(config.iconSize(), 16, 128, 1));
        iconSizeSpinner.addChangeListener(e -> setConfigValue("iconSize", iconSizeSpinner.getValue()));
        content.add(iconSizeLabel);
        content.add(iconSizeSpinner);

        JLabel iconGapLabel = new JLabel("Icon Gap Spacing");
        JSpinner iconGapSpinner = new JSpinner(new SpinnerNumberModel(config.iconGapSpacing(), 0, 50, 1));
        iconGapSpinner.addChangeListener(e -> setConfigValue("iconGapSpacing", iconGapSpinner.getValue()));
        content.add(iconGapLabel);
        content.add(iconGapSpinner);

        JLabel maxColsLabel = new JLabel("Max Columns");
        JSpinner maxColsSpinner = new JSpinner(new SpinnerNumberModel(config.maxCols(), 1, 10, 1));
        maxColsSpinner.addChangeListener(e -> setConfigValue("maxCols", maxColsSpinner.getValue()));
        content.add(maxColsLabel);
        content.add(maxColsSpinner);

        JCheckBox showObtainedBox = new JCheckBox("Show Obtained Items", config.showObtainedItems());
        showObtainedBox.addActionListener(e -> setConfigValue("showObtainedItems", showObtainedBox.isSelected()));
        content.add(showObtainedBox);

        JCheckBox overrideColors = new JCheckBox("Override Outfit Colors", config.overrideOutfitColors());
        overrideColors.addActionListener(e -> setConfigValue("overrideOutfitColors", overrideColors.isSelected()));
        content.add(overrideColors);

        JButton colorButton = new JButton("Pick Outfit Name Color");
        colorButton.addActionListener(e -> {
            Color newColor = JColorChooser.showDialog(this, "Choose Outfit Name Color", config.outfitNameColor());
            if (newColor != null)
                setConfigValue("outfitNameColor", newColor.getRGB());
        });
        content.add(colorButton);

        JCheckBox popoutModeBox = new JCheckBox("Enable Popout Config Mode", config.enablePopoutConfigMode());
        popoutModeBox.addActionListener(e -> setConfigValue("enablePopoutConfigMode", popoutModeBox.isSelected()));
        content.add(popoutModeBox);

        // ---------------- Outfit Toggles ----------------
        Arrays.asList(
                "Graceful", "Prospector", "Lumberjack", "Angler",
                "Pyromancer", "Farmer", "Carpenter", "Rogues",
                "Shayzien", "Void"
        ).forEach(outfit -> {
            boolean initial = getBooleanConfigValue("display" + outfit);
            JCheckBox box = new JCheckBox("Display " + outfit + " Outfit", initial);
            box.addActionListener(e -> setConfigValue("display" + outfit, box.isSelected()));
            content.add(box);
        });

        content.add(Box.createVerticalGlue());

        JScrollPane scrollPane = new JScrollPane(content);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        add(scrollPane, BorderLayout.CENTER);

        setVisible(true);
    }

    private void setConfigValue(String key, Object value)
    {
        configManager.setConfiguration("skillingoutfit", key, value);

        if (panel != null)
            panel.refresh();

        ConfigChanged changed = new ConfigChanged();
        changed.setGroup("skillingoutfit");
        changed.setKey(key);
        changed.setNewValue(value != null ? value.toString() : null);
        eventBus.post(changed);
    }

    private boolean getBooleanConfigValue(String key)
    {
        Object val = configManager.getConfiguration("skillingoutfit", key);
        return val == null || Boolean.parseBoolean(val.toString());
    }
}
