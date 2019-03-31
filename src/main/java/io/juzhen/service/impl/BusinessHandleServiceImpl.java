package io.juzhen.service.impl;

import io.juzhen.base.dto.BaseResp;
import io.juzhen.base.dto.Data;
import io.juzhen.base.exception.ServiceException;
import io.juzhen.channel.dto.BaseRspDTO;
import io.juzhen.channel.dto.busniess.*;
import io.juzhen.channel.dto.file.RiskAddDTO;
import io.juzhen.channel.dto.process.ProcessStartDTO;
import io.juzhen.channel.dto.process.ProcessStartResData;
import io.juzhen.channel.dto.process.ProcessTasksDTO;
import io.juzhen.channel.dto.querymanager.*;
import io.juzhen.channel.service.BusinessManagerService;
import io.juzhen.channel.service.FileService;
import io.juzhen.channel.service.ProcessService;
import io.juzhen.channel.service.QueryManagerService;
import io.juzhen.dao.*;
import io.juzhen.dto.*;
import io.juzhen.dto.business.QueryEnumRespDTO;
import io.juzhen.dto.business.QueryForstStreamResp;
import io.juzhen.dto.business.QueryFundTypeRespDTO;
import io.juzhen.dto.business.QueryProductInfoResp;
import io.juzhen.dto.business.web.QueryCitiesRespDTO;
import io.juzhen.dto.business.web.QueryInStockHoldersRespDTO;
import io.juzhen.dto.business.web.QueryProvincesRespDTO;
import io.juzhen.dto.business.web.QueryStockHoldersRespDTO;
import io.juzhen.po.LibInvestorUser;
import io.juzhen.service.BusinessHandleService;
import io.juzhen.util.BaseRespUtils;
import io.juzhen.util.JuDataTypeUtils;
import io.juzhen.util.ObjectUtil;
import io.juzhen.utils.JsonUtils;
import io.juzhen.vo.BaseVo;
import io.juzhen.vo.business.*;
import io.juzhen.vo.user.ModifyCustMajorFlowVo;
import io.juzhen.vo.user.web.QueryCitiesVo;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.*;

@SuppressWarnings("all")
@Service
public class BusinessHandleServiceImpl extends AbstractService implements BusinessHandleService {

    @Autowired
    private QueryManagerService queryManagerService;
    @Autowired
    private ProcessService processService;
    @Autowired
    private UserDao userDao;
    @Autowired
    private BusinessManagerService businessManagerService;
    @Autowired
    private AdjustStockDao adjustStockDao;
    @Autowired
    private ProductDao productDao;
    @Autowired
    private FrostStockDao frostStockDao;
    @Autowired
    private TransferStockNatureDao transferStockNatureDao;
    @Autowired
    private UnFreezeStockDao unFreezeStockDao;
    @Autowired
    private FileService fileService;
    @Autowired
    private TransferOwnershipDao transferOwnershipDao;

    /**
     * 非交易过户流程提交
     */
    @Override
    public BaseResp transferStockFlow(TransferStockFlowVo stockFlowVo) {
        BaseRspDTO startRes = this.getPrpcessStartRsp(stockFlowVo.getCreator(), stockFlowVo.getSource(),
                stockFlowVo.getProcessDefinitionKey(), stockFlowVo, stockFlowVo.getFundCode());

        // 如果是PCI ，则需要插入数据到合约
        if (JuDataTypeUtils.checkDataCode(startRes)) {
            if (stockFlowVo.getSource().equalsIgnoreCase("PCI")) {
                TransferOwnershipDTO transferOwnershipDTO = new TransferOwnershipDTO();
                transferOwnershipDTO.setFileIdList(stockFlowVo.getFileIds());
                transferOwnershipDTO.setFilePathList(stockFlowVo.getFilePaths());
                transferOwnershipDTO.setFundCode(Integer.valueOf(stockFlowVo.getFundCode()));
                transferOwnershipDTO.setFundName(stockFlowVo.getFundName());
                transferOwnershipDTO.setInAccountId(stockFlowVo.getInAccount());
                transferOwnershipDTO.setInStockNature(Integer.parseInt(stockFlowVo.getStockNatureBefore()));
                transferOwnershipDTO.setOrderId(getProssId(startRes));// 流水id
                transferOwnershipDTO.setOutAccountId(stockFlowVo.getOutAccount());// otcp返回的流水id
                transferOwnershipDTO.setInAccountName(stockFlowVo.getInAccountName());// 过入账户名称
                transferOwnershipDTO.setOutAccountName(stockFlowVo.getOutAccountName());// 过出账户名称
                transferOwnershipDTO.setOutNum(String.valueOf(stockFlowVo.getOutNum()));
                transferOwnershipDTO.setOutStockNature(String.valueOf(stockFlowVo.getStockNatureBefore()));// 源股份性质
                transferOwnershipDTO.setRemark(JuDataTypeUtils.getString(stockFlowVo.getRemark()));
                transferOwnershipDTO.setStatus(1);// 未受理
                transferOwnershipDTO.setTransferType(String.valueOf(stockFlowVo.getTransferType()));
                // 插入合约数据
                boolean insertTransferOwnershipInfo = transferOwnershipDao.insertTransferOwnershipInfo(transferOwnershipDTO);
            }
        }
        // TODO: 2017/9/27 取不到userId
        // 发送消息参数
//        ProcessStartResData readValue = JsonUtils.readValue(JsonUtils.toJson(startRes.getData()), ProcessStartResData.class);
//        String date = DateTimeUtil.dateTimeToStr(new Date(), DateTimeUtil.STR_DATETIME_PATTERN);
//        Object[] objects = {date, stockFlowVo.getCreator(), ProcessDefinitionEnums.getName(stockFlowVo.getProcessDefinitionKey()), readValue.getId()};
//
//        if (0 == startRes.getCode()) {
//            // 业务提交
//            this.sendMessage("" ,"key14", "key14", 000, objects);
//        }
        return BaseRespUtils.convertBaseRspDTO2BaseResp(startRes);
    }

    /**
     * 冻结份额流程提交
     */
    @Override
    public BaseResp frostStockFlow(FrostStockFlowVo frostStockFlowVo) {
        BaseRspDTO startRes = getPrpcessStartRsp(frostStockFlowVo.getCreator(),
                frostStockFlowVo.getSource(),
                frostStockFlowVo.getProcessDefinitionKey(), frostStockFlowVo, frostStockFlowVo.getFundCode());
        // 如果是PCI ，则需要插入数据到合约
        if (JuDataTypeUtils.checkDataCode(startRes)) {
            if (frostStockFlowVo.getSource().equalsIgnoreCase("PCI")) {
                FrostStockDTO frostStockDTO = new FrostStockDTO();
                frostStockDTO.setAccountId(frostStockFlowVo.getAccount());
                frostStockDTO.setCreTime(frostStockFlowVo.getTimestamp());// 创建日期？？？？
                frostStockDTO.setFileIdList(frostStockFlowVo.getFileIds());
                frostStockDTO.setFilePathList(frostStockFlowVo.getFilePaths());
                frostStockDTO.setFrostDate(frostStockFlowVo.getTimestamp());// 冻结日期
//                frostStockDTO.setFrostNum(Integer.parseInt(frostStockFlowVo.getFrostNum()));
                frostStockDTO.setFrostType(Integer.valueOf(frostStockFlowVo
                        .getFrostType()));
                frostStockDTO.setFundCode(Integer.valueOf(frostStockFlowVo
                        .getFundCode()));
                frostStockDTO.setFundName(frostStockFlowVo.getFundName());
                frostStockDTO.setOrderId(getProssId(startRes));
                frostStockDTO.setRemark(JuDataTypeUtils
                        .getString(frostStockFlowVo.getRemark()));
                frostStockDTO.setStatus(1);// 提交
                frostStockDTO.setStockNature(frostStockFlowVo.getStockNature());
                // 插入合约数据
                boolean insertTransferOwnershipInfo = frostStockDao
                        .insertFrostStockInfo(frostStockDTO);
            }
        }
        return BaseRespUtils.convertBaseRspDTO2BaseResp(startRes);
    }

