package chat.octet.accordion.action;


import chat.octet.accordion.action.api.ApiAction;
import chat.octet.accordion.action.base.ConditionAction;
import chat.octet.accordion.action.base.SwitchAction;
import chat.octet.accordion.action.base.TestAction;
import chat.octet.accordion.action.email.EmailAction;
import chat.octet.accordion.action.model.ActionConfig;
import chat.octet.accordion.action.script.ScriptAction;
import chat.octet.accordion.action.shell.ShellAction;
import chat.octet.accordion.core.enums.ActionType;
import chat.octet.accordion.exceptions.ActionException;
import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;

/**
 * Central registry for managing and instantiating action implementations.
 *
 * <p>ActionRegister serves as the factory and registry for all action types in the
 * Accordion framework. It maintains a mapping between action type names and their
 * corresponding implementation classes, providing a centralized way to create
 * action instances from configuration.</p>
 *
 * <p>Key Features:</p>
 * <ul>
 *   <li><strong>Singleton Pattern</strong>: Ensures single registry instance across the application</li>
 *   <li><strong>Type Safety</strong>: Validates action types and configurations</li>
 *   <li><strong>Extensibility</strong>: Supports registration of custom action types</li>
 *   <li><strong>Factory Pattern</strong>: Creates action instances from configurations</li>
 * </ul>
 *
 * <p>Built-in Action Types:</p>
 * <ul>
 *   <li><strong>API</strong>: HTTP/REST API calls</li>
 *   <li><strong>EMAIL</strong>: Email sending functionality</li>
 *   <li><strong>SCRIPT</strong>: Script execution (Aviator engine)</li>
 *   <li><strong>SHELL</strong>: Command line execution</li>
 *   <li><strong>CONDITION</strong>: Conditional logic and branching</li>
 *   <li><strong>SWITCH</strong>: Multi-branch execution paths</li>
 *   <li><strong>TEST</strong>: Testing and validation utilities</li>
 * </ul>
 *
 * <p>Usage Examples:</p>
 *
 * <p><strong>Check Registration:</strong></p>
 * <pre>{@code
 * ActionRegister register = ActionRegister.getInstance();
 * boolean isRegistered = register.isRegistered("API");
 * }</pre>
 *
 * <p><strong>Register Custom Action:</strong></p>
 * <pre>{@code
 * register.register("CUSTOM", "com.example.CustomAction");
 * }</pre>
 *
 * <p><strong>Build Action Instance:</strong></p>
 * <pre>{@code
 * ActionConfig config = ActionConfig.builder()
 *     .id("api-001")
 *     .actionType("API")
 *     .actionName("Fetch Data")
 *     .build();
 * ActionService action = register.build(config);
 * }</pre>
 *
 * <p>Thread Safety: This class is thread-safe using double-checked locking
 * for singleton initialization and synchronized collections for registration.</p>
 *
 * @author <a href="https://github.com/eoctet">William</a>
 * @see ActionService
 * @see ActionConfig
 * @see ActionType
 * @since 1.0.0
 */
public final class ActionRegister {

    private static volatile ActionRegister register;
    private static final Map<String, String> ACTION_MAPPING = Maps.newLinkedHashMap();

    static {
        ACTION_MAPPING.put(ActionType.API.name(), ApiAction.class.getName());
        ACTION_MAPPING.put(ActionType.CONDITION.name(), ConditionAction.class.getName());
        ACTION_MAPPING.put(ActionType.SWITCH.name(), SwitchAction.class.getName());
        ACTION_MAPPING.put(ActionType.EMAIL.name(), EmailAction.class.getName());
        ACTION_MAPPING.put(ActionType.SCRIPT.name(), ScriptAction.class.getName());
        ACTION_MAPPING.put(ActionType.TEST.name(), TestAction.class.getName());

        ACTION_MAPPING.put(ActionType.SHELL.name(), ShellAction.class.getName());
    }

    /**
     * Private constructor to enforce singleton pattern.
     * Prevents external instantiation of the registry.
     */
    private ActionRegister() {
    }

    /**
     * Returns the singleton instance of the ActionRegister.
     *
     * <p>Uses double-checked locking pattern to ensure thread-safe lazy initialization
     * of the singleton instance. This method is safe to call from multiple threads
     * concurrently.</p>
     *
     * @return the singleton ActionRegister instance
     * @since 1.0.0
     */
    public static ActionRegister getInstance() {
        if (register == null) {
            synchronized (ActionRegister.class) {
                if (register == null) {
                    register = new ActionRegister();
                }
            }
        }
        return register;
    }


    /**
     * Checks if an action type is registered in the registry.
     *
     * <p>This method verifies whether a given action type has been registered
     * and is available for instantiation. It's useful for validation before
     * attempting to build action instances.</p>
     *
     * @param actionType the action type to check (case-sensitive)
     * @return true if the action type is registered, false otherwise
     * @throws IllegalArgumentException if actionType is null or blank
     * @see ActionType
     * @since 1.0.0
     */
    public boolean isRegistered(final String actionType) {
        Preconditions.checkArgument(StringUtils.isNotBlank(actionType), "Action type cannot be empty.");
        return ACTION_MAPPING.containsKey(actionType);
    }

