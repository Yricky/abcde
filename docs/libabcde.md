# abc文件解析库

## 编译构建
构建核心jar包
```shell
./gradlew :modules:abcde:jvmJar
```

## 如何使用
> 以下是一个代码样例，读取某个abc文件中的所有类信息

```kotlin
val file = File("/path/to/modules.abc")
val mmap = FileChannel.open(file.toPath()).map(FileChannel.MapMode.READ_ONLY,0,file.length())
val abc = AbcBuf(file.path, mmap)
abc.classes.forEach { l ->
    val it = l.value
    if(it is AbcClass) {
        println("${it.region}c:${it.name}\n${it.data}")
        it.fields.forEach {
            println("(f)\t${it.name}")
        }
        it.methods.forEach {
            println("(m) ${it.defineStr(showClass = true)}")
        }
    } else {
        println("fc:${it.name}")
    }
}
```
