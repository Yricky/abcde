# 字符串查找工具示例

这个子工程是一个基于abcde的字符串查找工具示例。

## 用法
构建命令：
```shell
# 在项目根目录目录下执行
./gradlew fatJar
```
然后可以在`build/libs`下找到构建好的jar包。

使用以下命令执行构建好的jar包：
```shell
java -jar /path/to/jarfile.jar /path/to/an/abcfile.abc
```

运行后会在标准输出中输出硬编码的中文字符串