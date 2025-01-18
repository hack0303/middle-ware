package org.cland.mock.testable;

import com.alibaba.testable.core.annotation.MockMethod;
import org.junit.Test;

import static com.alibaba.testable.core.matcher.InvokeVerifier.verify;
import static com.alibaba.testable.core.tool.TestableTool.MOCK_CONTEXT;
import static com.alibaba.testable.core.tool.TestableTool.SOURCE_METHOD;
import static org.junit.Assert.assertEquals;

/**
 * 演示基本的Mock功能
 * Demonstrate basic mock functionality
 */
public class DemoMockTest {

    private DemoMock demoMock = new DemoMock();

    @MockMethod(targetClass = DemoMock.class)
    private String innerFunc(String text) {
        return "mock_" + text;
    }

    @MockMethod(targetClass = DemoMock.class)
    private String staticFunc() {
        return "_MOCK_TAIL";
    }

    @MockMethod(targetClass = String.class)
    private String trim() {
        return "trim_string";
    }

    @MockMethod(targetClass = String.class, targetMethod = "substring")
    private String sub(int i, int j) {
        return "sub_string";
    }

    @MockMethod(targetClass = String.class)
    private boolean startsWith(String s) {
        return false;
    }


    @MockMethod(targetClass = DemoMock.class)
    private String callFromDifferentMethod() {
        if ("special_case".equals(MOCK_CONTEXT.get("case"))) {
            return "mock_special";
        }
        switch (SOURCE_METHOD) {
            case "callerOne": return "mock_one";
            default: return "mock_others";
        }
    }



    @Test
    public void should_able_to_mock_member_method() throws Exception {
        assertEquals("{ \"res\": \"mock_hello_MOCK_TAIL\"}", demoMock.outerFunc("hello"));
        verify("innerFunc").with("hello");
        verify("staticFunc").with();
    }

    @Test
    public void should_able_to_mock_common_method() {
        assertEquals("trim_string__sub_string__false", demoMock.commonFunc());
        verify("trim").withTimes(1);
        verify("sub").withTimes(1);
        verify("startsWith").withTimes(1);
    }


}
