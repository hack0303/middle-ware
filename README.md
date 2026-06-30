# middle-ware

[![GitHub stars](https://img.shields.io/github/license/hack0303/middle-ware?style=plastic)](https://github.com/hack0303/middle-ware/)

Java 中间件学习与实践项目，涵盖主流中间件的 Demo 示例和 DAG 图建模组件。

---

## 模块导航

| 模块 | 说明 |
|------|------|
| `basic-demos` | 基础工具：Docker、K8s、Linux、Nginx、Git 等 |
| `rdb-demos` | 关系型数据库：MySQL、H2、MariaDB、SQLite |
| `nosql-demos` | NoSQL 数据库：MongoDB、Cassandra、SSDB、LevelDB |
| `cache-demos` | 缓存中间件：Redis、Hazelcast、Memcached |
| `mq-demos` | 消息队列：Kafka、RocketMQ、RabbitMQ、ActiveMQ、ZeroMQ |
| `bigdata-demos` | 大数据：HBase、Hive、Spark、Flink、ES、TiDB |
| `rpc-demos` | RPC 框架：Dubbo、gRPC、Thrift、Ice |
| `kv-demos` | KV 存储：ZooKeeper、etcd、LevelDB、RocksDB |
| `level0-demo` | 云原生组件：Nacos、Sentinel、SkyWalking、K8s |
| `tx-demos` | 分布式事务：Seata |
| `communication-demo` | 通信框架：Netty、Mina |
| `dag` | 🆕 DAG 图建模（Guava Graph + JGraphT，含状态机 + 边条件） |
| `artifact` | 工具组件：FastJSON、OkHttp、Mockito、Testable |
| `db-support-demos` | 数据库中间件：ShardingSphere |
| `xxl-job-demo` | 分布式调度：XXL-JOB |
| `docs/DAG` | DAG 技术选型与制品文档 |

---

## DAG 模块快速开始

```bash
# 构建
./mvnw clean compile -pl dag

# 运行测试（52 个用例）
./mvnw test -pl dag
```

详细文档见 [docs/DAG/](docs/DAG/)：
- [选型指南](docs/DAG/README.md) — Java DAG 组件技术选型
- [制品文档](docs/DAG/dag-artifact.md) — dag 模块 API 与测试
- [对比文档](docs/DAG/compare.md) — Guava Graph vs JGraphT

---

## 构建

本项目使用 Maven Wrapper，无需全局安装 Maven：

```bash
# Unix
./mvnw clean install

# Windows
mvnw.cmd clean install
```

---

## Docker Compose

```bash
sudo curl -L https://github.com/docker/compose/releases/download/1.21.2/docker-compose-$(uname -s)-$(uname -m) -o /usr/local/bin/docker-compose
sudo chmod +x /usr/local/bin/docker-compose
```

---

## License

[Apache License 2.0](LICENSE)
