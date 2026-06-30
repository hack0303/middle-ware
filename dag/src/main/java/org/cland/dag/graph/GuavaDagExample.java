package org.cland.dag.graph;

import com.google.common.graph.*;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Guava Graph DAG 建模示例（含状态机 + 边条件）
 * <p>
 * Guava Graph 提供轻量级图数据结构，支持：
 * - DAG 拓扑建模：节点（N）、有向边、自定义节点属性
 * - 节点状态机：强制合法的状态流转，禁止非法跳转
 * - 边条件：每条边可附加条件表达式，只有满足条件时才激活下游
 * - 可达性查询、拓扑排序、环检测
 * - 无内置执行器，需自行封装调度逻辑
 * <p>
 * 参考文档：https://github.com/google/guava/wiki/GraphsExplained
 */
public class GuavaDagExample {

    // ============================================================
    //  一、节点状态机（State Machine）
    // ============================================================

    /**
     * DAG 节点状态枚举
     */
    public enum NodeStatus {
        INIT,       // 初始态
        RUNNING,    // 运行中
        SUCCESS,    // 成功
        FAIL,       // 失败
        CANCEL;     // 取消

        /**
         * 合法状态转移表：current → {allowed next states}
         */
        private static final Map<NodeStatus, Set<NodeStatus>> TRANSITIONS = new EnumMap<>(NodeStatus.class);

        static {
            TRANSITIONS.put(INIT,      EnumSet.of(RUNNING, CANCEL));
            TRANSITIONS.put(RUNNING,   EnumSet.of(SUCCESS, FAIL, CANCEL));
            TRANSITIONS.put(SUCCESS,   EnumSet.of(RUNNING, CANCEL));       // 允许重跑
            TRANSITIONS.put(FAIL,      EnumSet.of(RUNNING, CANCEL));       // 允许重试
            TRANSITIONS.put(CANCEL,    EnumSet.of(RUNNING));               // 允许重新启动
        }

        /**
         * 判断从当前状态转移到目标状态是否合法
         */
        public boolean canTransitionTo(NodeStatus target) {
            Set<NodeStatus> allowed = TRANSITIONS.get(this);
            return allowed != null && allowed.contains(target);
        }

        /**
         * 获取所有允许的后续状态
         */
        public Set<NodeStatus> allowedTransitions() {
            return TRANSITIONS.getOrDefault(this, Collections.emptySet());
        }

        /**
         * 是否为终态（不会再自动变化）
         */
        public boolean isTerminal() {
            return this == SUCCESS || this == FAIL || this == CANCEL;
        }

        /**
         * 是否可重试
         */
        public boolean isRetryable() {
            return this == FAIL;
        }
    }

    // ============================================================
    //  二、边条件（Edge Condition）
    // ============================================================

    /**
     * 边条件判断接口，用于决定下游节点是否应该执行
     * <p>
     * 接收上游节点信息和执行上下文，返回 boolean 决定是否激活此边。
     * 典型场景：根据上游结果码、阈值判断走哪条分支。
     */
    @FunctionalInterface
    public interface EdgeCondition {
        /**
         * 判断是否满足条件
         * @param predecessor 上游节点
         * @param context     全局执行上下文（可读写）
         * @return true=激活此边，下游可执行；false=跳过此边
         */
        boolean evaluate(DagNode predecessor, Map<String, Object> context);
    }

    /**
     * 内置的默认条件——永远为 true（无条件边）
     */
    public static final EdgeCondition ALWAYS_TRUE = (predecessor, ctx) -> true;

    /**
     * 根据上游节点状态判断的条件
     */
    public static EdgeCondition whenSuccess() {
        return (predecessor, ctx) -> predecessor.getStatus() == NodeStatus.SUCCESS;
    }

    public static EdgeCondition whenFail() {
        return (predecessor, ctx) -> predecessor.getStatus() == NodeStatus.FAIL;
    }

    /**
     * 根据上游节点某属性值判断
     */
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
     * DAG 节点定义（含状态机）
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
         * @return this（链式调用）
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

        /**
         * 判断是否可以转移到目标状态
         */
        public boolean canTransition(NodeStatus target) {
            return status.canTransitionTo(target);
        }

        /**
         * 重置为 INIT 状态（仅 FAIL / CANCEL 可重置）
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
        public int hashCode() {
            return Objects.hash(id);
        }

        @Override
        public String toString() {
            return "DagNode{id='" + id + "', name='" + name + "', status=" + status + '}';
        }
    }

    // ============================================================
    //  四、边定义（带条件）
    // ============================================================

    /**
     * DAG 有向边定义（条件边）
     */
    public static class DagEdge {
        private final DagNode source;
        private final DagNode target;
        private final String id;
        private final EdgeCondition condition;
        private final Map<String, Object> attributes;

