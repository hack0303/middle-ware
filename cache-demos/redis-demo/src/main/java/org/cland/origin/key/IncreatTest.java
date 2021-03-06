package org.cland.origin.key;

import lombok.extern.slf4j.Slf4j;
import org.cland.origin.BaseSupport;

@Slf4j
public class IncreatTest extends BaseSupport {
    public static String keyA = "key-a";
    public static String msgTemplate = "{} now is {}";

    public static void main(String[] args) {
        log.info(msgTemplate, keyA, jd.incr(keyA));
        log.info(msgTemplate, keyA, jd.incrBy(keyA, 10));
        log.info(msgTemplate, keyA, jd.decr(keyA));
        log.info(msgTemplate, keyA, jd.decrBy(keyA, 10));
        Long opResult=jd.del(keyA);
        if (opResult.equals(1L)) {
            log.info("{} clean suc", keyA);
        }
    }

}
