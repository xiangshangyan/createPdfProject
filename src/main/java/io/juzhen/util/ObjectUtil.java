package io.juzhen.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;

import io.juzhen.utils.JsonUtils;
@SuppressWarnings("all")
public class ObjectUtil {

	/**
	 * 转换json到对象
	 * @param jsonStr
	 * @param clazz
	 * @return
	 */
	public static <T> T convertJsonToBean(String jsonStr, Class<T> clazz) {
		T object = JsonUtils.readValue(jsonStr, clazz);
		return object;
	}

	/**
	 * 转换map到对象
	 * @param map
	 * @param clazz
	 * @return
	 */
	public static <T> T convertMapToBean(Map map, Class<T> clazz) {
		T object = JsonUtils.readValue(JsonUtils.toJson(map), clazz);
		return object;
	}
	
	/**
	 * 转换bena到Map
	 * @param <T>
	 * @param bean
	 * @return
	 */
	public static <T> Map convertBeanToMap(T bean) {
		Map map = JsonUtils.readValue(JsonUtils.toJson(bean), Map.class);
		return map;
	}
	
	/**
	 * 转换List<Map>到List<Object>
	 * @param list
	 * @param clazz
	 * @return
	 */
	public static <T> List<T> convertMapListToBeanList(List<T> list, Class<T> clazz) {
		if (null == list){
			return null;
		}
		List<T> retList = new ArrayList<>();
		for (T obj : list) {
			String jsonStr = JsonUtils.toJson(obj);
			retList.add(JsonUtils.readValue(jsonStr, clazz));
		}
		return retList;
	}
	
	/**
	 * 转换传入的map到指定的map（key值自定义 如：certificatetype->certType）
	 * @param map 
	 * @param clazz
	 * @return
	 */
	public static Map convertMap(Map map, Class<?> clazz) {
		Object object = JsonUtils.readValue(JsonUtils.toJson(map), clazz);
		Map result = JsonUtils.readValue(JSON.toJSONString(object,SerializerFeature.WriteMapNullValue,SerializerFeature.WriteNullNumberAsZero),Map.class);
		return result;
	}
	
	/**
	 * 转换传入的List<map>到指定的List<map> （map的key值自定义 如：certificatetype->certType）
	 * @param mapList
	 * @param clazz
	 * @return
	 */
	public static List<Map> convertListMap(List<Map> mapList, Class<?> clazz) {
		if (CollectionUtils.isEmpty(mapList)){
			return null;
		}
		List<Map> retMap = new ArrayList<>();
		for (Map map: mapList) {
			retMap.add(convertMap(map, clazz));
		}
		return retMap;
	}

}
