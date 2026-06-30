# DAG 模块制品文档

## 文档信息

| 项 | 内容 |
|----|------|
| 文档名称 | DAG模块制品文档 |
| 文档版本 | V1.0 |
| 编写日期 | 2026-06-30 |
| 对应模块 | `dag` |
| 对应选型文档 | `README.md` — 章节 2.1「底层图算法库（仅建模，无执行逻辑）」 |

---

## 一、模块概述

`dag` 模块实现了 **底层图算法库** 选型方案，基于 **Guava Graph** 和 **JGraphT** 两个开源组件，提供 DAG（有向无环图）的数据建模与标准图算法能力。

### 设计定位

| 维度 | 说明 |
|------|------|
| 建模能力 | ✅ 定义节点、有向边、节点自定义属性/状态 |
| 执行能力 | ❌ 无线程池、无调度、无重试、无持久化 |
| 嵌入逻辑 | ❌ 不可直接嵌入业务代码，需自行封装调度器 |
| 适用场景 | 自研 DAG 调度引擎底层存储、规则引擎拓扑校验、流程图数据源 |

### 模块坐标

```xml
<groupId>org.cland</groupId>
<artifactId>dag</artifactId>
<version>0.0.1-SNAPSHOT</version>
```

---

## 二、依赖清单

| 依赖 | 版本 | 用途 |
|------|------|------|
| `com.google.guava:guava` | 33.2.1-jre | Guava Graph 轻量图结构 |
| `org.jgrapht:jgrapht-core` | 1.5.2 | JGraphT 全量图算法库 |
| `org.jgrapht:jgrapht-io` | 1.5.2 | DOT/GraphML 图格式导入导出 |
| `org.jgrapht:jgrapht-ext` | 1.5.2 | 图扩展（可视化等） |
| `org.apache.commons:commons-collections4` | 4.4 | 集合工具类 |
| `junit:junit` | 4.13.2 | 单元测试 |

## 三、架构设计

### 3.1 包结构

```
org.cland.dag.graph
├── GuavaDagExample.java     — 基于 Guava Graph 的 DAG 建模封装
└── JGraphTDagExample.java   — 基于 JGraphT 的 DAG 建模封装
```

### 3.2 核心模型

两种实现共享统一的 DAG 节点模型：

```
DagNode
├── id        : String    — 节点唯一标识
├── name      : String    — 节点名称
├── status    : NodeStatus — 节点状态枚举
│   ├── INIT              — 初始态
│   ├── RUNNING           — 运行中
│   ├── SUCCESS           — 成功
│   ├── FAIL              — 失败
│   └── CANCEL            — 取消
└── attributes: Map<String, Object> — 自定义属性扩展
```

JGraphT 额外支持 `DagEdge` 边模型：

```
DagEdge
├── id        : String    — 边唯一标识
└── attributes: Map<String, Object> — 边自定义属性（权重、超时等）
```

### 3.3 能力对比

| 能力项 | Guava Graph (GuavaDagExample) | JGraphT (JGraphTDagExample) |
|--------|-------------------------------|-----------------------------|
| 图结构 | `MutableGraph`（轻量） | `DefaultDirectedGraph`（全功能） |
| 边模型 | 无独立边对象，仅连接关系 | 独立 `DagEdge` 对象，支持权重/属性 |
| 环检测 | 自实现 DFS 递归算法 | 内置 `CycleDetector` |
| 拓扑排序 | 自实现 Kahn 算法 | 内置 `TopologicalOrderIterator` |
| 反向拓扑 | — | `Collections.reverse()` |
| 路径查询 | 自实现 DFS 全路径搜索 | 内置 `AllDirectedPaths` + `DijkstraShortestPath` |
| 源/叶子节点 | 自实现 | 自实现 |
| 图导出 | — | `DOTExporter` → DOT 格式 |
| 节点状态管理 | 自实现状态枚举 | 自实现状态枚举 |
| 自定义属性 | 节点属性 Map | 节点属性 Map + 边属性 Map |

---

## 四、API 文档

### 4.1 GuavaDagExample

| 方法 | 返回值 | 说明 |
|------|--------|------|
| `addNode(DagNode)` | `GuavaDagExample` | 添加节点（链式调用） |
| `addEdge(DagNode, DagNode)` | `GuavaDagExample` | 添加有向边（链式调用） |
| `getNodes()` | `Set<DagNode>` | 获取所有节点 |
| `getPredecessors(DagNode)` | `Set<DagNode>` | 获取某节点的前驱节点 |
| `getSuccessors(DagNode)` | `Set<DagNode>` | 获取某节点的后继节点 |
| `hasCycle()` | `boolean` | 检测是否有环 |
| `topologicalSort()` | `List<DagNode>` | 拓扑排序（Kahn 算法） |
| `getSourceNodes()` | `Set<DagNode>` | 获取源节点（入度为 0） |
| `getLeafNodes()` | `Set<DagNode>` | 获取叶子节点（出度为 0） |
| `findAllPaths(DagNode, DagNode)` | `List<List<DagNode>>` | 查找所有路径 |
| `printDag()` | `void` | 打印 DAG 结构 |

