package org.cland.dag;

import org.cland.dag.graph.JGraphTDagExample;
import org.jgrapht.GraphPath;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.*;

/**
 * JGraphT DAG 测试（含状态机 + 边条件）
 * <p>
 * 测试场景：微服务调用链 DAG
 * <pre>
 *         API Gateway
 *          /    |    \
 *      User  Order  Payment
 *        |    /    \    |
 *        |  Stock  Notification
 *        |    |       |
 *        +----+-------+
 *             |
 *         Data Sync
 * </pre>
 */
public class JGraphTDagTest {

    private JGraphTDagExample dag;
    private JGraphTDagExample.DagNode apiGateway;
    private JGraphTDagExample.DagNode userService;
    private JGraphTDagExample.DagNode orderService;
    private JGraphTDagExample.DagNode paymentService;
    private JGraphTDagExample.DagNode stockService;
    private JGraphTDagExample.DagNode notificationService;
    private JGraphTDagExample.DagNode dataSync;

    @Before
    public void setUp() {
        dag = new JGraphTDagExample();

        apiGateway = new JGraphTDagExample.DagNode("gateway", "API Gateway");
        userService = new JGraphTDagExample.DagNode("user", "User");
        orderService = new JGraphTDagExample.DagNode("order", "Order");
        paymentService = new JGraphTDagExample.DagNode("payment", "Payment");
        stockService = new JGraphTDagExample.DagNode("stock", "Stock");
        notificationService = new JGraphTDagExample.DagNode("notif", "Notification");
        dataSync = new JGraphTDagExample.DagNode("sync", "Data Sync");

        dag.addNode(apiGateway).addNode(userService).addNode(orderService)
                .addNode(paymentService).addNode(stockService)
                .addNode(notificationService).addNode(dataSync);

        dag.addEdge(apiGateway, userService);
        dag.addEdge(apiGateway, orderService);
        dag.addEdge(apiGateway, paymentService);
        dag.addEdge(userService, dataSync);
        dag.addEdge(orderService, stockService);
        dag.addEdge(orderService, notificationService);
        dag.addEdge(paymentService, notificationService);
        dag.addEdge(stockService, dataSync);
        dag.addEdge(notificationService, dataSync);
    }

    // ==================== 结构测试 ====================

    @Test
    public void testDagStructure() {
        assertEquals(7, dag.getGraph().vertexSet().size());
        assertEquals(9, dag.getGraph().edgeSet().size());
    }

    // ==================== 环检测 ====================

    @Test
    public void testNoCycle() {
        assertFalse("Valid DAG should not have cycle", dag.hasCycle());
    }

    @Test
    public void testCycleDetection() {
        dag.addEdge(dataSync, apiGateway);
        assertTrue("Graph with cycle should be detected", dag.hasCycle());
        assertFalse("Cycle nodes should not be empty", dag.findCycleNodes().isEmpty());
    }

    // ==================== 拓扑排序 ====================

    @Test
    public void testTopologicalSort() {
        List<JGraphTDagExample.DagNode> sorted = dag.topologicalSort();
        assertEquals(7, sorted.size());

        assertTrue(sorted.indexOf(apiGateway) < sorted.indexOf(userService));
        assertTrue(sorted.indexOf(apiGateway) < sorted.indexOf(orderService));
        assertTrue(sorted.indexOf(apiGateway) < sorted.indexOf(paymentService));
        assertTrue(sorted.indexOf(orderService) < sorted.indexOf(stockService));
        assertTrue(sorted.indexOf(orderService) < sorted.indexOf(notificationService));
        assertTrue(sorted.indexOf(paymentService) < sorted.indexOf(notificationService));
        assertTrue(sorted.indexOf(userService) < sorted.indexOf(dataSync));
        assertTrue(sorted.indexOf(stockService) < sorted.indexOf(dataSync));
        assertTrue(sorted.indexOf(notificationService) < sorted.indexOf(dataSync));
    }

    @Test
    public void testReverseTopologicalSort() {
        List<JGraphTDagExample.DagNode> reversed = dag.topologicalSortReverse();
        assertEquals(7, reversed.size());
        assertEquals(dataSync, reversed.get(0));
    }

    @Test(expected = IllegalStateException.class)
    public void testTopologicalSortWithCycle() {
        dag.addEdge(dataSync, apiGateway);
        dag.topologicalSort();
    }

