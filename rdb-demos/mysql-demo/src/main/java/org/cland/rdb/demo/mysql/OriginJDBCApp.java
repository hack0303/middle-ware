package org.cland.rdb.demo.mysql;

import com.alibaba.fastjson.JSON;
import com.zaxxer.hikari.HikariDataSource;
import lombok.SneakyThrows;
import org.apache.shardingsphere.driver.api.ShardingSphereDataSourceFactory;
import org.apache.shardingsphere.infra.config.algorithm.ShardingSphereAlgorithmConfiguration;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.StandardShardingStrategyConfiguration;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

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

// Configure database sharding strategy
        orderTableRuleConfig.setDatabaseShardingStrategy(new StandardShardingStrategyConfiguration("user_id", "dbShardingAlgorithm"));

// Configure table sharding strategy
        orderTableRuleConfig.setTableShardingStrategy(new StandardShardingStrategyConfiguration("id", "tableShardingAlgorithm"));

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
        tableShardingAlgorithmrProps.setProperty("algorithm-expression", "t_order${id % 2}");
        shardingRuleConfig.getShardingAlgorithms().put("tableShardingAlgorithm", new ShardingSphereAlgorithmConfiguration("INLINE", tableShardingAlgorithmrProps));

// Create ShardingSphereDataSource
        DataSource dataSource = ShardingSphereDataSourceFactory.createDataSource(dataSourceMap, Collections.singleton(shardingRuleConfig), new Properties());

        ;doInsertion(dataSource);
        printSelect(dataSource);

    }

    @SneakyThrows
    public static void doInsertion(DataSource dataSource){
        //DataSource dataSource = ShardingSphereDataSourceFactory.createDataSource(dataSourceMap, Collections.singleton(shardingRuleConfig), new Properties());
        String sql = "INSERT t_order(order_id,user_id) VALUES(?,?)";
        try (
                Connection conn = dataSource.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)){
            ps.setString(1,"1");
            ps.setString(2,"1");
            ps.setString(1,"2");
            ps.setString(2,"1");
            ps.setString(1,"3");
            ps.setString(2,"2");
            ps.setString(1,"4");
            ps.setString(2,"2");
            ps.executeLargeUpdate();
        }
    }

    @SneakyThrows
    public static void printSelect(DataSource dataSource){
        //DataSource dataSource = ShardingSphereDataSourceFactory.createDataSource(dataSourceMap, Collections.singleton(shardingRuleConfig), new Properties());
        String sql = "SELECT i.* FROM t_order o JOIN t_order_item i ON o.id=i.id WHERE o.user_id=? AND o.id=? order by o.id";
        try (
                Connection conn = dataSource.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, 10);
                ps.setInt(2, 1000);
                try (ResultSet rs = ps.executeQuery()) {
                   while(rs.next()) {
                      System.out.println(JSON.toJSONString(rs));
                }
            }
        }
    }
}
