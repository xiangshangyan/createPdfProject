package io.juzhen.util;

import com.alibaba.fastjson.JSON;
import io.juzhen.base.dto.BaseResp;
import io.juzhen.base.dto.Data;
import io.juzhen.channel.dto.BaseRspDTO;
import io.juzhen.utils.JsonUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("all")
public class BaseRespUtils {

    private BaseRespUtils() {
    }

    /**
     * 转换BaseRspDTO对象
     *
     * @param respDTO
     * @return BaseResp
     */
    public static BaseResp convertBaseRspDTO2BaseResp(BaseRspDTO respDTO) {
        if (null == respDTO) {
            return new BaseResp(500, "系统异常");
        }
        BaseResp resp = new BaseResp();
        Integer total = 0;
        if (respDTO.getData() != null && respDTO.getData() instanceof List<?>) {
            List<?> list = (List<?>) respDTO.getData();
            if(list.size() > 0){
            	Map<String,Object> map = (Map)list.get(0);
            	if(map.containsKey("ROWSCOUNT")){
            		Object o = map.get("ROWSCOUNT");
            		if(o instanceof String){
            			total = Integer.parseInt((String)map.get("ROWSCOUNT"));
            		} else if(o instanceof Integer){
            			total = (Integer) map.get("ROWSCOUNT");
            		}
            	}
            }
            Data data = null;
            if(total != 0){
            	data = new Data(total, list);
            } else {
            	data = new Data(list.size(), list);
            }
            resp.setData(data);
        } else {
            resp = new BaseResp(respDTO.getData());
        }
        resp.setRet(respDTO.getCode());
        resp.setMessage(0 == respDTO.getCode() ? "成功" : respDTO.getMessage());
        return resp;
    }


    public static BaseResp convertBaseRspDTO2BaseResp(BaseRspDTO respDTO, Class<?> clazz) {
        if (null == respDTO) {
            return new BaseResp(500, "系统异常");
        }
        BaseResp resp = new BaseResp();
        Integer total = 0;
        if (respDTO.getData() != null && (respDTO.getData() instanceof List<?> || respDTO.getData() instanceof Map)) {
            List<?> list = null;
            if (respDTO.getData() instanceof Map) {
                list = (List<?>) ((Map) respDTO.getData()).get("content");
                if(((Map) respDTO.getData()).containsKey("totalElements")){
                	total = (Integer) ((Map) respDTO.getData()).get("totalElements");
                }
            } else {
                list = (List<?>) respDTO.getData();
                if(list.size() > 0){
                	Map<String,Object> map = (Map)list.get(0);
                	if(map.containsKey("ROWSCOUNT")){
                		Object o = map.get("ROWSCOUNT");
                		if(o instanceof String){
                			total = Integer.parseInt((String)map.get("ROWSCOUNT"));
                		} else if(o instanceof Integer){
                			total = (Integer) map.get("ROWSCOUNT");
                		}
                	}
                }
            }
            
            List<Object> retList = new ArrayList<>();
            for (Object obj : list) {
            	System.out.println(JsonUtils.toJson(obj));
                Object object = JsonUtils.readValue(JsonUtils.toJson(obj), clazz);
                Map map = JsonUtils.readValue(JSON.toJSON(object).toString(), Map.class);
                retList.add(map);
            }
            Data data = null;
            if(total != 0){
            	data = new Data(total, retList);
            } else {
            	data = new Data(retList.size(), retList);
            }
            
            resp.setData(data);
        } else {
            resp = new BaseResp(respDTO.getData());
        }
        resp.setRet(respDTO.getCode());
        resp.setMessage(0 == respDTO.getCode() ? "成功" : respDTO.getMessage());
        return resp;
    }


    /**
     * 转换BaseRspDTO对象
     *
     * @param str
     * @return BaseResp
     */
    public static BaseResp convertStringDTO2BaseResp(String str) {
        if (null == str) {
            return new BaseResp(500, "系统异常");
        }
        BaseResp resp = new BaseResp();
        Map<String, Object> map = JsonUtils.readValue(str, Map.class);
        if (map.get("data") != null && map.get("data") instanceof List<?>) {
            List<?> list = (List<?>) map.get("data");
            Data data = new Data(list.size(), list);
            resp.setData(data);
        } else {
            resp = new BaseResp(map.get("data"));
        }
        resp.setRet((int) map.get("code"));
        resp.setMessage(0 == (int) map.get("code") ? "成功" : map.get("message").toString());
        return resp;
    }


    /**
     * 把返回信息组装成Map对象
     * @param baseRspDTO
     * @return
     */
    public static Map<String, Object> respMap(BaseRspDTO baseRspDTO) {
        Map<String,Object> resMap = new HashMap<>();//保存股交返回信息
        // 查询成功则返回数据
        if (JuDataTypeUtils.checkDataCode(baseRspDTO)) {
            if (baseRspDTO.getData() == null ) {
                return resMap;
            }
            List list = (List)baseRspDTO.getData();
            if (list.size() == 0) {
                return resMap;
            }
            resMap = JsonUtils.readValue(JsonUtils.toJson(list.get(0)), Map.class);
        }
        return resMap;
    }

    public static void main(String[] args) {
        if (1 > 0 && (2 > 0 || 3 < 0)) {
            System.out.println("-------------");
        }
    }

}