    // ==================== 源/叶子节点 ====================

    @Test
    public void testSourceAndLeafNodes() {
        assertEquals(1, dag.getSourceNodes().size());
        assertTrue(dag.getSourceNodes().contains(apiGateway));

        assertEquals(1, dag.getLeafNodes().size());
        assertTrue(dag.getLeafNodes().contains(dataSync));
    }

    // ==================== 前驱/后继 ====================

    @Test
    public void testPredecessorsAndSuccessors() {
        assertTrue(dag.getPredecessors(apiGateway).isEmpty());

        Set<JGraphTDagExample.DagNode> gatewaySucc = dag.getSuccessors(apiGateway);
        assertEquals(3, gatewaySucc.size());
        assertTrue(gatewaySucc.contains(userService));
        assertTrue(gatewaySucc.contains(orderService));
        assertTrue(gatewaySucc.contains(paymentService));

        Set<JGraphTDagExample.DagNode> syncPred = dag.getPredecessors(dataSync);
        assertEquals(3, syncPred.size());
        assertTrue(syncPred.contains(userService));
        assertTrue(syncPred.contains(stockService));
        assertTrue(syncPred.contains(notificationService));
    }

    // ==================== 路径查找 ====================

    @Test
    public void testFindAllPaths() {
        List<GraphPath<JGraphTDagExample.DagNode, JGraphTDagExample.DagEdge>> paths =
                dag.findAllPaths(apiGateway, dataSync);
        assertEquals("Should have 4 paths from API Gateway to Data Sync", 4, paths.size());
        for (GraphPath<JGraphTDagExample.DagNode, JGraphTDagExample.DagEdge> path : paths) {
            assertEquals(apiGateway, path.getStartVertex());
            assertEquals(dataSync, path.getEndVertex());
        }
    }

    @Test
    public void testShortestPath() {
        GraphPath<JGraphTDagExample.DagNode, JGraphTDagExample.DagEdge> path =
                dag.findShortestPath(apiGateway, dataSync);
        assertNotNull("Path should exist", path);
        assertEquals(2, path.getLength());
    }

    // ==================== DOT 导出 ====================

    @Test
    public void testDotExport() {
        String dot = dag.exportToDot();
        assertNotNull(dot);
        assertTrue(dot.contains("digraph"));
        assertTrue(dot.contains("gateway"));
        assertTrue(dot.contains("sync"));
    }

    // ==================== 状态机测试 ====================

    @Test
    public void testNodeStatusInitialState() {
        assertEquals(JGraphTDagExample.NodeStatus.INIT, apiGateway.getStatus());
    }

    @Test
    public void testValidStateTransitions() {
        apiGateway.tryTransition(JGraphTDagExample.NodeStatus.RUNNING);
        assertEquals(JGraphTDagExample.NodeStatus.RUNNING, apiGateway.getStatus());

        apiGateway.tryTransition(JGraphTDagExample.NodeStatus.SUCCESS);
        assertEquals(JGraphTDagExample.NodeStatus.SUCCESS, apiGateway.getStatus());
    }

    @Test
    public void testFailAndRetry() {
        apiGateway.tryTransition(JGraphTDagExample.NodeStatus.RUNNING);
        apiGateway.tryTransition(JGraphTDagExample.NodeStatus.FAIL);
        assertEquals(JGraphTDagExample.NodeStatus.FAIL, apiGateway.getStatus());
        assertTrue(apiGateway.getStatus().isRetryable());

        apiGateway.tryTransition(JGraphTDagExample.NodeStatus.RUNNING);
        assertEquals(JGraphTDagExample.NodeStatus.RUNNING, apiGateway.getStatus());
    }

    @Test
    public void testCancelAnyTime() {
        // INIT → CANCEL
        apiGateway.tryTransition(JGraphTDagExample.NodeStatus.CANCEL);
        assertEquals(JGraphTDagExample.NodeStatus.CANCEL, apiGateway.getStatus());

        // CANCEL → RUNNING (重新启动)
        apiGateway.tryTransition(JGraphTDagExample.NodeStatus.RUNNING);
        assertEquals(JGraphTDagExample.NodeStatus.RUNNING, apiGateway.getStatus());
    }

    @Test(expected = IllegalStateException.class)
    public void testIllegalTransitionInitToSuccess() {
        apiGateway.tryTransition(JGraphTDagExample.NodeStatus.SUCCESS);
    }

