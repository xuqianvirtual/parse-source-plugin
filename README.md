# parse-source-plugin
generate-sources 阶段解析 Java 文件的 Maven 插件，可以扫描项目中的 Java 文件，并根据需要执行一些逻辑。
本示例展示了如何收集 Java 全限定类名并将其写入 resources/collect_class.txt 文件中。
## 模块功能说明
### parse-source-antlr
提供了使用 antlr 解析 Java 文件的能力，g4 文件来自 https://github.com/antlr/grammars-v4 。
### parse-source-generate
核心插件模块，提供了收集 Java 全限定类名的能力。
### parse-source-example
示例模块，用于展示插件效果。
## 使用说明
在项目根目录下执行如下命令：
        mvn clean install -DskipTests
即可在 src/main/resources 目录下看到 collect_class.txt 文件，其中包含当前模块下所有类的全限定类名。