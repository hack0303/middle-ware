package org.clang.origin;

public class Bootstrap extends BaseSupport{

    public static void main(String[] args) throws InterruptedException {
        synchronized (Bootstrap.class) {
            Bootstrap.class.wait();
        }
    }
}