    @Test(expected = IllegalStateException.class)
    public void testIllegalTransitionSuccessToFail() {
        apiGateway.tryTransition(JGraphTDagExample.NodeStatus.RUNNING);
        apiGateway.tryTransition(JGraphTDagExample.NodeStatus.SUCCESS);
        apiGateway.tryTransition(JGraphTDagExample.NodeStatus.FAIL); // 非法
    }

    @Test
    public void testCanTransition() {
        assertTrue(apiGateway.canTransition(JGraphTDagExample.NodeStatus.RUNNING));
        assertFalse(apiGateway.canTransition(JGraphTDagExample.NodeStatus.SUCCESS));
    }

    @Test
    public void testTerminalStates() {
        apiGateway.tryTransition(JGraphTDagExample.NodeStatus.RUNNING);
        apiGateway.tryTransition(JGraphTDagExample.NodeStatus.SUCCESS);
        assertTrue(apiGateway.getStatus().isTerminal());

        assertTrue(JGraphTDagExample.NodeStatus.FAIL.isTerminal());
        assertTrue(JGraphTDagExample.NodeStatus.CANCEL.isTerminal());
        assertFalse(JGraphTDagExample.NodeStatus.INIT.isTerminal());
        assertFalse(JGraphTDagExample.NodeStatus.RUNNING.isTerminal());
    }

    // ==================== 边条件测试 ====================

    @Test
    public void testConditionalEdge() {
        JGraphTDagExample conditionDag = new JGraphTDagExample();
        JGraphTDagExample.DagNode a = new JGraphTDagExample.DagNode("A", "A");
        JGraphTDagExample.DagNode b = new JGraphTDagExample.DagNode("B", "B");
        conditionDag.addNode(a).addNode(b);
        conditionDag.addEdge(a, b, JGraphTDagExample.whenSuccess());

        Map<String, Object> ctx = new HashMap<>();

        // A INIT, 不满足条件
        assertTrue(conditionDag.getActivatedSuccessors(a, ctx).isEmpty());

        // A 成功
        a.tryTransition(JGraphTDagExample.NodeStatus.RUNNING);
        a.tryTransition(JGraphTDagExample.NodeStatus.SUCCESS);
        assertEquals(1, conditionDag.getActivatedSuccessors(a, ctx).size());
        assertTrue(conditionDag.getActivatedSuccessors(a, ctx).contains(b));
    }

    @Test
    public void testFailConditionEdge() {
        JGraphTDagExample conditionDag = new JGraphTDagExample();
        JGraphTDagExample.DagNode a = new JGraphTDagExample.DagNode("A", "A");
        JGraphTDagExample.DagNode b = new JGraphTDagExample.DagNode("B", "B");
        conditionDag.addNode(a).addNode(b);
        conditionDag.addEdge(a, b, JGraphTDagExample.whenFail());

        Map<String, Object> ctx = new HashMap<>();

        // A 成功，不满足失败条件
        a.tryTransition(JGraphTDagExample.NodeStatus.RUNNING);
        a.tryTransition(JGraphTDagExample.NodeStatus.SUCCESS);
        assertTrue(conditionDag.getActivatedSuccessors(a, ctx).isEmpty());

        // A 失败
        a.reset();
        a.tryTransition(JGraphTDagExample.NodeStatus.RUNNING);
        a.tryTransition(JGraphTDagExample.NodeStatus.FAIL);
        assertEquals(1, conditionDag.getActivatedSuccessors(a, ctx).size());
        assertTrue(conditionDag.getActivatedSuccessors(a, ctx).contains(b));
    }

    @Test
    public void testAttributeConditionEdge() {
        JGraphTDagExample conditionDag = new JGraphTDagExample();
        JGraphTDagExample.DagNode a = new JGraphTDagExample.DagNode("A", "A");
        JGraphTDagExample.DagNode b = new JGraphTDagExample.DagNode("B", "B");
        conditionDag.addNode(a).addNode(b);
        conditionDag.addEdge(a, b,
                JGraphTDagExample.whenAttribute("result", v -> (int) v > 80));

        Map<String, Object> ctx = new HashMap<>();

        a.putAttribute("result", 60);
        assertTrue(conditionDag.getActivatedSuccessors(a, ctx).isEmpty());

        a.putAttribute("result", 95);
        assertEquals(1, conditionDag.getActivatedSuccessors(a, ctx).size());
        assertTrue(conditionDag.getActivatedSuccessors(a, ctx).contains(b));
    }

