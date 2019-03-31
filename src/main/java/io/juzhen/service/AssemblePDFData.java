package io.juzhen.service;

import io.juzhen.channel.dto.BaseRspDTO;
import io.juzhen.channel.dto.querymanager.CustinfoQueryDTO;
import io.juzhen.channel.dto.querymanager.SharesinfoQueryDTO;
import io.juzhen.channel.service.QueryManagerService;
import io.juzhen.util.BaseRespUtils;
import io.juzhen.util.JuDataTypeUtils;
import io.juzhen.util.PDFConstant;
import io.juzhen.vo.business.PrintStockVO;

import javax.annotation.Resource;

import java.util.List;
import java.util.Map;

/**
 * Desc:
 * Created by jinx on 2017/9/28.
 */
@SuppressWarnings("all")
public abstract class AssemblePDFData extends AbstractPdfService {

    @Resource
    private QueryManagerService queryManagerService;
    
    /**
     * 根据vo组装pdf相关数据信息
     * @param t 具体的某个vo
     * @return
     */
    protected abstract <T> Object assembleCustinfoQueryDTO(T t);

    @Override
    protected  <T> Map selectData(T t) {
    	CustinfoQueryDTO custinfoQueryDTO= (CustinfoQueryDTO) this.assembleCustinfoQueryDTO(t);
        BaseRspDTO custinfo = queryManagerService.getCustinfo(custinfoQueryDTO);
        Map map  = BaseRespUtils.respMap(custinfo);
        map.put(t.getClass().getSimpleName(),t);
        return map;
    }

}
