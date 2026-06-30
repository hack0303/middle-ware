# Guava Graph vs JGraphT 对比

## 对比总览

| 对比维度 | Guava Graph | JGraphT |
|----------|:-:|:-:|
| 包名 | `com.google.common.graph` | `org.jgrapht` |
| 定位 | Guava 内置轻量图结构 | 专业全功能图算法库 |
| 核心接口 | `MutableGraph<N>` / `ValueGraph<N,V>` | `Graph<V,E>` / `DirectedGraph<V,E>` |
| 边模型 | ❌ 无独立边对象 | ✅ 边是 first-class 对象 |
| 内置图算法 | ❌ 仅提供图结构（无算法） | ✅ 30+ 算法（环检测/拓扑排序/最短路径/最大流/聚类系数...） |
| 序列化/IO | ❌ 不支持 | ✅ DOT / GraphML / CSV / GML / JSON |
| 持久化友好度 | ⭐ | ⭐⭐⭐⭐⭐ |
| 内存计算轻量度 | ⭐⭐⭐⭐⭐ | ⭐⭐⭐ |
| 学习成本 | ⭐（只需 Guava 基础） | ⭐⭐⭐（需了解图论概念） |
| 包体积 | ~600KB (Guava 全量) | ~2.5MB (jgrapht-core + io + ext) |
| 成熟度 | Google 维护，稳定 | 20+ 年开源，学术+工业广泛使用 |

---

## 一、边模型对比

这是两者最本质的差异。

### Guava Graph：边是隐式的

```java
MutableGraph<String> graph = GraphBuilder.directed().build();
graph.addNode("A");
graph.addNode("B");
graph.putEdge("A", "B");          // 边没有对象，只是一个关系

// 只能遍历关系，无法拿到"边本身"
Set<String> successors = graph.successors("A");  // → ["B"]
// 没有 edge.getSource(), edge.getTarget(), edge.getId()
```

**持久化时** — 需自建映射表：

```java
// 必须额外维护一个 Map 来存边属性
Map<Pair<String,String>, EdgeInfo> edgeProps = new HashMap<>();
graph.putEdge("A", "B");
edgeProps.put(Pair.of("A","B"), new EdgeInfo("edge1", "whenSuccess", 10));

// 恢复时需同步维护：
// 如果 graph.removeEdge("A", "B") 忘记删 edgeProps → 不一致！
```

### JGraphT：边是显式的

```java
// 边是独立对象
Graph<MyNode, MyEdge> graph = new DefaultDirectedGraph<>(MyEdge.class);
MyEdge edge = new MyEdge("AB-edge", "whenSuccess", 10);
graph.addEdge(nodeA, nodeB, edge);

// 可直接操作边
MyEdge e = graph.getEdge(nodeA, nodeB);
String id = e.getId();           // "AB-edge"
MyNode src = graph.getEdgeSource(e);  // → nodeA
MyNode tgt = graph.getEdgeTarget(e);  // → nodeB
```

**持久化时** — 天然映射：

```java
// 恢复完整一致，不会有"图恢复了但条件丢了"的问题
graph.edgeSet().forEach(e -> {
    insertIntoDb(
        e.getId(),
        graph.getEdgeSource(e).getId(),
        graph.getEdgeTarget(e).getId(),
        e.getAttribute("condition")
    );
});
```

---

## 二、图算法能力对比

| 算法 | Guava Graph | JGraphT |
|------|:-:|:-:|
| 环检测 | ❌ 自实现 | ✅ `CycleDetector` |
| 拓扑排序 | ❌ 自实现 | ✅ `TopologicalOrderIterator` |
| 最短路径 | ❌ 自实现 | ✅ `DijkstraShortestPath`, `BellmanFord`, `FloydWarshall`, `BFS`, `A*` |
| 所有路径 | ❌ 自实现 | ✅ `AllDirectedPaths` |
| 最小生成树 | ❌ | ✅ `Prim`, `Kruskal` |
| 强连通分量 | ❌ | ✅ `Kosaraju`, `Tarjan` |
| 最大流 | ❌ | ✅ `EdmondsKarp`, `Dinic` |
| 聚类系数 | ❌ | ✅ `ClusteringCoefficient` |
| 中心度 | ❌ | ✅ `BetweennessCentrality`, `PageRank` |
| 二分图 | ❌ | ✅ `BipartiteGraph` |
| 图同构 | ❌ | ✅ `VF2GraphIsomorphism` |

Guava Graph 定位就是**数据结构**，不是算法库。所有算法都得手写。

JGraphT 定位就是**算法库**，30+ 内置算法直接调用。

---

## 三、序列化 / IO 对比

### Guava Graph