    /**
     * 用户质押冻结股权信息第二流程提交。
     */
    @Override
    public BaseResp frostStockTwoFlow(FrostStockTwoFlow frostStockTwoFlow) {
        ProcessTasksDTO dto = new ProcessTasksDTO();
        dto.setBusinessKey(frostStockTwoFlow.getBusinessKey());// 表单id
        BaseRspDTO tasks = processService.tasks(dto);// 获取任务列表
        ProcessStartResData readValue = JsonUtils.readValue(
                JsonUtils.toJson(tasks.getData()), ProcessStartResData.class);
        BaseRspDTO startRes = getPrpcessStartRsp(frostStockTwoFlow.getCreator(),
                getSource(readValue.getSource()),
                frostStockTwoFlow.getBusinessKey(), frostStockTwoFlow, "");
        return BaseRespUtils.convertBaseRspDTO2BaseResp(startRes);
    }

    /**
     * 解冻份额流程提交
     */
    @Override
    public BaseResp unfreezeStockFlow(UnfreezeStockFlowVo unfreezeStockFlowVo) {
        BaseRspDTO startRes = getPrpcessStartRsp(
                unfreezeStockFlowVo.getCreator(),
                unfreezeStockFlowVo.getSource(),
                unfreezeStockFlowVo.getProcessDefinitionKey(),
                unfreezeStockFlowVo, unfreezeStockFlowVo.getFundCode());
        // 如果是PCI ，则需要插入数据到合约
        if (JuDataTypeUtils.checkDataCode(startRes)) {
            if (unfreezeStockFlowVo.getSource().equalsIgnoreCase("PCI")) {
                UnfreezeStockInsertDTO unfreezeStockInsertDTO = new UnfreezeStockInsertDTO();
                unfreezeStockInsertDTO.setAccountId(unfreezeStockFlowVo
                        .getAccount());
                unfreezeStockInsertDTO.setFileIdList(unfreezeStockFlowVo
                        .getFileIds());
                unfreezeStockInsertDTO.setFilePathList(unfreezeStockFlowVo
                        .getFilePaths());
                unfreezeStockInsertDTO.setFrostType(Integer
                        .valueOf(unfreezeStockFlowVo.getFrostType()));
                unfreezeStockInsertDTO.setFundCode(Integer
                        .valueOf(unfreezeStockFlowVo.getFundCode()));
                unfreezeStockInsertDTO.setFundName(unfreezeStockFlowVo
                        .getFundName());
                unfreezeStockInsertDTO
                        .setOrderId(getProssId(startRes));// 流水号
                unfreezeStockInsertDTO.setRemark(JuDataTypeUtils
                        .getString(unfreezeStockFlowVo.getRemark()));
                unfreezeStockInsertDTO.setStatus(1);// 提交
                unfreezeStockInsertDTO.setStockNature(unfreezeStockFlowVo
                        .getStockNature());
                unfreezeStockInsertDTO.setUnfreezeNum(Integer.parseInt(unfreezeStockFlowVo
                        .getUnfreezeNum()));
                // 插入合约数据
                boolean insertTransferOwnershipInfo = unFreezeStockDao
                        .insertUnFreezeStockInfo(unfreezeStockInsertDTO);
            }
        }
        return BaseRespUtils.convertBaseRspDTO2BaseResp(startRes);
    }

    /**
     * 查询枚举接口
     */
    @Override
    public BaseResp queryEnum(QueryEnumVo enumVo) {
//		BaseRspDTO custlogbusinesscode = queryManagerService
//				.custlogbusinesscode();
        DictQueryDTO dictQueryDTO = new DictQueryDTO(100, 1, enumVo.getType(), null);
        BaseRspDTO custlogbusinesscode = queryManagerService.getDict(dictQueryDTO);

        if (JuDataTypeUtils.checkDataCode(custlogbusinesscode)) {
            ArrayList<LinkedHashMap<String, Object>> list = (ArrayList) custlogbusinesscode.getData();
            List<QueryEnumRespDTO> dList = new ArrayList<>();
            for (int i = 0; i < list.size(); i++) {
                QueryEnumRespDTO dto = new QueryEnumRespDTO(list.get(i).get("SUBITEM") + "", list.get(i).get("SUBITEMNAME") + "");
                dList.add(dto);
            }
            BaseResp resp = new BaseResp();
            Data data = new Data<>();
            data.setItems(dList);
            resp.setData(data);
            return resp;
        }
        return new BaseResp<>(10000, "失败");
    }

    /**
     * 查询冻结流水接口
     */
    @Override
    public BaseResp queryForstStream(QueryForstStreamVo forstStreamVo) {
        if (forstStreamVo != null) {
        	 BaseResp baseResp = new BaseResp();
            String account = "";
            if (StringUtils.isNotBlank(forstStreamVo.getUserId())) {
//                LibInvestorUser userInfo = getUserinfoById(forstStreamVo.getUserId());
//                account = userInfo.getAccountId();
            	
            	 Map<String, String> registerinfo = this.getRegisterinfo(forstStreamVo.getUserId());
                 account = registerinfo.get("accoundId");
                 if(StringUtils.isBlank(account)){
                	 return baseResp;
                 }
            } else {
                account = forstStreamVo.getAccount();
            }

            //如果是企业用户必填产品代码
           
            if (StringUtils.isNotBlank(forstStreamVo.getType())) {
                if (StringUtils.isBlank(forstStreamVo.getFundCode())) {
                    baseResp.setRet(1000);
                    baseResp.setMessage("企业用户产品代码必填");
                    return baseResp;
                }
                String accountTemp = this.getFundAccount(forstStreamVo.getFundCode());
                if (StringUtils.isBlank(accountTemp)) {
                    return baseResp;
                }
                //如果产品代码和本人不匹配则直接返回
                if (!accountTemp.equals(forstStreamVo.getApply())) {
                    return baseResp;
                }
            }

            if (StringUtils.isNotBlank(forstStreamVo.getFundName()) && StringUtils.isBlank(forstStreamVo.getFundCode())) {
                String fundCode = this.getFundCode(forstStreamVo.getFundName());
                if (StringUtils.isBlank(fundCode)) {
                    return baseResp;
                } else {
                    forstStreamVo.setFundCode(fundCode);
                }
            }

            String funName = JuDataTypeUtils.getString(forstStreamVo
                    .getFundName());// 产品名称
            String fundCode = JuDataTypeUtils.getString(forstStreamVo
                    .getFundCode());// 产品代码
            String frozenType = JuDataTypeUtils.getString(forstStreamVo
                    .getFrozenType());// 冻结类型
            Integer pageNumber = JuDataTypeUtils.getInt(forstStreamVo
                    .getPageNumber());// 页码
            Integer pageSize = JuDataTypeUtils.getInt(forstStreamVo
                    .getPageSize());// 每页显示数量
            FrozenLogQueryDTO frozenLogQueryDTO = new FrozenLogQueryDTO();
            frozenLogQueryDTO.setFrozencause(frozenType);
            frozenLogQueryDTO.setFundcode(fundCode);
            frozenLogQueryDTO.setPageNo(pageNumber);
            frozenLogQueryDTO.setRows(pageSize);
            frozenLogQueryDTO.setTaaccountid(account);
            BaseRspDTO frozenLog = queryManagerService
                    .getFrozenLog(frozenLogQueryDTO);
            return BaseRespUtils.convertBaseRspDTO2BaseResp(frozenLog, QueryForstStreamResp.class);
        }
        return new BaseResp(10000, "失败");
    }

    /**
     * 流程start返回信息
     *
     * @param creator 客户内码
     * @param source  数据来源
     * @param search  用户查询流程key的关键字
     * @param obj     传入的对象
     * @return
     */
    @Override
    public BaseRspDTO getPrpcessStartRsp(String creator, String source,
                                         String processKey, Object obj, String fundCode) {
        // 获取流程字典
        BaseRspDTO procdef = processService.procdef();
        Map<String, String> readValue = JsonUtils.readValue(JsonUtils.toJson(procdef.getData()), Map.class);
        // 设置相应的任务状态
        String taskStatus = JuDataTypeUtils.getTaskStatus(readValue, processKey);
        ProcessStartDTO processStartDTO = new ProcessStartDTO(source, creator,
                processKey, JsonUtils.toJson(obj));
        if (obj instanceof ModifyCustMajorFlowVo) {//如果是重要信息流程提交  任务状态为3
            processStartDTO.setTaskStatus("3");
        } else {
            processStartDTO.setTaskStatus(taskStatus);
        }
        processStartDTO.setEntCode(fundCode);
        processStartDTO.setStatus("1");// 未受理
        // 流程start
        BaseRspDTO startRes = processService.start(processStartDTO);
        return startRes;
    }