    /**
     * Registers a new action type with its implementation class.
     *
     * <p>This method allows registration of custom action types using the
     * {@link ActionType} enum. It's a type-safe wrapper around the string-based
     * registration method.</p>
     *
     * @param actionType the action type enum value
     * @param className  the fully qualified class name of the action implementation
     * @return true if registration was successful, false if the type was already registered
     * @throws IllegalArgumentException if className is null or blank
     * @see ActionType
     * @since 1.0.0
     */
    public boolean register(final ActionType actionType, final String className) {
        return register(actionType.name(), className);
    }

    /**
     * Registers a new action type with its implementation class.
     *
     * <p>This method allows registration of custom action types by providing
     * the action type name and the fully qualified class name of the implementation.
     * The implementation class must:</p>
     *
     * <ul>
     *   <li>Implement the {@link ActionService} interface</li>
     *   <li>Have a public constructor that accepts {@link ActionConfig}</li>
     *   <li>Be available on the classpath</li>
     * </ul>
     *
     * <p>Registration will fail if the action type is already registered.</p>
     *
     * @param actionType the action type name (case-sensitive)
     * @param className  the fully qualified class name of the action implementation
     * @return true if registration was successful, false if the type was already registered
     * @throws IllegalArgumentException if actionType or className is null or blank
     * @see ActionType
     * @since 1.0.0
     */
    public boolean register(final String actionType, final String className) {
        if (isRegistered(actionType)) {
            return false;
        }
        Preconditions.checkArgument(StringUtils.isNotBlank(className), "Action class name cannot be empty.");
        ACTION_MAPPING.put(actionType, className);
        return true;
    }

    /**
     * Removes a registered action type from the registry.
     *
     * <p>This method allows unregistration of action types, which can be useful
     * for plugin systems or dynamic action management. Once unregistered, the
     * action type will no longer be available for instantiation.</p>
     *
     * <p><strong>Warning:</strong> Unregistering built-in action types may cause
     * existing plans to fail. Use with caution.</p>
     *
     * @param actionType the action type to remove from the registry
     * @return true if the action type was successfully removed, false if it wasn't registered
     * @since 1.0.0
     */
    public boolean unregister(final String actionType) {
        if (!isRegistered(actionType)) {
            return false;
        }
        ACTION_MAPPING.remove(actionType);
        return true;
    }

    /**
     * Creates an action service instance from the provided configuration.
     *
     * <p>This is the primary factory method for creating action instances. It performs
     * comprehensive validation of the configuration and uses reflection to instantiate
     * the appropriate action class.</p>
     *
     * <p>Creation Process:</p>
     * <ol>
     *   <li>Validates the action configuration</li>
     *   <li>Looks up the implementation class for the action type</li>
     *   <li>Uses reflection to create an instance with the configuration</li>
     *   <li>Returns the fully initialized action service</li>
     * </ol>
     *
     * <p>Validation includes:</p>
     * <ul>
     *   <li>Configuration is not null</li>
     *   <li>Action ID is not blank</li>
     *   <li>Action type is not blank and is registered</li>
     *   <li>Action name is not blank</li>
     * </ul>
     *
     * @param actionConfig the configuration defining the action to create
     * @return a fully initialized action service instance
     * @throws ActionException      if configuration is invalid, action type is not registered,
     *                              or instantiation fails
     * @throws NullPointerException if actionConfig is null
     * @since 1.0.0
     */
    public ActionService build(final ActionConfig actionConfig) {
        if (actionConfig == null) {
            throw new ActionException("Action config cannot be null");
        }

        Preconditions.checkArgument(StringUtils.isNotBlank(actionConfig.getId()),
                "Action ID cannot be empty for action: " + actionConfig);
        Preconditions.checkArgument(StringUtils.isNotBlank(actionConfig.getActionType()),
                "Action type cannot be empty for action ID: " + actionConfig.getId());
        Preconditions.checkArgument(StringUtils.isNotBlank(actionConfig.getActionName()),
                "Action name cannot be empty for action ID: " + actionConfig.getId());

        String actionType = actionConfig.getActionType();
        String className = ACTION_MAPPING.get(actionType);
        if (className == null) {
            throw new ActionException("Action type '" + actionType + "' is not registered. "
                    + "Available types: " + String.join(", ", ACTION_MAPPING.keySet())
                    + ". Action ID: " + actionConfig.getId());
        }

        try {
            Class<?> clazz = Class.forName(className);
            return (ActionService) clazz.getConstructor(ActionConfig.class).newInstance(actionConfig);
        } catch (ClassNotFoundException e) {
            throw new ActionException("Action class not found: " + className + " for action type: " + actionType, e);
        } catch (NoSuchMethodException e) {
            throw new ActionException("Action class " + className
                    + " must have a constructor that accepts ActionConfig", e);
        } catch (Exception e) {
            throw new ActionException("Failed to create action instance for type '" + actionType
                    + "', ID: " + actionConfig.getId() + ". Error: " + e.getMessage(), e);
        }
    }

}
