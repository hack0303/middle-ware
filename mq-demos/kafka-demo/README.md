# 概念
实例 broker 分区 副本因子
# 性能
单节点10W以上的吞吐量
# 不足
- 不支持事务
- 据说topic 在达到50~100时，性能会有下降

# kafka 源码
主要分包
clients,connect,core,streams

# detail
topic pattern is [a-z0-9A-Z.-_]{1,249}

#### REF
[Byzantine fault](https://en.wikipedia.org/wiki/Byzantine_fault#cite_note-9)
[分布式系统入门 | 拜占庭将军问题](https://www.cnblogs.com/alan-yin/p/14901121.html)
[kafka文档](https://kafka.apache.org/documentation/#majordesignelements)