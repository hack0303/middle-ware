package org.cloud;


import lombok.SneakyThrows;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.*;
import org.apache.hadoop.hbase.client.*;

import java.util.Random;

public class Bootstart {

    public static Configuration configuration;

    @SneakyThrows
    public static void main(String[] args){
        configuration = HBaseConfiguration.create();
        configuration.set("hbase.zookeeper.quorum", "node01");
        final Connection connection= ConnectionFactory.createConnection(configuration);
        final Admin admin=connection.getAdmin();
        TableName tableName=TableName.valueOf("tableA");
        final String colFamily="col1";
        TableDescriptor  tableDescriptor= TableDescriptorBuilder.newBuilder(tableName).setColumnFamily(ColumnFamilyDescriptorBuilder.newBuilder(colFamily.getBytes()).build()).build();
        admin.createTable(tableDescriptor);
        admin.close();
        final Table table=connection.getTable(tableName);
        for(int row=0;row<1000;row++) {
            final Put put = new Put(colFamily.getBytes());
            put.addColumn(colFamily.getBytes(),"a".getBytes(), (new Random().nextInt()+"").getBytes());
            table.put(put);
        }
        Get get = new Get("1".getBytes());
        get.addColumn(colFamily.getBytes(),"a".getBytes());
        Result result =table.get(get);
        result.getColumnCells(colFamily.getBytes(),"a".getBytes()).forEach(e->{
            System.out.printf("%s，%s:%s is %s%n",table.getName().getNameAsString(),new String(e.getFamilyArray()),new String(e.getQualifierArray()),new String(e.getValueArray()));
        });
        table.close();
        connection.close();
    }
}
