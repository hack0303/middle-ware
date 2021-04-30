# install
```
下载镜像
docker pull kibana:7.5.1
 

配置文件
mkdir -p /data/elk7/kibana/config/
vi /data/elk7/kibana/config/kibana.yml
内容如下：

#
# ** THIS IS AN AUTO-GENERATED FILE **
#

# Default Kibana configuration for docker target
server.name: kibana
server.host: "0"
elasticsearch.hosts: [ "http://真实IP:9200" ]
xpack.monitoring.ui.container.elasticsearch.enabled: true

 


docker run -d \
  --name=kibana \
  --restart=always \
  -p 5601:5601 \
  -v /data/elk7/kibana/config/kibana.yml:/usr/share/kibana/config/kibana.yml \
  kibana:7.5.1

```

# ref
[docker安装kibana](https://blog.csdn.net/shykevin/article/details/108272260)