    /**
     * 获取发起人（即股交游客id）
     *
     * @param userId
     * @return
     */
    private String getCreator(String userId) {
        // 通过合约查询信息
        LibInvestorUser investorUser = userDao.selectInvestorUserById(userId);
        // 流程发起人（合约存储的股交游客id）
        String creator = JuDataTypeUtils.getString(investorUser.getCustId());
        return creator;
    }

    /**
     * 根据股交任务列表返回数据获取来源
     *
     * @param str
     * @return
     */
    private String getSource(String str) {
        String source = null;
        if (str.endsWith("APP")) {
            source = "APP";
        } else if (str.endsWith("PCI")) {
            source = "PCI";
        } else if (str.endsWith("PCC")) {
            source = "PCC";
        }
        return source;
    }

    /**
     * 5.1.1.企业信息登记提交流程请求（包括产品信息）
     */
    @Override
    public BaseResp registerFundFlow(RegisterFundFlowVo registerFundFlowVo) {
        BaseRspDTO startRes = getPrpcessStartRsp(registerFundFlowVo.getCreator(), registerFundFlowVo.getSource(), registerFundFlowVo.getProcessDefinitionKey(),
                registerFundFlowVo, registerFundFlowVo.getFundCode());
        // 如果是PCI ，则需要插入数据到合约
        if (JuDataTypeUtils.checkDataCode(startRes)) {
            if (registerFundFlowVo.getSource().equalsIgnoreCase("PCI")) {
                ProductInsertDTO productInsertDTO = new ProductInsertDTO();
                productInsertDTO.setAccountId(registerFundFlowVo.getAccount());
                // productInsertDTO.setClearDate(registerFundFlowVo.getPayDate());//清算日期
                if (JuDataTypeUtils.getStringFlag(registerFundFlowVo.getCoupon())) {
                	productInsertDTO.setCoupon(Integer.valueOf(registerFundFlowVo
                			.getCoupon()));
				}
                if (JuDataTypeUtils.getStringFlag(registerFundFlowVo.getDateType())) {
                	productInsertDTO.setDateType(Integer.valueOf(registerFundFlowVo
                			.getDateType()));
				}
                if (JuDataTypeUtils.getStringFlag(registerFundFlowVo.getDeadLine())) {
                	productInsertDTO.setDeadLine(Integer.valueOf(registerFundFlowVo
                			.getDeadLine()));
				}
                productInsertDTO.setEndDate(JuDataTypeUtils
                        .getString(registerFundFlowVo.getEndDate()));
                if (JuDataTypeUtils.getStringFlag(registerFundFlowVo.getFaceValue())) {
                	productInsertDTO.setFaceValue(Integer
                			.valueOf(registerFundFlowVo.getFaceValue()));
				}
                productInsertDTO.setFileIdList(registerFundFlowVo.getFileIds());
                productInsertDTO.setFilePathList(registerFundFlowVo
                        .getFilePaths());
                productInsertDTO.setFloatShares(registerFundFlowVo
                        .getFloatShares() == null ? null : registerFundFlowVo
                        .getFloatShares());
                productInsertDTO
                        .setFundClass(registerFundFlowVo.getFundClass());// 所属板块
                productInsertDTO.setFundCode(registerFundFlowVo.getFundCode());
                productInsertDTO.setFundName(registerFundFlowVo.getFundName());
                productInsertDTO.setFundShortName(registerFundFlowVo
                        .getFundAbbr());// 产品简称
                if (JuDataTypeUtils.getStringFlag(registerFundFlowVo.getFundType())) {
                	productInsertDTO.setFundType(Integer.valueOf(registerFundFlowVo
                			.getFundType()));
				}
                // productInsertDTO.setHolder(holder);//企业股东流水信息
                productInsertDTO
                        .setHolders(registerFundFlowVo.getHolders() == null ? null
                                : registerFundFlowVo.getHolders());
                productInsertDTO.setIntrestDate(JuDataTypeUtils
                        .getString(registerFundFlowVo.getIntrestDate()));
                productInsertDTO.setIsOriginal(registerFundFlowVo
                        .getIsOriginal());
                productInsertDTO.setOperId(registerFundFlowVo.getOperId());
                productInsertDTO.setOrderId(startRes.getData().toString());// 流水id
                productInsertDTO.setPayDate(registerFundFlowVo.getPayDate());
                productInsertDTO.setPublisherShares(registerFundFlowVo
                        .getPublisherShares() == null ? null
                        : registerFundFlowVo.getPublisherShares());
                productInsertDTO.setRegisterShares(registerFundFlowVo
                        .getRegisterShares());// 注册份额
                productInsertDTO
                        .setRepayment(registerFundFlowVo.getRepayment() == null ? null
                                : registerFundFlowVo.getRepayment());
                productInsertDTO.setStartDate(JuDataTypeUtils
                        .getString(registerFundFlowVo.getStartDate()));
                productInsertDTO.setStatus(1);// 提交
                // productInsertDTO.setSubmitterId(registerFundFlowVo.getAccount());//提交人地址
                // productInsertDTO.setSubmitterName(submitterName);
                productInsertDTO.setTotalShares(registerFundFlowVo
                        .getTotalShares());// 总额
                if (JuDataTypeUtils.getStringFlag(registerFundFlowVo.getUnitvol())) {
                	productInsertDTO.setUnitvol(Integer.valueOf(registerFundFlowVo.getUnitvol()));
				}
                // productInsertDTO.setUpdTime(registerFundFlowVo.get);
                // 插入合约数据
                boolean insertTransferOwnershipInfo = productDao
                        .insertProductInfo(productInsertDTO);
            }
        }
        return BaseRespUtils.convertBaseRspDTO2BaseResp(startRes);
    }

    /**
     * 5.1.2.份额调整提交流程请求接口
     */
    @Override
    public BaseResp adjustStockFlow(AdjustStockFlowVo adjustStockFlowVo) {
        BaseRspDTO startRes = getPrpcessStartRsp(
                adjustStockFlowVo.getCreator(), adjustStockFlowVo.getSource(),
                adjustStockFlowVo.getProcessDefinitionKey(), adjustStockFlowVo, adjustStockFlowVo.getFundCode());
        // 如果是PCI ，则需要插入数据到合约
        if (JuDataTypeUtils.checkDataCode(startRes)) {
            if (adjustStockFlowVo.getSource().equalsIgnoreCase("PCI")) {
                AdjustStockInsertDTO adjustStockInsertDTO = new AdjustStockInsertDTO();
                adjustStockInsertDTO.setAccoundId(adjustStockFlowVo
                        .getAccount());
                adjustStockInsertDTO.setAdjustNum(adjustStockFlowVo
                        .getAdjustNum());
                adjustStockInsertDTO.setAdjustType(adjustStockFlowVo
                        .getAdjustType());
                adjustStockInsertDTO.setFileIdList(adjustStockFlowVo
                        .getFileIds());
                adjustStockInsertDTO.setFilePathList(adjustStockFlowVo
                        .getFilePaths());
                adjustStockInsertDTO.setFundCode(adjustStockFlowVo
                        .getFundCode());
                adjustStockInsertDTO.setFundName(adjustStockFlowVo
                        .getFundName());
                adjustStockInsertDTO.setOrderId(getProssId(startRes));// otcp返回的流水id
                adjustStockInsertDTO.setRemark(JuDataTypeUtils
                        .getString(adjustStockFlowVo.getRemark()));
                adjustStockInsertDTO.setStatus(1);// 提交
                adjustStockInsertDTO.setStockNature(adjustStockFlowVo
                        .getStockNature());
                // 调用合约
                adjustStockDao.insertAdjustStockInfo(adjustStockInsertDTO);
            }
        }
        return BaseRespUtils.convertBaseRspDTO2BaseResp(startRes);
    }

