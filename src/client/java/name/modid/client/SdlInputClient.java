package name.modid.client;

import name.modid.client.config.SdlInputConfig;
import name.modid.client.input.SdlInputHandler;
import net.fabricmc.api.ClientModInitializer;

public class SdlInputClient implements ClientModInitializer {
	private static SdlInputConfig config;

	public static SdlInputConfig getConfig() { return config; }

	@Override
	public void onInitializeClient() {
		// This entrypoint is suitable for setting up client-specific logic, such as rendering.
		config = new SdlInputConfig();
		config.load();

		SdlInputHandler handler = new SdlInputHandler();
		handler.register();
	}
}