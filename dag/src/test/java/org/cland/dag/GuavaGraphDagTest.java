package org.cland.dag;

import org.cland.dag.graph.GuavaDagExample;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Guava Graph DAG 测试（含状态机 + 边条件）
 * <p>
 * 测试场景：数据仓库 ETL 任务 DAG
 * <pre>
 *         SourceA  SourceB
 *            \     /
 *           Extract
 *           /     \
 *     TransformA TransformB
 *           \     /
 *            Load
 *             |
 *          Report
 * </pre>
 */
public class GuavaGraphDagTest {

    private GuavaDagExample dag;
    private GuavaDagExample.DagNode sourceA;
    private GuavaDagExample.DagNode sourceB;
    private GuavaDagExample.DagNode extract;
    private GuavaDagExample.DagNode transformA;
    private GuavaDagExample.DagNode transformB;
    private GuavaDagExample.DagNode load;
    private GuavaDagExample.DagNode report;

    @Before
    public void setUp() {
        dag = new GuavaDagExample();

        sourceA = new GuavaDagExample.DagNode("1", "SourceA");
        sourceB = new GuavaDagExample.DagNode("2", "SourceB");
        extract = new GuavaDagExample.DagNode("3", "Extract");
        transformA = new GuavaDagExample.DagNode("4", "TransformA");
        transformB = new GuavaDagExample.DagNode("5", "TransformB");
        load = new GuavaDagExample.DagNode("6", "Load");
        report = new GuavaDagExample.DagNode("7", "Report");

        dag.addNode(sourceA).addNode(sourceB).addNode(extract)
                .addNode(transformA).addNode(transformB)
                .addNode(load).addNode(report);

        dag.addEdge(sourceA, extract)
                .addEdge(sourceB, extract)
                .addEdge(extract, transformA)
                .addEdge(extract, transformB)
                .addEdge(transformA, load)
                .addEdge(transformB, load)
                .addEdge(load, report);
    }

    // ==================== 结构测试 ====================

    @Test
    public void testDagStructure() {
        assertEquals(7, dag.getNodes().size());
        assertEquals(2, dag.getPredecessors(extract).size());
        assertTrue(dag.getPredecessors(extract).contains(sourceA));
        assertTrue(dag.getPredecessors(extract).contains(sourceB));
        assertEquals(2, dag.getSuccessors(extract).size());
        assertTrue(dag.getSuccessors(extract).contains(transformA));
        assertTrue(dag.getSuccessors(extract).contains(transformB));
        assertTrue(dag.getLeafNodes().contains(report));
        assertEquals(1, dag.getLeafNodes().size());
    }

    // ==================== 环检测 ====================

    @Test
    public void testNoCycle() {
        assertFalse("Valid DAG should not have cycle", dag.hasCycle());
    }

    @Test
    public void testCycleDetection() {
        dag.addEdge(report, sourceA);
        assertTrue("Graph with cycle should be detected", dag.hasCycle());
    }

    // ==================== 拓扑排序 ====================

    @Test
    public void testTopologicalSort() {
        List<GuavaDagExample.DagNode> sorted = dag.topologicalSort();
        assertEquals(7, sorted.size());

        assertTrue(sorted.indexOf(sourceA) < sorted.indexOf(extract));
        assertTrue(sorted.indexOf(sourceB) < sorted.indexOf(extract));
        assertTrue(sorted.indexOf(extract) < sorted.indexOf(transformA));
        assertTrue(sorted.indexOf(extract) < sorted.indexOf(transformB));
        assertTrue(sorted.indexOf(transformA) < sorted.indexOf(load));
        assertTrue(sorted.indexOf(transformB) < sorted.indexOf(load));
        assertTrue(sorted.indexOf(load) < sorted.indexOf(report));
    }

    @Test(expected = IllegalStateException.class)
    public void testTopologicalSortWithCycle() {
        dag.addEdge(report, sourceA);
        dag.topologicalSort();
    }

    // ==================== 源/叶子节点 ====================

