package org.cland.dag.graph;

import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.cycle.CycleDetector;
import org.jgrapht.alg.shortestpath.AllDirectedPaths;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.graph.*;
import org.jgrapht.nio.dot.DOTExporter;
import org.jgrapht.traverse.TopologicalOrderIterator;

import java.io.StringWriter;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * JGraphT DAG 建模示例（含状态机 + 边条件）
 * <p>
 * JGraphT 提供全功能图算法库，支持：
 * - DAG 拓扑数据结构：节点、有向边（带权重/条件）、节点自定义属性
 * - 节点状态机：强制合法的状态流转，禁止非法跳转
 * - 边条件：每条边可附加条件表达式，只有满足条件时才激活下游
 * - 内置环检测、拓扑排序、最短路径、子图等丰富算法
 * - 支持 DOT/GraphML 格式导入导出
 * - 无内置执行器，需自行封装调度逻辑
 * <p>
 * 参考文档：https://jgrapht.org/
 */
public class JGraphTDagExample {

    // ============================================================
    //  一、节点状态机（State Machine）
    // ============================================================

    /**
     * DAG 节点状态枚举
     */
    public enum NodeStatus {
        INIT,
        RUNNING,
        SUCCESS,
        FAIL,
        CANCEL;

        private static final Map<NodeStatus, Set<NodeStatus>> TRANSITIONS = new EnumMap<>(NodeStatus.class);

        static {
            TRANSITIONS.put(INIT,    EnumSet.of(RUNNING, CANCEL));
            TRANSITIONS.put(RUNNING, EnumSet.of(SUCCESS, FAIL, CANCEL));
            TRANSITIONS.put(SUCCESS, EnumSet.of(RUNNING, CANCEL));
            TRANSITIONS.put(FAIL,    EnumSet.of(RUNNING, CANCEL));
            TRANSITIONS.put(CANCEL,  EnumSet.of(RUNNING));
        }

        public boolean canTransitionTo(NodeStatus target) {
            Set<NodeStatus> allowed = TRANSITIONS.get(this);
            return allowed != null && allowed.contains(target);
        }

        public Set<NodeStatus> allowedTransitions() {
            return TRANSITIONS.getOrDefault(this, Collections.emptySet());
        }

        public boolean isTerminal() {
            return this == SUCCESS || this == FAIL || this == CANCEL;
        }

        public boolean isRetryable() {
            return this == FAIL;
        }
    }

    // ============================================================
    //  二、边条件（Edge Condition）
    // ============================================================

    @FunctionalInterface
    public interface EdgeCondition {
        boolean evaluate(DagNode predecessor, Map<String, Object> context);
    }

    public static final EdgeCondition ALWAYS_TRUE = (predecessor, ctx) -> true;

    public static EdgeCondition whenSuccess() {
        return (predecessor, ctx) -> predecessor.getStatus() == NodeStatus.SUCCESS;
    }

    public static EdgeCondition whenFail() {
        return (predecessor, ctx) -> predecessor.getStatus() == NodeStatus.FAIL;
    }

    public static EdgeCondition whenAttribute(String key, Predicate<Object> predicate) {
        return (predecessor, ctx) -> {
            Object value = predecessor.getAttribute(key);
            return value != null && predicate.test(value);
        };
    }

    // ============================================================
    //  三、DAG 节点定义
    // ============================================================

    /**
     * DAG 节点定义（作为 JGraphT 的 vertex，含状态机）
     */
    public static class DagNode {
        private final String id;
        private final String name;
        private NodeStatus status;
        private final Map<String, Object> attributes;

        public DagNode(String id, String name) {
            this.id = id;
            this.name = name;
            this.status = NodeStatus.INIT;
            this.attributes = new HashMap<>();
        }

        public String getId() { return id; }
        public String getName() { return name; }
        public NodeStatus getStatus() { return status; }

        /**
         * 尝试状态转移。如果转移非法，抛出 IllegalStateException。
         */
        public DagNode tryTransition(NodeStatus target) {
            if (!status.canTransitionTo(target)) {
                throw new IllegalStateException(
                        String.format("Illegal state transition: %s -> %s (node=%s). Allowed: %s",
                                status, target, name, status.allowedTransitions()));
            }
            this.status = target;
            return this;
        }

        public boolean canTransition(NodeStatus target) {
            return status.canTransitionTo(target);
        }