```java
MutableGraph<String> graph = GraphBuilder.directed().build();
// ... 没有 toJson(), 没有 toDot(), 没有 toGraphML()
// 只能手写遍历：
StringBuilder sb = new StringBuilder("digraph G {\n");
for (String node : graph.nodes()) {
    for (String succ : graph.successors(node)) {
        sb.append("  ").append(node).append(" -> ").append(succ).append(";\n");
    }
}
sb.append("}");
```

### JGraphT

```java
// DOT 导出（可视化）
DOTExporter<MyNode, MyEdge> dot = new DOTExporter<>();
dot.exportGraph(graph, writer);
// → digraph G { "A" -> "B"; "B" -> "C"; }

// GraphML 导出（可持久化 XML）
GraphMLExporter<MyNode, MyEdge> graphml = new GraphMLExporter<>();
graphml.exportGraph(graph, writer);
// → <?xml ...><graphml><graph edgedefault="directed">...

// CSV 导出（导入数据库）
CSVExporter<MyNode, MyEdge> csv = new CSVExporter<>();
csv.exportGraph(graph, writer);
// → A, B; B, C

// 从文件恢复
DOTImporter<MyNode, MyEdge> dotIn = new DOTImporter<>();
dotIn.importGraph(graph, reader);
```

---

## 四、持久化方案推荐

### 场景 A：小数据量 + 文件备份（推荐 JGraphT + GraphML）

```java
// 保存
GraphMLExporter<DagNode, DagEdge> exporter = new GraphMLExporter<>();
exporter.exportGraph(graph, new FileWriter("dag.graphml"));

// 恢复
GraphMLImporter<DagNode, DagEdge> importer = new GraphMLImporter<>();
importer.importGraph(graph, new FileReader("dag.graphml"));
```

### 场景 B：中等数据量 + 关系型数据库（推荐 JGraphT）

```sql
CREATE TABLE dag_node (
    id   VARCHAR(64) PRIMARY KEY,
    name VARCHAR(128),
    status VARCHAR(16) DEFAULT 'INIT',
    attributes JSON
);

CREATE TABLE dag_edge (
    id         VARCHAR(64) PRIMARY KEY,
    source_id  VARCHAR(64) NOT NULL REFERENCES dag_node(id),
    target_id  VARCHAR(64) NOT NULL REFERENCES dag_node(id),
    condition  VARCHAR(256) DEFAULT 'ALWAYS_TRUE',
    attributes JSON
);
```

```java
// 保存到 DB
for (DagNode node : graph.vertexSet()) {
    jdbcTemplate.update("INSERT INTO dag_node ...", node.getId(), node.getName(), ...);
}
for (DagEdge edge : graph.edgeSet()) {
    jdbcTemplate.update("INSERT INTO dag_edge ...", edge.getId(),
        graph.getEdgeSource(edge).getId(),
        graph.getEdgeTarget(edge).getId(),
        ...);
}

// 从 DB 恢复
dag.query("SELECT * FROM dag_node", (rs) -> {
    DagNode node = new DagNode(rs.getString("id"), rs.getString("name"));
    dag.addNode(node);
});
dag.query("SELECT * FROM dag_edge", (rs) -> {
    DagNode src = nodeMap.get(rs.getString("source_id"));
    DagNode tgt = nodeMap.get(rs.getString("target_id"));
    DagEdge edge = new DagEdge(rs.getString("id"), parseCondition(rs.getString("condition")));
    dag.addEdge(src, tgt, edge);
});
```

### 场景 C：大数据量 + 图数据库（Neo4j / JanusGraph）

两种库都适合——JGraphT 作为**内存建模层**，Neo4j 作为**持久化层**：

```
JGraphT (内存图)  ←→  Neo4j (持久化图)
    │                       │
    │ 拓扑排序/环检测         │ 存储/查询
    │ Condition 评估         │ 子图匹配
    │ 路径搜索               │ 图遍历
```

---

## 五、选型建议

### 选 Guava Graph 🙋 当：

```
□ 只需要图结构，不需要算法
□ 图的生命周期仅限一次请求（用完即弃）
□ 不需要持久化
□ 不想引入额外依赖（已在用 Guava）
□ 图结构简单（几十个节点）
```

### 选 JGraphT 🙋 当：

```
✅ 需要持久化到文件或数据库
✅ 需要图算法（环检测/拓扑排序/最短路径等）
✅ 边需要附加属性/条件
✅ 需要导入导出（DOT 可视化 / GraphML 持久化）
✅ 图结构复杂（上千节点）
✅ 需要从文件恢复 DAG 定义
```

### 一句话总结

| | Guava Graph | JGraphT |
|--|:-:|:-:|
| 适合**内存计算** | ⭐⭐⭐⭐⭐ | ⭐⭐⭐ |
| 适合**持久化** | ⭐ | ⭐⭐⭐⭐⭐ |

**需要持久化 → 用 JGraphT。** 它的边对象、IO 格式、数据库映射能力都是 Guava Graph 不具备的。