    /**
     * 5.1.3.股权性质变更提交流程请求接口
     */
    @Override
    public BaseResp transferNatureFlow(TransferNatureFlowVo transferNatureFlowVo) {
        BaseRspDTO startRes = getPrpcessStartRsp(
                transferNatureFlowVo.getCreator(),
                transferNatureFlowVo.getSource(),
                transferNatureFlowVo.getProcessDefinitionKey(),
                transferNatureFlowVo, transferNatureFlowVo.getFundCode());
        // 如果是PCI ，则需要插入数据到合约
        if (JuDataTypeUtils.checkDataCode(startRes)) {
            if (transferNatureFlowVo.getSource().equalsIgnoreCase("PCI")) {
                TransfetStockInsertDTO transfetStockInsertDTO = new TransfetStockInsertDTO();
                transfetStockInsertDTO.setAccountId(transferNatureFlowVo
                        .getAccount());
                transfetStockInsertDTO.setFileIdList(transferNatureFlowVo
                        .getFileIds());
                transfetStockInsertDTO.setFilePathList(transferNatureFlowVo
                        .getFilePaths());
                transfetStockInsertDTO.setFundCode(Integer
                        .valueOf(transferNatureFlowVo.getFundCode()));
                transfetStockInsertDTO.setFundName(transferNatureFlowVo
                        .getFundName());
                transfetStockInsertDTO
                        .setOrderId(getProssId(startRes));// otcp返回的流水id
                transfetStockInsertDTO.setRemark(JuDataTypeUtils
                        .getString(transferNatureFlowVo.getRemark()));
                transfetStockInsertDTO.setStatus(1);// 提交
                transfetStockInsertDTO.setStockNatureAfter(Integer.parseInt(transferNatureFlowVo
                        .getStockNatureAfter()));
                transfetStockInsertDTO
                        .setStockNatureBefore(Integer.parseInt(transferNatureFlowVo
                                .getStockNatureBefore()));
                transfetStockInsertDTO.setTransferNum(transferNatureFlowVo
                        .getFrostNum());
                // 调用合约
                transferStockNatureDao
                        .insertTransferNatureInfo(transfetStockInsertDTO);
            }
        }

        return BaseRespUtils.convertBaseRspDTO2BaseResp(startRes);
    }

    /**
     * 用户变更股权性质。
     */
    @Override
    public BaseResp transferNature(TransferNatureVo transferNatureVo) {
        NatureCodeDTO natureCodeDTO = new NatureCodeDTO();
        natureCodeDTO.setApplicationvol(transferNatureVo.getFrostNum()
                .toString());
        natureCodeDTO.setFundcode(transferNatureVo.getFundCode());
        natureCodeDTO.setNaturecode(transferNatureVo.getStockNatureBefore()
                .toString());// 调整前份额
        natureCodeDTO.setOperid(transferNatureVo.getOperId());
        natureCodeDTO.setSpecification(transferNatureVo.getRemark());
        natureCodeDTO.setTaaccountid(transferNatureVo.getAccount());
        natureCodeDTO.setTargetnaturecode(transferNatureVo
                .getStockNatureAfter().toString());
        // 调用股交变更股权性质接口
        BaseRspDTO changeNatureCode = businessManagerService
                .changeNatureCode(natureCodeDTO);
        if (!JuDataTypeUtils.checkDataCode(changeNatureCode)) {
            return BaseRespUtils.convertBaseRspDTO2BaseResp(changeNatureCode);
        }
        ArrayList<LinkedHashMap<String, Object>> list = (ArrayList) changeNatureCode.getData();
        String orderId = list.get(0).get("appsheetserialno").toString();
        // 添加风险资料
        BaseRspDTO addRiskData = AddRiskData(transferNatureVo,
                changeNatureCode, "权益性质变更", orderId);
        // 是否添加资料成功，不成功则直接返回
        if (!JuDataTypeUtils.checkDataCode(addRiskData)) {
            return BaseRespUtils.convertBaseRspDTO2BaseResp(addRiskData);
        }
        TransferStockFinishDTO transferStockDTO = new TransferStockFinishDTO();
        transferStockDTO.setAccountId(transferNatureVo.getAccount());
        transferStockDTO.setCreTime(transferNatureVo.getTimestamp());
        transferStockDTO.setFileIdList(transferNatureVo.getFileIds());
        transferStockDTO.setFilePathList(transferNatureVo.getFilePaths());
        transferStockDTO.setFundCode(transferNatureVo.getFundCode());
        transferStockDTO.setFundName(transferNatureVo.getFundName());
        transferStockDTO.setOrderId(orderId);
        transferStockDTO.setRemark(JuDataTypeUtils.getString(transferNatureVo
                .getRemark()));
        transferStockDTO.setStatus(2);// 完成
        transferStockDTO.setStockNatureAfter(Integer.parseInt(transferNatureVo
                .getStockNatureAfter()));
        transferStockDTO.setStockNatureBefore(Integer.parseInt(transferNatureVo
                .getStockNatureBefore()));
        transferStockDTO.setTransferNum(Integer.parseInt(transferNatureVo.getFrostNum()));
        // 调用股份性质变更合约
        boolean insertTransferNatureInfo = transferStockNatureDao
                .finishTransferNature(transferStockDTO);
        //TODO 暂时不管合约填入
//		if (insertTransferNatureInfo) {
        BaseResp resp = new BaseResp();
        Data data = new Data<>();
        data.setOrderId(orderId);
        resp.setData(data);
        return resp;
//		}
//		return new BaseResp<>(10000, "失败");
    }

    /**
     * 用户调整自身份额信息。
     */
    @Override
    public BaseResp adjustStock(AdjustStockVo adjustStockVo) {
        // 成功则调用股交的调整份额流程
        AdjustStockDTO adjustStockDTO = new AdjustStockDTO();
        adjustStockDTO.setTaaccountid(adjustStockVo.getAccount());// 账户
        adjustStockDTO.setAdjusttype(String.valueOf(adjustStockVo.getAdjustType() - 1));// 类型
        adjustStockDTO.setApplicationvol(adjustStockVo.getAdjustNum());// 调整份额
        adjustStockDTO.setFundcode(adjustStockVo.getFundCode());
        adjustStockDTO.setNaturecode(adjustStockVo.getStockNature());// 产品类型
        adjustStockDTO.setOperid(adjustStockVo.getOperId());
        adjustStockDTO.setSpecification(JuDataTypeUtils
                .getStringValue(adjustStockVo.getRemark()));// 备注
        // 调用股交份额调整接口
        BaseRspDTO adjustStock = businessManagerService
                .adjustStock(adjustStockDTO);
        if (!JuDataTypeUtils.checkDataCode(adjustStock)) {
            return BaseRespUtils.convertBaseRspDTO2BaseResp(adjustStock);
        }
        ArrayList<LinkedHashMap<String, Object>> list = (ArrayList) adjustStock.getData();
        String orderId = list.get(0).get("appsheetserialno").toString();
        // 添加风险资料
        BaseRspDTO addRiskData = AddRiskData(adjustStockVo, adjustStock,
                "股东份额调整", orderId);
        if (!JuDataTypeUtils.checkDataCode(addRiskData)) {
            return BaseRespUtils.convertBaseRspDTO2BaseResp(addRiskData);
        }
        AdjustStockFinishDTO dto = new AdjustStockFinishDTO();
        dto.setAccountId(adjustStockVo.getAccount());
        dto.setAdjustNum(Integer.parseInt(adjustStockVo.getAdjustNum()));
        dto.setAdjustType(adjustStockVo.getAdjustType());
        dto.setFileIdList(adjustStockVo.getFileIds());
        dto.setFilePathList(adjustStockVo.getFilePaths());
        dto.setFundCode(adjustStockVo.getFundCode());
        dto.setFundName(adjustStockVo.getFundName());
        dto.setOrderId(orderId);// otcp返回的流水id
        dto.setRemark(JuDataTypeUtils.getString(adjustStockVo.getRemark()));// 备注
        dto.setStatus(2);// 完成
        dto.setStockNature(Integer.parseInt(adjustStockVo.getStockNature()));
        // 存储流水信息到合约
        boolean insertAdjustStockInfo = adjustStockDao.finishAdjustStock(dto);


        //TODO 暂时不管合约成不成功
//		if (insertAdjustStockInfo) {
        BaseResp resp = new BaseResp();
        Data data = new Data<>();
        data.setOrderId(orderId);
        resp.setData(data);
        return resp;
//		}
//		return new BaseResp<>(10000, "失败");
    }

    /**
     * 添加风险资料信息
     *
     * @param obj
     * @param dto
     * @param businessSName
     * @return
     */
    private BaseRspDTO AddRiskData(Object obj, BaseRspDTO dto,
                                   String businessSName, String orderId) {
        RiskAddDTO riskAddDTO = new RiskAddDTO();
        riskAddDTO.setBusinessName(businessSName);// 业务名称 必填
        riskAddDTO.setAttachment(JsonUtils.toJson(obj));// 附件json数据 必填
        riskAddDTO.setBusinessSerialId(orderId);// 业务流水号
        BaseRspDTO riskAdd = fileService.riskAdd(riskAddDTO);
        return riskAdd;
    }

