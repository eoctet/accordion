package chat.octet.accordion.action.script;

import chat.octet.accordion.utils.CommonUtils;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.jackson.Jacksonized;

/**
 * Script action parameter.
 *
 * @author <a href="https://github.com/eoctet">William</a>
 */
@Getter
@Builder
@ToString
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class ScriptParameter {
    /**
     * Script id, Automatically generated by default.
     */
    @Builder.Default
    private String scriptId = CommonUtils.randomString("script");
    /**
     * Script code snippets.
     */
    private String script;
    /**
     * Debug mode, default: false.
     * NOTE: Do not enable it in the production env.
     */
    private boolean debug;
}
