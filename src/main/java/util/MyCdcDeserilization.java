package util;

import com.alibaba.fastjson.JSONObject;
import com.ververica.cdc.debezium.DebeziumDeserializationSchema;
import org.apache.flink.api.common.typeinfo.BasicTypeInfo;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.util.Collector;
import org.apache.kafka.connect.data.Field;
import org.apache.kafka.connect.data.Struct;
import org.apache.kafka.connect.source.SourceRecord;

import java.util.List;

/**
 * ============================================================
 * Description ：
 * <p> flink cdc 序列化
 * ============================================================
 * ProjectName ：jlrealtimedatawarehouse
 * Created: [2021/9/14 15:04] by bonc
 * Copyright com.bonc
 * @author bonc
 * ============================================================
 */
public class MyCdcDeserilization implements DebeziumDeserializationSchema<String> {

    /**
     * 同时存在 beforeStruct 跟 afterStruct数据的话，就代表是update的数据
     * 只存在 beforeStruct 就是delete数据
     * 只存在 afterStruct数据 就是insert数据
     * @param record
     * @param collector  {"after":{"age":"33","name":"张三"},"before":{"age":"20","name":"张三"},"type":"update"}
     * @throws Exception
     */
    @Override
    public void deserialize(SourceRecord record, Collector collector) throws Exception {
        Struct dataRecord  =  (Struct)record.value();

        Struct afterStruct = dataRecord.getStruct("after");
        Struct beforeStruct = dataRecord.getStruct("before");

        JSONObject returnJson = new JSONObject();
        //修改后的数据封装
        JSONObject dataAfterJson = new JSONObject();
        //修改前的数据封装
        JSONObject dataBeforeJson = new JSONObject();


        List<Field> fieldsList = null;
        List<Field> fieldsAfterList = null;
        List<Field> fieldsBeforeList = null;
        //修改数据
        if(afterStruct !=null && beforeStruct !=null){

            fieldsAfterList = afterStruct.schema().fields();
            fieldsBeforeList = beforeStruct.schema().fields();

            for (Field field : fieldsAfterList) {
                String fieldName = field.name();
                Object fieldValue = afterStruct.get(fieldName);
                dataAfterJson.put(fieldName,fieldValue);
                returnJson.put("after",dataAfterJson);
            }
            for (Field field : fieldsBeforeList) {
                String fieldName = field.name();
                Object fieldValue = beforeStruct.get(fieldName);
                dataBeforeJson.put(fieldName,fieldValue);
                returnJson.put("before",dataBeforeJson);
            }
            returnJson.put("type","update");
            //新增数据
        }else if (afterStruct !=null){

            fieldsList = afterStruct.schema().fields();
            for (Field field : fieldsList) {
                String fieldName = field.name();
                Object fieldValue = afterStruct.get(fieldName);
                dataAfterJson.put(fieldName,fieldValue);
                returnJson.put("after",dataAfterJson);
            }
            returnJson.put("type","create");
            //删除数据
        }else if (beforeStruct !=null){
            fieldsList = beforeStruct.schema().fields();
            for (Field field : fieldsList) {
                String fieldName = field.name();
                Object fieldValue = beforeStruct.get(fieldName);
                dataAfterJson.put(fieldName,fieldValue);
                returnJson.put("after",dataAfterJson);
            }
            returnJson.put("type","delete");
        }

        collector.collect(returnJson);

    }

    @Override
    public TypeInformation getProducedType() {
        return BasicTypeInfo.of(JSONObject.class);
    }
}

/**
 * {"after":{"sex":"男","name":"小明","id":15,"age":19},"type":"create"}
 * {"after":{"sex":"男","name":"小李","id":31,"age":20},"type":"create"}
 * {"after":{"sex":"小张","name":"8","id":3,"age":3},"type":"create"}
 */