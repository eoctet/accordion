# Accordion 🪗

## Introduction

`Accordion` is an automated task framework. You can combine multiple actions to achieve an efficient automation task, just like the `IFTTT` simple and straightforward, quickly improving the efficiency of your system.


## Quick start

Create your own project and use `Maven` or `Gradle` to import the accordion framework.

> For the latest version, check out GitHub Release or search the Maven repository.

#### Maven

```xml
<dependency>
    <groupId>chat.octet</groupId>
    <artifactId>accordion</artifactId>
    <version>LAST_RELEASE_VERSION</version>
</dependency>
```

#### Gradle
```txt
implementation group: 'chat.octet', name: 'accordion', version: 'LAST_RELEASE_VERSION'
```

#### Hello world

Create a configuration template for an action, here we use a `custom script` to output `Hello world`.

```java
ActionConfig myAction = ActionConfig.builder()
        .id(CommonUtils.randomString("ACT").toUpperCase())
        .actionType(ActionType.SCRIPT.name())
        .actionName("My action")
        .actionDesc("My first action example")
        .actionParams(ScriptParameter.builder().script("println('Hello world')").build())
        .build();
```

Create the accordion score `AccordionPlan`, which is a complete execution plan that includes the execution actions for each step. Then create an `Accordion` to play the score.

```java
AccordionPlan plan = AccordionPlan.of().start(myAction);
Accordion accordion = new Accordion(plan);
accordion.play(true);
System.out.println("Accordion plan:\n" + accordion.verbose());
```

The completed output is as follows. The `verbose` method can be used to print the execution link and the execution status of each action.

```log
Hello world
... ...

Accordion plan:
🅞───⨀ ✅ My action (ACT-DEM9W7UDPC)
```

## Function introduction

The following is an introduction to the main functions of this project. You can selectively read according to your needs, which will help you quickly understand the technical features and usage methods of the accordion framework.

### AccordionPlan

`AccordionPlan` is an Execution Plan (DAG) consisting of an Execution Chain and an Action. The execution plan starts with a starting action and then connects other actions through the `next` method to form an execution chain.

> ℹ️ **TIPS**
>
> - The execution chain connects each action, which in the DAG is the `Edge`, and the action is the specific `Node`.
>
> - The execution of each action depends on the preceding action. If the preceding action fails, the following action will not be executed.

`AccordionPlan` API

```java
/**
 * Add a start action.
 *
 * @param actionConfig Action config
 * @return AccordionPlan
 */
public AccordionPlan start(ActionConfig actionConfig);

/**
 * Add a next action.
 *
 * @param previousAction Previous action
 * @param nextAction     Next action
 * @return AccordionPlan
 */
public AccordionPlan next(ActionConfig previousAction, ActionConfig nextAction);

/**
 * Add one or more next actions.
 *
 * @param previousAction Previous action
 * @param nextActions    One or more next actions
 * @return AccordionPlan
 */
public AccordionPlan next(ActionConfig previousAction, ActionConfig... nextActions);

/**
 * Reset the plan status.
 */
public void reset();

/**
 * Export the plan to JSON.
 *
 * @return String
 */
public String exportToJsonConfig();

/**
 * Import the plan from JSON.
 *
 * @param accordionConfigJson Accordion config JSON.
 * @return AccordionPlan
 */
public AccordionPlan importConfig(String accordionConfigJson);

/**
 * Import the plan from AccordionConfig.
 *
 * @param accordionConfig Accordion config.
 * @return AccordionPlan
 */
public AccordionPlan importConfig(AccordionConfig accordionConfig);

```

**Example**

Here a complex execution plan is used for demo, as shown in the following figure, which includes a total of 10 actions, each connected through the `next` method.

![sample_01.png](sample_01.png)

Example of execution plan:

```java
// ... ...
AccordionPlan plan = AccordionPlan.of()
        .next(a, b, c, r, w)
        .next(b, d)
        .next(c, d)
        .next(d, e, g, f)
        .next(e, h)
        .next(f, h)
        .next(g, h);

Accordion accordion = new Accordion(plan);
accordion.play(true);
System.out.println("Accordion plan:\n" + accordion.verbose());
```

Output of execution plan:

```text
Accordion plan:
🅞───⨀ ✅ A (ACT-M43B62QK56)
	├───⨀ ✅ B (ACT-HBT8E98ZJP)
	├───⨀ ✅ C (ACT-7239Z51LKX)
	├───⨀ ✅ R (ACT-5VR1Y3WMBP)
	└───⨀ ✅ W (ACT-T39P7JFIVL)
		└───⨀ ✅ D (ACT-XUM24TLIBI)
			├───⨀ ✅ E (ACT-0KSSQYF52E)
			├───⨀ ✅ G (ACT-XU3OXGBZ5Y)
			└───⨀ ✅ F (ACT-56Q2VNG5B4)
				└───⨀ ✅ H (ACT-LERLLYDHQN)
```

