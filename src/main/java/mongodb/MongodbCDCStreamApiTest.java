package mongodb;

import com.ververica.cdc.connectors.mongodb.MongoDBSource;
import com.ververica.cdc.debezium.JsonDebeziumDeserializationSchema;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.source.SourceFunction;
import util.KerberosAuth1;

import java.util.Properties;

public class MongodbCDCStreamApiTest {
    public static void main(String[] args) throws Exception {
        mongodb_cdc();
    }
    public static void mongodb_cdc() throws Exception {
        KerberosAuth1.kerberosAuth(true);

        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        // enable checkpoint
        env.enableCheckpointing(3000);

        // Stream API 写法
        SourceFunction<String> sourceFunction = MongoDBSource.<String>builder()
                .hosts("172.16.43.182:27018")
                .username("test")
                .password("123456")
                .databaseList("admin")
                .collectionList("book")
                .deserializer(new JsonDebeziumDeserializationSchema())
                .build();

//        SourceFunction<String> sourceFunction = MongoDBSource.<String>builder()
//                .hosts("172.16.43.182:27018")
//                .databaseList("")
//                .collectionList("")
//                //.pipeline("[{'$match': {'ns.db': {'$regex': '/^(sandbox|firewall)$/'}}}]")
//                .deserializer(new JsonDebeziumDeserializationSchema())
//                .build();


        env.addSource(sourceFunction,"MongoDB Source").print();

        env.execute("Print MongoDB Snapshot + Binlog");
    }
}