    /**
     * 用户冻结自己所持有股权信息。
     */
    @Override
    public BaseResp frostStock(FrostStockVo frostStockVo) {
        FrozenEquityDTO frozenEquityDTO = new FrozenEquityDTO();
        frozenEquityDTO.setApplicationvol(frostStockVo.getFrostNum());// 冻结数量
        frozenEquityDTO.setFrozencause(frostStockVo.getFrostType());// 冻结类别
        frozenEquityDTO.setFundcode(frostStockVo.getFundCode());
        frozenEquityDTO.setNaturecode(frostStockVo.getStockNature());// 份额性质
        frozenEquityDTO.setOperid(frostStockVo.getOperId());
        frozenEquityDTO.setSpecification(JuDataTypeUtils
                .getStringValue(frostStockVo.getRemark()));// 备注
        frozenEquityDTO.setTaaccountid(frostStockVo.getAccount());
        // 调用股交冻结接口
        BaseRspDTO frozenEquity = businessManagerService
                .frozenEquity(frozenEquityDTO);// 冻结
        if (!JuDataTypeUtils.checkDataCode(frozenEquity)) {
            return BaseRespUtils.convertBaseRspDTO2BaseResp(frozenEquity);
        }
        ArrayList<LinkedHashMap<String, Object>> list = (ArrayList) frozenEquity.getData();
        String orderId = list.get(0).get("appsheetserialno").toString();
        // 添加风险资料
        BaseRspDTO addRiskData = AddRiskData(frostStockVo, frozenEquity, "质押冻结", orderId);
        // 是否添加资料成功，不成功则直接返回
        if (!JuDataTypeUtils.checkDataCode(addRiskData)) {
            return BaseRespUtils.convertBaseRspDTO2BaseResp(addRiskData);
        }
        FrostStockDTO dto = new FrostStockDTO();
        dto.setAccountId(frostStockVo.getAccount());// 登记账户
        dto.setCreTime(frostStockVo.getTimestamp());// 创建时间
        dto.setFileIdList(frostStockVo.getFileIds());// 文件id集合
        dto.setFilePathList(frostStockVo.getFilePaths());// 文件路径集合
        dto.setFrostDate(frostStockVo.getTimestamp());// 时间戳
//        dto.setFrostNum(Integer.valueOf(frostStockVo.getFrostNum()));// 冻结数量
//        dto.setFrostType(Integer.valueOf(frostStockVo.getFrostType()));// 冻结类型
        dto.setFundCode(Integer.valueOf(frostStockVo.getFundCode()));// 产品代码
        dto.setFundName(frostStockVo.getFundName());// 产品名称
        dto.setOrderId(orderId);// otcp返回的流水id
        dto.setRemark(JuDataTypeUtils.getString(frostStockVo.getRemark()));
        dto.setStatus(2);// 流程结束
        dto.setStockNature(frostStockVo.getStockNature());// 份额性质

        // 调用合约审核流程通过则保存流水信息
//        boolean insertFrostStockInfo = frostStockDao.finishFrostStock(dto);

        //TODO 暂时不管合约填入
//		if (insertFrostStockInfo) {// 成功
        BaseResp resp = new BaseResp();
        Data data = new Data<>();
        data.setOrderId(orderId);// 流水号
        resp.setData(data);
        return resp;
//		}
//		return new BaseResp<>(10000, "失败");
    }

    /**
     * 户修改股东信息流程提交接口。
     */
    @Override
    public BaseResp modifyHolderFlow(ModifyHolderFlowVo modifyHolderFlowVo) {
        // 流程开始
        BaseRspDTO startRes = getPrpcessStartRsp(
                modifyHolderFlowVo.getCreator(),
                modifyHolderFlowVo.getSource(), modifyHolderFlowVo.getProcessDefinitionKey(),
                modifyHolderFlowVo, modifyHolderFlowVo.getFundCode());// 修改股东信息(股权登记)
        // 如果是PCI ，则需要插入数据到合约
        if (JuDataTypeUtils.checkDataCode(startRes)) {
            if (modifyHolderFlowVo.getSource().equalsIgnoreCase("PCI")) {
                // 调用合约
                userDao.insert_InvestorUser(modifyHolderFlowVo);
            }
        }
        return BaseRespUtils.convertBaseRspDTO2BaseResp(startRes);
    }

    /**
     * 解冻份额
     */
    @Override
    public BaseResp unfreezeStock(UnfreezeStockVo unfreezeStockVo) {
        UnfrozenEquityDTO unfrozenEquityDTO = new UnfrozenEquityDTO();
        unfrozenEquityDTO.setApplicationvol(String.valueOf(unfreezeStockVo
                .getFrostNum()));// 解冻份额
        unfrozenEquityDTO.setAppsheetserialno(unfreezeStockVo
                .getAppsheetserialno());// 冻结流水号
        unfrozenEquityDTO.setFundcode(unfreezeStockVo.getFundCode());
        unfrozenEquityDTO.setOperid(unfreezeStockVo.getOperId());
        unfrozenEquityDTO.setSpecification(JuDataTypeUtils
                .getString(unfreezeStockVo.getRemark()));// 备注
        unfrozenEquityDTO.setTaaccountid(unfreezeStockVo.getAccount());
        // 调用股交的解冻接口
        BaseRspDTO unfrozenEquity = businessManagerService
                .unfrozenEquity(unfrozenEquityDTO);
        if (!JuDataTypeUtils.checkDataCode(unfrozenEquity)) {
            return BaseRespUtils.convertBaseRspDTO2BaseResp(unfrozenEquity);
        }
        ArrayList<LinkedHashMap<String, Object>> list = (ArrayList) unfrozenEquity.getData();
        String orderId = list.get(0).get("appsheetserialno").toString();
        // 添加风险资料
        BaseRspDTO addRiskData = AddRiskData(unfreezeStockVo, unfrozenEquity,
                "质押解冻", orderId);
        // 是否添加资料成功，不成功则直接返回
        if (!JuDataTypeUtils.checkDataCode(addRiskData)) {
            return BaseRespUtils.convertBaseRspDTO2BaseResp(addRiskData);
        }
        UnFreezeStockFinishDTO unFreezeStockFinishDTO = new UnFreezeStockFinishDTO();
        unFreezeStockFinishDTO.setAccountId(unfreezeStockVo.getAccount());
        unFreezeStockFinishDTO.setCreTime(unfreezeStockVo.getTimestamp());
        unFreezeStockFinishDTO.setFileIdList(unfreezeStockVo.getFileIds());
        unFreezeStockFinishDTO.setFilePathList(unfreezeStockVo.getFilePaths());
        unFreezeStockFinishDTO.setFrostNum(unfreezeStockVo.getFrostNum());
        unFreezeStockFinishDTO.setFrostType(Integer.parseInt(unfreezeStockVo.getFrostType()));
        unFreezeStockFinishDTO.setFundCode(Integer.valueOf(unfreezeStockVo
                .getFundCode()));
        unFreezeStockFinishDTO.setFundName(unfreezeStockVo.getFundName());
        unFreezeStockFinishDTO.setOrderId(orderId);
        unFreezeStockFinishDTO.setStatus(2);// 状态
        unFreezeStockFinishDTO.setStockNature(Integer.parseInt(unfreezeStockVo.getStockNature()));
        unFreezeStockFinishDTO.setRemark(JuDataTypeUtils
                .getString(unfreezeStockVo.getRemark()));
        // 存储流水到合约
        boolean finishUnFreezeStockInfo = unFreezeStockDao
                .finishUnFreezeStockInfo(unFreezeStockFinishDTO);

        //TODO 暂时不管合约填入
//		if (finishUnFreezeStockInfo) {// 成功
        BaseResp resp = new BaseResp();
        Data data = new Data<>();
        data.setOrderId(orderId);
        resp.setData(data);
        return resp;
//		}
//		return new BaseResp<>(10000, "失败");
    }