**Import & Export accordion plan**

After creating an execution plan, you can export it as a `JSON` for persistence storage or import it into another execution plan.

```json
{
  "id": "ACR-MDESSISDQB",
  "name": "Default accordion name",
  "desc": "Default accordion desc",
  "graph_config": {
    "actions": [
      {
        "id": "ACT-BKFR133GZ0",
        "action_type": "SCRIPT",
        "action_name": "My action",
        "action_desc": "My first action example",
        "action_params": {
          "scriptId": "script-dyk9obhc63",
          "script": "1+1",
          "debug": false
        },
        "action_output": [
          {
            "name": "number",
            "datatype": "LONG",
            "desc": "Calc result"
          }
        ]
      },
      {
        "id": "ACT-OLH1M13ZIR",
        "action_type": "CONDITION",
        "action_name": "Condition action",
        "action_desc": "Condition action example",
        "action_params": {
          "expression": "(number == 2)",
          "debug": false
        }
      }
    ],
    "edges": [
      {
        "previous_action": "ACT-BKFR133GZ0",
        "next_action": "ACT-OLH1M13ZIR"
      }
    ]
  },
  "updatetime": "2024-01-09 12:51:43"
}
```

### Action

Action is an execution unit that can do many things. Each action has three stages: `input`, `execution`, and `output`.

- **Input**
    - The input parameters of an action can be the output result of the previous action or the event message. In development, we do not need to declare, `AbstractAction` will be automatically processed.

- **Execution**
    - This is the execution module for actions, where you can implement your business functions. You can refer to the code for preset actions for details.

- **Output**
    - The output parameters of the action need to be specified in the action configuration template as `actionOutput`.

- **Status**
    - ⚪️ `NORMAL`  Default state
    - 🅾️ `ERROR`   Execution error occurred
    - ✅ `SUCCESS` Execution completed
    - 🟡 `SKIP`    Execution skipped, for example: when an error occurs in the execution of an action, the next action will be skipped.


The following is a list of preset basic actions that you can expand as needed.

#### ApiAction

> `chat.octet.accordion.action.api.ApiAction`

`ApiAction` Supports calling third-party Restful APIs. supports requests and responses in JSON and XML data formats, and also supports the use of proxy services.

By default, the timeout for requesting an API is 5 seconds, and you can adjust it according to the actual situation.

> All actions use build mode to create object instances, including parameter templates for actions.

```java
ActionConfig action = ActionConfig.builder()
        .id(CommonUtils.randomString("ACT").toUpperCase())
        .actionType(ActionType.API.name())
        .actionName("API")
        .actionDesc("An api request action")
        .actionParams(
                ApiParameter.builder()
                        .url("https://127.0.0.1:8080/api/query")
                        .method(HttpMethod.GET)
                        .build()
        )
        .build();
```

- __Action Parameters__

| Parameter                | Required | Description                                           |
|--------------------------|----------|-------------------------------------------------------|
| url                      | Y        | Request url, http://127.0.0.1/api                     |
| method                   | Y        | Request methods, GET/POST/PUT...                      |
| headers                  | N        | Request header parameter list {"args": "value"}       |
| request                  | N        | Request parameter list {"args": "value"}              |
| form                     | N        | Request form parameter list {"args": "value"}         |
| body                     | N        | Request body, supports JSON or XML                    |
| timeout                  | N        | Request timeout time (milliseconds), default: 5000 ms |
| responseDataFormat       | N        | Response data format, supports JSON or XML            |
| retryOnConnectionFailure | N        | Retry the request when an error occurs                |
| proxyType                | N        | Proxy type: DIRECT/HTTP/SOCKS                         |
| proxyServerAddress       | N        | Proxy server address 127.0.0.1                        |
| proxyServerPort          | N        | Proxy server port 8080                                |

- __Action Output Parameters__

According to the return value of the request, specify the required output parameters. 

For example, if the response data of the request contains the `status` field, then we can use `status` as the output parameter for subsequent actions.

----

#### ConditionAction

> `chat.octet.accordion.action.base.ConditionAction`

`ConditionAction` is similar to the `if` in Java, used to control the execution process.
When the conditional judgment is not true, the execution will be interrupted.

