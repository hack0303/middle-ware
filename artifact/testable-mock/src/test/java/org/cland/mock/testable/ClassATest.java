package org.cland.mock.testable;

import com.alibaba.testable.core.annotation.MockMethod;
import org.junit.Assert;
import org.junit.Test;

import static com.alibaba.testable.core.matcher.InvokeVerifier.verify;

public class ClassATest {

    private ClassA classA=new ClassA();


        @MockMethod(targetClass = ClassA.class,targetMethod = "doSomething")
        public String doSomething(String x, String y){
            return "mock-result";
        }

    @MockMethod(targetClass = ClassA.class,targetMethod = "x")
    public String x(String x){
        return "x0";
    }

    @MockMethod(targetClass = ClassA.class,targetMethod = "y")
    public String y( String y){
        return "y0";
    }

    @Test
    public void test_method(){
        Assert.assertEquals("x0y0", classA.doSomething("1", "2"));
        verify("x").with("1");
        verify("y").with("2");
    }


}
