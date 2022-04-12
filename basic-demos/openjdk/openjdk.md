# openjdk

## 查询

[openjkd官网]: https://jdk.java.net/java-se-ri/11



## 下载

```
wget https://download.java.net/openjdk/jdk11/ri/openjdk-11+28_linux-x64_bin.tar.gz
```



## 解压

```
tar -xvf xxx
```



## 配置

```
vi /etc/profile
      JAVA_HOME=/usr/local/jdk1.7.0_71

      CLASSPATH=.:$JAVA_HOME/lib.tools.jar

      PATH=$JAVA_HOME/bin:$PATH

      export JAVA_HOME CLASSPATH PATH 
```



## 测试

```
java -version 
```



## 相关

# [linux 卸载jdk和安装](https://www.cnblogs.com/javabg/p/10332993.html)

