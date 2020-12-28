package io.juzhen.service;

import com.alibaba.fastjson.JSON;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Desc:
 * Created by xangshang on 2017/9/28.
 */
@SuppressWarnings("all")
public abstract class AssemblePDFData extends AbstractPdfService {
    
    /**
     * 根据vo组装pdf相关数据信息
     * @param t 具体的某个vo
     * @return
     */
    protected abstract <T> Object assembleCustinfoQueryDTO(T t);

    @Override
    protected  <T> Map selectData(T t) {
    	Object custinfoQueryDTO = this.assembleCustinfoQueryDTO(t);
    	if (Objects.isNull(custinfoQueryDTO)) {
    	    return new HashMap();
        }
        Map map  = JSON.parseObject(JSON.toJSONString(custinfoQueryDTO), Map.class);
        map.put(t.getClass().getSimpleName(),t);
        return map;
    }

}