    /**
     * 实现非交易过户业务
     */
    @Override
    public BaseResp transferStock(TransferStockVo transferStockVo) {
        NontransferDTO dto = new NontransferDTO();
        dto.setApplicationvol(transferStockVo.getOutNum());// 过出份额
        dto.setFundcode(transferStockVo.getFundCode());
        dto.setNonedealcls(transferStockVo.getTransferType());// 过户类型
        dto.setNaturecode(transferStockVo.getStockNatureBefore());// 原股份性质
        dto.setOperid(transferStockVo.getOperId());
        dto.setSpecification(JuDataTypeUtils.getString(transferStockVo
                .getRemark()));
        dto.setTaaccountid(transferStockVo.getOutAccount());
        dto.setTargetnaturecode(transferStockVo.getStockNatureAfter());// 转入股份性质
        dto.setTargettaaccountid(transferStockVo.getInAccount());// 过入方登记账户
        // 调用股交非交易过户请求
        BaseRspDTO nontransfer = businessManagerService.nontransfer(dto);
        if (!JuDataTypeUtils.checkDataCode(nontransfer)) {
            return BaseRespUtils.convertBaseRspDTO2BaseResp(nontransfer);
        }
        ArrayList<LinkedHashMap<String, Object>> list = (ArrayList) nontransfer.getData();
        String orderId = list.get(0).get("appsheetserialno").toString();
        // 添加风险资料
        BaseRspDTO addRiskData = AddRiskData(transferStockVo, nontransfer,
                "非交易过户", orderId); // TODO 是个人发起还是企业发起
        // 是否添加资料成功，不成功则直接返回
        if (!JuDataTypeUtils.checkDataCode(addRiskData)) {
            return BaseRespUtils.convertBaseRspDTO2BaseResp(addRiskData);
        }
        TransferOwnershipDTO transferOwnershipDTO = new TransferOwnershipDTO();
        transferOwnershipDTO.setFileIdList(transferStockVo.getFileIds());
        transferOwnershipDTO.setFilePathList(transferStockVo.getFilePaths());
        transferOwnershipDTO.setFundCode(Integer.valueOf(transferStockVo.getFundCode()));
        transferOwnershipDTO.setFundName(transferStockVo.getFundName());
        transferOwnershipDTO.setInAccountId(transferStockVo.getInAccount());
        transferOwnershipDTO.setInAccountName("123");
        transferOwnershipDTO.setInStockNature(Integer.parseInt(transferStockVo.getStockNatureAfter()));
        transferOwnershipDTO.setOrderId(orderId);// 流水号
        transferOwnershipDTO.setOutAccountId(transferStockVo.getOutAccount());
        transferOwnershipDTO.setOutAccountName("123");
        transferOwnershipDTO.setOutNum(transferStockVo.getOutNum() + "");
        transferOwnershipDTO.setOutStockNature(transferStockVo.getStockNatureAfter() + "");
        transferOwnershipDTO.setStatus(2);
        transferOwnershipDTO.setRemark(JuDataTypeUtils.getString(transferStockVo.getRemark()));
        transferOwnershipDTO.setTransferType(transferStockVo.getTransferType() + "");
        // 更新非交易过户
        boolean finishTransferOwnership = transferOwnershipDao.finishTransferOwnership(transferOwnershipDTO);

        //TODO 暂时不管合约填入
//		if (finishTransferOwnership) {// 成功
        BaseResp resp = new BaseResp();
        Data data = new Data<>();
        data.setOrderId(orderId);
        resp.setData(data);
        return resp;
//		}
//		return new BaseResp<>(10000, "失败");
    }

    /**
     * 用户登记产品和企业信息接口。
     */
    @Override
    public BaseResp registerFund(RegisterFundVo registerFundVo) {
    	CustinfoQueryDTO custinfoQueryDTO = new CustinfoQueryDTO(2, 1);
    	custinfoQueryDTO.setTaaccountid(registerFundVo.getAccount());
    	// 先进行查询账户信息
    	BaseRspDTO custinfo = queryManagerService.getCustinfo(custinfoQueryDTO);
    	Map<String, Object> respMap = BaseRespUtils.respMap(custinfo);
		// 如果没有countryname字段信息则直接返回错误信息
		String countryname =  JuDataTypeUtils.getStringValue(respMap.get("countryname"));
		if (StringUtils.isBlank(countryname)) {
			return new BaseResp<>(19901, "该机构无法发起股权登记，请先完善机构开户信息");
		}
        RegisterFundflowDTO registerFundflowDTO = new RegisterFundflowDTO();
        registerFundflowDTO.setCoupon(JuDataTypeUtils.getString(registerFundVo.getCoupon()));//票面利率
        registerFundflowDTO.setDatetype(JuDataTypeUtils.getString(registerFundVo.getDateType()));//期限单位
        registerFundflowDTO.setDealline(JuDataTypeUtils.getString(registerFundVo.getDeadLine()));//产品期限
        registerFundflowDTO.setEnddate(JuDataTypeUtils.getString(registerFundVo.getEndDate()));
        registerFundflowDTO.setFacevalue(JuDataTypeUtils.getString(registerFundVo.getFaceValue()));
        registerFundflowDTO.setFullname(registerFundVo.getFundName());//全称
        registerFundflowDTO.setFundclass(JuDataTypeUtils.getString(registerFundVo.getFundClass()));
        registerFundflowDTO.setFundcode(registerFundVo.getFundCode());
        registerFundflowDTO.setFundname(registerFundVo.getFundAbbr());
        registerFundflowDTO.setFundtype(registerFundVo.getFundType());
        registerFundflowDTO.setHolders(JuDataTypeUtils.getString(registerFundVo.getHolders() + ""));
        registerFundflowDTO.setIntrestdate(JuDataTypeUtils.getString(registerFundVo.getIntrestDate()));
        registerFundflowDTO.setIsoriginal(registerFundVo.getIsOriginal() + "");
        registerFundflowDTO.setOperid(registerFundVo.getOperId());
        registerFundflowDTO.setPaydate(JuDataTypeUtils.getString(registerFundVo.getPayDate()));
        registerFundflowDTO.setPublishershares(JuDataTypeUtils.getString(registerFundVo.getPublisherShares()));
        registerFundflowDTO.setRepayment(JuDataTypeUtils.getString(registerFundVo.getRepayment()));
        String serviceType = null;
        switch (registerFundVo.getFlag()) {
            case 1:
                serviceType = "I";
                break;
            case 2:
                serviceType = "U";
                break;

            default:
                break;
        }
        registerFundflowDTO.setServicetype(serviceType);
        registerFundflowDTO.setStartdate(JuDataTypeUtils.getString(registerFundVo.getStartDate()));
        registerFundflowDTO.setTaaccountid(registerFundVo.getAccount());
        registerFundflowDTO.setTotalshares(registerFundVo.getTotalShares());
        registerFundflowDTO.setUnitvol(JuDataTypeUtils.getString(registerFundVo.getUnitvol()));
        // 调用股交产品信息维护接口
        BaseRspDTO registerFund = businessManagerService.registerFund(registerFundflowDTO);
        //成功返回消息
        if (JuDataTypeUtils.checkDataCode(registerFund)) {
            BaseResp resp = new BaseResp();
            Data data = new Data<>();
            data.setFundCode(JuDataTypeUtils.getString(registerFund.getData() + ""));
            resp.setData(data);
            return resp;
        }
        // 返回信息
        return BaseRespUtils.convertBaseRspDTO2BaseResp(registerFund);
    }

    /**
     * 用户修改企业信息接口。
     */
    @Override
    public BaseResp modifyFund(ModifyFundVo modifyFundVo) {
//		// 流程开始
//		BaseRspDTO startRes = getPrpcessStartRsp(modifyFundVo.getCreator(),
//				modifyFundVo.getSource(), "process_modify_info_ent",
//				modifyFundVo,"");// 企业信息变更
        return null;
    }

    /**
     * 中心人员维护产品类型接口。
     */
    @Override
    public BaseResp defendProductType(DefendProductTypeVo defendProductTypeVo) {
        FundTypeDTO fundTypeDTO = new FundTypeDTO(
                getServiceType(defendProductTypeVo.getServicetype()),
                defendProductTypeVo.getFundtype(),
                defendProductTypeVo.getTypename(),
                defendProductTypeVo.getOperid());
        // 调用股交企业类型维护接口
        BaseRspDTO saveFundType = businessManagerService
                .saveFundType(fundTypeDTO);
        // TODO ????????????
        // 返回信息
        return BaseRespUtils.convertBaseRspDTO2BaseResp(saveFundType);
    }