    @Test
    public void testSourceAndLeafNodes() {
        assertEquals(2, dag.getSourceNodes().size());
        assertTrue(dag.getSourceNodes().contains(sourceA));
        assertTrue(dag.getSourceNodes().contains(sourceB));

        assertEquals(1, dag.getLeafNodes().size());
        assertTrue(dag.getLeafNodes().contains(report));
    }

    // ==================== 路径查找 ====================

    @Test
    public void testFindAllPaths() {
        assertEquals(2, dag.findAllPaths(sourceA, report).size());
        assertEquals(2, dag.findAllPaths(extract, report).size());
    }

    // ==================== 状态机测试 ====================

    @Test
    public void testNodeStatusInitialState() {
        assertEquals(GuavaDagExample.NodeStatus.INIT, sourceA.getStatus());
    }

    @Test
    public void testValidStateTransitions() {
        sourceA.tryTransition(GuavaDagExample.NodeStatus.RUNNING);
        assertEquals(GuavaDagExample.NodeStatus.RUNNING, sourceA.getStatus());

        sourceA.tryTransition(GuavaDagExample.NodeStatus.SUCCESS);
        assertEquals(GuavaDagExample.NodeStatus.SUCCESS, sourceA.getStatus());
    }

    @Test
    public void testFailAndRetry() {
        sourceA.tryTransition(GuavaDagExample.NodeStatus.RUNNING);
        sourceA.tryTransition(GuavaDagExample.NodeStatus.FAIL);
        assertEquals(GuavaDagExample.NodeStatus.FAIL, sourceA.getStatus());
        assertTrue(sourceA.getStatus().isRetryable());

        sourceA.tryTransition(GuavaDagExample.NodeStatus.RUNNING);
        assertEquals(GuavaDagExample.NodeStatus.RUNNING, sourceA.getStatus());
    }

    @Test
    public void testCancelAnyTime() {
        // INIT → CANCEL
        sourceA.tryTransition(GuavaDagExample.NodeStatus.CANCEL);
        assertEquals(GuavaDagExample.NodeStatus.CANCEL, sourceA.getStatus());

        // CANCEL → RUNNING (重新启动)
        sourceA.tryTransition(GuavaDagExample.NodeStatus.RUNNING);
        assertEquals(GuavaDagExample.NodeStatus.RUNNING, sourceA.getStatus());
    }

    @Test(expected = IllegalStateException.class)
    public void testIllegalTransitionInitToSuccess() {
        sourceA.tryTransition(GuavaDagExample.NodeStatus.SUCCESS);
    }

    @Test(expected = IllegalStateException.class)
    public void testIllegalTransitionSuccessToFail() {
        sourceA.tryTransition(GuavaDagExample.NodeStatus.RUNNING);
        sourceA.tryTransition(GuavaDagExample.NodeStatus.SUCCESS);
        sourceA.tryTransition(GuavaDagExample.NodeStatus.FAIL); // SUCCESS → FAIL 非法
    }

    @Test
    public void testCanTransition() {
        assertTrue(sourceA.canTransition(GuavaDagExample.NodeStatus.RUNNING));
        assertFalse(sourceA.canTransition(GuavaDagExample.NodeStatus.SUCCESS));
        assertFalse(sourceA.canTransition(GuavaDagExample.NodeStatus.FAIL));
    }

    @Test
    public void testResetFromFail() {
        sourceA.tryTransition(GuavaDagExample.NodeStatus.RUNNING);
        sourceA.tryTransition(GuavaDagExample.NodeStatus.FAIL);
        sourceA.reset();
        assertEquals(GuavaDagExample.NodeStatus.INIT, sourceA.getStatus());
    }

    @Test
    public void testTerminalStates() {
        sourceA.tryTransition(GuavaDagExample.NodeStatus.RUNNING);
        sourceA.tryTransition(GuavaDagExample.NodeStatus.SUCCESS);
        assertTrue(sourceA.getStatus().isTerminal());

        assertTrue(GuavaDagExample.NodeStatus.FAIL.isTerminal());
        assertTrue(GuavaDagExample.NodeStatus.CANCEL.isTerminal());
        assertFalse(GuavaDagExample.NodeStatus.INIT.isTerminal());
        assertFalse(GuavaDagExample.NodeStatus.RUNNING.isTerminal());
    }

