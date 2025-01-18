#note
快速使用

#install
```
docker run --name questdb -p 9000:9000 -p 9009:9009 -p 8812:8812 questdb/questdb --allwaystart
```
# basic use case
## maven
```        <dependency>
            <groupId>org.questdb</groupId>
            <artifactId>questdb</artifactId>
            <version>6.4.3</version>
        </dependency>
        <!-- https://mvnrepository.com/artifact/org.postgresql/postgresql -->
        <dependency>
            <groupId>org.postgresql</groupId>
            <artifactId>postgresql</artifactId>
            <version>42.4.1</version>
        </dependency>
```
##　insert
```
/**
 * 同步 influxdb protocol
 * */
public class Sample001 {

    static Random random=new Random();

    public static void main(String[] args) {
        try (Sender sender = Sender.builder().address("vm001:9009").build()) {
            for(int x=0;x<1000;x++) {
                sender.table("tx_table_20220815")
                        .symbol("a_customer", "alice")
                        .symbol("b_customer", "bob")
                        .symbol("currency", "CNY")
                        .doubleColumn("value", random.nextInt(10000)/10d)
                        .atNow();
            }
        }
    }
}
```

##　query
```
/**
 * 同步
 * */
public class Sample002 {

    public static void main(String[] args) throws SQLException {
        Properties properties = new Properties();
        properties.setProperty("user", "admin");
        properties.setProperty("password", "quest");
        properties.setProperty("sslmode", "disable");

        final Connection connection = DriverManager.getConnection(
                "jdbc:postgresql://vm001:8812/qdb", properties);
        try (PreparedStatement preparedStatement = connection.prepareStatement(
                "SELECT * FROM tx_table_20220815;")) {
            try (ResultSet rs = preparedStatement.executeQuery()) {
                while (rs.next()) {
                    System.out.println(rs.getString(1));
                }
            }
        }
        connection.close();
    }
}
```

#ref
[index](https://questdb.io/)
[influxDB LINE PROTOCOL](https://questdb.io/docs/develop/insert-data)
[docker-install](https://questdb.io/docs/get-started/docker)
[tech-detail](https://questdb.io/docs/concept/storage-model)
[o3-detail](https://questdb.io/docs/guides/out-of-order-commit-lag)
