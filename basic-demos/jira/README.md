# 安装
```
mysql -h127.0.0.1 -uroot -p123456 -e "create database jira 
default character set utf8 collate utf8_bin;
grant all on jira.* to 'jira'@'%' identified by '123456' "
```
jira 库 安装
```
create database jira default character set utf8 collate utf8_bin;

create user jira identified by '123456';

grant all privileges on jira.* to confluence@'%';

flush  privileges;
```

2.可以通过以下命令下载特定安装包

wget https://downloads.atlassian.com/software/jira/downloads/atlassian-jira-software-7.2.2-x64.bin

3.安装后，更换文件之前，需要先停止jira。

cd /opt/atlassian/jira/bin/ 
./stop-jira.sh #停止jira 
./startup.sh #启动jira

# 参考
[JIRA 7.2.2破解版 服务端安装](https://blog.csdn.net/weixin_39008941/article/details/77485415)
[中文和破解](https://www.cnblogs.com/ilanni/p/6200875.html)