### 4.2 JGraphTDagExample

| 方法 | 返回值 | 说明 |
|------|--------|------|
| `addNode(DagNode)` | `JGraphTDagExample` | 添加节点（链式调用） |
| `addEdge(DagNode, DagNode)` | `JGraphTDagExample` | 添加有向边（自动创建边对象） |
| `addEdge(DagNode, DagNode, DagEdge)` | `JGraphTDagExample` | 添加有向边（指定边对象） |
| `getGraph()` | `Graph<DagNode, DagEdge>` | 获取 JGraphT 核心图对象 |
| `hasCycle()` | `boolean` | 检测是否有环 |
| `findCycleNodes()` | `Set<DagNode>` | 获取环中的节点 |
| `topologicalSort()` | `List<DagNode>` | 拓扑排序（顶层优先） |
| `topologicalSortReverse()` | `List<DagNode>` | 反向拓扑排序（底层优先） |
| `getSourceNodes()` | `Set<DagNode>` | 获取源节点（入度为 0） |
| `getLeafNodes()` | `Set<DagNode>` | 获取叶子节点（出度为 0） |
| `getPredecessors(DagNode)` | `Set<DagNode>` | 获取所有前驱节点 |
| `getSuccessors(DagNode)` | `Set<DagNode>` | 获取所有后继节点 |
| `findAllPaths(DagNode, DagNode)` | `List<GraphPath<...>>` | 查找所有路径 |
| `findShortestPath(DagNode, DagNode)` | `GraphPath<...>` | 查找最短路径 |
| `exportToDot()` | `String` | 导出为 DOT 格式 |
| `printDag()` | `void` | 打印 DAG 结构 |

---

## 五、测试覆盖

### 5.1 GuavaGraphDagTest（11 个测试用例）

测试场景：**数据仓库 ETL 任务 DAG**

```
         SourceA  SourceB
            \     /
           Extract
           /     \
     TransformA TransformB
           \     /
            Load
             |
          Report
```

| # | 测试方法 | 测试内容 |
|---|---------|---------|
| 1 | `testDagStructure` | 验证节点数、前驱/后继关系、叶子节点 |
| 2 | `testNoCycle` | 合法 DAG 无环 |
| 3 | `testCycleDetection` | 添加环后正确检测 |
| 4 | `testTopologicalSort` | 拓扑排序长度正确，依赖约束满足 |
| 5 | `testSourceAndLeafNodes` | 源节点（2个：SourceA/SourceB）、叶子节点（1个：Report） |
| 6 | `testFindAllPaths` | SourceA→Report 有 2 条路径，Extract→Report 有 2 条路径 |
| 7 | `testNodeStatus` | 状态流转：INIT→RUNNING→SUCCESS/FAIL/CANCEL |
| 8 | `testNodeCustomAttributes` | 自定义属性（timeout/retry/handler） |
| 9 | `testPrintDag` | 打印不抛异常 |
| 10 | `testTopologicalSortWithCycle` | 含环时拓扑排序抛 `IllegalStateException` |
| 11 | `testComplexDag` | 8 节点多层并行 DAG（A→B/C/D→E→F/G→H），验证全依赖约束 |

### 5.2 JGraphTDagTest（15 个测试用例）

测试场景：**微服务调用链 DAG**

```
         API Gateway
          /    |    \
      User  Order  Payment
        |    /    \    |
        |  Stock  Notification
        |    |       |
        +----+-------+
             |
         Data Sync
```

