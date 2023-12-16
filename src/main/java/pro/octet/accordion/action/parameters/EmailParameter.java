package pro.octet.accordion.action.parameters;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.jackson.Jacksonized;
import org.apache.commons.lang3.StringUtils;

@Getter
@Builder
@ToString
@Jacksonized
public class EmailParameter {
    @Builder.Default
    private String server = "127.0.0.1";
    @Builder.Default
    private int smtpPort = 25;
    private boolean ssl;
    private boolean tls;
    private String username;
    private String password;
    private String subject;
    private String from;
    private String to;
    private String cc;
    private String content;
    @Builder.Default
    private Long timeout = 1000L * 5;
    private boolean debug;

    @JsonIgnore
    public String[] getRecipients() {
        if (StringUtils.isNotBlank(to)) {
            return StringUtils.split(to, ",");
        }
        return null;
    }

    @JsonIgnore
    public String[] getCarbonCopies() {
        if (StringUtils.isNotBlank(cc)) {
            return StringUtils.split(cc, ",");
        }
        return null;
    }
}
