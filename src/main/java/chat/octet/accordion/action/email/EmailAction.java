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

    private final EmailParameter params;

    public EmailAction(ActionConfig actionConfig) {
        super(actionConfig);
        this.params = actionConfig.getActionParams(EmailParameter.class, "Email parameter cannot be null.");
        Preconditions.checkArgument(StringUtils.isNotBlank(params.getServer()), "Email SMTP server address cannot be empty.");
        Preconditions.checkArgument(StringUtils.isNotBlank(params.getFrom()), "Email sender cannot be empty.");
        Preconditions.checkArgument(StringUtils.isNotBlank(params.getSubject()), "Email subject cannot be empty.");
        Preconditions.checkArgument(params.getRecipients() != null, "Email recipient cannot be empty.");
        Preconditions.checkArgument(StringUtils.isNotBlank(params.getContent()), "Email content cannot be empty.");
    }

    private HtmlEmail createEmail(EmailParameter params, String contentId) {
        // setting email server config
        HtmlEmail email = new HtmlEmail();
        email.setSocketTimeout(params.getTimeout().intValue());
        email.setSocketConnectionTimeout(params.getTimeout().intValue());
        email.setDebug(params.isDebug());
        Map<String, String> headers = Maps.newHashMap();
        headers.put("Content-ID", contentId);
        email.setHeaders(headers);
        email.setHostName(params.getServer());
        email.setSmtpPort(params.getSmtpPort());
        email.setSslSmtpPort(String.valueOf(params.getSmtpPort()));
        email.setSSLOnConnect(params.isSsl());
        email.setStartTLSEnabled(params.isTls());
        if (StringUtils.isNotBlank(params.getUsername()) || StringUtils.isNotBlank(params.getPassword())) {
            email.setAuthenticator(new DefaultAuthenticator(params.getUsername(), params.getPassword()));
        }
        return email;
    }


    private String send(EmailParameter params, String content) throws EmailException {
        // setting email server config
        String contentId = CommonUtils.randomString("accordion-email");
        HtmlEmail email = createEmail(params, contentId);
        // setting email header config
        email.setFrom(params.getFrom(), StringUtils.substringBefore(params.getFrom(), "@"));
        email.setCharset(StandardCharsets.UTF_8.name());
        email.setSubject(params.getSubject());
        email.addTo(params.getRecipients());
        if (params.getCarbonCopies() != null) {
            email.addCc(params.getCarbonCopies());
        }
        email.setHtmlMsg(content);
        log.debug("Create a new email, Content ID: {}.", contentId);
        return email.send();
    }

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
