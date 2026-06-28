package org.dromara.carbon.vendor.overview;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;

@Tag("dev")
class CvOverviewSqlServerTest {

    @Test
    void overviewQueriesDoNotUseMysqlLimitSyntax() throws Exception {
        String overviewService = Files.readString(resolveProjectFile(
            "ruoyi-modules/carbon-vendor/src/main/java/org/dromara/carbon/vendor/overview/service/impl/CvOverviewServiceImpl.java"));

        assertContainsNone(overviewService, List.of(
            ".last(\"limit",
            "new Page<>(1, size, false)"
        ));
    }

    private static void assertContainsNone(String text, List<String> forbiddenFragments) {
        for (String forbiddenFragment : forbiddenFragments) {
            assertFalse(text.contains(forbiddenFragment), "Expected overview service not to contain: " + forbiddenFragment);
        }
    }

    private static Path resolveProjectFile(String relativePath) {
        Path current = Path.of("").toAbsolutePath();
        while (current != null) {
            Path candidate = current.resolve(relativePath);
            if (Files.exists(candidate)) {
                return candidate;
            }
            current = current.getParent();
        }
        throw new IllegalStateException("Unable to find " + relativePath);
    }
}
