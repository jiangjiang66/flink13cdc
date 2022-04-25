package mysql;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.ververica.cdc.connectors.mysql.source.MySqlSource;
import com.ververica.cdc.debezium.StringDebeziumDeserializationSchema;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import util.KerberosAuth1;
import util.MyCdcDeserilization;
import util.MyCdcDeserilization2;

import java.util.Properties;

public class MysqlCDCStreamApiTest {

    public static void main(String[] args) throws Exception {
        mysql_cdc();
    }
    public static void mysql_cdc() throws Exception {
        KerberosAuth1.kerberosAuth(true);

        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        // enable checkpoint
        env.enableCheckpointing(3000);

        Properties props = new Properties();
        props.setProperty("scan.startup.mode","initial");
        /**
        MySqlSource<String> mySqlSource = MySqlSource.<String>builder()
                .hostname("172.16.43.164")
                .port(3306)
                //.serverTimeZone("convertToNull")
                .databaseList("164testdatax")
                .tableList("164testdatax.user")
                .username("root")
                .password("123")
                .debeziumProperties(props)
                //.deserializer(new StringDebeziumDeserializationSchema())
                .deserializer(new MyCdcDeserilization())
                .build();
        **/

        MySqlSource<JSONObject> mySqlSource = MySqlSource.<JSONObject>builder()
                .hostname("172.16.43.164")
                .port(3306)
                //.serverTimeZone("convertToNull")
                .databaseList("164testdatax")
                .tableList("164testdatax.user")
                .username("root")
                .password("123")
                .debeziumProperties(props)
                .deserializer(new MyCdcDeserilization2())
                .build();

        env
                .fromSource(mySqlSource, WatermarkStrategy.noWatermarks(), "MySQL Source")
                .setParallelism(1)
                .print().setParallelism(1);

        env.execute("Print MySQL Snapshot + Binlog");
    }

}