        /**
         * 重置为 INIT 状态
         */
        public DagNode reset() {
            if (status == NodeStatus.FAIL || status == NodeStatus.CANCEL || status == NodeStatus.SUCCESS) {
                this.status = NodeStatus.INIT;
                return this;
            }
            throw new IllegalStateException(
                    "Cannot reset node " + name + " from state " + status);
        }

        public DagNode putAttribute(String key, Object value) {
            attributes.put(key, value);
            return this;
        }

        @SuppressWarnings("unchecked")
        public <T> T getAttribute(String key) {
            return (T) attributes.get(key);
        }

        public Map<String, Object> getAttributes() {
            return Collections.unmodifiableMap(attributes);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof DagNode)) return false;
            DagNode node = (DagNode) o;
            return id.equals(node.id);
        }

        @Override
        public int hashCode() { return Objects.hash(id); }

        @Override
        public String toString() {
            return name;
        }
    }

    // ============================================================
    //  四、边定义（带条件）
    // ============================================================

    /**
     * 有向边定义（作为 JGraphT 的 edge，含条件 + 属性）
     */
    public static class DagEdge {
        private final String id;
        private final EdgeCondition condition;
        private final Map<String, Object> attributes;

        public DagEdge(String id) {
            this(id, ALWAYS_TRUE);
        }

        public DagEdge(String id, EdgeCondition condition) {
            this.id = id;
            this.condition = condition;
            this.attributes = new HashMap<>();
        }

        public String getId() { return id; }
        public EdgeCondition getCondition() { return condition; }

        /**
         * 在上游节点和上下文中判断此边是否满足条件
         */
        public boolean evaluate(DagNode predecessor, Map<String, Object> context) {
            return condition.evaluate(predecessor, context);
        }

        public DagEdge putAttribute(String key, Object value) {
            attributes.put(key, value);
            return this;
        }

        @SuppressWarnings("unchecked")
        public <T> T getAttribute(String key) {
            return (T) attributes.get(key);
        }

        @Override
        public String toString() {
            return id;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof DagEdge)) return false;
            DagEdge dagEdge = (DagEdge) o;
            return id.equals(dagEdge.id);
        }

        @Override
        public int hashCode() {
            return Objects.hash(id);
        }
    }

    // ============================================================
    //  五、DAG 图定义
    // ============================================================

    private final Graph<DagNode, DagEdge> graph;

    public JGraphTDagExample() {
        this.graph = new DefaultDirectedGraph<>(DagEdge.class);
    }

    /**
     * 添加节点
     */
    public JGraphTDagExample addNode(DagNode node) {
        graph.addVertex(node);
        return this;
    }

    /**
     * 添加无条件边（ALWAYS_TRUE）
     */
    public JGraphTDagExample addEdge(DagNode predecessor, DagNode successor) {
        DagEdge edge = new DagEdge(predecessor.getName() + "->" + successor.getName());
        graph.addEdge(predecessor, successor, edge);
        return this;
    }

    /**
     * 添加条件边
     */
    public JGraphTDagExample addEdge(DagNode predecessor, DagNode successor, EdgeCondition condition) {
        DagEdge edge = new DagEdge(predecessor.getName() + "->" + successor.getName(), condition);
        graph.addEdge(predecessor, successor, edge);
        return this;
    }

    /**
     * 添加自定义边对象
     */
    public JGraphTDagExample addEdge(DagNode predecessor, DagNode successor, DagEdge edge) {
        graph.addEdge(predecessor, successor, edge);
        return this;
    }

    /**
     * 获取 JGraphT 核心图对象
     */
    public Graph<DagNode, DagEdge> getGraph() {
        return graph;
    }

    /**
     * 获取某节点的所有出边
     */
    public Set<DagEdge> getOutEdges(DagNode node) {
        return graph.outgoingEdgesOf(node);
    }

    /**
     * 获取某节点的所有入边
     */
    public Set<DagEdge> getInEdges(DagNode node) {
        return graph.incomingEdgesOf(node);
    }

    /**
     * 获取某节点在给定上下文中满足条件的出边目标节点
     */
    public List<DagNode> getActivatedSuccessors(DagNode node, Map<String, Object> context) {
        List<DagNode> activated = new ArrayList<>();
        for (DagEdge edge : graph.outgoingEdgesOf(node)) {
            if (edge.evaluate(node, context)) {
                activated.add(graph.getEdgeTarget(edge));
            }
        }
        return activated;
    }

    // ============================================================
    //  六、图算法
    // ============================================================

    /**
     * 检测是否有环
     */
    public boolean hasCycle() {
        CycleDetector<DagNode, DagEdge> detector = new CycleDetector<>(graph);
        return detector.detectCycles();
    }

    /**
     * 获取环中的节点
     */
    public Set<DagNode> findCycleNodes() {
        CycleDetector<DagNode, DagEdge> detector = new CycleDetector<>(graph);
        return detector.findCycles();
    }

    /**
     * 拓扑排序（顶层依赖优先）
     */
    public List<DagNode> topologicalSort() {
        if (hasCycle()) {
            throw new IllegalStateException("Graph has a cycle, cannot perform topological sort");
        }
        List<DagNode> sorted = new ArrayList<>();
        TopologicalOrderIterator<DagNode, DagEdge> iterator =
                new TopologicalOrderIterator<>(graph);
        iterator.forEachRemaining(sorted::add);
        return sorted;
    }

    /**
     * 反向拓扑排序（底层依赖优先）
     */
    public List<DagNode> topologicalSortReverse() {
        List<DagNode> sorted = topologicalSort();
        Collections.reverse(sorted);
        return sorted;
    }

    /**
     * 获取源节点（入度为 0）
     */
    public Set<DagNode> getSourceNodes() {
        Set<DagNode> sources = new HashSet<>();
        for (DagNode node : graph.vertexSet()) {
            if (graph.incomingEdgesOf(node).isEmpty()) sources.add(node);
        }
        return sources;
    }

    /**
     * 获取叶子节点（出度为 0）
     */
    public Set<DagNode> getLeafNodes() {
        Set<DagNode> leaves = new HashSet<>();
        for (DagNode node : graph.vertexSet()) {
            if (graph.outgoingEdgesOf(node).isEmpty()) leaves.add(node);
        }
        return leaves;
    }

    /**
     * 获取某节点的所有前驱
     */
    public Set<DagNode> getPredecessors(DagNode node) {
        Set<DagNode> predecessors = new HashSet<>();
        for (DagEdge edge : graph.incomingEdgesOf(node)) {
            predecessors.add(graph.getEdgeSource(edge));
        }
        return predecessors;
    }

    /**
     * 获取某节点的所有后继
     */
    public Set<DagNode> getSuccessors(DagNode node) {
        Set<DagNode> successors = new HashSet<>();
        for (DagEdge edge : graph.outgoingEdgesOf(node)) {
            successors.add(graph.getEdgeTarget(edge));
        }
        return successors;
    }

    /**
     * 查找两个节点之间的所有路径
     */
    public List<GraphPath<DagNode, DagEdge>> findAllPaths(DagNode source, DagNode target) {
        AllDirectedPaths<DagNode, DagEdge> allPaths = new AllDirectedPaths<>(graph);
        return allPaths.getAllPaths(source, target, true, null);
    }

    /**
     * 查找两个节点间的最短路径（基于边数）
     */
    public GraphPath<DagNode, DagEdge> findShortestPath(DagNode source, DagNode target) {
        DijkstraShortestPath<DagNode, DagEdge> dijkstra = new DijkstraShortestPath<>(graph);
        return dijkstra.getPath(source, target);
    }

    /**
     * 导出为 DOT 格式
     */
    public String exportToDot() {
        StringWriter writer = new StringWriter();
        DOTExporter<DagNode, DagEdge> exporter = new DOTExporter<>();
        exporter.setVertexIdProvider(
                v -> v.getId().replaceAll("[^a-zA-Z0-9]", "_")
        );
        exporter.exportGraph(graph, writer);
        return writer.toString();
    }

    /**
     * 打印 DAG 结构
     */
    public void printDag() {
        System.out.println("=== JGraphT DAG (with state machine & edge conditions) ===");
        System.out.println("Nodes: " + graph.vertexSet().size());
        System.out.println("Edges: " + graph.edgeSet().size());
        for (DagEdge edge : graph.edgeSet()) {
            DagNode source = graph.getEdgeSource(edge);
            DagNode target = graph.getEdgeTarget(edge);
            String cond = edge.getCondition() != ALWAYS_TRUE ? " [conditioned]" : "";
            System.out.println("  " + source.getName() + " -> " + target.getName()
                    + " [" + edge.getId() + "]" + cond);
        }
        System.out.println("Has cycle: " + hasCycle());
        if (!hasCycle()) {
            System.out.println("Topological order: " + topologicalSort().stream()
                    .map(DagNode::getName).collect(Collectors.joining(" -> ")));
        }
        System.out.println("Node state transition rules:");
        for (NodeStatus s : NodeStatus.values()) {
            System.out.println("  " + s + " -> " + s.allowedTransitions());
        }
    }
}
