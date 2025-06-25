# 包体积分析工具示例

这个子工程是一个基于abcde的包体积分析工具示例。

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
这会在当前目录下生成一份txt格式的报告，其中包含：

- abc文件中被纳入统计的所有区间以及区间的含义
- 类独占的空间及其占比（按类完整路径名的第一级分类）
- 总空间、杂项空间以及统计到的体积占文件总体积的占比等信息