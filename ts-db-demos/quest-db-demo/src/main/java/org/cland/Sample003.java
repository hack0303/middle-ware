package org.cland;

import java.sql.*;
import java.util.Properties;

/**
 * 同步
 * */
public class Sample003 {

    public static void main(String[] args) throws SQLException {
        Properties properties = new Properties();
        properties.setProperty("user", "admin");
        properties.setProperty("password", "quest");
        properties.setProperty("sslmode", "disable");

        final Connection connection = DriverManager.getConnection(
                "jdbc:postgresql://vm001:8812/qdb", properties);
        try (PreparedStatement preparedStatement = connection.prepareStatement(
                "SELECT sum(value) FROM tx_table_20220815 where a_customer = 'alice';")) {
            try (ResultSet rs = preparedStatement.executeQuery()) {
                while (rs.next()) {
                    System.out.printf("total transfer is %f CNY %n",rs.getDouble(1));
                }
            }
        }
        connection.close();
    }
}
