# 文档目录

## 核心库部分

核心库部分目前支持部署到maven仓库中供其他工程引用。
本篇文档中以部署到maven本地仓并在其他工程中引用为例。

> 准备工作

使用如下命令构建所有核心库并部署到本地仓

```shell
./gradlew publishToMavenLocal
```

如果正确执行，命令行中会有如下输出

```
Done!
groupId: io.github.yricky.oh //这部分为下述$groupId
version: 0.1.0-dev-4d03a43 //这部分为下述$version
```

接下来在你要引入核心库的项目的gradle文件中添加maven本地仓库依赖

```kotlin
    repositories {
        mavenLocal() //添加这一行
        google()
        mavenCentral()
    }
```
准备工作完成，可以继续往下看了
### 核心库列表

#### [abcde(abc文件解析库)](libabcde.md)
引入方式：
```kotlin
    dependencies {
        // implementation("$groupId:abcde-jvm:$version")
        implementation("io.github.yricky.oh:abcde-jvm:0.1.0-dev-4d03a43")
    }
```

