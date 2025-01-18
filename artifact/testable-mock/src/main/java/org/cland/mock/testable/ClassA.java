package org.cland.mock.testable;

public class ClassA {

    public String doSomething(String x,String y){
        return x(x)+y(y);
    }

    public String x(String x){
        return x;
    }

    public String y(String y){
        return y;
    }

}
