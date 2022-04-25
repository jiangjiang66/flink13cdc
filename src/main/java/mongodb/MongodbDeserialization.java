package mongodb;

import com.alibaba.fastjson.JSONObject;
import com.ververica.cdc.debezium.DebeziumDeserializationSchema;
import org.apache.flink.api.common.typeinfo.BasicTypeInfo;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.util.Collector;
import org.apache.kafka.connect.json.JsonConverter;
import org.apache.kafka.connect.source.SourceRecord;
import org.apache.kafka.connect.storage.ConverterType;

import java.util.HashMap;
import java.util.Map;

public class MongodbDeserialization implements DebeziumDeserializationSchema<String> {
    private static final long serialVersionUID = 1L;
    private transient JsonConverter jsonConverter;
    private final Boolean includeSchema;
    private Map<String, Object> customConverterConfigs;

    public MongodbDeserialization() {
        this(false);
    }

    public MongodbDeserialization(Boolean includeSchema) {
        this.includeSchema = includeSchema;
    }

    public MongodbDeserialization(Boolean includeSchema, Map<String, Object> customConverterConfigs) {
        this.includeSchema = includeSchema;
        this.customConverterConfigs = customConverterConfigs;
    }

    @Override
    public void deserialize(SourceRecord record, Collector<String> out) throws Exception {
        if (this.jsonConverter == null) {
            this.initializeJsonConverter();
        }

        byte[] bytes = this.jsonConverter.fromConnectData(record.topic(), record.valueSchema(), record.value());
        String s = new String(bytes);
        JSONObject jsonObject = JSONObject.parseObject(s);
        String fullDocument = jsonObject.getString("fullDocument");
        String s1 = fullDocument.replaceAll(" \\{\"\\$oid\": ", "").replaceAll("\\},", ",");
        // 去掉_id
        StringBuffer stringBuffer = new StringBuffer("{");
        String[] split = s1.split(",");
        for (int i = 1; i < split.length; i++) {
            stringBuffer.append(split[i]);
            if (i != split.length -1){
                stringBuffer.append(",");
            }
        }

        out.collect(stringBuffer.toString());
    }

    private void initializeJsonConverter() {
        this.jsonConverter = new JsonConverter();
        HashMap<String, Object> configs = new HashMap(2);
        configs.put("converter.type", ConverterType.VALUE.getName());
        configs.put("schemas.enable", this.includeSchema);
        if (this.customConverterConfigs != null) {
            configs.putAll(this.customConverterConfigs);
        }

        this.jsonConverter.configure(configs);
    }

    @Override
    public TypeInformation<String> getProducedType() {
        return BasicTypeInfo.STRING_TYPE_INFO;
    }

}
