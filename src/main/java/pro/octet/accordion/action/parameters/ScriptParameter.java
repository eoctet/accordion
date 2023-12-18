package pro.octet.accordion.action.parameters;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.jackson.Jacksonized;
import pro.octet.accordion.utils.CommonUtils;


@Getter
@Builder
@ToString
@Jacksonized
public class ScriptParameter {
    @Builder.Default
    private String scriptId = CommonUtils.randomString("script");
    private String script;
    private boolean debug;
}