    @Test
    public void testMixedConditionalAndUnconditionalEdges() {
        JGraphTDagExample dag = new JGraphTDagExample();
        JGraphTDagExample.DagNode a = new JGraphTDagExample.DagNode("A", "A");
        JGraphTDagExample.DagNode b = new JGraphTDagExample.DagNode("B", "B");
        JGraphTDagExample.DagNode c = new JGraphTDagExample.DagNode("C", "C");
        dag.addNode(a).addNode(b).addNode(c);
        dag.addEdge(a, b, JGraphTDagExample.whenSuccess());
        dag.addEdge(a, c); // 无条件

        Map<String, Object> ctx = new HashMap<>();

        // A INIT, B 不可达, C 可达
        assertEquals(1, dag.getActivatedSuccessors(a, ctx).size());
        assertTrue(dag.getActivatedSuccessors(a, ctx).contains(c));

        // A 成功
        a.tryTransition(JGraphTDagExample.NodeStatus.RUNNING);
        a.tryTransition(JGraphTDagExample.NodeStatus.SUCCESS);
        assertEquals(2, dag.getActivatedSuccessors(a, ctx).size());
        assertTrue(dag.getActivatedSuccessors(a, ctx).contains(b));
        assertTrue(dag.getActivatedSuccessors(a, ctx).contains(c));
    }

    // ==================== 边自定义属性 ====================

    @Test
    public void testNodeCustomAttributesOnEdge() {
        JGraphTDagExample.DagEdge edge = new JGraphTDagExample.DagEdge("test-edge");
        edge.putAttribute("weight", 10)
                .putAttribute("timeout", 5000)
                .putAttribute("async", true);

        assertEquals(Integer.valueOf(10), edge.getAttribute("weight"));
        assertEquals(Integer.valueOf(5000), edge.getAttribute("timeout"));
        assertEquals(true, edge.getAttribute("async"));
    }

    @Test
    public void testEdgeWithConditionAndAttributes() {
        JGraphTDagExample.DagEdge edge = new JGraphTDagExample.DagEdge("cond-edge",
                JGraphTDagExample.whenSuccess());
        edge.putAttribute("priority", "high");

        assertEquals("cond-edge", edge.getId());
        assertNotNull(edge.getCondition());

        JGraphTDagExample.DagNode a = new JGraphTDagExample.DagNode("A", "A");
        Map<String, Object> ctx = new HashMap<>();

        // A INIT, 条件不满足
        assertFalse(edge.evaluate(a, ctx));

        // A 成功, 条件满足
        a.tryTransition(JGraphTDagExample.NodeStatus.RUNNING);
        a.tryTransition(JGraphTDagExample.NodeStatus.SUCCESS);
        assertTrue(edge.evaluate(a, ctx));

        assertEquals("high", edge.getAttribute("priority"));
    }

    // ==================== 打印 ====================

    @Test
    public void testPrintDag() {
        dag.printDag();
    }

    // ==================== 边界情况 ====================

    @Test
    public void testSingleNodeDag() {
        JGraphTDagExample singleDag = new JGraphTDagExample();
        JGraphTDagExample.DagNode single = new JGraphTDagExample.DagNode("1", "Single");
        singleDag.addNode(single);
        assertFalse(singleDag.hasCycle());
        assertEquals(1, singleDag.topologicalSort().size());
        assertEquals(1, singleDag.getSourceNodes().size());
        assertEquals(1, singleDag.getLeafNodes().size());
    }

    @Test
    public void testLinearDag() {
        JGraphTDagExample linearDag = new JGraphTDagExample();
        JGraphTDagExample.DagNode n1 = new JGraphTDagExample.DagNode("1", "Step1");
        JGraphTDagExample.DagNode n2 = new JGraphTDagExample.DagNode("2", "Step2");
        JGraphTDagExample.DagNode n3 = new JGraphTDagExample.DagNode("3", "Step3");

        linearDag.addNode(n1).addNode(n2).addNode(n3);
        linearDag.addEdge(n1, n2).addEdge(n2, n3);

        List<JGraphTDagExample.DagNode> sorted = linearDag.topologicalSort();
        assertEquals(n1, sorted.get(0));
        assertEquals(n2, sorted.get(1));
        assertEquals(n3, sorted.get(2));
    }
}
