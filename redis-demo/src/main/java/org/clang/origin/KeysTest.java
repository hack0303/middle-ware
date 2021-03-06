package org.clang.origin;

import lombok.extern.log4j.Log4j2;
import redis.clients.jedis.ScanParams;
import redis.clients.jedis.ScanResult;

import java.util.HashSet;
import java.util.Set;

@Log4j2
public class KeysTest extends BaseSupport
{

	public static void main(String[] args) {
		jd.set("a","hahsad");
		jd.set("b","hahsad");
		jd.set("c","hahsad");
		jd.set("d","hahsad");
		jd.set("e","hahsad");
		jd.set("f","hahsad");
		jd.set("g","hahsad");
		jd.set("h","hahsad");
		jd.set("i","hahsad");
		jd.set("j","hahsad");
		jd.keys("*").forEach(item->log.info(item));
		ScanParams params=new ScanParams();
		params.count(5);
		params.match("*");
		ScanResult<String> result=jd.scan(ScanParams.SCAN_POINTER_START, params);
		Set<String> target=new HashSet<>();
		target.addAll(result.getResult());
		while(!result.getCursor().equals("0")) {
			log.info("------------------------");
			result=jd.scan(result.getCursor(), params);
			target.addAll(result.getResult());
		}
		target.forEach(e->log.info(e));
	}

}