        public DagEdge(DagNode source, DagNode target, String id, EdgeCondition condition) {
            this.source = source;
            this.target = target;
            this.id = id;
            this.condition = condition;
            this.attributes = new HashMap<>();
        }

        public DagNode getSource() { return source; }
        public DagNode getTarget() { return target; }
        public String getId() { return id; }
        public EdgeCondition getCondition() { return condition; }

        /**
         * 判断此边在上游节点和上下文中是否满足条件
         */
        public boolean evaluate(Map<String, Object> context) {
            return condition.evaluate(source, context);
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
            return source.getName() + " --[" + id + "]--> " + target.getName();
        }
    }

    // ============================================================
    //  五、DAG 图定义
    // ============================================================

    // Guava Graph 原生图：只存节点连接关系，不含边对象
    private final MutableGraph<DagNode> graph;
    // 边条件映射：source -> target -> edge detail
    private final Map<DagNode, Map<DagNode, DagEdge>> edgeMap;

    public GuavaDagExample() {
        this.graph = GraphBuilder.directed()
                .allowsSelfLoops(false)
                .build();
        this.edgeMap = new HashMap<>();
    }

    /**
     * 添加节点
     */
    public GuavaDagExample addNode(DagNode node) {
        graph.addNode(node);
        return this;
    }

    /**
     * 添加无条件边（ALWAYS_TRUE）
     */
    public GuavaDagExample addEdge(DagNode predecessor, DagNode successor) {
        DagEdge edge = new DagEdge(predecessor, successor,
                predecessor.getName() + "->" + successor.getName(), ALWAYS_TRUE);
        return addEdgeRaw(predecessor, successor, edge);
    }

    /**
     * 添加条件边
     */
    public GuavaDagExample addEdge(DagNode predecessor, DagNode successor, EdgeCondition condition) {
        DagEdge edge = new DagEdge(predecessor, successor,
                predecessor.getName() + "->" + successor.getName(), condition);
        return addEdgeRaw(predecessor, successor, edge);
    }

    /**
     * 添加自定义边对象
     */
    public GuavaDagExample addEdge(DagNode predecessor, DagNode successor, DagEdge edge) {
        return addEdgeRaw(predecessor, successor, edge);
    }

    private GuavaDagExample addEdgeRaw(DagNode source, DagNode target, DagEdge edge) {
        graph.putEdge(source, target);
        edgeMap.computeIfAbsent(source, k -> new HashMap<>()).put(target, edge);
        return this;
    }

    /**
     * 获取两个节点间的边对象
     */
    public Optional<DagEdge> getEdge(DagNode source, DagNode target) {
        Map<DagNode, DagEdge> successors = edgeMap.get(source);
        if (successors == null) return Optional.empty();
        return Optional.ofNullable(successors.get(target));
    }

    /**
     * 获取某节点的所有出边（含条件）
     */
    public List<DagEdge> getOutEdges(DagNode node) {
        Map<DagNode, DagEdge> successors = edgeMap.getOrDefault(node, Collections.emptyMap());
        return new ArrayList<>(successors.values());
    }

    /**
     * 获取某节点的所有入边（含条件）
     */
    public List<DagEdge> getInEdges(DagNode node) {
        List<DagEdge> result = new ArrayList<>();
        for (DagNode pred : graph.predecessors(node)) {
            Map<DagNode, DagEdge> successors = edgeMap.get(pred);
            if (successors != null) {
                DagEdge edge = successors.get(node);
                if (edge != null) result.add(edge);
            }
        }
        return result;
    }

    // ============================================================
    //  六、图算法
    // ============================================================

    /**
     * 获取所有节点
     */
    public Set<DagNode> getNodes() {
        return graph.nodes();
    }

    /**
     * 获取某节点的前驱节点
     */
    public Set<DagNode> getPredecessors(DagNode node) {
        return graph.predecessors(node);
    }

    /**
     * 获取某节点的后继节点
     */
    public Set<DagNode> getSuccessors(DagNode node) {
        return graph.successors(node);
    }

    /**
     * 检测是否有环（是否是合法 DAG）
     */
    public boolean hasCycle() {
        Set<DagNode> visited = new HashSet<>();
        Set<DagNode> recursionStack = new HashSet<>();
        for (DagNode node : graph.nodes()) {
            if (hasCycleDFS(node, visited, recursionStack)) {
                return true;
            }
        }
        return false;
    }

