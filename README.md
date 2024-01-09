# Accordion 🪗

[![CI](https://github.com/eoctet/accordion/actions/workflows/maven_build_deploy.yml/badge.svg)](https://github.com/eoctet/accordion/actions/workflows/maven_build_deploy.yml)
[![Maven Central](https://img.shields.io/maven-central/v/chat.octet/accordion?color=orange)](https://mvnrepository.com/artifact/chat.octet/accordion)
[![README Zh_CN](https://img.shields.io/badge/Lang-中文-red)](./README.Zh_CN.md)

`Accordion` is an automated task framework. You can combine multiple actions to achieve an efficient automation task, just like the `IFTTT` simple and straightforward, quickly improving the efficiency of your system.

#### Supported actions

| Action       | Description                                                      |
|--------------|------------------------------------------------------------------|
| 💡 API       | Calling third-party Restful APIs.                                |
| ⚙️ Condition | Logical judgment of combination conditions and single conditions. |
| 🔗 Switch    | Multi-branch link execution.                                     |
| ✉️ Email     | You can send custom emails.                                                    |
| 📝 Script    | Execute custom scripts.                                                   |


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

#### Examples

Another simple example of `Hello world`

```java
public class HelloWorld {

    public static void main(String[] args) {
        ActionConfig myAction = ActionConfig.builder()
                .id(CommonUtils.randomString("ACT").toUpperCase())
                .actionType(ActionType.SCRIPT.name())
                .actionName("My action")
                .actionDesc("My first action example")
                .actionParams(ScriptParameter.builder().script("println('Hello world')").build())
                .build();

        AccordionPlan plan = AccordionPlan.of().start(myAction);
        Accordion accordion = new Accordion(plan);
        accordion.play(true);
        System.out.println("Accordion plan:\n" + accordion.verbose());
    }
}
```


```text
Hello world
... ...

Accordion plan:
🅞───⨀ ✅ My Action (ACT-WD4J1ZK2IU)
```

> More examples: `chat.octet.accordion.examples.*`


## Manual

- __[Development manual](https://github.com/eoctet/accordion/wiki/使用手册)__


## Feedback

- If you have any questions, please submit them in GitHub Issue.