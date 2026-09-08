package com.worldsoul.harness;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class PlayerSnapshotTest {
    @Test
    public void serializesVersionNeutralPlayerStateForHarness() {
        PlayerSnapshot snapshot = new PlayerSnapshot(
                "minecraft:overworld", 12.5, 64, -3, 90, 10, "west",
                18, 20, 17, "survival", true, 2,
                List.of(new PlayerSnapshot.InventoryStack(2, "minecraft:diamond_pickaxe", 1, 10, 1561)),
                List.of(new PlayerSnapshot.EquipmentStack("head", "minecraft:diamond_helmet", 1, 4, 363)));
        JsonObject json = new Gson().toJsonTree(snapshot).getAsJsonObject();
        assertEquals("minecraft:overworld", json.get("dimension").getAsString());
        assertEquals(12.5, json.get("x").getAsDouble(), 0.0);
        assertEquals("west", json.get("facing").getAsString());
        assertEquals("survival", json.get("gameMode").getAsString());
        assertTrue(json.get("onGround").getAsBoolean());
        assertEquals(2, json.get("selectedHotbarSlot").getAsInt());
        assertEquals("minecraft:diamond_pickaxe",
                json.getAsJsonArray("inventory").get(0).getAsJsonObject().get("item").getAsString());
        assertEquals("head", json.getAsJsonArray("equipment").get(0).getAsJsonObject().get("slot").getAsString());
    }
}
