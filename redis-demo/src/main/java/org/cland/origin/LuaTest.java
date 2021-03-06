package org.cland.origin;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LuaTest extends BaseSupport {

	public static void main(String[] args) {
		runCase01();
	}

	public static void runCase01(){
		log.info("case01===================");
		String cmd="return 'hello world'";
		String sha1=jd.scriptLoad(cmd);
		log.info("{}=>{}",cmd,jd.evalsha(sha1));
		log.info("====================case01");
	}

}