    // ==================== 边条件测试 ====================

    @Test
    public void testConditionalEdge() {
        // 构建条件 DAG: SourceA ->{success} Extract
        GuavaDagExample conditionDag = new GuavaDagExample();
        GuavaDagExample.DagNode a = new GuavaDagExample.DagNode("A", "A");
        GuavaDagExample.DagNode b = new GuavaDagExample.DagNode("B", "B");
        conditionDag.addNode(a).addNode(b);
        conditionDag.addEdge(a, b, GuavaDagExample.whenSuccess());

        Map<String, Object> ctx = new HashMap<>();

        // A 初始 INIT，不满足条件
        assertTrue(conditionDag.getActivatedSuccessors(a, ctx).isEmpty());

        // A 执行成功
        a.tryTransition(GuavaDagExample.NodeStatus.RUNNING);
        a.tryTransition(GuavaDagExample.NodeStatus.SUCCESS);
        assertEquals(1, conditionDag.getActivatedSuccessors(a, ctx).size());
        assertTrue(conditionDag.getActivatedSuccessors(a, ctx).contains(b));
    }

    @Test
    public void testFailConditionEdge() {
        GuavaDagExample conditionDag = new GuavaDagExample();
        GuavaDagExample.DagNode a = new GuavaDagExample.DagNode("A", "A");
        GuavaDagExample.DagNode b = new GuavaDagExample.DagNode("B", "B");
        conditionDag.addNode(a).addNode(b);
        conditionDag.addEdge(a, b, GuavaDagExample.whenFail());

        Map<String, Object> ctx = new HashMap<>();

        // A 成功，不满足失败条件
        a.tryTransition(GuavaDagExample.NodeStatus.RUNNING);
        a.tryTransition(GuavaDagExample.NodeStatus.SUCCESS);
        assertTrue(conditionDag.getActivatedSuccessors(a, ctx).isEmpty());

        // A 失败，满足条件
        a.reset();
        a.tryTransition(GuavaDagExample.NodeStatus.RUNNING);
        a.tryTransition(GuavaDagExample.NodeStatus.FAIL);
        assertEquals(1, conditionDag.getActivatedSuccessors(a, ctx).size());
        assertTrue(conditionDag.getActivatedSuccessors(a, ctx).contains(b));
    }

    @Test
    public void testAttributeConditionEdge() {
        GuavaDagExample conditionDag = new GuavaDagExample();
        GuavaDagExample.DagNode a = new GuavaDagExample.DagNode("A", "A");
        GuavaDagExample.DagNode b = new GuavaDagExample.DagNode("B", "B");
        conditionDag.addNode(a).addNode(b);

        // 当 A.result > 80 时走 B
        conditionDag.addEdge(a, b,
                GuavaDagExample.whenAttribute("result", v -> (int) v > 80));

        Map<String, Object> ctx = new HashMap<>();

        a.putAttribute("result", 60);
        assertTrue(conditionDag.getActivatedSuccessors(a, ctx).isEmpty());

        a.putAttribute("result", 95);
        assertEquals(1, conditionDag.getActivatedSuccessors(a, ctx).size());
        assertTrue(conditionDag.getActivatedSuccessors(a, ctx).contains(b));
    }

    @Test
    public void testMixedConditionalAndUnconditionalEdges() {
        // A -> B (条件: success), A -> C (无条件)
        GuavaDagExample dag = new GuavaDagExample();
        GuavaDagExample.DagNode a = new GuavaDagExample.DagNode("A", "A");
        GuavaDagExample.DagNode b = new GuavaDagExample.DagNode("B", "B");
        GuavaDagExample.DagNode c = new GuavaDagExample.DagNode("C", "C");
        dag.addNode(a).addNode(b).addNode(c);
        dag.addEdge(a, b, GuavaDagExample.whenSuccess());
        dag.addEdge(a, c); // 无条件

        Map<String, Object> ctx = new HashMap<>();

        // A INIT, B 因条件不满足不可达, C 无条件可达
        assertEquals(1, dag.getActivatedSuccessors(a, ctx).size());
        assertTrue(dag.getActivatedSuccessors(a, ctx).contains(c));

        // A 执行成功
        a.tryTransition(GuavaDagExample.NodeStatus.RUNNING);
        a.tryTransition(GuavaDagExample.NodeStatus.SUCCESS);
        assertEquals(2, dag.getActivatedSuccessors(a, ctx).size());
        assertTrue(dag.getActivatedSuccessors(a, ctx).contains(b));
        assertTrue(dag.getActivatedSuccessors(a, ctx).contains(c));
    }

