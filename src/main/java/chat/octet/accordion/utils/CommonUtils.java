package chat.octet.accordion.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.security.SecureRandom;
import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Slf4j
public final class CommonUtils {

    private static final String CHARACTER_SET = "abcdefghijklmnopqrstuvwxyz0123456789";
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private CommonUtils() {
    }

    public static String randomString(final String prefixString) {
        String randomString = IntStream.range(0, 10).map(i -> SECURE_RANDOM.nextInt(CHARACTER_SET.length())).mapToObj(randomInt -> CHARACTER_SET.substring(randomInt, randomInt + 1)).collect(Collectors.joining());
        if (prefixString == null) {
            return "null-" + randomString;
        }
        return StringUtils.join(prefixString, "-", randomString);
    }

    public static boolean isEmpty(final Collection<?> collection) {
        return collection == null || collection.isEmpty();
    }

}
