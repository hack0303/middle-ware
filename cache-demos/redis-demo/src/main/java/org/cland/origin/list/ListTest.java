package org.cland.origin.list;

import org.cland.origin.BaseSupport;

public class ListTest extends BaseSupport {

	public static void main(String[] args) {
	 jd.select(2);
     jd.rpush("lst01", "a","b","c");
     jd.rpush("lst01","d","e","f");
     jd.rpop("lst01");
     jd.expire("lst01", 10);
	}

}
