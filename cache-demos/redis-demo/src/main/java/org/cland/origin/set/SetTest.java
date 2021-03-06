package org.cland.origin.set;

import lombok.extern.slf4j.Slf4j;
import org.cland.origin.BaseSupport;

import java.util.HashMap;
import java.util.Map;

@Slf4j
public class SetTest extends BaseSupport {

    public static void main(String[] args) {
        runCase01();
        runCase02();
    }

    public static void runCase01() {
        String[] vals = new String[]{"a", "b", "c", "b", "d"};
        Long sucNum = jd.sadd("set", vals);
        log.info("{},origin num is {},after num is {}", vals, vals.length, sucNum);
    }

    /**
     * zset
     * */
    public static void runCase02() {
        Map<String,Double> key2Score=new HashMap<>();
        key2Score.put("a",1d);
        key2Score.put("b",3d);
        key2Score.put("c",2d);
        //key2Score.put("b",0d);
        key2Score.put("d",5d);
        Long sucNum = jd.zadd("zset", key2Score);
        log.info("{},origin num is {},after num is {}", key2Score, key2Score.size(), sucNum);
        log.info("in [0,1] is {}",jd.zrange("zset",0,1));
        key2Score.put("b",0d);
        log.info("in [0,1] is {}",jd.zrange("zset",0,1));
        log.info("b rank is {}",jd.zrank("zset","b"));
    }


}
