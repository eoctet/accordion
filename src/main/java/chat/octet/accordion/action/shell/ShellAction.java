package chat.octet.accordion.action.shell;


import chat.octet.accordion.action.AbstractAction;
import chat.octet.accordion.action.model.ActionConfig;
import chat.octet.accordion.action.model.ExecuteResult;
import chat.octet.accordion.action.model.OutputParameter;
import chat.octet.accordion.exceptions.ActionException;
import chat.octet.accordion.utils.CommonUtils;
import com.google.common.base.Preconditions;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringSubstitutor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serial;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
public class ShellAction extends AbstractAction {
    @Serial
    private static final long serialVersionUID = 1L;
    private final transient ShellParameter params;

    public ShellAction(final ActionConfig actionConfig) {
        super(actionConfig);
        this.params = actionConfig.getActionParams(ShellParameter.class, "Shell parameter cannot be null.");
        Preconditions.checkArgument(StringUtils.isNotBlank(params.getShell()), "Shell cannot be empty.");
    }

    private String getProcessOutput(final Process process) {
        StringBuilder output = new StringBuilder();
        try (InputStreamReader input = new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8);
             BufferedReader reader = new BufferedReader(input)) {
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return !output.isEmpty() ? output.deleteCharAt(output.length() - 1).toString() : "";
    }

    /**
     * Executes the shell command and returns the result.
     *
     * @return the execution result containing output and status
     * @throws ActionException if the shell command execution fails
     */
    @Override
    public ExecuteResult execute() throws ActionException {
        ExecuteResult executeResult = new ExecuteResult();
        try {
            //format shell and inject dynamic variables
            String shell = StringSubstitutor.replace(params.getShell(), getInputParameter());
            ProcessBuilder builder;
            if (ShellParameter.ShellType.CMD == params.getType()) {
                builder = new ProcessBuilder("cmd", "/c", shell);
            } else if (ShellParameter.ShellType.POWERSHELL == params.getType()) {
                builder = new ProcessBuilder("powershell", "-Command", shell);
            } else {
                builder = new ProcessBuilder("bash", "-c", shell);
            }
            Process process = builder.start();
            String output = getProcessOutput(process);
            log.debug("Command execute output:\n{}", output);

            process.waitFor(params.getTimeout(), TimeUnit.MILLISECONDS);
            int exitCode = process.exitValue();
            if (exitCode != 0) {
                log.warn("Command completed, exit code: {}.", exitCode);
            } else {
                log.debug("Command completed, exit code: {}.", exitCode);
            }

            //update action execute result
            List<OutputParameter> outputParams = getActionOutput();
            if (!CommonUtils.isEmpty(outputParams)) {
                OutputParameter outputParameter = getActionOutput().stream().findFirst().get();
                executeResult.add(outputParameter.getName(), output);
            }
        } catch (IOException | InterruptedException e) {
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            setExecuteThrowable(new ActionException(e.getMessage(), e));
        }
        return executeResult;
    }
}
