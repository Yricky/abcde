# ABCDE
> OpenHarmony逆向工具包 by Yricky

ABCDE是一个使用Kotlin编写的OpenHarmony逆向工具包，目前已经实现的功能为解析方舟字节码文件中的类信息、方法信息、字面量数组信息以及对方法进行反汇编等功能

## 构建
### 环境需求
- JDK17+
### 构建核心包
```shell
./gradlew :kra:jar
```
### 构建UberJar
```shell
./gradlew packageReleaseUberJarForCurrentOS
```
## 核心库
> 该工具核心功能由纯kotlin（jvm）实现，因此可以提供平台无关的jar包供java工程引用并二次开发
以下是一个代码样例，读取某个abc文件中的所有类信息

```kotlin
val file = File("/path/to/modules.abc")
val mmap = FileChannel.open(file.toPath()).map(FileChannel.MapMode.READ_ONLY,0,file.length())
val abc = AbcBuf(mmap)
abc.classes.forEach { l ->
    val it = l.value
    if(it is ClassItem) {
        println("${it.region}c:${it.name}\n${it.data}")
        it.fields.forEach {
            println("(f)\t${it.name}")
        }
        it.methods.forEach {
            println("(m) ${it.clazz.name} ${it.proto.shorty}\t${it.name}")
        }
    } else {
        println("fc:${it.name}")
    }
}
```
