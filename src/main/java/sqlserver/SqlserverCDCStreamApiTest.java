package sqlserver;

import com.alibaba.fastjson.JSONObject;
import com.ververica.cdc.connectors.sqlserver.SqlServerSource;
import com.ververica.cdc.debezium.JsonDebeziumDeserializationSchema;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.source.SourceFunction;
import util.KerberosAuth1;
import util.MyCdcDeserilization2;

import java.util.Properties;

public class SqlserverCDCStreamApiTest {
    public static void main(String[] args) throws Exception {
        sqlserver_cdc();
    }
    public static void sqlserver_cdc() throws Exception {
        KerberosAuth1.kerberosAuth(true);

//        Properties properties = new Properties();
//        properties.setProperty("debezium.database.tablename.case.insensitive", "false");
//        properties.setProperty("debezium.log.mining.strategy", "online_catalog");
//        properties.setProperty("debezium.log.mining.continuous.mine", "true");
//        properties.setProperty("scan.startup.mode", "latest-offset");
        //properties.setProperty("debezium.snapshot.mode", "latest-offset");

        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        // enable checkpoint
        env.enableCheckpointing(3000);

        Properties props = new Properties();
        props.setProperty("scan.startup.mode","initial");
        // Stream API 写法
        /**
        SourceFunction<String> sourceFunction = SqlServerSource.<String>builder()
                .hostname("172.16.43.182")
                .port(1433)
                .database("flinkcdc")
                .tableList("dbo.student")
                .username("SA")
                .password("!QAZxsw2")
                .debeziumProperties(props)
                //.deserializer(new JsonDebeziumDeserializationSchema())
                .deserializer(new MyCdcDeserilization())
                .build();
         **/
        SourceFunction<JSONObject> sourceFunction = SqlServerSource.<JSONObject>builder()
                .hostname("172.16.43.182")
                .port(1433)
                .database("flinkcdc")
                .tableList("dbo.student")
                .username("SA")
                .password("!QAZxsw2")
                .debeziumProperties(props)
                //.deserializer(new JsonDebeziumDeserializationSchema())
                .deserializer(new MyCdcDeserilization2())
                .build();

        //env.fromSource(sourceFunction, WatermarkStrategy.noWatermarks(), "MySQL Source")
        env.addSource(sourceFunction,"Sqlserver Source").print();

        env.execute("Print Sqlserver Snapshot + Binlog");
    }
}
