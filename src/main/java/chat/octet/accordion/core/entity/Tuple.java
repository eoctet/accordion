package chat.octet.accordion.core.entity;


import chat.octet.accordion.core.enums.DataType;
import chat.octet.accordion.core.handler.DataTypeConvert;
import chat.octet.accordion.utils.JsonUtils;
import com.google.common.collect.Maps;

import javax.annotation.Nullable;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Tuple<K, V> extends ConcurrentHashMap<K, V> implements Serializable {

    public Long getLong(String key) {
        return DataTypeConvert.getValue(DataType.LONG, this.get(key));
    }

    public Integer getInt(String key) {
        return DataTypeConvert.getValue(DataType.INT, this.get(key));
    }

    public Double getDouble(String key) {
        return DataTypeConvert.getValue(DataType.DOUBLE, this.get(key));
    }

    public String getString(String key) {
        return DataTypeConvert.getValue(DataType.STRING, this.get(key));
    }

    public Date getDate(String key) {
        return DataTypeConvert.getValue(DataType.DATETIME, this.get(key));
    }

    public BigDecimal getDecimal(String key) {
        return DataTypeConvert.getValue(DataType.DECIMAL, this.get(key));
    }

    public boolean getBoolean(String key) {
        return DataTypeConvert.getValue(DataType.BOOLEAN, this.get(key));
    }

    public <T> T getObject(String key, @Nullable Class<T> clazz) {
        return JsonUtils.parseToObject(String.valueOf(this.get(key)), clazz);
    }

    public <E> LinkedList<E> getList(String key, @Nullable Class<E> clazz) {
        return JsonUtils.parseJsonToList(String.valueOf(this.get(key)), clazz);
    }

    public Map<K, V> toMap() {
        return Maps.newHashMap(this);
    }

    @Override
    public String toString() {
        return JsonUtils.toJson(this);
    }

}
