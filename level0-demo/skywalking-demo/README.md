# install with docker
```
命令：docker pull

elasticsearch:7.5.1
apache/skywalking-oap-server:6.6.0-es7
apache/skywalking-ui:6.6.0

docker run -d --name=es7 \
-p 9200:9200 -p 9300:9300 \
-e "discovery.type=single-node" elasticsearch:7.5.1

docker run --name oap --restart always -d \
-e TZ=Asia/Shanghai \
-p 12800:12800 \
-p 11800:11800 \
--link es7:es7 \
-e SW_STORAGE=elasticsearch \
-e SW_STORAGE_ES_CLUSTER_NODES=es7:9200 \
apache/skywalking-oap-server:6.6.0-es7

docker run -d --name skywalking-ui \
-e TZ=Asia/Shanghai \
-p 8081:8081 \
--link oap:oap \
-e SW_OAP_ADDRESS=oap:12800 \
apache/skywalking-ui:6.6.0

```

# REF
[部署参考](https://blog.csdn.net/OptimusPP/article/details/106425807)