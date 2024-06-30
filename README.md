# ABCDE
> OpenHarmony逆向工具包 by Yricky
> 
> [查看完整文档目录](docs/index.md)


ABCDE是一个使用Kotlin编写的OpenHarmony逆向工具包，目前已经实现的功能为解析方舟字节码文件中的类信息、方法信息、字面量数组信息以及对方法进行反汇编等功能

该工具核心功能由纯kotlin（jvm）实现，因此可以提供平台无关的jar包供java工程引用并二次开发

## 构建
### 环境需求
- JDK17+

### 构建UberJar
```shell
./gradlew :abcdecoder:packageReleaseUberJarForCurrentOS
```
