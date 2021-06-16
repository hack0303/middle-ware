package org.cland.rdb.demo.mysql;

import cn.hutool.core.lang.Snowflake;
import com.alibaba.fastjson.JSON;
import com.zaxxer.hikari.HikariDataSource;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.driver.api.ShardingSphereDataSourceFactory;
import org.apache.shardingsphere.infra.config.algorithm.ShardingSphereAlgorithmConfiguration;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.keygen.KeyGenerateStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.StandardShardingStrategyConfiguration;
import org.apache.shardingsphere.transaction.core.TransactionType;
import org.apache.shardingsphere.transaction.core.TransactionTypeHolder;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;

@Slf4j
public class OriginJDBCApp {

    static String driverName="com.mysql.cj.jdbc.Driver";
    static String userName="root";
    static String password="123456";
    @SneakyThrows
    public static void main(String[] args){
        // Configure actual data sources
        Map<String, DataSource> dataSourceMap = new HashMap<>();
// Configure the first data source
        HikariDataSource dataSource1 = new HikariDataSource();
        dataSource1.setDriverClassName(driverName);
        dataSource1.setJdbcUrl("jdbc:mysql://node01:3306/ds0");
        dataSource1.setUsername(userName);
        dataSource1.setPassword(password);
        dataSourceMap.put("ds0", dataSource1);

// Configure the second data source
        HikariDataSource dataSource2 = new HikariDataSource();
        dataSource2.setDriverClassName(driverName);
        dataSource2.setJdbcUrl("jdbc:mysql://node01:3306/ds1");
        dataSource2.setUsername(userName);
        dataSource2.setPassword(password);
        dataSourceMap.put("ds1", dataSource2);

// Configure order table rule
        ShardingTableRuleConfiguration orderTableRuleConfig = new ShardingTableRuleConfiguration("t_order", "ds${0..1}.t_order${0..1}");

        orderTableRuleConfig.setKeyGenerateStrategy(new KeyGenerateStrategyConfiguration("order_id","snowflakeAlgorithm"));
// Configure database sharding strategy
        orderTableRuleConfig.setDatabaseShardingStrategy(new StandardShardingStrategyConfiguration("user_id", "dbShardingAlgorithm"));

// Configure table sharding strategy
        orderTableRuleConfig.setTableShardingStrategy(new StandardShardingStrategyConfiguration("order_id", "tableShardingAlgorithm"));


// Omit t_order_item table rule configuration ...
// ...

// Configure sharding rule
        ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();
        shardingRuleConfig.getTables().add(orderTableRuleConfig);



// Configure database sharding algorithm
        Properties dbShardingAlgorithmrProps = new Properties();
        dbShardingAlgorithmrProps.setProperty("algorithm-expression", "ds${user_id % 2}");
        shardingRuleConfig.getShardingAlgorithms().put("dbShardingAlgorithm", new ShardingSphereAlgorithmConfiguration("INLINE", dbShardingAlgorithmrProps));

// Configure table sharding algorithm
        Properties tableShardingAlgorithmrProps = new Properties();
        tableShardingAlgorithmrProps.setProperty("algorithm-expression", "t_order${order_id % 2}");
        shardingRuleConfig.getShardingAlgorithms().put("tableShardingAlgorithm", new ShardingSphereAlgorithmConfiguration("INLINE", tableShardingAlgorithmrProps));

        Properties snowflakeAlgorithmProps = new Properties();
        snowflakeAlgorithmProps.put("worker-id",0);
        //snowflakeAlgorithmProps.put("center-id",0);
        shardingRuleConfig.getKeyGenerators().put("snowflakeAlgorithm",new ShardingSphereAlgorithmConfiguration("SNOWFLAKE",snowflakeAlgorithmProps));

        //shardingRuleConfig.getBindingTableGroups().add("t_order");
// Create ShardingSphereDataSource
        Properties props=new Properties();
        props.put("sql-show",true);
        DataSource dataSource = ShardingSphereDataSourceFactory.createDataSource(dataSourceMap, Collections.singleton(shardingRuleConfig), props);

        //;doInsertion2(dataSource);
        printSelect(dataSource);

        synchronized (OriginJDBCApp.class){
            OriginJDBCApp.class.wait();
        }

    }

    @SneakyThrows
    public static void doInsertion(DataSource dataSource){
        TransactionTypeHolder.set(TransactionType.XA);
        //DataSource dataSource = ShardingSphereDataSourceFactory.createDataSource(dataSourceMap, Collections.singleton(shardingRuleConfig), new Properties());
        String sql = "INSERT t_order(order_id,user_id) VALUES(?,?)";
        try (
                Connection conn = dataSource.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)){
            conn.setAutoCommit(false);
            ps.setLong(1,1);
            ps.setLong(2,1);
            ps.execute();
            ps.setLong(1,2);
            ps.setLong(2,1);
            ps.execute();
            ps.setLong(1,3);
            ps.setLong(2,2);
            ps.execute();
            ps.setLong(1,4);
            ps.setLong(2,2);
            ps.execute();
            conn.commit();
            log.info("<== 提交成功");
        }
    }
    static Snowflake snowflake=new Snowflake(0l,0l,true);
    static Random random=new Random();
    @SneakyThrows
    public static void doInsertion2(DataSource dataSource){
        TransactionTypeHolder.set(TransactionType.XA);
        //DataSource dataSource = ShardingSphereDataSourceFactory.createDataSource(dataSourceMap, Collections.singleton(shardingRuleConfig), new Properties());
        String sql = "INSERT t_order(user_id) VALUES(?)";
        log.info("==> 插入事务 开始");
        for(int uc=100;uc>0;uc--) {
            Long userId=snowflake.nextId()+random.nextInt(2);
            log.info("==> userId is {}",userId);
            try (
                    Connection conn = dataSource.getConnection();
                    PreparedStatement ps = conn.prepareStatement(sql)){
                conn.setAutoCommit(false);
            for (int count = 1000; count > 0; count--) {
                    //ps.setLong(1,1);
                    ps.setLong(1, userId);
                    ps.execute();
            }
                conn.commit();
                log.info("<== 提交成功");
            }
        }
    }

    @SneakyThrows
    public static void printSelect(DataSource dataSource){
        log.info("==> print start!");
        //DataSource dataSource = ShardingSphereDataSourceFactory.createDataSource(dataSourceMap, Collections.singleton(shardingRuleConfig), new Properties());
        //String sql = "SELECT i.* FROM t_order o JOIN t_order_item i ON o.order_id=i.order_id WHERE o.user_id=? AND o.order_id=? order by o.order_id limit 0,1000";
        String sql = "SELECT o.* FROM t_order o WHERE o.user_id=? order by o.order_id limit 0,1000";
        try (
                Connection conn = dataSource.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setLong(1, 1405074546742001665L);
                //ps.setInt(2, 1000);
                try (ResultSet rs = ps.executeQuery()) {
                   while(rs.next()) {
                       log.info("<==");
                      log.info("order_id={},user_id={}",rs.getLong(1),rs.getLong(2));
                }
                    log.info("<== end");
            }
        }
    }
}
