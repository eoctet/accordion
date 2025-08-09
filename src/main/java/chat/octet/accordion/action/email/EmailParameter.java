package chat.octet.accordion.action.email;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.jackson.Jacksonized;
import org.apache.commons.lang3.StringUtils;

/**
 * Email action parameter.
 *
 * @author <a href="https://github.com/eoctet">William</a>
 */
@Getter
@Builder
@ToString
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class EmailParameter {
    /**
     * SMTP server address.
     */
    @Builder.Default
    private String server = "127.0.0.1";
    /**
     * SMTP server port.
     */
    @Builder.Default
    private int smtpPort = 25;
    /**
     * Use SSL, default: false.
     */
    private boolean ssl;
    /**
     * Use TLS, default: false.
     */
    private boolean tls;
    /**
     * SMTP sender username.
     */
    private String username;
    /**
     * SMTP sender password.
     */
    private String password;
    /**
     * Email subject.
     */
    private String subject;
    /**
     * Email sender.
     */
    private String from;
    /**
     * Email recipient, Using "," dividing multiple parameters.
     */
    private String to;
    /**
     * Email carbon copy, Using "," dividing multiple parameters.
     */
    private String cc;
    /**
     * Email content.
     */
    private String content;
    /**
     * Connection timeout, default: 5000 ms.
     */
    @Builder.Default
    private Long timeout = 1000L * 5;
    /**
     * Debug mode, default: false.
     */
    private boolean debug;

    /**
     * Gets the email recipients as an array by splitting the 'to' field.
     *
     * @return array of recipient email addresses
     */
    @JsonIgnore
    public String[] getRecipients() {
        if (StringUtils.isNotBlank(to)) {
            return StringUtils.split(to, ",");
        }
        return new String[0];
    }

    /**
     * Gets the carbon copy recipients as an array by splitting the 'cc' field.
     *
     * @return array of carbon copy email addresses
     */
    @JsonIgnore
    public String[] getCarbonCopies() {
        if (StringUtils.isNotBlank(cc)) {
            return StringUtils.split(cc, ",");
        }
        return new String[0];
    }
}
