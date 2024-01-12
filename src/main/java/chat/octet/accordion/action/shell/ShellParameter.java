package chat.octet.accordion.action.shell;

import com.fasterxml.jackson.annotation.JsonInclude;
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
public class ShellParameter {

    @Builder.Default
    private ShellType type = ShellType.BASH;

    private String shell;

    @Builder.Default
    private long timeout = 60 * 1000;

    public enum ShellType {
        BASH,
        CMD,
        POWERSHELL
    }

}