    /**
     * 中心人员维护产品下属业务接口。
     */
    @Override
    public BaseResp defendBusiness(DefendBusinessVo defendBusinessVo) {
        FundTypeChildServiceDTO fundTypeChildServiceDTO = new FundTypeChildServiceDTO();
        fundTypeChildServiceDTO.setBusinesscodes(defendBusinessVo
                .getBusinessCode());
        fundTypeChildServiceDTO.setCustbalusabledate("0");// 买入份额
        // ？？？？？？？？？？？？？？？
        fundTypeChildServiceDTO.setCustfunddrawdate("0");
        ;// 卖出资金可取？？？？？？？？？？？？？？？？？
        fundTypeChildServiceDTO.setCustfundusabledate("0");// 卖出资金T+?可用
        // ??????????????
        fundTypeChildServiceDTO.setFundtype(defendBusinessVo.getFundtype());// 类型
        fundTypeChildServiceDTO.setOperid(defendBusinessVo.getOperid());
        String servicetype = getServiceType(defendBusinessVo.getServicetype());
        fundTypeChildServiceDTO.setServicetype(servicetype);
        BaseRspDTO saveChildServiceOfFundType = businessManagerService
                .saveChildServiceOfFundType(fundTypeChildServiceDTO);
        // 返回信息
        return BaseRespUtils.convertBaseRspDTO2BaseResp(saveChildServiceOfFundType);
    }

    private String getServiceType(String serviceType) {
        String servicetype = null;
        switch (JuDataTypeUtils.getString(serviceType)) {
            case "1":
                servicetype = "I";
                break;
            case "2":
                servicetype = "U";
                break;
            case "3":
                servicetype = "D";
                break;

            default:
                break;
        }
        return servicetype;
    }

    @Override
    public BaseResp queryFundType(FundTypeVo fundTypeVo) {
        FundTypeQueryDTO fundTypeDTO = new FundTypeQueryDTO(
                fundTypeVo.getPageSize(), fundTypeVo.getPageNumber(),
                fundTypeVo.getFundtype(), fundTypeVo.getBusinessCode());
        BaseRspDTO rundType = queryManagerService.getFundType(fundTypeDTO);
        // 返回信息
        return BaseRespUtils.convertBaseRspDTO2BaseResp(rundType,QueryFundTypeRespDTO.class);
    }

    @Override
    public BaseResp queryCapitalInfo(CapitalInfoVo capitalInfoVo) {
        BaseRspDTO capitalinfo = queryManagerService
                .getCapitalinfo(capitalInfoVo.getCustmerno(),
                        capitalInfoVo.getCapitalAccount());
        // 返回信息
        return BaseRespUtils.convertBaseRspDTO2BaseResp(capitalinfo);
    }

    @Override
    public BaseResp queryProductInfo(ProductInfoVo productInfoVo) {
        FundinfoQueryDTO fundinfoQueryDTO = new FundinfoQueryDTO();
        fundinfoQueryDTO.setFundcode(productInfoVo.getFundCode());
        fundinfoQueryDTO.setFundname(productInfoVo.getFundName());
        fundinfoQueryDTO.setFundtype(productInfoVo.getFundType());
        fundinfoQueryDTO.setTaaccountid(productInfoVo.getAccount());
        fundinfoQueryDTO.setRows(productInfoVo.getPageSize());
        fundinfoQueryDTO.setPageNo(productInfoVo.getPageNumber());
        BaseRspDTO capitalinfo = queryManagerService.getFundinfo(fundinfoQueryDTO);
        // 返回信息
        return BaseRespUtils.convertBaseRspDTO2BaseResp(capitalinfo, QueryProductInfoResp.class);
    }

    /**
     * 5.3.1.查询企业持有人名册信息
     */
    @Override
    public BaseResp queryStockholders(QueryStockholdersVo queryStockholdersVo) {
        StockHoldersQueryDTO stockHoldersQueryDTO = new StockHoldersQueryDTO();
        stockHoldersQueryDTO.setFundcode(queryStockholdersVo.getFundCode());
        stockHoldersQueryDTO.setOperid(queryStockholdersVo.getOperId());
        stockHoldersQueryDTO.setPageNo(queryStockholdersVo.getPageNumber());
        stockHoldersQueryDTO.setRows(queryStockholdersVo.getPageSize());
        stockHoldersQueryDTO.setQuerytype(queryStockholdersVo.getQueryType()
                + "");
        stockHoldersQueryDTO.setQuerydate(queryStockholdersVo.getQueryDate());
        // 调用股交的查询接口
        BaseRspDTO stockHolders = queryManagerService
                .getStockHolders(stockHoldersQueryDTO);
        // 返回信息
        return BaseRespUtils.convertBaseRspDTO2BaseResp(stockHolders, QueryStockHoldersRespDTO.class);
    }

    /**
     * 5.3.2.处理持有人名册导入信息
     */
    @Override
    public BaseResp processStockholders(
            ProcessStockholdersVo processStockholdersVo) {
        ProcessHoldersImportDTO processHoldersImportDTO = new ProcessHoldersImportDTO();
        processHoldersImportDTO.setBatchno(processStockholdersVo.getBatchno());
        processHoldersImportDTO.setBranchcode(processStockholdersVo
                .getBranchcode() + "");
        processHoldersImportDTO
                .setFundcode(processStockholdersVo.getFundcode());
        processHoldersImportDTO.setOperid(processStockholdersVo.getOperid()
                + "");
        // 调用股交的接口
        BaseRspDTO processsholdersimport = queryManagerService
                .processsholdersimport(processHoldersImportDTO);

        if (JuDataTypeUtils.checkDataCode(processsholdersimport)) {
            BaseResp resp = new BaseResp();
            Data data = new Data<>();
            data.setBatchno(processsholdersimport.getData() + "");
            ;
            resp.setData(data);
            return resp;
        }
        // 返回信息
        return BaseRespUtils.convertBaseRspDTO2BaseResp(processsholdersimport);
    }

    /**
     * 5.3.3.导入持有人名册
     */
    @Override
    public BaseResp inStockholders(InStockholdersVo inStockholdersVo) {
        // 调用股交持有人导入后查询
        BaseRspDTO insholdersimport = queryManagerService.insholdersimport(
                inStockholdersVo.getFundcode(), inStockholdersVo.getFilepath(),
                inStockholdersVo.getOperid());
        if (JuDataTypeUtils.checkDataCode(insholdersimport)) {
            BaseResp resp = new BaseResp();
            Data data = new Data<>();
            data.setBatchno(insholdersimport.getData() + "");
            ;
            resp.setData(data);
            return resp;
        }
        // 返回信息
        return BaseRespUtils.convertBaseRspDTO2BaseResp(insholdersimport);
    }

    /**
     * 5.3.4.持有人名册导入后查询信息
     */
    @Override
    public BaseResp queryInStockholders(
            QueryInStockholdersVo queryInStockholders) {
        GetholdersImportDTO getholdersImportDTO = new GetholdersImportDTO();
        getholdersImportDTO.setBatchno(queryInStockholders.getBatchno()); // 批次号
        getholdersImportDTO.setIsbefore(queryInStockholders.getIsbefore());// 0导入后查询;1处理后查询
        getholdersImportDTO.setPageNumber(queryInStockholders.getPageNo() + "");// 当前页
        getholdersImportDTO.setPageSize(queryInStockholders.getPageSize() + "");// 每页显示
        // 调用股交持有人导入后查询
        BaseRspDTO getholdersimport = queryManagerService
                .getholdersimport(getholdersImportDTO);
        // 返回信息
        return BaseRespUtils.convertBaseRspDTO2BaseResp(getholdersimport, QueryInStockHoldersRespDTO.class);
    }

    public String getBusinessKey(String search) {
        // 获取流程字典
        BaseRspDTO procdef = processService.procdef();
        Map<String, String> readValue = JsonUtils.readValue(
                JsonUtils.toJson(procdef.getData()), Map.class);
        // 找出符合条件的流程key
        String processKey = JuDataTypeUtils.getProcessKey(readValue, search);
        return processKey;
    }

    @Override
    public BaseResp queryProvinces() {
        return BaseRespUtils.convertBaseRspDTO2BaseResp(businessManagerService
                .queryProvinces(), QueryProvincesRespDTO.class);
    }

    @Override
    public BaseResp queryCities(QueryCitiesVo citiesVo) {
        return BaseRespUtils.convertBaseRspDTO2BaseResp(businessManagerService
                .queryCities(citiesVo.getProvinceId()), QueryCitiesRespDTO.class);
    }

    /**
     * 添加风险资料
     */
    @Override
    public BaseResp saveRiskFile(SaveRiskFileVo saveRiskFileVo) {
        RiskAddDTO riskAddDTO = new RiskAddDTO();
        riskAddDTO.setAttachment(JsonUtils.toJson(saveRiskFileVo));// json数据
        riskAddDTO.setBusinessSerialId(JuDataTypeUtils.getString(saveRiskFileVo.getOrderId()));// 业务流水号
        riskAddDTO.setWorkflowId(JuDataTypeUtils.getString(saveRiskFileVo.getBusinessKey()));// 流程编号
        riskAddDTO.setBusinessName(JuDataTypeUtils.getString(saveRiskFileVo.getBusinessName()));// 流程名称
        BaseRspDTO riskAdd = fileService.riskAdd(riskAddDTO);

        return BaseRespUtils.convertBaseRspDTO2BaseResp(riskAdd);
    }

