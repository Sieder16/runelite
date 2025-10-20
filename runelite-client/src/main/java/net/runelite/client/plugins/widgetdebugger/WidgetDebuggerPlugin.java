package net.runelite.client.plugins.widgetdebugger;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.events.WidgetLoaded;
import net.runelite.api.events.WidgetClosed;
import net.runelite.api.widgets.Widget;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

import javax.inject.Inject;

@Slf4j
@PluginDescriptor(
        name = "Widget Debugger",
        description = "Logs Giants’ Foundry Reputation using RuneLite API constants",
        tags = {"debug", "widget", "ui", "runelite"}
)
public class WidgetDebuggerPlugin extends Plugin
{
    @Inject
    private Client client;

    @Inject
    private ClientThread clientThread;

    private boolean foundryCheckActive = false;

    @Subscribe
    public void onWidgetLoaded(WidgetLoaded event)
    {
        int groupId = event.getGroupId();
        log.info("[WIDBUG] Widget loaded: groupId = {}", groupId);

        if (groupId == InterfaceID.GiantsFoundryRewardShop.FRAME && !foundryCheckActive)
        {
            foundryCheckActive = true;
            log.info("[WIDBUG] Giants’ Foundry Reward Shop opened — reading Foundry Reputation...");

            // Get the rep immediately on the client thread
            clientThread.invoke(this::logFoundryRep);
        }
    }

    @Subscribe
    public void onWidgetClosed(WidgetClosed event)
    {
        if (event.getGroupId() == InterfaceID.GiantsFoundryRewardShop.FRAME)
        {
            foundryCheckActive = false;
            log.info("[WIDBUG] Giants’ Foundry Reward Shop closed");
        }
    }

    private void logFoundryRep()
    {
        Widget content = client.getWidget(InterfaceID.GiantsFoundryRewardShop.CONTENT);
        if (content == null)
        {
            log.warn("[WIDBUG] Could not find Giants’ Foundry CONTENT widget");
            return;
        }

        Widget pointsWidget = content.getChild(InterfaceID.GiantsFoundryRewardShop.POINTS_VALUE);
        if (pointsWidget == null)
        {
            log.warn("[WIDBUG] Could not find POINTS_VALUE widget");
            return;
        }

        String repText = pointsWidget.getText();
        log.info("[WIDBUG] ✅ Foundry Reputation: {}", repText);
    }
}
