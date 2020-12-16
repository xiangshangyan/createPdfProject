package io.juzhen.service;

import io.juzhen.base.dto.BaseResp;
import io.juzhen.channel.dto.BaseRspDTO;
import io.juzhen.vo.BaseVo;
import io.juzhen.vo.business.*;
import io.juzhen.vo.user.web.QueryCitiesVo;

import java.io.InputStream;

/**
 * @author niujsj
 * 业务处理service
 */
@SuppressWarnings("all")
public interface BusinessHandleService {
	
	/**
	 * 处理非交易过户流程
	 * @param stockFlowVo
	 * @return
	 */
	BaseResp transferStockFlow(TransferStockFlowVo stockFlowVo);
	
	/**
	 * 用户提交冻结自己所持有股权信息申请
	 * @param frostStockFlowVo
	 * @return
	 */
	BaseResp frostStockFlow(FrostStockFlowVo frostStockFlowVo);
	
	/**
	 * 用户质押冻结股权信息第二流程提交
	 * @param frostStockFlowVo
	 * @return
	 */
	BaseResp frostStockTwoFlow(FrostStockTwoFlow frostStockTwoFlow);
	
	/**
	 * 用户解冻自己之前冻结股权信息
	 * @param unfreezeStockFlowVo
	 * @return
	 */
	BaseResp unfreezeStockFlow(UnfreezeStockFlowVo unfreezeStockFlowVo);
	
	
	/**
	 * 用户页面查询枚举
	 * @param enumVo
	 * @return
	 */
	BaseResp queryEnum(QueryEnumVo enumVo);
	
	/**
	 * 查询冻结流水
	 * @param forstStreamVo
	 * @return
	 */
	BaseResp queryForstStream(QueryForstStreamVo forstStreamVo);

	/**
	 * 企业信息登记提交流程请求（包括产品信息）
	 * @param registerFundFlowVo
	 * @return
	 */
	BaseResp registerFundFlow(RegisterFundFlowVo registerFundFlowVo);

	/**
	 * 5.1.2.份额调整提交流程请求接口
	 * @param adjustStockFlowVo
	 * @return
	 */
	BaseResp adjustStockFlow(AdjustStockFlowVo adjustStockFlowVo);

	/**
	 * 5.1.3.股权性质变更提交流程请求接口
	 * @param transferNatureFlowVo
	 * @return
	 */
	BaseResp transferNatureFlow(TransferNatureFlowVo transferNatureFlowVo);

	/**
	 * 用户调整自身份额信息
	 * @param adjustStockVo
	 * @return
	 */
	BaseResp adjustStock(AdjustStockVo adjustStockVo);
	
	/**
	 * 股权性质变更
	 * @param transferNatureVo
	 * @return
	 */
	BaseResp transferNature(TransferNatureVo transferNatureVo);
	
	/**
	 * 用户冻结自己所持有股权信息
	 * @param FrostStock
	 * @return
	 */
	BaseResp frostStock(FrostStockVo frostStockVo);
	
	/**
	 * 户修改股东信息流程提交接口
	 * @param modifyHolderFlowVo
	 * @return
	 */
	BaseResp modifyHolderFlow(ModifyHolderFlowVo modifyHolderFlowVo);

	/**
	 * 解冻份额
	 * @param unfreezeStockVo
	 * @return
	 */
	BaseResp unfreezeStock(UnfreezeStockVo unfreezeStockVo);

	/**

	 * 实现非交易过户业务
	 * @param transferStockVo
	 * @return
	 */
	BaseResp transferStock(TransferStockVo transferStockVo);

	/**
	 * 用户登记产品和企业信息
	 * @param registerFundVo
	 * @return
	 */
	BaseResp registerFund(RegisterFundVo registerFundVo);

	/**
	 * 用户修改企业信息接口
	 * @param modifyFundVo
	 * @return
	 */
	BaseResp modifyFund(ModifyFundVo modifyFundVo);

	/**
	 * 中心人员维护产品类型接口
	 * @param defendProductTypeVo
	 * @return
	 */
	BaseResp defendProductType(DefendProductTypeVo defendProductTypeVo);
	
	/**
	 * 中心人员维护产品下属业务接口
	 * @param defendBusinessVo
	 * @return
	 */
	BaseResp defendBusiness(DefendBusinessVo defendBusinessVo);

	/**
	 * 查询产品类型
	 * @param fundTypeVo
	 * @return
	 */
	BaseResp queryFundType(FundTypeVo fundTypeVo);

	/**
	 * 查询资金账户信息
	 * @param capitalInfoVo
	 * @return
	 */
	BaseResp queryCapitalInfo(CapitalInfoVo capitalInfoVo);

	/**
	 * 查询产品信息
	 * @param productInfoVo
	 * @return
	 */
	BaseResp queryProductInfo(ProductInfoVo productInfoVo);

	/**
	 * 5.3.1.查询企业持有人名册信息
	 * @param queryStockholdersVo
	 * @return
	 */
	BaseResp queryStockholders(QueryStockholdersVo queryStockholdersVo);
	
	/**
	 * 5.3.2.处理持有人名册导入信息
	 * @param processStockholdersVo
	 * @return
	 */
	BaseResp processStockholders(ProcessStockholdersVo processStockholdersVo);
	
	/**
	 * 5.3.3.导入持有人名册信息
	 * @param inStockholdersVo
	 * @return
	 */
	BaseResp inStockholders(InStockholdersVo inStockholdersVo);
	
	/**
	 * 5.3.4.持有人名册信息导入后查询
	 * @param queryInStockholdersVo
	 * @return
	 */
	BaseResp queryInStockholders(QueryInStockholdersVo queryInStockholdersVo);

	/**
	 * 查询省份
	 * @return
	 */
	BaseResp queryProvinces();

	/**
	 * 查询省份下的城市
	 * @param citiesVo
	 * @return
	 */
	BaseResp queryCities(QueryCitiesVo citiesVo);

	/**
	 * 存储风控资料接口。
	 * @param saveRiskFileVo
	 * @return
	 */
	BaseResp saveRiskFile(SaveRiskFileVo saveRiskFileVo);
	
	
	/**
	 * 流程提交
	 * @param creator
	 * @param source
	 * @param processKey
	 * @param obj
	 * @return
	 */
	public BaseRspDTO getPrpcessStartRsp(String creator, String source,
			String processKey, Object obj,String fundCode);
	
	/**
	 * 股交文件信息流
	 * @param outStockholdersVo
	 * @return
	 */
	InputStream outStockholders(OutStockholdersVo outStockholdersVo);
	
	/**
	 * 下载文件
	 * @param downLoadVo
	 * @return
	 */
	InputStream downloadFile(DownLoadVo downLoadVo);
	
	/**
	 * 更新表单数据
	 * @param updateProcessVo
	 * @return
	 */
	BaseResp updateProcess(UpdateProcessVo updateProcessVo);

	/**
	 * 查询业务类型代码接口
	 * @param baseVo
	 * @return
	 */
	BaseResp custLogCode(BaseVo baseVo);

	/**
	 * 托管中心柜员投资者用户查询登记企业标识
	 * @param companyFlagVo
	 * @return
	 */
	BaseResp queryCompanyFlag(QueryCompanyFlagVo companyFlagVo);

    /**
     * 修改登记企业标识信息
     * @param modifyCompanyFlagVO
     * @return
     */
    BaseResp modifyCompanyFlag(ModifyCompanyFlagVO modifyCompanyFlagVO);

}