    /**
     * 获取返回的id
     *
     * @param baseRspDTO
     * @return
     */
    public String getProssId(BaseRspDTO baseRspDTO) {
        String str = JsonUtils.readValue(JuDataTypeUtils.getString(JsonUtils.toJson(baseRspDTO)), Map.class).get("id") + "";
        return str;
    }

    /**
     * 导出持有人名册
     */
    @Override
    public InputStream outStockholders(OutStockholdersVo outStockholdersVo) {
        ExportStockholersDTO exportStockholersDTO = new ExportStockholersDTO();
        exportStockholersDTO.setFundcode(outStockholdersVo.getFundcode());
        exportStockholersDTO.setQuerydate(outStockholdersVo.getQuerydate());
        exportStockholersDTO.setV_batchno(outStockholdersVo.getBatchno() + "");
        InputStream exportStockholders = queryManagerService.exportStockholders(exportStockholersDTO);
        return exportStockholders;
    }

    private LibInvestorUser getUserinfoById(String userId) {
        LibInvestorUser investorUserInfo = userDao.selectInvestorUserByName(userId);
        if (investorUserInfo == null) {
            throw new ServiceException("用户不存在");
        }
        return investorUserInfo;
    }

    @Override
    public InputStream downloadFile(DownLoadVo downLoadVo) {
        InputStream downloadFile = fileService.downloadFile(downLoadVo.getFilePath(), JuDataTypeUtils.getString(downLoadVo.getFileName()));
        return downloadFile;
    }

    @Override
    public BaseResp updateProcess(UpdateProcessVo updateProcessVo) {
        BaseRspDTO updateProcess = processService.updateProcess(updateProcessVo.getBusinessKey(), updateProcessVo.getFormData());
        return BaseRespUtils.convertBaseRspDTO2BaseResp(updateProcess);
    }

    @Override
    public BaseResp custLogCode(BaseVo baseVo) {
        BaseRspDTO custlogbusinesscode = queryManagerService.custlogbusinesscode();
        if (JuDataTypeUtils.checkDataCode(custlogbusinesscode)) {
            ArrayList<LinkedHashMap<String, Object>> list = (ArrayList) custlogbusinesscode.getData();
            List<QueryEnumRespDTO> dList = new ArrayList<>();
            for (int i = 0; i < list.size(); i++) {
                QueryEnumRespDTO dto = new QueryEnumRespDTO(list.get(i).get("SUBITEM") + "", list.get(i).get("SUBITEMNAME") + "");
                dList.add(dto);
            }
            BaseResp resp = new BaseResp();
            Data data = new Data<>();
            data.setItems(dList);
            resp.setData(data);
            return resp;
        }
        return new BaseResp<>(10000, "失败");
    }

    public String getFundCode(String fundName) {
        FundinfoQueryDTO fundinfoQueryDTO = new FundinfoQueryDTO();
        fundinfoQueryDTO.setFundname(fundName);
        fundinfoQueryDTO.setRows(1);
        fundinfoQueryDTO.setPageNo(1);
        BaseRspDTO baseRspDTO = queryManagerService.getFundinfo(fundinfoQueryDTO);
        if (baseRspDTO.getCode() != 0) {
            return null;
        } else {
            List list = (List) baseRspDTO.getData();
            if (list.size() == 0)
                return null;
            Map userMap = (Map) list.get(0);
            QueryProductInfoResp queryProductInfoResp = ObjectUtil.convertMapToBean(userMap, QueryProductInfoResp.class);
            return queryProductInfoResp.getFundCode();
        }
    }

    /**
     * 查询客户信息
     *
     * @param custMerno 客户内码
     * @param account   账户
     * @return
     */
    private Map<String, Object> getCustInfoRespMap(String custMerno, String account) {
        CustinfoQueryDTO custinfoQueryDTO = new CustinfoQueryDTO(10, 1);
        custinfoQueryDTO.setTaaccountid(account);
        custinfoQueryDTO.setCustomerno(custMerno);
        BaseRspDTO custinfo = queryManagerService.getCustinfo(custinfoQueryDTO);
        return BaseRespUtils.respMap(custinfo);//保存股交返回信息
    }

    public String getFundAccount(String fundCode) {
        FundinfoQueryDTO fundinfoQueryDTO = new FundinfoQueryDTO();
        fundinfoQueryDTO.setFundcode(fundCode);
        fundinfoQueryDTO.setRows(1);
        fundinfoQueryDTO.setPageNo(1);
        BaseRspDTO baseRspDTO = queryManagerService.getFundinfo(fundinfoQueryDTO);
        if (baseRspDTO.getCode() != 0) {
            return null;
        } else {
            List list = (List) baseRspDTO.getData();
            if (list.size() == 0)
                return null;
            Map userMap = (Map) list.get(0);
            QueryProductInfoResp queryProductInfoResp = ObjectUtil.convertMapToBean(userMap, QueryProductInfoResp.class);
            return queryProductInfoResp.getAccount();
        }
    }

    @Override
    public BaseResp queryCompanyFlag(QueryCompanyFlagVo companyFlagVo) {
        LibInvestorUser investorUser = super.selectInvestorUserByCustmerno(companyFlagVo.getCustMerno());
        Map<String, Object> retMap = new HashMap<>();
        retMap.put("isCompany", 0);
        if (null != investorUser && "3".equals(investorUser.getUserType())) {
            retMap.put("isCompany", 1);
        }
        return new BaseResp(retMap);
    }

    @Override
    public BaseResp modifyCompanyFlag(ModifyCompanyFlagVO modifyCompanyFlagVO) {
        LibInvestorUser investorUser = super.selectInvestorUserByCustmerno(modifyCompanyFlagVO.getCustMerno());
        if ("1".equals(modifyCompanyFlagVO)) {
            investorUser.setUserType("3");
        }
        if ("0".equals(modifyCompanyFlagVO.getIsCompany())) {
            investorUser.setUserType("2");
        }
        boolean result = userDao.updateAccount(investorUser);
        if (!result) {
            return new BaseResp(10000, "修改登记企业标识信息失败");
        }
        return new BaseResp();
    }

    @Override
    public BaseResp validTranPwd(ValidTranPwdVO validTranPwdVO) {
        PasswordIsValidDTO isValidDTO = new PasswordIsValidDTO();
        isValidDTO.setCustomerno(validTranPwdVO.getCustMerno());
        isValidDTO.setPassword(validTranPwdVO.getTransPwd());
        BaseRspDTO baseRspDTO = businessManagerService.validTranPwd(isValidDTO);
        return BaseRespUtils.convertBaseRspDTO2BaseResp(baseRspDTO);
    }
    
    private Map getRegisterinfo(String userId) {
        Map<String, String> map = new HashMap<String, String>();
        AusQueryDTO ausQueryDTO = new AusQueryDTO(10, 1);
        ausQueryDTO.setName(userId);
        // 根据userId查询游客列表
        BaseRspDTO registerinfo = queryManagerService.getRegisterinfo(ausQueryDTO);
        List<Map<String, String>> custmernoList = (List) registerinfo.getData();
        String custmerno = null;
        if (custmernoList != null && custmernoList.size() > 0) {
            custmerno = custmernoList.get(0).get("customerno");
            String loginName = custmernoList.get(0).get("name");
            map.put("custmerno", custmerno);
            map.put("loginName", loginName);
        }
        // 根据custmerno（客户内码）查询账户信息列表
        CustinfoQueryDTO custinfoQueryDTO = new CustinfoQueryDTO(10, 1);
        custinfoQueryDTO.setCustomerno(custmerno);
        BaseRspDTO custinfo = queryManagerService.getCustinfo(custinfoQueryDTO);
        List<Map<String, String>> custinfoList = (List) custinfo.getData();
        if (custinfoList != null && custinfoList.size() > 0) {
            map.put("custno", custinfoList.get(0).get("custno"));
            map.put("userType", custinfoList.get(0).get("individualorinstitution"));
            map.put("accoundId", custinfoList.get(0).get("taaccountid"));
        }
        return map;
    }
}
