package com.groundMarkerProfiles;

import com.google.gson.Gson;
import com.google.inject.Provides;
import javax.inject.Inject;

import com.groundMarkerProfiles.ui.GroundMarkerOverlay;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.events.GameStateChanged;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.util.ImageUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.image.BufferedImage;

@PluginDescriptor(
	name = "Ground Marker Profiles"
)
public class GroundMarkerProfilesPlugin extends Plugin {
	@Inject
	private Client client;

	@Inject
	private ClientToolbar clientToolbar;

	@Inject
	private GroundMarkerProfilesConfig config;

	@Inject
	private Gson gson;

	@Inject
	private OverlayManager overlayManager;

	public ProfileSwapper profileSwapper;
	private PointManager pointManager;
	private GroundMarkerOverlay groundMarkerOverlay;
	private GroundMarkerProfilesSidePanel sidePanel;
	private NavigationButton sidePanelButton;

	public static final Logger log = LoggerFactory.getLogger(GroundMarkerProfilesPlugin.class);

	@Override
	protected void startUp() throws Exception
	{
		log.info("Ground Marker Profiles started");

		profileSwapper = new ProfileSwapper(this, config);
		pointManager = new PointManager(profileSwapper, gson, client);

		groundMarkerOverlay = new GroundMarkerOverlay(pointManager, client, config);
		overlayManager.add(groundMarkerOverlay);

		pointManager.loadPoints();
		createSidePanel();
	}

	@Override
	protected void shutDown() throws Exception
	{
		log.info("Ground Marker Profiles stopped");
		overlayManager.remove(groundMarkerOverlay);
		removeSidePanel();
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged gameStateChanged) {
		if (gameStateChanged.getGameState() == GameState.LOGGED_IN) {
			pointManager.loadPoints();
		}
	}

	@Provides
	GroundMarkerProfilesConfig provideConfig(ConfigManager configManager) {
		return configManager.getConfig(GroundMarkerProfilesConfig.class);
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged configChanged) {
		if (configChanged.getGroup().equals(GroundMarkerProfilesConfig.GROUP)) {
			// Example key name is "nameProfile9" NOTE: these can go to multiple digits
			String keyChanged = configChanged.getKey();
			if (keyChanged.startsWith("nameProfile")) {
				Integer indexChanged = Integer.valueOf(keyChanged.substring(11));
				sidePanel.updateButtonName(indexChanged, configChanged.getNewValue());
			} else {
				profileSwapper.updateTileProfiles();
				pointManager.loadPoints();
			}
		}
	}

	private void createSidePanel() {
		final BufferedImage icon = ImageUtil.loadImageResource(getClass(), "/ico.png");
		sidePanel = new GroundMarkerProfilesSidePanel(this, config);
		sidePanelButton = NavigationButton.builder().tooltip("Ground Marker Profiles").icon(icon).priority(6).panel(sidePanel).build();
		clientToolbar.addNavigation(sidePanelButton);
		sidePanel.startPanel();
	}

	private void removeSidePanel()
	{
		clientToolbar.removeNavigation(sidePanelButton);
	}
}
