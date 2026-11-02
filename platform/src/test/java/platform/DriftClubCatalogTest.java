package platform;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;
import platform.Models.Order;
import platform.Models.OrderStatus;
import platform.Models.Offer;
import platform.adapters.DriftClubAdapter;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class DriftClubCatalogTest {

// Those test cases ideally should be the part of the Drift Club Adapter test suite, 
// but for demonstration purposes they are included here.
// The assumption is that the Drift Club team is not available to provide the test suite.


// Handover docymentation states that the currency is USD, but the adapter is currently returning EUR.
// Test case to highlight this discrepency.
    @Test
    void parsesCatalogItemsForDriftClub() throws Exception {
        DriftClubAdapter adapter = new DriftClubAdapter();
        JsonNode body = Http.MAPPER.readTree("""
                {"items":[
                  {"itemId":"gems.small","name":"Nitro Can","priceMinor":499},
                  {"itemId":"gems.large","name":"Nitro Barrel","priceMinor":1999},
                  {"itemId":"drift.pass","name":"Drift Pass","priceMinor":999},
                  {"itemId":"vip.lounge","name":"VIP Lounge Access","priceMinor":2999}
                ]}
                """);

        List<Offer> offers = adapter.parseCatalog(200, body);

        assertFalse(adapter.live());
        assertEquals(4, offers.size());
        assertEquals(List.of("gems.small", "gems.large", "drift.pass", "vip.lounge"),
                offers.stream().map(Offer::gameItemId).toList());
        assertEquals(List.of("Nitro Can", "Nitro Barrel", "Drift Pass", "VIP Lounge Access"),
                offers.stream().map(Offer::name).toList());
        assertEquals(List.of(499L, 1999L, 999L, 2999L),
                offers.stream().map(Offer::priceMinor).toList());
        assertEquals(List.of("USD", "USD", "USD", "USD"),
                offers.stream().map(Offer::currency).toList());
        assertEquals(List.of("drift-club", "drift-club", "drift-club", "drift-club"),
                offers.stream().map(Offer::gameId).toList());
    }

    @Test
        void purchasesDriftClubItemWhenLive() {
        String playerId = "p-1001";
        GameRegistry registry = new GameRegistry();
        GameClient client = new GameClient();
        CatalogService catalog = new CatalogService(registry, client);
        OrderService orders = new OrderService(registry, client, catalog);

                Order order = orders.createOrder(
                                "drift-club", playerId, "gems.small", "driftclub-test-order", false);

                assertEquals("gems.small", order.gameItemId);
                assertEquals(OrderStatus.PENDING, order.status);
                assertEquals(1, orders.ordersFor(playerId).size());
    }
}
