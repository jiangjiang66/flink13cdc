package postgresql;

import com.alibaba.fastjson.JSONObject;
import com.ververica.cdc.connectors.postgres.PostgreSQLSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.source.SourceFunction;
import util.KerberosAuth1;
import util.MyCdcDeserilization;
import util.MyCdcDeserilization2;

import java.util.Properties;

public class PostgresqlCDCStreamApiTest {
    public static void main(String[] args) throws Exception {
        postgresql_cdc();
    }
    public static void postgresql_cdc() throws Exception {
        KerberosAuth1.kerberosAuth(true);

        Properties props = new Properties();
        props.setProperty("scan.startup.mode","initial");

        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        // enable checkpoint
        env.enableCheckpointing(3000);

        // Stream API 写法
        /**
        SourceFunction<String> sourceFunction = PostgreSQLSource.<String>builder()
                .hostname("172.16.43.182")
                .port(5432)
                .database("pgsql")
                .schemaList("public")
                .tableList("public.cdc_test")
                .username("pgsql")
                .password("123456")
                .debeziumProperties(props)
                .decodingPluginName("pgoutput") //默认值decoderbufs，不设置会报文件找不到的错误
                //.deserializer(new JsonDebeziumDeserializationSchema())
                .deserializer(new MyCdcDeserilization())
                .build();
         **/
        SourceFunction<JSONObject> sourceFunction = PostgreSQLSource.<JSONObject>builder()
                .hostname("172.16.43.182")
                .port(5432)
                .database("pgsql")
                .schemaList("public")
                .tableList("public.cdc_test")
                .username("pgsql")
                .password("123456")
                .debeziumProperties(props)
                .decodingPluginName("pgoutput") //默认值decoderbufs，不设置会报文件找不到的错误
                //.deserializer(new JsonDebeziumDeserializationSchema())
                .deserializer(new MyCdcDeserilization2())
                .build();

        env.addSource(sourceFunction,"Postgresql Source").print();

        env.execute("Print Postgresql Snapshot + Binlog");
    }
}