| # | 测试方法 | 测试内容 |
|---|---------|---------|
| 1 | `testDagStructure` | 7 节点、9 边 |
| 2 | `testNoCycle` | 合法 DAG 无环 |
| 3 | `testCycleDetection` | 添加环后正确检测，环中节点非空 |
| 4 | `testTopologicalSort` | 拓扑排序长度正确，所有依赖约束满足 |
| 5 | `testReverseTopologicalSort` | 反向拓扑排序，Data Sync 排首位 |
| 6 | `testSourceAndLeafNodes` | 1 个源节点（API Gateway）、1 个叶子节点（Data Sync） |
| 7 | `testPredecessorsAndSuccessors` | API Gateway 无前驱、有 3 个后继；Data Sync 有 3 个前驱 |
| 8 | `testFindAllPaths` | API Gateway→Data Sync 有 4 条路径 |
| 9 | `testShortestPath` | 最短路径：Gateway→User→Data Sync（2 条边） |
| 10 | `testDotExport` | DOT 导出包含 `digraph`、节点 ID |
| 11 | `testPrintDag` | 打印不抛异常 |
| 12 | `testTopologicalSortWithCycle` | 含环时拓扑排序抛 `IllegalStateException` |
| 13 | `testNodeCustomAttributesOnEdge` | 边自定义属性（weight/timeout/async） |
| 14 | `testSingleNodeDag` | 单节点 DAG：无环、拓扑序 1 个、源=叶子=自身 |
| 15 | `testLinearDag` | 线性 DAG 3 节点：拓扑序 Step1→Step2→Step3 |

### 5.3 测试结果

**26 / 26 测试通过** ✅

```
[INFO] Tests run: 11, Failures: 0, Errors: 0, Skipped: 0 — GuavaGraphDagTest
[INFO] Tests run: 15, Failures: 0, Errors: 0, Skipped: 0 — JGraphTDagTest
[INFO] BUILD SUCCESS
```

---

## 六、使用示例

### 6.1 Guava Graph 构建 DAG

```java
GuavaDagExample dag = new GuavaDagExample();

GuavaDagExample.DagNode a = new GuavaDagExample.DagNode("A", "数据源A");
GuavaDagExample.DagNode b = new GuavaDagExample.DagNode("B", "数据源B");
GuavaDagExample.DagNode c = new GuavaDagExample.DagNode("C", "合并处理");

dag.addNode(a).addNode(b).addNode(c);
dag.addEdge(a, c);
dag.addEdge(b, c);

// 环检测
boolean hasCycle = dag.hasCycle();                    // false

// 拓扑排序
List<GuavaDagExample.DagNode> sorted = dag.topologicalSort();  // [A, B, C] 或 [B, A, C]

// 查找路径
List<List<GuavaDagExample.DagNode>> paths = dag.findAllPaths(a, c); // 1 条路径

// 节点状态
a.setStatus(GuavaDagExample.NodeStatus.RUNNING);
a.setStatus(GuavaDagExample.NodeStatus.SUCCESS);
```

### 6.2 JGraphT 构建 DAG

```java
JGraphTDagExample dag = new JGraphTDagExample();

JGraphTDagExample.DagNode gateway = new JGraphTDagExample.DagNode("gw", "Gateway");
JGraphTDagExample.DagNode userSvc = new JGraphTDagExample.DagNode("user", "UserService");
JGraphTDagExample.DagNode orderSvc = new JGraphTDagExample.DagNode("order", "OrderService");

dag.addNode(gateway).addNode(userSvc).addNode(orderSvc);
dag.addEdge(gateway, userSvc);
dag.addEdge(gateway, orderSvc);

// 图算法
boolean hasCycle = dag.hasCycle();
List<JGraphTDagExample.DagNode> sorted = dag.topologicalSort();
JGraphTDagExample.DagEdge edge = new JGraphTDagExample.DagEdge("gw->user");
edge.putAttribute("weight", 10);

// DOT 导出
String dot = dag.exportToDot();
```

---

## 七、与选型文档的对应关系

| 选型文档章节 | 文档位置 | 对应实现 |
|-------------|---------|---------|
| 2.1 底层图算法库 | `README.md` | GuavaDagExample、JGraphTDagExample |
| 术语定义 — DAG建模能力 | `README.md` | 节点/边/状态定义、拓扑排序、环检测 |
| 场景1：仅需绘制/存储DAG拓扑 | `README.md` §四 | 推荐 JGraphT / Guava Graph |
| 场景2：单机短时并行任务 | `README.md` §四 | ❌ 需选择 EasyDAG 等执行框架 |

---

## 八、开发注意事项

1. **Guava Graph 无独立边对象**：边仅表示节点间的连接关系，无法附加权重/属性。如需边属性，使用 JGraphT。
2. **Guava Graph 不自带图算法**：环检测、拓扑排序、路径搜索均需自行实现（本模块已封装）。
3. **JGraphT 图算法完整**：内置 `CycleDetector`、`TopologicalOrderIterator`、`AllDirectedPaths`、`DijkstraShortestPath` 等。
4. **不可直接执行任务**：本模块仅提供 DAG 建模与算法，无执行引擎。需自研调度器：拓扑排序 → 线程池执行 → 手动更新节点状态。
5. **DOT 导出依赖 jgrapht-io**：`DOTExporter` 位于 `org.jgrapht.nio.dot` 包。
