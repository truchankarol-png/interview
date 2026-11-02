package platform;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import platform.Models.Offer;
import platform.adapters.GameAdapter;
import platform.adapters.NovaArenaAdapter;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * A starting point, not a suggestion about where your tests belong.
 * Delete it if it is in the way.
 */
class ExampleAdapterTest {

    @Test
    void parsesNovaArenaCatalogue() throws Exception {
        JsonNode body = Http.MAPPER.readTree("""
                {"items":[{"itemId":"gems_small","name":"Small Gem Pack",
                           "priceMinor":499,"currency":"EUR"}]}
                """);

        List<Offer> offers = new NovaArenaAdapter().parseCatalog(200, body);

        assertEquals(1, offers.size());
        assertEquals(499, offers.get(0).priceMinor());
        assertEquals("EUR", offers.get(0).currency());
    }

    @Test
    void allGameHealthEndpointsRespondWithHttp200() {
        GameClient client = new GameClient();
        List<Executable> checks = new ArrayList<>();
        for (GameAdapter adapter : new GameRegistry().all()) {
            checks.add(() -> assertEquals(
                    200,
                    client.get(adapter.baseUrl() + "/game/v1/health").status(),
                    adapter.gameId() + " health endpoint"));
        }

        assertAll("game health endpoints", checks);
    }

    @Test
    void allGamesAreEnabledForIntegration() {
        List<Executable> checks = new ArrayList<>();
        for (GameAdapter adapter : new GameRegistry().all()) {
            checks.add(() -> assertTrue(adapter.live(), adapter.gameId() + " should be live"));
        }

        assertAll("game live status", checks);
    }
}
