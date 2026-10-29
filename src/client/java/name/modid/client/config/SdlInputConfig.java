package name.modid.client.config;

import com.google.gson.*;
import net.fabricmc.loader.api.FabricLoader;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Path;

public class SdlInputConfig {
    private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("sdl-input.json");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private boolean enabled = true;

    public void load() {
        if (!CONFIG_PATH.toFile().exists()) {
            save();
            return;
        }
        try (Reader reader = new FileReader(CONFIG_PATH.toFile())) {
            JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
            if (json.has("enabled"))
                enabled = json.get("enabled").getAsBoolean();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void save() {
        JsonObject json = new JsonObject();
        json.addProperty("enabled", enabled);
        try (Writer writer = new FileWriter(CONFIG_PATH.toFile())) {
            GSON.toJson(json, writer);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean isEnabled() { return  enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; save(); }
}