    @Test
    public void testEdgeCustomAttributes() {
        GuavaDagExample.DagEdge edge = new GuavaDagExample.DagEdge(
                sourceA, extract, "test-edge", GuavaDagExample.ALWAYS_TRUE);
        edge.putAttribute("weight", 10)
                .putAttribute("timeout", 5000)
                .putAttribute("retry", 3);

        assertEquals(Integer.valueOf(10), edge.getAttribute("weight"));
        assertEquals(Integer.valueOf(5000), edge.getAttribute("timeout"));
        assertEquals(Integer.valueOf(3), edge.getAttribute("retry"));
    }

    // ==================== 自定义属性 ====================

    @Test
    public void testNodeCustomAttributes() {
        sourceA.putAttribute("timeout", 30000)
                .putAttribute("retry", 3)
                .putAttribute("handler", "com.example.SourceHandler");

        assertEquals(Integer.valueOf(30000), sourceA.getAttribute("timeout"));
        assertEquals(Integer.valueOf(3), sourceA.getAttribute("retry"));
        assertEquals("com.example.SourceHandler", sourceA.getAttribute("handler"));
    }

    // ==================== 打印 ====================

    @Test
    public void testPrintDag() {
        dag.printDag();
    }

    // ==================== 复杂 DAG ====================

    @Test
    public void testComplexDag() {
        GuavaDagExample complexDag = new GuavaDagExample();
        GuavaDagExample.DagNode a = new GuavaDagExample.DagNode("A", "A");
        GuavaDagExample.DagNode b = new GuavaDagExample.DagNode("B", "B");
        GuavaDagExample.DagNode c = new GuavaDagExample.DagNode("C", "C");
        GuavaDagExample.DagNode d = new GuavaDagExample.DagNode("D", "D");
        GuavaDagExample.DagNode e = new GuavaDagExample.DagNode("E", "E");
        GuavaDagExample.DagNode f = new GuavaDagExample.DagNode("F", "F");
        GuavaDagExample.DagNode g = new GuavaDagExample.DagNode("G", "G");
        GuavaDagExample.DagNode h = new GuavaDagExample.DagNode("H", "H");

        complexDag.addNode(a).addNode(b).addNode(c).addNode(d)
                .addNode(e).addNode(f).addNode(g).addNode(h);

        complexDag.addEdge(a, b).addEdge(a, c).addEdge(a, d)
                .addEdge(b, e).addEdge(c, e).addEdge(d, e)
                .addEdge(e, f).addEdge(e, g)
                .addEdge(f, h).addEdge(g, h);

        assertFalse(complexDag.hasCycle());
        assertEquals(8, complexDag.topologicalSort().size());

        List<GuavaDagExample.DagNode> sorted = complexDag.topologicalSort();
        assertTrue(sorted.indexOf(a) < sorted.indexOf(b));
        assertTrue(sorted.indexOf(a) < sorted.indexOf(c));
        assertTrue(sorted.indexOf(a) < sorted.indexOf(d));
        assertTrue(sorted.indexOf(b) < sorted.indexOf(e));
        assertTrue(sorted.indexOf(c) < sorted.indexOf(e));
        assertTrue(sorted.indexOf(d) < sorted.indexOf(e));
        assertTrue(sorted.indexOf(e) < sorted.indexOf(f));
        assertTrue(sorted.indexOf(e) < sorted.indexOf(g));
        assertTrue(sorted.indexOf(f) < sorted.indexOf(h));
        assertTrue(sorted.indexOf(g) < sorted.indexOf(h));
    }
}
