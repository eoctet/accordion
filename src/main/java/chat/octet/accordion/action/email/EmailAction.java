package chat.octet.accordion.action.email;


import chat.octet.accordion.action.AbstractAction;
import chat.octet.accordion.action.model.ActionConfig;
import chat.octet.accordion.action.model.ExecuteResult;
import chat.octet.accordion.exceptions.ActionException;
import chat.octet.accordion.utils.CommonUtils;
import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.mail.DefaultAuthenticator;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.HtmlEmail;
import org.apache.commons.text.StringSubstitutor;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;

/**
 * EmailAction Supports sending emails to multiple email addresses.
 * By default, the content format of the email is in HTML format, and email attachments are not currently supported.
 *
 * @author <a href="https://github.com/eoctet">William</a>
 * @see EmailParameter
 */
@Slf4j
public class EmailAction extends AbstractAction {
    private static final long serialVersionUID = 1L;

    private final transient EmailParameter params;

    public EmailAction(final ActionConfig actionConfig) {
        super(actionConfig);
        this.params = actionConfig.getActionParams(EmailParameter.class, "Email parameter cannot be null.");
        Preconditions.checkArgument(StringUtils.isNotBlank(params.getServer()), "Email SMTP server address cannot be empty.");
        Preconditions.checkArgument(StringUtils.isNotBlank(params.getFrom()), "Email sender cannot be empty.");
        Preconditions.checkArgument(StringUtils.isNotBlank(params.getSubject()), "Email subject cannot be empty.");
        Preconditions.checkArgument(params.getRecipients() != null, "Email recipient cannot be empty.");
        Preconditions.checkArgument(StringUtils.isNotBlank(params.getContent()), "Email content cannot be empty.");
    }

    private HtmlEmail createEmail(final EmailParameter emailParams, final String contentId) {
        // setting email server config
        HtmlEmail email = new HtmlEmail();
        email.setSocketTimeout(Duration.ofMillis(emailParams.getTimeout()));
        email.setSocketConnectionTimeout(Duration.ofMillis(emailParams.getTimeout()));
        email.setDebug(emailParams.isDebug());
        Map<String, String> headers = Maps.newHashMap();
        headers.put("Content-ID", contentId);
        email.setHeaders(headers);
        email.setHostName(emailParams.getServer());
        email.setSmtpPort(emailParams.getSmtpPort());
        email.setSslSmtpPort(String.valueOf(emailParams.getSmtpPort()));
        email.setSSLOnConnect(emailParams.isSsl());
        email.setStartTLSEnabled(emailParams.isTls());
        if (StringUtils.isNotBlank(emailParams.getUsername()) || StringUtils.isNotBlank(emailParams.getPassword())) {
            email.setAuthenticator(new DefaultAuthenticator(emailParams.getUsername(), emailParams.getPassword()));
        }
        return email;
    }


    private String send(final EmailParameter emailParams, final String content) throws EmailException {
        // setting email server config
        String contentId = CommonUtils.randomString("accordion-email");
        HtmlEmail email = createEmail(emailParams, contentId);
        // setting email header config
        email.setFrom(emailParams.getFrom(), StringUtils.substringBefore(emailParams.getFrom(), "@"));
        email.setCharset(StandardCharsets.UTF_8.name());
        email.setSubject(emailParams.getSubject());
        email.addTo(emailParams.getRecipients());
        if (emailParams.getCarbonCopies() != null) {
            email.addCc(emailParams.getCarbonCopies());
        }
        email.setHtmlMsg(content);
        log.debug("Create a new email, Content ID: {}.", contentId);
        return email.send();
    }

    /**
     * Executes the email action by sending an email with the configured parameters.
     *
     * @return ExecuteResult containing the execution result
     * @throws ActionException if email sending fails
     */
    @Override
    public ExecuteResult execute() throws ActionException {
        ExecuteResult executeResult = new ExecuteResult();
        try {
            String content = StringSubstitutor.replace(params.getContent(), getInputParameter());
            String status = send(params, content);
            log.debug("The email has been sent, status: " + status);
        } catch (Exception e) {
            setExecuteThrowable(new ActionException(e.getMessage(), e));
        }
        return executeResult;
    }
}