    private boolean hasCycleDFS(DagNode node, Set<DagNode> visited, Set<DagNode> recursionStack) {
        if (recursionStack.contains(node)) return true;
        if (visited.contains(node)) return false;

        visited.add(node);
        recursionStack.add(node);

        for (DagNode successor : graph.successors(node)) {
            if (hasCycleDFS(successor, visited, recursionStack)) {
                return true;
            }
        }

        recursionStack.remove(node);
        return false;
    }

    /**
     * 拓扑排序（Kahn 算法）
     */
    public List<DagNode> topologicalSort() {
        if (hasCycle()) {
            throw new IllegalStateException("Graph has a cycle");
        }
        Map<DagNode, Integer> inDegree = new HashMap<>();
        for (DagNode node : graph.nodes()) inDegree.put(node, 0);
        for (DagNode node : graph.nodes()) {
            for (DagNode successor : graph.successors(node)) {
                inDegree.merge(successor, 1, Integer::sum);
            }
        }
        Queue<DagNode> queue = new LinkedList<>();
        for (Map.Entry<DagNode, Integer> e : inDegree.entrySet()) {
            if (e.getValue() == 0) queue.add(e.getKey());
        }
        List<DagNode> sorted = new ArrayList<>();
        while (!queue.isEmpty()) {
            DagNode node = queue.poll();
            sorted.add(node);
            for (DagNode successor : graph.successors(node)) {
                inDegree.merge(successor, -1, Integer::sum);
                if (inDegree.get(successor) == 0) queue.add(successor);
            }
        }
        if (sorted.size() != graph.nodes().size())
            throw new IllegalStateException("Graph has a cycle");
        return sorted;
    }

    /**
     * 获取所有源节点（入度为 0）
     */
    public Set<DagNode> getSourceNodes() {
        Set<DagNode> result = new HashSet<>(graph.nodes());
        for (DagNode node : graph.nodes()) {
            if (!graph.predecessors(node).isEmpty()) result.remove(node);
        }
        return result;
    }

    /**
     * 获取所有叶子节点（出度为 0）
     */
    public Set<DagNode> getLeafNodes() {
        Set<DagNode> result = new HashSet<>();
        for (DagNode node : graph.nodes()) {
            if (graph.successors(node).isEmpty()) result.add(node);
        }
        return result;
    }

    /**
     * 获取某节点在给定上下文中满足条件的出边目标节点
     */
    public List<DagNode> getActivatedSuccessors(DagNode node, Map<String, Object> context) {
        List<DagNode> activated = new ArrayList<>();
        for (DagNode successor : graph.successors(node)) {
            DagEdge edge = getEdge(node, successor).orElse(null);
            if (edge == null || edge.evaluate(context)) {
                activated.add(successor);
            }
        }
        return activated;
    }

    /**
     * 查找所有路径（仅考虑无条件边）
     */
    public List<List<DagNode>> findAllPaths(DagNode source, DagNode target) {
        List<List<DagNode>> paths = new ArrayList<>();
        findAllPathsDFS(source, target, new ArrayList<>(), paths);
        return paths;
    }

    private void findAllPathsDFS(DagNode current, DagNode target,
                                 List<DagNode> currentPath, List<List<DagNode>> paths) {
        currentPath.add(current);
        if (current.equals(target)) {
            paths.add(new ArrayList<>(currentPath));
        } else {
            for (DagNode successor : graph.successors(current)) {
                findAllPathsDFS(successor, target, currentPath, paths);
            }
        }
        currentPath.remove(currentPath.size() - 1);
    }

    /**
     * 打印 DAG 结构（含边条件信息）
     */
    public void printDag() {
        System.out.println("=== Guava Graph DAG (with state machine & edge conditions) ===");
        System.out.println("Nodes: " + graph.nodes().size());
        System.out.println("Edges:");
        for (DagNode node : graph.nodes()) {
            Set<DagNode> successors = graph.successors(node);
            if (successors.isEmpty()) {
                System.out.println("  " + node.getName() + " (leaf)");
            } else {
                for (DagNode succ : successors) {
                    DagEdge edge = getEdge(node, succ).orElse(null);
                    String conditionInfo = "";
                    if (edge != null && edge.getCondition() != ALWAYS_TRUE) {
                        conditionInfo = " [conditioned]";
                    }
                    System.out.println("  " + node.getName() + " -> " + succ.getName() + conditionInfo);
                }
            }
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
