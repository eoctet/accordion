package chat.octet.accordion.action.shell;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.jackson.Jacksonized;

/**
 * ShellAction parameter.
 *
 * @author <a href="https://github.com/eoctet">William</a>
 */
@Getter
@Builder
@ToString
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class ShellParameter {

    /**
     * Shell language type, default: bash.
     */
    @Builder.Default
    private ShellType type = ShellType.BASH;

    /**
     * Shell code snippets.
     */
    private String shell;

    /**
     * Execution timeout, default: 60000ms.
     */
    @Builder.Default
    private long timeout = 60 * 1000;

    public enum ShellType {
        BASH,
        CMD,
        POWERSHELL
    }

}