```java
Condition condition = new Condition("age", ConditionOperator.GT, 18)
        .and("age", ConditionOperator.LT, 30);

String expression = ConditionBuilder.getInstance().build(condition);

ActionConfig action = ActionConfig.builder()
        .id(CommonUtils.randomString("ACT").toUpperCase())
        .actionType(ActionType.CONDITION.name())
        .actionName("Condition example")
        .actionDesc("A condition action example")
        .actionParams(
                ConditionParameter.builder().expression(expression).build()
        )
        .build();
```

- __Conditional expressions__

There are two ways to use conditional expressions. The first is to write the expression directly, and the second is to create it using `Condition`.

Example 1:

```java
Condition condition = new Condition("age", ConditionOperator.GT, 18)
        .and("age", ConditionOperator.LT, 30);

String expression = ConditionBuilder.getInstance().build(condition);
```

Example 2:

```text
(age > 18) and (age < 30)
```

Conditional expressions can be evaluated using common combinations of logical operators.

such as:

```text
# Evaluate expressions
1+1==2

# Conditional expressions
(a/b>1) or (a/b==0)

# Complex multi-conditional expressions
((a/b>1) or (a/b==0)) and (a==x)
```

In conditional expressions, we can use `dynamic variables`. In the above example, parameters `a`, `b`, and `x` will be replaced with actual values for calculation.


> ⚠️ **Attention**: Missing dynamic variable values will be replaced with `null`, which will result in the calculation conditions not being true.

- __Action Parameters__

| Parameter  | Required | Description             |
|------------|----------|-------------------------|
| expression | Y        | Conditional expressions |

- __Action Output Parameters__

After the conditional action is executed, it will return either `true` or `false`.

----


#### SwitchAction

> `chat.octet.accordion.action.base.SwitchAction`

`SwitchAction` is a combination of multiple sets of conditions used to control multiple different execution chains, suitable for execution scenarios of multiple independent tasks.

```java
SwitchParameter switchParameter = SwitchParameter.builder().build().addBranch(
    SwitchParameter.Branch.builder().name("Go A").actionId(a.getId()).expression("2==1").build(),
    SwitchParameter.Branch.builder().name("Go B").actionId(b.getId()).expression("1+1>1").build(),
    SwitchParameter.Branch.builder().name("Go C").actionId(c.getId()).expression("1==1").build()
);

ActionConfig switchAction = ActionConfig.builder()
        .id(CommonUtils.randomString("ACT").toUpperCase())
        .actionType(ActionType.SWITCH.name())
        .actionName("Switch action")
        .actionDesc("A switch action example")
        .actionParams(switchParameter)
        .build();
```

> In the above example, only the B and C branches will be executed.

- __Action Parameters__

| Parameter  | Required | Description                        |
|------------|----------|------------------------------------|
| branches   | Y        | Branch List  [...]                 |
| name       | Y        | Branch name                        |
| actionId   | Y        | Action ID for branch execution     |
| expression | Y        | Conditional expressions            |
| negation   | Y        | Conditional reversal: true / false |

- __Action Output Parameters__

After the SwitchAction is executed, a list of objects containing the branch name and execution result will be returned.

----

#### EmailAction

> `chat.octet.accordion.action.eamil.EmailAction`

`EmailAction` Supports sending emails to multiple email addresses.

By default, the content format of the email is in HTML format, and email attachments are not currently supported.

> ⚠️ **Attention**: Please strictly check the content of your email to ensure that it does not contain script injections such as `js`, which are often intercepted by the email system.

```java
ActionConfig emailAction = ActionConfig.builder()
        .id(CommonUtils.randomString("ACT").toUpperCase())
        .actionType(ActionType.EMAIL.name())
        .actionName("Email action")
        .actionDesc("A email notify action")
        .actionParams(
                EmailParameter.builder()
                        .subject("A testing email")
                        .from("accordion@octet.pro")
                        .to("test@octet.pro,other@octet.pro")
                        .cc("manager@octet.pro")
                        .content("<p>Testing from <h1>Accordion</h1></p>")
                        .server("smtp.examples.com")
                        .username("ACCOUNT")
                        .password("PASS")
                        .build()
        )
        .build();
```

- __Action Parameters__

| Parameter | Required | Description                                               |
|-----------|----------|-----------------------------------------------------------|
| server    | Y        | Email server address `127.0.0.1`                          |
| smtpPort  | Y        | Email server port，default: `25`                           |
| ssl       | N        | Use SSL, default: false                                   |
| tls       | N        | Use TLS, default: false                                   |
| username  | N        | Sender account                                            |
| password  | N        | Sender password                                           |
| subject   | Y        | Email subject                                             |
| from      | Y        | Email sender                                              |
| to        | Y        | Email recipient, Using "," dividing multiple parameters   |
| cc        | N        | Email carbon copy, Using "," dividing multiple parameters |
| content   | Y        | Email content                                             |
| timeout   | N        | Connection timeout, default: 5000 ms                      |
| debug     | N        | Debug mode, default: false                                |

