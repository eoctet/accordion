# Accordion 🪗

[![CI](https://github.com/eoctet/accordion/actions/workflows/maven_build_deploy.yml/badge.svg)](https://github.com/eoctet/accordion/actions/workflows/maven_build_deploy.yml)
[![Maven Central](https://img.shields.io/maven-central/v/chat.octet/accordion?color=orange)](https://mvnrepository.com/artifact/chat.octet/accordion)
[![README English](https://img.shields.io/badge/Lang-English-red)](./README.md)

`手风琴` 是一个自动化任务框架，你可以将多个动作组合起来实现高效的自动化任务，就像 `IFTTT` 简单且直接，快速提升你的系统效率。

#### 支持的动作清单

| 动作名称       | 功能                   |
|------------|----------------------|
| 💡 调用接口    | 调用第三方Restful API。    |
| ⚙️ 条件判断    | 组合条件、单一条件的逻辑判断。      |
| 🔗 条件分支    | 多分支链路执行。             |
| ✉️ 发送邮件    | 可以发送自定义的邮件。          |
| 📝 自定义脚本   | 执行自定义脚本。             |
| 💻 命令行     | 执行自定义命令行。            |
| 🤖 LlamaAI | 使用Llama AI生成对话和续写文本。 |

## 快速开始

创建你自己的项目，使用 `Maven` 或 `Gradle` 引入手风琴框架。

> 最新的版本请查阅 GitHub Release 或搜索 Maven repository。

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

另一个简单的 `Hello world` 示例。

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

> 更多示例: `chat.octet.accordion.examples.*`


## 开发文档

- __[开发手册](https://github.com/eoctet/accordion/wiki/开发手册)__
- __[Development manual](https://github.com/eoctet/accordion/wiki/Development-manual)__


## 问题反馈

- 如果你有任何疑问，欢迎在GitHub Issue中提交。