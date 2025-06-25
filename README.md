# ABCDE
> OpenHarmony逆向工具包 by Yricky
> 
> 项目中的其他文档：
> - [将abcde(abc文件解析库)引入项目](docs/libabcde.md)
> - [包体积分析工具示例](examples/abclen/readme.md)
> - [字符串查找工具示例](examples/findStr/readme.md)



ABCDE是一个使用Kotlin编写的OpenHarmony逆向工具包，目前已经实现的功能为解析方舟字节码文件中
的类信息、方法信息、字面量数组信息以及对方法进行反汇编，解析资源索引文件等功能。

该工具核心功能由纯kotlin（jvm）实现，因此可以提供平台无关的jar包供java工程引用并二次开发

## 构建
### 环境需求
- JDK17+

### 构建UberJar
```shell
./gradlew :abcdecoder:packageReleaseUberJarForCurrentOS
```
> 也可以去[github actions](https://github.com/Yricky/abcde/actions)
> 中下载最新的构建

## 功能演示
### 主页面
![main](docs/image/2025-01-02%2000.44.07.webp)
可以拖入或点击打开文件，支持文件类型有.abc、.hap、.index等
### hap页面
![hap](docs/image/2025-01-02%2000.45.38.webp)
这里可以以树形结构查看hap中的内容，其中的abc和index文件能够点击打开。
如果解析成功，该hap的部分包信息将在右侧展示。
### 资源索引查看页面
![resi](docs/image/2025-01-02%2000.46.57.webp)
![resi2](docs/image/2025-01-02%2000.47.01.webp)

这里提供与android studio查看arsc类似的功能，可以查看openHarmony
资源索引文件中的内容，并支持按类型区分和名称+内容的查找。
### abc文件查看页面
![abc](docs/image/2025-01-02%2000.48.52.webp)
这里可以按照树形结构查看abc字节码文件中的类信息，支持按名称查找。左侧
信息页签也支持查看字节码版本、校验和等信息。
### 类信息和字节码查看
![class](docs/image/2025-01-02%2000.50.08.webp)
点入某个类后，可以查看类的方法和字段。支持简单的索引，左侧信息页签中可以查看
这个类的导入导出信息
![bytecode](docs/image/2025-01-02%2000.50.37.webp)
点击类中的某个方法即可查看该方法的字节码。

> 这里展示的字节码相比于官方工具，额外提供了如下实用功能：
> 1. 对于引用了导入模块的指令，展示可读的模块导入
> 2. 如果是Hap包中的abc文件，会同时尝试解析hao包中的资源索引并在引用处展示为字符串
> 3. ctrl+单击指令引用的方法可以跳转

### 反编译（实验性）

ABCDE支持将部分方舟字节码反编译为js代码。这项功能目前处于实验阶段，有以下局限性：
- 不支持包含tryCatch的函数
- 不支持部分字节码，如async相关字节码等
- 反编译js代码为直译，尚未优化，可读性不够高。

![discompiler_demo](docs/image/2025-03-09%2021.58.31.webp)



### 命令行
目前支持使用命令行解析出abc文件中的class列表和资源索引文件中的内容
以下是命令行示例：

> dump class
```shell
java -jar /path/to/abcdecoder.jar --cli --dump-class /path/to/module.abc [--out=out.txt]
```

> dump index
```shell
java -jar /path/to/abcdecoder.jar --cli --dump-index /path/to/resources.index [--out=out.json]
```