- __Action Output Parameters__

Email action has no output parameters.

----

#### ScriptAction

> chat.octet.accordion.action.script.ScriptAction

`ScriptAction` support `Java` language syntax, used to implement complex task scenarios.

> ℹ️ **Features**
>
> - `Java` and `lambda` expression
> - `Debug mode` Easy to track issues
> - `Dynamic variables` For example: `num = x + y`
> - `Functions` For example: `Mathlib`, `String processing`, etc
>
> Implement using the `com.googlecode.aviator` framework. For more syntax rules, please refer to the aviator user manual.
>
> ⚠️ **Attention: By default, custom scripts disable some language features to ensure system security.**

```java
ActionConfig scriptAction = ActionConfig.builder()
        .id(CommonUtils.randomString("ACT").toUpperCase())
        .actionType(ActionType.SCRIPT.name())
        .actionName("Script action")
        .actionDesc("A script action example")
        .actionParams(
                ScriptParameter.builder().script("println('Hello world')").build()
        )
        .build();
```

- __Action Parameters__

| Parameter | Required | Description                                   |
|-----------|----------|-----------------------------------------------|
| scriptId  | N        | Script id, Automatically generated by default |
| script    | Y        | Script code snippets                          |
| debug     | N        | Debug mode, default: false                    |

- __Action Output Parameters__

Without specifying output parameters, custom actions will use the default output parameters `ACTION_SCRIPT_RESULT`, of type `String`, is used to save the result of script execution.

| Parameter            | DataType | Description              |
|----------------------|----------|--------------------------|
| ACTION_SCRIPT_RESULT | String   | Default output parameter |

----

#### Custom action

In addition to preset actions, you can create your own custom actions, which can be implemented as follows:

- Implement custom actions

```java
public class MyAction extends AbstractAction {

    public MyAction(ActionConfig actionConfig) {
        super(actionConfig);
    }

    @Override
    public ExecuteResult execute() throws ActionException {
        ExecuteResult executeResult = new ExecuteResult();
       
        try {
             //YOUR CODE...
        } catch (Exception e) {
            setExecuteThrowable(new ActionException(e.getMessage(), e));
        }
        return executeResult;
    }
}
```

> ℹ️ **TIPS**
> 
> - All actions inherit `AbstractAction` and implement the `execute` method, initializing the required parameters in the constructor.
> 
> - `execute` method defaults to using `try-catch` to handle exceptions, and you can also choose to throw an exception.
> 
> - If you need to pass output parameters, please write them to `ExecuteResult`.


- Register custom actions

Custom actions require registration to be used, and `ActionType` is globally unique. You can define a special name to identify it.

```java
ActionRegister.getInstance().register("MyAction", MyAction.class.getName());
```

- unregister custom actions

For unnecessary actions, you can remove them through the `unregister` method.

```java
ActionRegister.getInstance().unregister("MyAction");
```


### Event messages

`Event message` is a data source, and in some scenarios, we need to provide feedback on event messages sent from upstream and perform a series of complex actions.

> ℹ️ **Scenario examples**
> 
> - In streaming computing scenarios, we can perform real-time computation and processing on consumed event messages.
>
> - In monitoring scenarios, we can alert for outliers and perform specific operations.

**Example**

> chat.octet.accordion.examples.[MessageExample](../src/test/java/chat/octet/accordion/examples/MessageExample.java)


### Parameter passing

Throughout the entire task execution process, we can use `Action output parameters` or `Action session` to pass parameter values.

- Action output parameters

__Only used between actions, the output parameter of the previous action will be passed as the input parameter of the next action by default.__ 

For example, requesting the return result field of a certain interface can be used to determine the condition of the next action.

- Action session

__Globally available__, `Action session` is initialized at the beginning of task execution, which stores the status and messages of the execution chain. You can store the parameters that need to be passed globally in the session.

> ⚠️ __Parameter naming convention__
>
> Use clear and easy to understand parameter names as much as possible to avoid duplicate parameter names that may cause parameters to be overwritten.

**Example**

> chat.octet.accordion.examples.[SimpleExample](../src/test/java/chat/octet/accordion/examples/SimpleExample.java)


### Data type

Supports data types `chat.octet.accordion.core.enums.DataType`

During the parameter transfer process, parameter type conversion will be automatically performed, and `String` will be used uniformly for parameters without specified data types.


----

## Appendix

#### Reference Framework

In this project, I used `okhtp3` to implement lightweight HTTP requests and `aviator` to implement advanced scripting language functionality.

- `aviator`
- `okhttp3`
