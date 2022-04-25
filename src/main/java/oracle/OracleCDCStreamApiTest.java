package oracle;

import com.alibaba.fastjson.JSONObject;
import com.ververica.cdc.connectors.oracle.OracleSource;
import com.ververica.cdc.debezium.JsonDebeziumDeserializationSchema;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.source.SourceFunction;
import util.KerberosAuth1;
import util.MyCdcDeserilization;
import util.MyCdcDeserilization2;

import java.util.Properties;

public class OracleCDCStreamApiTest {
    public static void main(String[] args) throws Exception {
        oracle_cdc();
    }
    public static void oracle_cdc() throws Exception {
        KerberosAuth1.kerberosAuth(true);

        Properties properties = new Properties();
        properties.setProperty("debezium.database.tablename.case.insensitive", "false");
        properties.setProperty("debezium.log.mining.strategy", "online_catalog");
        properties.setProperty("debezium.log.mining.continuous.mine", "true");
        //properties.setProperty("scan.startup.mode", "latest-offset");
        // 默认是initial，不写也行
        properties.setProperty("scan.startup.mode","initial");
        //properties.setProperty("debezium.snapshot.mode", "latest-offset");

        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        // enable checkpoint
        env.enableCheckpointing(3000);

        // Stream API 写法
        /**
         SourceFunction<String> sourceFunction = OracleSource.<String>builder()
                 .hostname("172.16.43.182")
                 .port(1521)
                 .database("helowin")
                 .schemaList("SCOTT")
                 .tableList("SCOTT.DEPT")
                 .username("system")
                 .password("system")
                 .debeziumProperties(properties)
                 //.deserializer(new JsonDebeziumDeserializationSchema())
                 .deserializer(new MyCdcDeserilization())
                 .build();
         **/
        SourceFunction<JSONObject> sourceFunction = OracleSource.<JSONObject>builder()
                .hostname("172.16.43.182")
                .port(1521)
                .database("helowin")
                .schemaList("SCOTT")
                .tableList("SCOTT.DEPT")
                .username("system")
                .password("system")
                .debeziumProperties(properties)
                //.deserializer(new JsonDebeziumDeserializationSchema())
                .deserializer(new MyCdcDeserilization2())
                .build();


         env.addSource(sourceFunction,"ORACLE Source").print();

        env.execute("Print ORACLE Snapshot + Binlog");
    }

}
