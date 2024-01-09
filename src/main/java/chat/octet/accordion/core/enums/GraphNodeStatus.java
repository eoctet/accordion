package chat.octet.accordion.core.enums;

import lombok.Getter;

/**
 * Accordion plan graph node status.
 *
 * @author <a href="https://github.com/eoctet">William</a>
 */
@Getter
public enum GraphNodeStatus {

    NORMAL("⚪️"), SUCCESS("✅"), ERROR("🅾️"), SKIP("🟡");

    private final String flag;

    GraphNodeStatus(String flag) {
        this.flag = flag;
    }
}
