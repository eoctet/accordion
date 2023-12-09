package pro.octet.accordion.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.security.SecureRandom;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Slf4j
public class CommonUtils {

    private static final String CHARACTER_SET = "abcdefghijklmnopqrstuvwxyz0123456789";

    private CommonUtils() {
    }

    public static String randomString(String prefixString) {
        String randomString = IntStream.range(0, 10).map(i -> new SecureRandom().nextInt(CHARACTER_SET.length())).mapToObj(randomInt -> CHARACTER_SET.substring(randomInt, randomInt + 1)).collect(Collectors.joining());
        return StringUtils.join(prefixString, "-", randomString);
    }

}
