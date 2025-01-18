package org.cland;

import io.questdb.client.Sender;

import java.sql.*;
import java.util.Properties;

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
