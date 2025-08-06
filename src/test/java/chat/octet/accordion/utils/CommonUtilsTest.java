package chat.octet.accordion.utils;

import chat.octet.accordion.test.AccordionTestBase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests for CommonUtils utility class.
 * 
 * @author <a href="https://github.com/eoctet">William</a>
 */
@DisplayName("CommonUtils Tests")
class CommonUtilsTest extends AccordionTestBase {

    @Nested
    @DisplayName("Random String Generation Tests")
    class RandomStringGenerationTests {

        @Test
        @DisplayName("Should generate random string with prefix")
        void should_generate_random_string_with_prefix() {
            // Given
            String prefix = "TEST";

            // When
            String result = CommonUtils.randomString(prefix);

            // Then
            assertThat(result)
                    .isNotNull()
                    .startsWith(prefix)
                    .hasSizeGreaterThan(prefix.length());
            
            logger.info("Generated random string: {}", result);
        }

        @ParameterizedTest
        @ValueSource(strings = {"ACT", "MSG", "PLAN", "TEST", "ACTION"})
        @DisplayName("Should generate unique strings for different prefixes")
        void should_generate_unique_strings_for_different_prefixes(String prefix) {
            // When
            String result1 = CommonUtils.randomString(prefix);
            String result2 = CommonUtils.randomString(prefix);

            // Then
            assertThat(result1)
                    .isNotNull()
                    .startsWith(prefix)
                    .isNotEqualTo(result2);
            
            assertThat(result2)
                    .isNotNull()
                    .startsWith(prefix);
            
            logger.info("Generated strings for prefix '{}': {} and {}", prefix, result1, result2);
        }

        @ParameterizedTest
        @NullAndEmptySource
        @DisplayName("Should handle null and empty prefix")
        void should_handle_null_and_empty_prefix(String prefix) {
            // When
            String result = CommonUtils.randomString(prefix);
            
            // Then
            assertThat(result).isNotNull();
            if (prefix == null) {
                assertThat(result).startsWith("null-");
            } else if (prefix.isEmpty()) {
                assertThat(result).startsWith("-");
            }
        }
    }

    @Nested
    @DisplayName("Collection Utility Tests")
    class CollectionUtilityTests {

        @Test
        @DisplayName("Should detect empty collections correctly")
        void should_detect_empty_collections_correctly() {
            // Given
            List<String> emptyList = Collections.emptyList();
            List<String> nullList = null;
            List<String> nonEmptyList = Arrays.asList("item1", "item2");

            // When & Then
            assertThat(CommonUtils.isEmpty(emptyList)).isTrue();
            assertThat(CommonUtils.isEmpty(nullList)).isTrue();
            assertThat(CommonUtils.isEmpty(nonEmptyList)).isFalse();
        }

        @Test
        @DisplayName("Should detect non-empty collections correctly")
        void should_detect_non_empty_collections_correctly() {
            // Given
            List<String> singleItemList = Collections.singletonList("item");
            List<Integer> numberList = Arrays.asList(1, 2, 3, 4, 5);

            // When & Then
            assertThat(CommonUtils.isEmpty(singleItemList)).isFalse();
            assertThat(CommonUtils.isEmpty(numberList)).isFalse();
        }
    }

    @Nested
    @DisplayName("Performance Tests")
    class PerformanceTests {

        @Test
        @DisplayName("Should generate random strings efficiently")
        void should_generate_random_strings_efficiently() {
            // Given
            String prefix = "PERF";
            int iterations = 1000;

            // When
            long startTime = System.currentTimeMillis();
            
            for (int i = 0; i < iterations; i++) {
                String result = CommonUtils.randomString(prefix);
                assertThat(result).startsWith(prefix);
            }
            
            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;

            // Then
            assertThat(duration).isLessThan(5000); // Should complete within 5 seconds
            logger.info("Generated {} random strings in {} ms", iterations, duration);
        }
    }
}