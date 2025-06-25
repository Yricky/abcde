# abc文件解析库


核心库部分目前支持部署到maven仓库中供其他工程引用。
本篇文档中以部署到maven本地仓并在其他工程中引用为例。

## 编译构建

使用如下命令构建所有核心库并部署到本地仓

```shell
# 在工程根目录执行
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

引入方式：
```kotlin
    dependencies {
        // implementation("$groupId:abcde-jvm:$version")
        implementation("io.github.yricky.oh:abcde-jvm:0.1.0-dev-4d03a43")
    }
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
