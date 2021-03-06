package org.clang.origin;

import lombok.extern.log4j.Log4j2;

import java.util.HashMap;
import java.util.Map;

@Log4j2
public class MapMultTest extends BaseSupport {

	public static void main(String[] args) {
     jd.select(4);
     Map<String,String> map=new HashMap<>();
     map.put("a","100aa");
     map.put("b","100bb");
     log.info(jd.hmset("hm001", map));
     jd.hmget("hm001", "a","b").forEach(item->{
    	 log.info("{}",item);
     });
     log.info(jd.hsetnx("hm001", "a", "aaa"));
	}

}
