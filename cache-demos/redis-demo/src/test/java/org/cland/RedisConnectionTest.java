package org.cland;

import lombok.extern.slf4j.Slf4j;
import org.junit.*;
import redis.clients.jedis.Jedis;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import static org.junit.Assert.assertNotNull;

@Slf4j
public class RedisConnectionTest {
    static Jedis jd = null;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        System.setProperty("host", "192.168.159.146");
        System.setProperty("port", "6379");
        String host = System.getProperty("host", "localhost");
        int port = Integer.parseInt(System.getProperty("port", "6379"));
        jd = new Jedis(host, port);
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Ignore
    @Test
    public void testLog() {
        log.debug("{},{}", "hello", "world!!");
    }

    @Ignore
    @Test
    public void testRedis001() throws FileNotFoundException, IOException {
        Properties props = new Properties();
        props.load(ClassLoader.getSystemClassLoader().getResourceAsStream("redis.config"));
        String host = props.getProperty("host", "localhost");
        int port = Integer.parseInt(props.getProperty("port", "6379"));
        log.debug("host:{},port:{}", host, port);
        Jedis jd = new Jedis(host, port);
        log.debug("connect to server,{}", "success");
        jd.set("x", "1");

    }

    @Test
    public void testRedis002() throws FileNotFoundException, IOException {
        log.debug("connect to server,{}", "success");
        jd.set("x", "1");
        jd.select(1);
        jd.set("x", "2");
        assertNotNull(jd.get("x"));
    }

}
