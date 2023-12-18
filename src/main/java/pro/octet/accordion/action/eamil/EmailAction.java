package pro.octet.accordion.action.eamil;


import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.mail.DefaultAuthenticator;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.HtmlEmail;
import pro.octet.accordion.action.AbstractAction;
import pro.octet.accordion.action.model.ActionConfig;
import pro.octet.accordion.action.model.ActionResult;
import pro.octet.accordion.action.parameters.EmailParameter;
import pro.octet.accordion.exceptions.ActionException;
import pro.octet.accordion.utils.CommonUtils;

import java.nio.charset.StandardCharsets;
import java.util.Map;

@Slf4j
public class EmailAction extends AbstractAction {

    private final EmailParameter params;

    public EmailAction(ActionConfig actionConfig) {
        super(actionConfig);
        this.params = actionConfig.getActionParams(EmailParameter.class);
        Preconditions.checkNotNull(params, "Email parameter can not be null.");
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


    public String send(EmailParameter params, String content) throws EmailException {
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
    public ActionResult execute() throws ActionException {
        ActionResult actionResult = new ActionResult();
        try {
            String content = CommonUtils.parameterFormat(getInputParameter(), params.getContent());
            String status = send(params, content);
            log.debug("The email has been sent, status: " + status);
        } catch (Exception e) {
            setExecuteThrowable(new ActionException(e.getMessage(), e));
        }
        return actionResult;
    }
}
