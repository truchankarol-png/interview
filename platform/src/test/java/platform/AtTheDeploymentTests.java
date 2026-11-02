package platform;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import platform.adapters.GameAdapter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 
 */

class AtTheDeploymentTests {
    // Monitoring of the game integration status and game health.
    // Ideally monitored by CI platform and reported to the dashboard independendly form the code base. 
    // The inegration suite of the Player Platform should not depend on those test cases,
    // This information is useful to the producers.
    // I would normally include shuch checks in the independed monitoring platform. 
    // Indluced test here only for the demonstration purposes and created with Git Copilot.

    @Test
    void allGameHealthEndpointsRespondWithHttp200() {
        GameClient client = new GameClient();
        List<Executable> checks = new ArrayList<>();
        List<DeploymentResult> gameResults = new ArrayList<>();
        String executedAt = Instant.now().toString();

        for (GameAdapter adapter : new GameRegistry().all()) {
            int status;
            try {
                status = client.get(adapter.baseUrl() + "/game/v1/health").status();
            } catch (InterruptedException interrupted) {
                Thread.currentThread().interrupt();
                status = -1;
            } catch (IOException unavailable) {
                status = -1;
            }

            int healthStatusCode = status;
            checks.add(() -> assertEquals(200, healthStatusCode, adapter.gameId() + " health endpoint"));

            gameResults.add(new DeploymentResult(
                    adapter.displayName(),
                    status == -1 ? "UNREACHABLE" : "HTTP " + status,
                    adapter.live() ? "LIVE" : "NOT LIVE",
                    executedAt));
        }

        appendResultsToCsv(gameResults);
        assertAll("game health endpoints", checks);
    }

    @Test
    void allGamesAreEnabled() {
        List<String> disabledGames = new ArrayList<>();
        for (GameAdapter adapter : new GameRegistry().all()) {
            if (!adapter.live()) {
                disabledGames.add(adapter.gameId());
            }
        }

        assertTrue(disabledGames.isEmpty(), "Games not enabled: " + String.join(", ", disabledGames));
    }

// The results are appended to a CSV file, which can be used by the CI platform to report the results to the dashboard.
// Example dashboard can be accessed:
// https://airtable.com/appwS5PQZYAZiq6cx/shrd11iAWs1w71NeZ

    private static void appendResultsToCsv(List<DeploymentResult> gameResults) {
        String configuredPath = System.getenv("DEPLOYMENT_RESULTS_CSV");
        Path outputFile = configuredPath == null || configuredPath.isBlank()
                ? Path.of("build", "reports", "deployment-results.csv")
                : Path.of(configuredPath);
        try {
            Path parent = outputFile.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }

            boolean includeHeader = !Files.exists(outputFile) || Files.size(outputFile) == 0;
            StringBuilder csv = new StringBuilder();
            if (includeHeader) {
                csv.append("game_title,health_status,live_status,executed_at\r\n");
            }
            for (DeploymentResult result : gameResults) {
                csv.append(csvValue(result.gameTitle())).append(',')
                        .append(csvValue(result.healthStatus())).append(',')
                        .append(csvValue(result.liveStatus())).append(',')
                        .append(csvValue(result.executedAt())).append("\r\n");
            }

            Files.writeString(outputFile, csv, StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException error) {
            throw new AssertionError("Could not write deployment results to " + outputFile, error);
        }
    }

    private static String csvValue(String value) {
        return "\"" + value.replace("\"", "\"\"") + "\"";
    }

    private record DeploymentResult(String gameTitle, String healthStatus, String liveStatus, String executedAt) {
    }
}
