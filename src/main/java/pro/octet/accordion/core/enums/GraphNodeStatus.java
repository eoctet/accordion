package pro.octet.accordion.core.enums;

import lombok.Getter;

@Getter
public enum GraphNodeStatus {

    NORMAL("❔"), SUCCESS("✅"), ERROR("❌"), SKIP("⛔️");

    private final String flag;

    GraphNodeStatus(String flag) {
        this.flag = flag;
    }
}
