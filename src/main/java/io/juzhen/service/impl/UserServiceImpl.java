package io.juzhen.service.impl;

import io.juzhen.base.dto.BaseResp;
import io.juzhen.base.dto.Data;
import io.juzhen.base.exception.ServiceException;
import io.juzhen.channel.dto.BaseRspDTO;
import io.juzhen.channel.dto.account.*;
import io.juzhen.channel.dto.process.ProcessCompleteDTO;
import io.juzhen.channel.dto.process.ProcessStartResData;
import io.juzhen.channel.dto.process.ProcessTasksDTO;
import io.juzhen.channel.dto.querymanager.*;
import io.juzhen.channel.dto.visitor.AddEmployeeDTO;
import io.juzhen.channel.dto.visitor.ResetTelNoDTO;
import io.juzhen.channel.dto.visitor.UpdateDTO;
import io.juzhen.channel.service.*;
import io.juzhen.dao.SysUserDao;
import io.juzhen.dao.UserDao;
import io.juzhen.dto.*;
import io.juzhen.dto.business.CustBindingInfoRespDTO;
import io.juzhen.dto.business.QueryProductInfoResp;
import io.juzhen.dto.user.*;
import io.juzhen.dto.user.web.QueryInvestorRespDTO;
import io.juzhen.dto.user.web.QueryTouristRespDTO;
import io.juzhen.enums.SmsType;
import io.juzhen.helper.InvestorUserHelper;
import io.juzhen.helper.MessageHelper;
import io.juzhen.po.LibInvestorUser;
import io.juzhen.po.LibUser;
import io.juzhen.redis.server.RedisCacheService;
import io.juzhen.service.BusinessHandleService;
import io.juzhen.service.UserService;
import io.juzhen.util.BaseRespUtils;
import io.juzhen.util.JuDataTypeUtils;
import io.juzhen.util.ObjectUtil;
import io.juzhen.util.PhoneFormatCheckUtils;
import io.juzhen.utils.DateTimeUtil;
import io.juzhen.utils.JsonUtils;
import io.juzhen.vo.user.*;
import io.juzhen.vo.user.web.*;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.websuites.utils.DESEncrypt;

import java.util.*;

@SuppressWarnings("all")
@Service
public class UserServiceImpl extends AbstractService implements UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    @Autowired
    private SysUserDao sysUserDao;
    @Autowired
    private BusinessHandleService businessHandleService;
    @Autowired
    private RedisCacheService redisCacheService;
    @Autowired
    private SmsService smsService;
    @Autowired
    private VisitorService visitorService;
    @Autowired
    private QueryManagerService queryManagerService;
    @Autowired
    private AccountManagerService accountManagerService;
    @Autowired
    private UserDao userDao;
    @Autowired
    private ProcessService processService;

    public static void main(String[] args) {
        String[] s = {"1", "2", "3", "5", "6"};
        List<String> listA = Arrays.asList(s);
        System.out.println(listA.size());

        String ss = null;
        String ss1 = "";
        System.out.println(StringUtils.isNotBlank(ss));
        System.out.println(StringUtils.isNotBlank(ss1));

        String date = DateTimeUtil.dateTimeToStr(new Date(), DateTimeUtil.STR_DATETIME_PATTERN);
        System.out.println(date);

    }

    private static String getUserType(String type) {
        String userType = "";
        switch (type) {
            case "0":
                userType = "1";
                break;
            case "1":
                userType = "2";
                break;
            case "N":
                userType = "3";
                break;

            default:
                break;
        }
        return userType;
    }

    @Override
    public BaseResp sendSMS(SendSMSVo smsVo) {
        //生成6位数字验证码并存入redis(60秒过期)
        String verifyCode = RandomStringUtils.randomNumeric(6);
        //调用OTCP系统发送短信
        BaseRspDTO rspDTO = null;
        if(smsVo.getType().intValue() == 1||smsVo.getType().intValue() == 2){
        	redisCacheService.setex(SmsType.getTypeByCode(smsVo.getType()), smsVo.getMobile(), 60, verifyCode);
        	rspDTO = smsService.asyncSendcode(smsVo.getMobile(), verifyCode, "1000","60");
        } else if(smsVo.getType().intValue() == 3){
        	//调用OTCP系统查询账户信息
        	String name = "";
            AusQueryDTO queryDTO = new AusQueryDTO(1, 1);
            queryDTO.setMobiletelno(smsVo.getMobile());
            BaseRspDTO baseRspDTO = queryManagerService.getRegisterinfo(queryDTO);
            if (0 != baseRspDTO.getCode()) {
                return new BaseResp(baseRspDTO.getCode(), baseRspDTO.getMessage());
            }
            List<Map> ret = (List<Map>) baseRspDTO.getData();
            if (!CollectionUtils.isEmpty(ret)) {
                QueryTouristRespDTO dto = ObjectUtil.convertMapToBean(ret.get(0), QueryTouristRespDTO.class);
                name = dto.getLoginName();
            }
          //先重置密码后发送短信
        	String passwod = DESEncrypt.strEnc(verifyCode, name, "zjexZJEX", null);
        	BaseRspDTO rspDTO2 = visitorService.resetPwd(name, passwod);
        	if (0 != rspDTO2.getCode()) {
                return new BaseResp(rspDTO2.getCode(), rspDTO2.getMessage());
            }
        	rspDTO = smsService.asyncSendpwd(smsVo.getMobile(), verifyCode, "1000");
        }

//        BaseRspDTO rspDTO = new BaseRspDTO(0, "成功");
        return BaseRespUtils.convertBaseRspDTO2BaseResp(rspDTO);
    }

    @Override
    public BaseResp verifyMsg(VerifyMsgVo msgVo) {
        //从redis获取验证码进行校验
        String verifyCode = redisCacheService.get(SmsType.getTypeByCode(msgVo.getType()), msgVo.getMobile());
        if (!"999999".equals(msgVo.getVerifyCode()) && !ObjectUtils.equals(msgVo.getVerifyCode(), verifyCode)) {
            throw new ServiceException("验证码错误");
        }
        //调用OTCP系统验证用户名
        BaseRspDTO rspDTO = visitorService.checkusername(msgVo.getLoginName());
        if (0 != rspDTO.getCode()) {
            return BaseRespUtils.convertBaseRspDTO2BaseResp(rspDTO);
        }
        //调用OTCP系统验证手机号
        rspDTO = visitorService.checktelephone(msgVo.getMobile().trim());
        if (0 != rspDTO.getCode()) {
            return BaseRespUtils.convertBaseRspDTO2BaseResp(rspDTO);
        }
        // 调用合约检查登录名是否存在
        boolean accountExist = sysUserDao.accountExist(msgVo.getLoginName());
        if (accountExist) {
            return new BaseResp<>(10000, "该用户名已存在");
        }
        return new BaseResp<>();
    }

    @Override
    public BaseResp register(RegisterVo registerVo) {
        List<String> list = new ArrayList<>();
        // 调用合约进行注册
//		String [] roleArray = {"100000" ,"100001", "100002" ,"100003"};
        if (StringUtils.equals(registerVo.getCustType(), "1")) {
            list.add("role100000");
        } else if (StringUtils.equals(registerVo.getCustType(), "2")) {
            list.add("role100001");
        }
        InsertUserDTO insertUserDTO = new InsertUserDTO();
        insertUserDTO.setAccount(registerVo.getLoginName());//登录名
        insertUserDTO.setMobile(registerVo.getMobile());//手机号
        insertUserDTO.setRoleIdList(list);//角色列表
        insertUserDTO.setName(registerVo.getLoginName());//用户名称
        insertUserDTO.setUserAddr(registerVo.getUserAddr());
        insertUserDTO.setDepartmentId("default");
        Map<String, Object> insert = sysUserDao.insert(insertUserDTO);
        int ret = (int) insert.get("ret");
        if (ret != 0) {//合约插入失败
            return new BaseResp<>(10000, String.valueOf(insert.get("message")));
        }
        //调用OTCP系统注册用户
        BaseRspDTO rspDTO = visitorService.add(registerVo.getLoginName(), registerVo.getPassword(), registerVo.getMobile());
        if (rspDTO.getCode() == 0 && StringUtils.equals(registerVo.getCustType(), "2")) {
            MessageDto dto = new MessageDto(registerVo.getLoginName(), "key2", 2);
            MessageHelper.sendWebMessageByMQ(dto);
        }
        return BaseRespUtils.convertBaseRspDTO2BaseResp(rspDTO);
    }

    @Override
    public BaseResp queryAccount(QueryAccountVo accountVo) {
        CustinfoQueryDTO queryDTO = new CustinfoQueryDTO(1, 1);
        queryDTO.setCertificateno(accountVo.getCertNo());
        queryDTO.setIndividualorinstitution((accountVo.getCertType() - 1) + "");

        //调用OTCP系统查询账户信息
        BaseRspDTO rspDTO = queryManagerService.getCustinfo(queryDTO);
        if (0 != rspDTO.getCode()) {
            return BaseRespUtils.convertBaseRspDTO2BaseResp(rspDTO);
        }
        //处理账号信息
        List list = (List) rspDTO.getData();
        BaseResp resp = new BaseResp<>();
        Map<String, Object> resMap = new HashMap<>();
        resp.setData(resMap);
        if (CollectionUtils.isEmpty(list)) {
            resMap.put("isAccount", 2);
            return resp;
        } else {
            //获取custId
            String custId = "";
            //判断是否绑定
            boolean isBind = true;
            if (StringUtils.isNotBlank(accountVo.getUserId())) {
                //调用OTCP系统查询账户信息
                AusQueryDTO ausQueryDTO = new AusQueryDTO(1, 1);
                ausQueryDTO.setName(accountVo.getUserId());
                BaseRspDTO baseRspDTO = queryManagerService.getRegisterinfo(ausQueryDTO);
                BaseResp baseResp = BaseRespUtils.convertBaseRspDTO2BaseResp(baseRspDTO);
                if (0 != baseResp.getRet()) {
                    return baseResp;
                }
                Data data = (Data) baseResp.getData();
                custId = (String) ((Map) data.getItems().get(0)).get("custid");
                String customerno = (String) ((Map) data.getItems().get(0)).get("customerno");
                resMap.put("custId", custId);
                if (StringUtils.isEmpty(customerno) || "-1".equals(customerno)) {
                    isBind = false;
                }

                //查询investeruser
                LibInvestorUser investerUser = selectInvestorUserByLoginName(accountVo.getUserId());
                resMap.put("status", investerUser.getStatus());
                resMap.put("addressId", investerUser.getUserId());
            }

            Map userMap = (Map) list.get(0);
            CustInfoDTO custInfoDto = ObjectUtil.convertMapToBean(userMap, CustInfoDTO.class);
            String customerno = custInfoDto.getCustomerno();
            //没有开户直接返回
            if (StringUtils.isEmpty(customerno) || "-1".equals(customerno)) {
                resMap.put("isAccount", 2);
                return resp;
            }
            //如果都未绑定则进行绑定
            if (!isBind) {
                BaseRspDTO baseRspDTO = accountManagerService.custbinding(custId, customerno);
                if (0 != baseRspDTO.getCode()) {
                    return BaseRespUtils.convertBaseRspDTO2BaseResp(baseRspDTO);
                }
            }
            if (custInfoDto.getIndividualorinstitution().equals("1") || custInfoDto.getIndividualorinstitution().equals("0")) {
                custInfoDto.setCustType(1);
            }
            custInfoDto.setCustId(custId);
            custInfoDto.setLoginName(accountVo.getUserId());
            //保存到investerUser合约
            InvestorUserDTO investorUserDTO = InvestorUserHelper.constructInvestorUserDTO(custInfoDto);
            boolean success = userDao.openAccount(investorUserDTO);
            if (!success) {
                return new BaseResp(20000, "同步开户数据错误");
            }
            resMap.put("isAccount", 1);
            resMap.put("custType", investorUserDTO.getUserType());
            resMap.put("userId", accountVo.getUserId());
            resMap.put("custNo", custInfoDto.getCustno());
            resMap.put("custMerno", custInfoDto.getCustomerno());
            resMap.put("account", custInfoDto.getTaaccountid());
        }

        return resp;
    }

    @Override
    public BaseResp openAccount(OpenAccountVo accountVo) {
        String userId = null;
        BaseRspDTO rspDTO = null;
        BaseResp resp = null;
        userId = accountVo.getUserId();
        String custId = "";
        if (StringUtils.isNotBlank(userId)) {
            //调用OTCP系统查询账户信息
            AusQueryDTO queryDTO = new AusQueryDTO(1, 1);
            queryDTO.setName(userId);
            rspDTO = queryManagerService.getRegisterinfo(queryDTO);
            resp = BaseRespUtils.convertBaseRspDTO2BaseResp(rspDTO);
            Data data = (Data) resp.getData();
            if (0 != resp.getRet() || data.getItems().isEmpty()) {
                resp.setRet(10000);
                resp.setMessage("用户未注册，不能开户");
                return resp;
            }
            custId = (String) ((Map) data.getItems().get(0)).get("custid");
        }
//        String certType = "";
//        if (accountVo.getCertType() == 1) {
//            certType = "0";
//        } else if (accountVo.getCertType() == 2) {
//            certType = "2";
//        } else if (accountVo.getCertType() == 3) {
//            certType = "N";
//        }

//        String transactorCertType = "";
//        if (accountVo.getTransactorCertType() != null) {
//            if (accountVo.getTransactorCertType() == 1) {
//                transactorCertType = "0";
//            } else if (accountVo.getTransactorCertType() == 2) {
//                transactorCertType = "2";
//            } else if (accountVo.getTransactorCertType() == 3) {
//                transactorCertType = "N";
//            }
//        }

        String type = "";
        if (accountVo.getType() != null && accountVo.getType().intValue() != 0) {
            if (accountVo.getType() == 1) {
                type = "0";
            } else if (accountVo.getType() == 2) {
                type = "1";
            } else if (accountVo.getType() == 3) {
                type = "1";
            }
        }
        AccountDTO accountDTO = new AccountDTO();
        accountDTO.setBranchcode(accountVo.getBranchCode());
        accountDTO.setBranchcodeflag((accountVo.getBranchCodeFlag() - 1) + "");
        accountDTO.setCertificateno(accountVo.getCertNo());
        accountDTO.setCertificatetype(accountVo.getCertType());
        accountDTO.setChinesename(accountVo.getName());
        accountDTO.setCurrencytype(accountVo.getCurrencyCode() + "");
        accountDTO.setCustid(custId);
        accountDTO.setEmail(accountVo.getEmail());
        accountDTO.setIndividualorinstitution(type);
        accountDTO.setMobile(accountVo.getMobile());
        accountDTO.setPassword(accountVo.getPwd());
        accountDTO.setTradepassword(accountVo.getTransPwd());
        accountDTO.setTransactorcertno(accountVo.getTransactorCertNo());
        accountDTO.setTransactorcerttype(accountVo.getTransactorCertType());
        accountDTO.setTransactormobiletel(accountVo.getTransactorMobile());
        accountDTO.setTransactorname(accountVo.getTransactorName());
        accountDTO.setTransactortelno(accountVo.getTransactorTel());
        //调用OTCP系统开户
        rspDTO = accountManagerService.addAccount(accountDTO);
        if (0 != rspDTO.getCode()) {
            //调用股交接口失败发送消息
            this.sendMessage(userId, "key5", "key6", accountVo.getType(), null);
            return BaseRespUtils.convertBaseRspDTO2BaseResp(rspDTO);
        }
        CustinfoQueryDTO dto = new CustinfoQueryDTO(1, 1);
        dto.setCustomerno((String) rspDTO.getData());

        //调用OTCP系统查询账户信息
        BaseRspDTO baseRspDTO = queryManagerService.getCustinfo(dto);
        if (0 != baseRspDTO.getCode()) {
            return BaseRespUtils.convertBaseRspDTO2BaseResp(baseRspDTO);
        }
        //处理账号信息
        List list = (List) baseRspDTO.getData();
        resp = new BaseResp<>();
        Map<String, Object> resMap = new HashMap<>();
        resp.setData(resMap);
        if (!CollectionUtils.isEmpty(list)) {
            Map userMap = (Map) list.get(0);
            CustInfoDTO custInfoDto = ObjectUtil.convertMapToBean(userMap, CustInfoDTO.class);
            String customerno = custInfoDto.getCustomerno();
            if (custInfoDto.getIndividualorinstitution().equals("1") || custInfoDto.getIndividualorinstitution().equals("0")) {
                custInfoDto.setCustType(1);
            }
            custInfoDto.setLoginName(userId);
            custInfoDto.setCustId(custId);
            //保存到investerUser合约
            InvestorUserDTO investorUserDTO = InvestorUserHelper.constructInvestorUserDTO(custInfoDto);
            boolean success = userDao.openAccount(investorUserDTO);
            if (!success) {
                return new BaseResp(20000, "同步开户数据错误");
            }
            //查询investeruser
            resMap.put("isAccount", 1);
            resMap.put("custType", accountVo.getType());
            resMap.put("custNo", custInfoDto.getCustno());
            resMap.put("custMerno", custInfoDto.getCustomerno());
            resMap.put("account", custInfoDto.getTaaccountid());
            resMap.put("userId", userId);
            resMap.put("custId", custId);
            resMap.put("type", type);
        }

        if (StringUtils.isNotBlank(userId)) {
            //查询investeruser
            LibInvestorUser investerUser = selectInvestorUserByLoginName(userId);
            resMap.put("status", investerUser.getStatus());
            resMap.put("addressId", investerUser.getUserId());
        }
        //消息埋点
        if (null != rspDTO && rspDTO.getCode() == 0) {
            this.sendMessage(userId, "key3", "key4", accountVo.getType(), null);
        } else {
            this.sendMessage(userId, "key5", "key6", accountVo.getType(), null);
        }
        return resp;
    }

    private LibInvestorUser getUserinfoById(String userId) {
        LibInvestorUser investorUserInfo = userDao.selectInvestorUserByName(userId);
        if (investorUserInfo == null) {
            throw new ServiceException("用户不存在");
        }
        return investorUserInfo;
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

    private LibUser getSysUserinfoById(String userId) {
        LibUser libUser = sysUserDao.findByAccount(userId);
        if (libUser == null) {
            throw new ServiceException("用户不存在");
        }
        return libUser;
    }

    @Override
    public BaseResp queryCustAccount(QueryCustAccountVo accountVo) {
        CustinfoQueryDTO custinfoQueryDTO = new CustinfoQueryDTO(1, 1);
        custinfoQueryDTO.setTaaccountid(accountVo.getAccount());
        custinfoQueryDTO.setCertificateno(accountVo.getCertNo());
        custinfoQueryDTO.setCustno(accountVo.getCustno());
        BaseResp resp = new BaseResp<>();
        if(StringUtils.isBlank(accountVo.getAccount()) && StringUtils.isBlank(accountVo.getCertNo())
        		&& StringUtils.isBlank(accountVo.getCustno())){
        	return resp; 
        }
        //调用OTCP系统查询账户信息
        BaseRspDTO rspDTO = queryManagerService.getCustinfo(custinfoQueryDTO);
        List list = (List) rspDTO.getData();
        
        if (CollectionUtils.isEmpty(list)) {
            QueryCustAccountRespDTO queryCustAccountRespDTO = new QueryCustAccountRespDTO();
            resp.setData(queryCustAccountRespDTO);
        } else {
            Map userMap = (Map) list.get(0);
            Map map = ObjectUtil.convertMap(userMap, QueryCustAccountRespDTO.class);
            resp.setData(map);
        }
        return resp;
    }

    @Override
    public BaseResp modifyCustMajorFlow(ModifyCustMajorFlowVo majorinfoVo) {
//		LibInvestorUser userInfo = getUserinfoById(majorinfoVo.getUserId());
        //调用股交流程start接口
        BaseRspDTO startRes = businessHandleService.getPrpcessStartRsp(majorinfoVo.getCreator(), majorinfoVo.getSource(), majorinfoVo.getProcessDefinitionKey(), majorinfoVo, null);
        // 如果是PCI ，则需要插入数据到合约  TODO
        if (JuDataTypeUtils.checkDataCode(startRes)) {
            if (majorinfoVo.getSource().equalsIgnoreCase("PCI")) {
                InsertInverUserInfoDTO insertInverUserInfoDTO = new InsertInverUserInfoDTO();
//				insertInverUserInfoDTO.setAccountId(majorinfoVo.getName());
                insertInverUserInfoDTO.setCertNo(majorinfoVo.getCertNo());
                insertInverUserInfoDTO.setCertType(majorinfoVo.getCertType());
                insertInverUserInfoDTO.setFileIdList(majorinfoVo.getFileIds());
                insertInverUserInfoDTO.setFilePathList(majorinfoVo.getFilePaths());
//				insertInverUserInfoDTO.setLoginName(majorinfoVo.getName());
                insertInverUserInfoDTO.setUserName(majorinfoVo.getName());
                insertInverUserInfoDTO.setOrderId(majorinfoVo.getCreator());
                insertInverUserInfoDTO.setUserType(majorinfoVo.getCustType());
//				insertInverUserInfoDTO.setSubmitterId(submitterId);
                // 插入流水信息
                boolean insertInverstorUserInfo = userDao.insertInverstorUserInfo(insertInverUserInfoDTO);
            }
        }
        return BaseRespUtils.convertBaseRspDTO2BaseResp(startRes);
//		//调用OTCP系统修改账户重要信息
//		String certType = "";
//		if(majorinfoVo.getCertType().equals("1")){
//			certType = "0";
//		} else if(majorinfoVo.getCertType().equals("2")){
//			certType = "2";
//		} else if(majorinfoVo.getCertType().equals("3")){
//			certType = "N";
//		}
//		ImportantInfoUpdateDTO updateDTO = new ImportantInfoUpdateDTO(majorinfoVo.getCustmerno(),majorinfoVo.getCustno(),certType,majorinfoVo.getName(),String.valueOf(majorinfoVo.getCertType()),String.valueOf(majorinfoVo.getCertNo()),majorinfoVo.getCustmerno());
//		BaseRspDTO rspDTO = accountManagerService.updateImportantInfo(updateDTO);
//		return BaseRespUtils.convertBaseRspDTO2BaseResp(rspDTO);
    }

    @Override
    public BaseResp resetTransPwd(ResetTransPwdVo pwdVo) {
        String custmerno = "";
        if (StringUtils.isNotBlank(pwdVo.getUserId())) {
            LibInvestorUser userInfo = getUserinfoById(pwdVo.getUserId());
            custmerno = userInfo.getCustmerno();
        } else {
            custmerno = pwdVo.getCustmerno();
        }
        //调用OTCP系统重置交易密码
        BaseRspDTO rspDTO = accountManagerService.resetPassword(custmerno, pwdVo.getNewPwd());
        return BaseRespUtils.convertBaseRspDTO2BaseResp(rspDTO);
    }

    @Override
    public BaseResp updatePwd(UpdatePwdVo pwdVo) {
        LibInvestorUser userInfo = null;
        BaseRspDTO update = null;
        Object[] objects = new Object[0];
        userInfo = getUserinfoById(pwdVo.getUserId());
        //调用OTCP系统更改登录密
        UpdateDTO updateDTO = new UpdateDTO(userInfo.getLoginName(), pwdVo.getOldPwd(), pwdVo.getNewPwd(), "1000");
        update = visitorService.update(updateDTO);
        //消息埋点
        objects = new Object[]{DateTimeUtil.dateTimeToStr(new Date(), DateTimeUtil.STR_DATETIME_PATTERN)};
        if (JuDataTypeUtils.checkDataCode(update)) {
            this.sendMessage(pwdVo.getUserId(), "key7", "key7", Integer.parseInt(userInfo.getUserType()), objects);
        } else {
            this.sendMessage(pwdVo.getUserId(), "key8", "key8", Integer.parseInt(userInfo.getUserType()), objects);
        }
        return BaseRespUtils.convertBaseRspDTO2BaseResp(update);
    }

    @Override
    public BaseResp updateTransPwd(UpdateTransPwdVo pwdVo) {
//        LibInvestorUser userInfo = getUserinfoById(pwdVo.getUserId());
        Map registerinfo = getRegisterinfo(pwdVo.getUserId());
        //调用OTCP系统更改交易密码
//		BaseRspDTO rspDTO = accountManagerService.updatePassword(userInfo.getCustmerno(), pwdVo.getOldPwd(), pwdVo.getNewPwd());
        BaseRspDTO rspDTO = accountManagerService.updatebyname((String) registerinfo.get("loginName"), pwdVo.getOldPwd(), pwdVo.getNewPwd());
        //消息埋点
        Object[] objects = {DateTimeUtil.dateTimeToStr(new Date(), DateTimeUtil.STR_DATETIME_PATTERN)};
        if (JuDataTypeUtils.checkDataCode(rspDTO)) {
            this.sendMessage(pwdVo.getUserId(), "key9", "key9", Integer.parseInt(this.getUserType((String) registerinfo.get("userType"))), objects);
        } else {
            this.sendMessage(pwdVo.getUserId(), "key10", "key10", Integer.parseInt(this.getUserType((String) registerinfo.get("userType"))), objects);
        }
        return BaseRespUtils.convertBaseRspDTO2BaseResp(rspDTO);
    }

    @Override
    public BaseResp resetLoginPwd(ResetLoginPwdVo pwdVo) {
        String name = pwdVo.getLoginName();
        if (!StringUtils.isEmpty(pwdVo.getMobile())) {
            //调用OTCP系统查询账户信息
            AusQueryDTO queryDTO = new AusQueryDTO(1, 1);
            queryDTO.setMobiletelno(pwdVo.getMobile());
            BaseRspDTO rspDTO = queryManagerService.getRegisterinfo(queryDTO);
            if (0 != rspDTO.getCode()) {
                return new BaseResp(rspDTO.getCode(), rspDTO.getMessage());
            }
            List<Map> ret = (List<Map>) rspDTO.getData();
            if (!CollectionUtils.isEmpty(ret)) {
                QueryTouristRespDTO dto = ObjectUtil.convertMapToBean(ret.get(0), QueryTouristRespDTO.class);
                name = dto.getLoginName();
            }
        }
        //调用OTCP系统重置登录密码
        BaseRspDTO rspDTO = visitorService.resetPwd(name, pwdVo.getNewPwd());
        return BaseRespUtils.convertBaseRspDTO2BaseResp(rspDTO);
    }

    @Override
    public BaseResp verifySMS(VerifySMSVo smsVo) {
        // 从redis获取验证码进行校验
        String verifyCode = redisCacheService.get(SmsType.getTypeByCode(smsVo.getType()), smsVo.getMobile());
        if (!"999999".equals(smsVo.getVerifyCode()) && !ObjectUtils.equals(smsVo.getVerifyCode(), verifyCode)) {
            throw new ServiceException("验证码错误");
        }
        return new BaseResp();
    }

    @Override
    public BaseResp queryCustInfo(QueryCustInfoVo custinfoVo) {
//		LibInvestorUser userInfo = getUserinfoById(custinfoVo.getUserId());
        //调用OTCP系统查询账户信息
        AusQueryDTO ausQueryDTO = new AusQueryDTO(1, 1);
        ausQueryDTO.setName(custinfoVo.getUserId());
        BaseRspDTO baseRspDTO = queryManagerService.getRegisterinfo(ausQueryDTO);
        BaseResp baseResp = BaseRespUtils.convertBaseRspDTO2BaseResp(baseRspDTO);
        if (0 != baseResp.getRet()) {
            return baseResp;
        }
        Data data = (Data) baseResp.getData();
        String customerno = (String) ((Map) data.getItems().get(0)).get("customerno");
        String mobiletelno = (String) ((Map) data.getItems().get(0)).get("mobiletelno");

        CustinfoQueryDTO custinfoQueryDTO = new CustinfoQueryDTO(1, 1);
        custinfoQueryDTO.setCustomerno(customerno);
        // 调用OTCP系统查询账户信息
        BaseRspDTO rspDTO = queryManagerService.getCustinfo(custinfoQueryDTO);
        if (0 != rspDTO.getCode()) {
            return BaseRespUtils.convertBaseRspDTO2BaseResp(rspDTO);
        }
        List list = (List) rspDTO.getData();
        BaseResp resp = new BaseResp<>();
        if (!CollectionUtils.isEmpty(list)) {
            Map userMap = (Map) list.get(0);
            Map map = ObjectUtil.convertMap(userMap, QueryCustInfoRespDTO.class);
            //手机号为注册手机号
            map.put("mobile", mobiletelno);
            resp.setData(map);
        }
        return resp;
    }

    @Override
    public BaseResp queryNoCashStream(QueryNoCashStreamVo statementVo) {
        String custno = "";
        BaseResp baseResp = new BaseResp();
        if (StringUtils.isNotBlank(statementVo.getUserId())) {
//            LibInvestorUser userInfo = getUserinfoById(statementVo.getUserId());
//            custno = userInfo.getCustno();
            Map<String, String> registerinfo = this.getRegisterinfo(statementVo.getUserId());
            custno = registerinfo.get("custno");
            if(StringUtils.isBlank(custno)){
           	 return baseResp;
            }
        } else if (StringUtils.isNotBlank(statementVo.getAccount())) {
            custno = "00" + statementVo.getAccount();
        }

        //如果是企业用户必填产品代码
        if (StringUtils.isNotBlank(statementVo.getType())) {
            if (StringUtils.isBlank(statementVo.getFundCode())) {
                baseResp.setRet(1000);
                baseResp.setMessage("企业用户产品代码必填");
                return baseResp;
            }
            String account = this.getFundAccount(statementVo.getFundCode());
            if (StringUtils.isBlank(account)) {
                return baseResp;
            }
            //如果产品代码和本人不匹配则直接返回
            if (!account.equals(statementVo.getApply())) {
                return baseResp;
            }
        }

        //将产品名称转换成产品代码进行查询
        if (StringUtils.isNotBlank(statementVo.getFundName()) && StringUtils.isBlank(statementVo.getFundCode())) {
            String fundCode = this.getFundCode(statementVo.getFundName());
            if (StringUtils.isBlank(fundCode)) {
                return baseResp;
            } else {
                statementVo.setFundCode(fundCode);
            }
        }

        CustLogQueryDTO custLogQueryDTO = new CustLogQueryDTO(statementVo.getPageSize(), statementVo.getPageNumber());
        custLogQueryDTO.setBegindate(statementVo.getBeginDate());
        custLogQueryDTO.setEnddate(statementVo.getEndDate());
        custLogQueryDTO.setBusinesscode(statementVo.getBusinessCode());
        custLogQueryDTO.setFundcode(statementVo.getFundCode());
        custLogQueryDTO.setCustno(custno);
        // 调用OTCP系统查询客户非资金流水信息
        BaseRspDTO rspDTO = queryManagerService.getCustLog(custLogQueryDTO);
        return BaseRespUtils.convertBaseRspDTO2BaseResp(rspDTO, QueryNoCashStreamRespDTO.class);
    }

    @Override
    public BaseResp queryCS(QueryCSVo statementVo) {
        if (StringUtils.isNotBlank(statementVo.getFundName()) && StringUtils.isBlank(statementVo.getFundCode())) {
            String fundCode = this.getFundCode(statementVo.getFundName());
            if (StringUtils.isBlank(fundCode)) {
                BaseResp baseResp = new BaseResp();
                return baseResp;
            } else {
                statementVo.setFundCode(fundCode);
            }
        }
        ProcessTasksDTO processTasksDTO = new ProcessTasksDTO(statementVo.getPageNumber() - 1, statementVo.getPageSize(), null != statementVo.getCreator() ? statementVo.getCreator() : null);
        processTasksDTO.setAssignee(null != statementVo.getAssignee() ? statementVo.getAssignee() : null);
        processTasksDTO.setCandidateGroups(null != statementVo.getCandidateGroups() ? statementVo.getCandidateGroups() : null);
        processTasksDTO.setEndCreateDate(null != statementVo.getEndDate() ? statementVo.getEndDate() : null);
        processTasksDTO.setStartCreateDate(null != statementVo.getCreDate() ? statementVo.getCreDate() : null);
        processTasksDTO.setProcessDefinitionKeys(null != statementVo.getProcessDefinitionKey() ? ProcessDefinitionEnums.getName(statementVo.getProcessDefinitionKey()) : null);
        processTasksDTO.setStatus(null != statementVo.getStatus() ? statementVo.getStatus() : null);
        processTasksDTO.setTaskStatus(null != statementVo.getTaskStatus() ? statementVo.getTaskStatus() : null);
        processTasksDTO.setEntCode(null != statementVo.getFundCode() ? statementVo.getFundCode() : null);
        // 调用OTCP系统查询客户非资金流水信息
        BaseRspDTO rspDTO = processService.tasks(processTasksDTO);
        return BaseRespUtils.convertBaseRspDTO2BaseResp(rspDTO, QueryCSRespDTO.class);
    }

    @Override
    public BaseResp queryCSDetail(QueryCSDetailVo statementDetailVo) {
        ProcessTasksDTO processTasksDTO = new ProcessTasksDTO();
        processTasksDTO.setProcessDefinitionKeys(null != statementDetailVo.getProcessDefinitionKey() ? statementDetailVo.getProcessDefinitionKey() : null);
        processTasksDTO.setBusinessKey(statementDetailVo.getBusinessKey());
        processTasksDTO.setPageNumber(0);
        processTasksDTO.setPageSize(1);
        // 调用OTCP系统查询客户非资金流水详情
        BaseRspDTO rspDTO = processService.tasks(processTasksDTO);
        List list = (List<?>) ((Map) rspDTO.getData()).get("content");

        BaseResp resp = new BaseResp<>();
        Map jsonObject = new HashMap();
        if (list == null || list.size() == 0) {
            Data data = new Data();
            data.setItems(list);
            resp.setData(data);
        } else {
        	jsonObject = JsonUtils.readValue(JsonUtils.toJson(list.get(0)), Map.class);
		}
        
        if (jsonObject.containsKey("formData")) {
            String formData = jsonObject.get("formData").toString();
            Map object = JsonUtils.readValue(formData, Map.class);
            //补充字段信息
            if (!object.containsKey("auditContent")) {
                object.put("auditContent", "{}");
            }
            // 查询产品详细信息
            FundinfoQueryDTO fundinfoQueryDTO = new FundinfoQueryDTO();
            fundinfoQueryDTO.setFundcode(JuDataTypeUtils.getStringValue(object.get("fundCode")));
            fundinfoQueryDTO.setFundname(JuDataTypeUtils.getStringValue(object.get("fundName")));
            fundinfoQueryDTO.setFundtype(JuDataTypeUtils.getStringValue(object.get("fundType")));
            fundinfoQueryDTO.setTaaccountid(JuDataTypeUtils.getStringValue(object.get("account")));
            fundinfoQueryDTO.setRows(10);
            fundinfoQueryDTO.setPageNo(1);
            BaseRspDTO capitalinfo = queryManagerService.getFundinfo(fundinfoQueryDTO);
            List<?> fundInfoList = (List<?>) capitalinfo.getData();
            Map capMap = null;
            if (fundInfoList == null || fundInfoList.size() == 0) {
                capMap = new HashMap();
            } else {
                capMap = JsonUtils.readValue(JsonUtils.toJson(fundInfoList.get(0)), Map.class);
            }
            // 返回信息中添加原股本信息
            object.put("firstShares", capMap.get("totalshares"));
            resp.setData(object);
        }
        return resp;
    }

    @Override
    public BaseResp queryCSLog(QueryCSLogVo statementLogVo) {
        // 调用OTCP系统查询客户流水审核日志
        BaseRspDTO rspDTO = processService.logs(statementLogVo.getBusinessKey());
        return BaseRespUtils.convertBaseRspDTO2BaseResp(rspDTO, QueryCSLogRespDTO.class);
    }

    @Override
    public BaseResp revoke(RevokeVo revokeVo) {
        String assignee = "";
        if (StringUtils.isNotBlank(revokeVo.getUserId())) {
//            LibInvestorUser userInfo = getUserinfoById(revokeVo.getUserId());
//            assignee = userInfo.getAccountId();
            Map<String, String> registerinfo = this.getRegisterinfo(revokeVo.getUserId());
            assignee = registerinfo.get("accoundId");
        } else if (StringUtils.isNotBlank(revokeVo.getAssignee())) {
            assignee = revokeVo.getAssignee();
        } else {
            BaseResp baseResp = new BaseResp();
            baseResp.setRet(1000);
            baseResp.setMessage("userId和assignee必传其一");
        }

        // 调用OTCP系统审核流程
        BaseRspDTO rspDTO = processService.claim(assignee, revokeVo.getBusinessKey(), revokeVo.getStatus(), revokeVo.getTaskStatus());
        return BaseRespUtils.convertBaseRspDTO2BaseResp(rspDTO);
    }

    @Override
    public BaseResp queryCustStock(QueryCustStockVo stockVo) {
        String account = "";
        if (StringUtils.isNotBlank(stockVo.getUserId())) {
            LibInvestorUser userInfo = getUserinfoById(stockVo.getUserId());
            account = userInfo.getAccountId();
        } else {
            account = stockVo.getAccount();
        }

        //如果是企业用户必填产品代码
        BaseResp baseResp = new BaseResp();
        if (StringUtils.isNotBlank(stockVo.getType())) {
            if (StringUtils.isBlank(stockVo.getFundCode())) {
                baseResp.setRet(1000);
                baseResp.setMessage("企业用户产品代码必填");
                return baseResp;
            }
            String accountTemp = this.getFundAccount(stockVo.getFundCode());
            if (StringUtils.isBlank(accountTemp)) {
                return baseResp;
            }
            //如果产品代码和本人不匹配则直接返回
            if (!accountTemp.equals(stockVo.getApply())) {
                return baseResp;
            }
        }

        if (StringUtils.isNotBlank(stockVo.getFundName()) && StringUtils.isBlank(stockVo.getFundCode())) {
            String fundCode = this.getFundCode(stockVo.getFundName());
            if (StringUtils.isBlank(fundCode)) {
                return baseResp;
            } else {
                stockVo.setFundCode(fundCode);
            }
        }

        SharesinfoQueryDTO sharesinfoQueryDTO = new SharesinfoQueryDTO(account, stockVo.getPageSize(), stockVo.getPageNumber());//todo
        sharesinfoQueryDTO.setNaturecode(stockVo.getStockNature());
        sharesinfoQueryDTO.setFundcode(stockVo.getFundCode());
        // 调用OTCP系统查询账户对应股权信息
        BaseRspDTO rspDTO = queryManagerService.getSharesinfo(sharesinfoQueryDTO);
        return BaseRespUtils.convertBaseRspDTO2BaseResp(rspDTO, QueryCustStockRespDTO.class);
    }

    @Override
    public BaseResp modifyCust(ModifyCustVo custVo) {
        String custmerno = "";
        String custno = "";
        if (StringUtils.isNotBlank(custVo.getUserId())) {
//            LibInvestorUser userInfo = getUserinfoById(custVo.getUserId());
//            custmerno = userInfo.getCustmerno();
//            custno = userInfo.getCustno();
            Map<String, String> registerinfo = this.getRegisterinfo(custVo.getUserId());
            custmerno = registerinfo.get("custmerno");
            custno = registerinfo.get("custno");
        } else if (StringUtils.isNotBlank(custVo.getCustMerno())) {
            //调用接口获取代码
            custmerno = custVo.getCustMerno();
            CustinfoQueryDTO queryDTO = new CustinfoQueryDTO(1, 1);
            queryDTO.setCustomerno(custmerno);

            //调用OTCP系统查询账户信息
            BaseRspDTO rspDTO = queryManagerService.getCustinfo(queryDTO);
            if (0 != rspDTO.getCode()) {
                return BaseRespUtils.convertBaseRspDTO2BaseResp(rspDTO);
            }
            //处理账号信息
            List list = (List) rspDTO.getData();
            Map userMap = (Map) list.get(0);
            CustInfoDTO custInfoDto = ObjectUtil.convertMapToBean(userMap, CustInfoDTO.class);
            custno = custInfoDto.getCustno();
        } else {
            BaseResp baseResp = new BaseResp();
            baseResp.setRet(1000);
            baseResp.setMessage("custmerno和userId必传其一");
            return baseResp;
        }


        GeneralInfoUpdateDTO generalInfoUpdateDTO = new GeneralInfoUpdateDTO(custmerno, custno, custVo.getCustType(), custVo.getContactPhone(), custVo.getAddress(), custmerno);
        // 调用OTCP系统更新客户一般信息
        BaseRspDTO rspDTO = accountManagerService.updateGeneralInfo(generalInfoUpdateDTO);
        return BaseRespUtils.convertBaseRspDTO2BaseResp(rspDTO);
    }

    @Override
    public BaseResp queryCust(QueryCustInfoVo custVo) {
        LibInvestorUser userInfo = getUserinfoById(custVo.getUserId());
        BaseResp resp = new BaseResp(new QueryCustRespDTO(userInfo.getCustId(), userInfo.getCustno(), userInfo.getCustmerno(), userInfo.getAccountId()));
        return resp;
    }

    @Override
    public BaseResp complete(CompleteVo completeVo) {
        String assignee = "";
        String userType = "";
        LibInvestorUser userInfo = new LibInvestorUser();;
        if (StringUtils.isNotBlank(completeVo.getUserId())) {
//            userInfo = getUserinfoById(completeVo.getUserId());
//            assignee = userInfo.getAccountId();
            Map<String, String> registerinfo = this.getRegisterinfo(completeVo.getUserId());
            assignee = registerinfo.get("accoundId");
            userType = registerinfo.get("userType");
            userInfo.setUserType(userType);
        } else if (StringUtils.isNotBlank(completeVo.getAssignee())) {
            assignee = completeVo.getAssignee();
        } else {
            BaseResp baseResp = new BaseResp();
            baseResp.setRet(1000);
            baseResp.setMessage("userId和assignee必传其一");
            return baseResp;
        }
        // 调用OTCP系统审核流程
        ProcessCompleteDTO processCompleteDTO = new ProcessCompleteDTO(assignee, completeVo.getBusinessKey(), Integer.parseInt(completeVo.getCheckFlag()));
        processCompleteDTO.setComment(completeVo.getComment());
        processCompleteDTO.setFormData(completeVo.getAuditContent());
        processCompleteDTO.setStatus(completeVo.getStatus());
        processCompleteDTO.setTaskStatus(completeVo.getTaskStatus());
        BaseRspDTO rspDTO = processService.complete(processCompleteDTO);
        // 发送消息参数
        ProcessTasksDTO dto = new ProcessTasksDTO();
        dto.setBusinessKey(completeVo.getBusinessKey());// 表单id
        BaseRspDTO tasks = processService.tasks(dto);// 获取任务列表
//        ProcessStartResData readValue = JsonUtils.readValue(JsonUtils.toJson(tasks.getData()), ProcessStartResData.class);
        Map<String, List<LinkedHashMap>> data = (Map<String, List<LinkedHashMap>>) tasks.getData();
        ProcessStartResData readValue = JsonUtils.readValue(JsonUtils.toJson(data.get("content").get(0)), ProcessStartResData.class);
        
        // 如果传的是Assignee而不是userId
        this.createUserType(completeVo, userInfo, readValue);
        
        String date = DateTimeUtil.dateTimeToStr(new Date(), DateTimeUtil.STR_DATETIME_PATTERN);
        Object[] objects = {date, readValue.getProcessName(), completeVo.getBusinessKey()};
        boolean flag = (completeVo.getAssignee() == null || "".equals(completeVo.getAssignee()) ? assignee : completeVo.getAssignee()).equals(readValue.getCreator());
        if (0 == rspDTO.getCode() && "-1".equals(completeVo.getCheckFlag()) && flag) {
            // 业务撤回成功
            super.sendMessage(completeVo.getUserId(), "key14", "key14", Integer.parseInt(userInfo.getUserType()), objects);
        }
        if (0 != rspDTO.getCode() && "-1".equals(completeVo.getCheckFlag()) && flag) {
            // 业务撤回失败
            super.sendMessage(completeVo.getUserId(), "key15", "key15", Integer.parseInt(userInfo.getUserType()), objects);
        }
        if (0 != rspDTO.getCode() && "1".equals(completeVo.getCheckFlag())) {
            // 业务通过
            super.sendMessage(completeVo.getUserId(), "key16", "key16", Integer.parseInt(userInfo.getUserType()), objects);
        }
        if (0 != rspDTO.getCode() && "2".equals(completeVo.getCheckFlag())) {
            // 业务驳回
            super.sendMessage(completeVo.getUserId(), "key17", "key17", Integer.parseInt(userInfo.getUserType()), objects);
        }
        if (0 != rspDTO.getCode() && "-1".equals(completeVo.getCheckFlag()) && !flag) {
            // 业务关闭
            super.sendMessage(completeVo.getUserId(), "key19", "key19", Integer.parseInt(userInfo.getUserType()), objects);
        }
        return BaseRespUtils.convertBaseRspDTO2BaseResp(rspDTO);
    }

	private void createUserType(CompleteVo completeVo, LibInvestorUser userInfo, ProcessStartResData readValue) {
		if (StringUtils.isNotBlank(completeVo.getAssignee())) {
        	// 获取账号
			String creator = readValue.getCreator();
			CustinfoQueryDTO custinfoQueryDTO = new CustinfoQueryDTO(1, 1);
			custinfoQueryDTO.setTaaccountid(creator);
			// 根据账号查询账户信息
			BaseRspDTO custinfo = queryManagerService.getCustinfo(custinfoQueryDTO);
			List<Map<String, String>> custInfoDataList = (List<Map<String, String>>) custinfo.getData();
			if (custInfoDataList != null && custInfoDataList.size() > 0) {
				// 设置相应的用户类型
				userInfo.setUserType(custInfoDataList.get(0).get("individualorinstitution"));
			}
        }
	}

    @Override
    public BaseResp modifyCustMajor(ModifyCustMajorVo custMajorVo) {
        //调用OTCP系统修改账户重要信息
        ImportantInfoUpdateDTO updateDTO = new ImportantInfoUpdateDTO(custMajorVo.getCustmerno(), custMajorVo.getCustno(), String.valueOf(custMajorVo.getCustType()), custMajorVo.getName(), custMajorVo.getCertType(), String.valueOf(custMajorVo.getCertNo()), custMajorVo.getCustmerno());
        BaseRspDTO rspDTO = accountManagerService.updateImportantInfo(updateDTO);
        return BaseRespUtils.convertBaseRspDTO2BaseResp(rspDTO);
    }

    @Override
    public BaseResp updateStatus(UpdateStatusVo statusVo) {
        String status = "";
        if (statusVo.getStatus().intValue() == 1) {
            status = "0";
        } else if (statusVo.getStatus().intValue() == 2) {
            status = "3";
        } else if (statusVo.getStatus().intValue() == 3) {
            status = "*";
        }
        BaseRspDTO rspDTO = visitorService.statusChange(statusVo.getCustId(), status);
        return BaseRespUtils.convertBaseRspDTO2BaseResp(rspDTO);
    }

    @Override
    public BaseResp updateMobile(UpdateMobileVo mobileVo) {
        ResetTelNoDTO resetTelNoDTO = new ResetTelNoDTO(mobileVo.getLoginName(), mobileVo.getOriMobile(), mobileVo.getNewMoblie());
        BaseRspDTO rspDTO = visitorService.resetTelNo(resetTelNoDTO);
        return BaseRespUtils.convertBaseRspDTO2BaseResp(rspDTO);
    }

    @Override
    public BaseResp bindAccount(BindAccountVo bindAccountVo) {
        BaseRspDTO rspDTO = accountManagerService.custbinding(bindAccountVo.getCustId(), bindAccountVo.getCustmerno());
        return BaseRespUtils.convertBaseRspDTO2BaseResp(rspDTO);
    }

    @Override
    public BaseResp queryTourist(QueryTouristVo touristVo) {
        String status = null;
        if (touristVo.getStatus().equals("1")) {
            status = "0";
        } else if (touristVo.getStatus().equals("2")) {
            status = "1";
        } else if (touristVo.getStatus().equals("3")) {
            status = "3";
        } else if (touristVo.getStatus().equals("4")) {
            status = "*";
        }
        AusQueryDTO ausQueryDTO = new AusQueryDTO(touristVo.getPageSize(), touristVo.getPageNumber(), touristVo.getCustId(), touristVo.getLoginName(), touristVo.getMobile(), status, touristVo.getBeginDate(), touristVo.getEndDate());
        BaseRspDTO rspDTO = queryManagerService.getRegisterinfo(ausQueryDTO);
        return BaseRespUtils.convertBaseRspDTO2BaseResp(rspDTO, QueryTouristRespDTO.class);
    }

	/*@Override
    public BaseResp login(LoginVo loginVo) {
		String userId = StringUtils.isEmpty(loginVo.getLoginName())?loginVo.getMobile():loginVo.getLoginName();
		LibUser userInfo = getSysUser(userId);
		LoginRespDTO dto = new LoginRespDTO(userId,1,2,1);
		dto.setId(userInfo.getLoginName());
		BaseResp rspDTO = new BaseResp(dto);
		return rspDTO;
	}*/

    public BaseResp queryTourist(String loginName, String custId) {
        AusQueryDTO queryDTO = new AusQueryDTO(1, 1);
        queryDTO.setName(loginName);
        queryDTO.setCustid(custId);
        BaseRspDTO rspDTO = queryManagerService.getRegisterinfo(queryDTO);
        return BaseRespUtils.convertBaseRspDTO2BaseResp(rspDTO, QueryTouristRespDTO.class);
    }

    @Override
    public BaseResp queryInvestor(QueryInvestorVo investorVo) {
        CustinfoQueryDTO custinfoQueryDTO = new CustinfoQueryDTO(investorVo.getPageSize(), investorVo.getPageNumber());
        //用户名不为空或者手机不为空则查询
        if (StringUtils.isNotBlank(investorVo.getAccountName())) {
            //调用OTCP系统查询账户信息
            AusQueryDTO ausQueryDTO = new AusQueryDTO(1, 1);
            if (PhoneFormatCheckUtils.isChinaPhoneLegal(investorVo.getAccountName())) {
                ausQueryDTO.setMobiletelno(investorVo.getAccountName());
            } else {
                ausQueryDTO.setName(investorVo.getAccountName());
            }
            BaseRspDTO baseRspDTO = queryManagerService.getRegisterinfo(ausQueryDTO);
            BaseResp baseResp = BaseRespUtils.convertBaseRspDTO2BaseResp(baseRspDTO);
            Data data = (Data) baseResp.getData();
            if (0 != baseResp.getRet() || data.getItems().size() == 0) {
                return baseResp;
            }
            String customerno = (String) ((Map) data.getItems().get(0)).get("customerno");
            custinfoQueryDTO.setCustomerno(customerno);
        }
        custinfoQueryDTO.setIndividualorinstitution(investorVo.getAccountType());
        custinfoQueryDTO.setCuststatus(investorVo.getStatus());
        custinfoQueryDTO.setBegindate(investorVo.getBeginDate());
        custinfoQueryDTO.setEnddate(investorVo.getEndDate());
        custinfoQueryDTO.setTaaccountid(investorVo.getAccount());
        custinfoQueryDTO.setCertificateno(investorVo.getCertNo());
        custinfoQueryDTO.setCustno(investorVo.getCustNo());
        BaseRspDTO rspDTO = queryManagerService.getCustinfo(custinfoQueryDTO);
        return BaseRespUtils.convertBaseRspDTO2BaseResp(rspDTO, QueryInvestorRespDTO.class);
    }

    @Override
    public BaseResp extendInfo(ExtendInfoVO extendInfoVO) {
        ExtendInfoDTO extendInfoDTO = new ExtendInfoDTO();

        CustinfoQueryDTO queryDTO = new CustinfoQueryDTO(1, 1);
        queryDTO.setCustomerno(extendInfoVO.getCustmerno());

        //调用OTCP系统查询账户信息
        BaseRspDTO rspDTO = queryManagerService.getCustinfo(queryDTO);
        if (0 != rspDTO.getCode()) {
            return BaseRespUtils.convertBaseRspDTO2BaseResp(rspDTO);
        }
        //处理账号信息
        List list = (List) rspDTO.getData();
        Map userMap = (Map) list.get(0);
        CustInfoDTO custInfoDto = ObjectUtil.convertMapToBean(userMap, CustInfoDTO.class);
        String custno = custInfoDto.getCustno();


        extendInfoDTO.setCity(JuDataTypeUtils.getString(extendInfoVO.getCityId()));
        extendInfoDTO.setCustno(custno);
        extendInfoDTO.setCustomerno(extendInfoVO.getCustmerno());
        extendInfoDTO.setNationality("CHN");
        extendInfoDTO.setInstrepraddress(JuDataTypeUtils.getString(extendInfoVO.getCorporationAddress()));
        extendInfoDTO.setInstrepremailaddress(JuDataTypeUtils.getString(extendInfoVO.getCorporationEmiall()));
        extendInfoDTO.setInstreprmanagerange(JuDataTypeUtils.getString(extendInfoVO.getScopeOfBusiness()));
        extendInfoDTO.setInstreprmobiletel(JuDataTypeUtils.getString(extendInfoVO.getCorporationMobile()));
        extendInfoDTO.setInstreprname(JuDataTypeUtils.getString(extendInfoVO.getCorporation()));
        extendInfoDTO.setInstreprofficetelno(JuDataTypeUtils.getString(extendInfoVO.getCorporationTel()));
        extendInfoDTO.setNationality(JuDataTypeUtils.getString(extendInfoDTO.getNationality()));
        extendInfoDTO.setNetassets(JuDataTypeUtils.getString(extendInfoVO.getNetAssets()));
        extendInfoDTO.setOperid(JuDataTypeUtils.getString(extendInfoVO.getOperId()));
        extendInfoDTO.setProvince(JuDataTypeUtils.getString(extendInfoVO.getProvinceId()));
        extendInfoDTO.setRegisteraddress(JuDataTypeUtils.getString(extendInfoVO.getRegisterAddress()));
        extendInfoDTO.setRegisterdate(JuDataTypeUtils.getString(extendInfoVO.getRegisterDate()));
        extendInfoDTO.setRegisteredcptl(JuDataTypeUtils.getString(extendInfoVO.getRegisterCapital()));
        extendInfoDTO.setWebsite(JuDataTypeUtils.getString(extendInfoVO.getWebSite()));
        BaseRspDTO baseRspDTO = accountManagerService.extendInfo(extendInfoDTO);
        return BaseRespUtils.convertBaseRspDTO2BaseResp(baseRspDTO);
    }

    @Override
    public LibInvestorUser selectInvestorUserByLoginName(String loginName) {
        LibInvestorUser investorUser = userDao.selectInvestorUserByName(loginName);
        if (investorUser == null) {
            investorUser = new LibInvestorUser();
        }
        LibUser userInfo = sysUserDao.findByAccount(loginName);
        if (userInfo == null) {
        	userInfo = new LibUser();
        } else {
        	investorUser.setUserId(userInfo.getUserAddr());
        }
        return investorUser;
    }

    @Override
    public LibInvestorUser selectInvestorUserById(String userId) {
        LibInvestorUser investorUser = userDao.selectInvestorUserById(userId);
        if (investorUser == null) {
            investorUser = new LibInvestorUser();
        }
        LibUser userInfo = sysUserDao.findByAccount(investorUser.getLoginName());
        investorUser.setUserId(userInfo.getUserAddr());
        return investorUser;
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
    public BaseResp addEmployee(AddEmployeeVo addEmployeeVo) {
        AddEmployeeDTO addEmployeeDTO = new AddEmployeeDTO(addEmployeeVo.getJobno(),
                addEmployeeVo.getName(),
                addEmployeeVo.getPassword(),
                addEmployeeVo.getDepartment(),
                addEmployeeVo.getOperId());
        addEmployeeDTO.setCertificateno(JuDataTypeUtils.getString(addEmployeeVo.getCertNo()));
        addEmployeeDTO.setCertificatetype(JuDataTypeUtils.getString(addEmployeeVo.getCertType()));
        BaseRspDTO respDTO = visitorService.addEmployee(addEmployeeDTO);
        return BaseRespUtils.convertBaseRspDTO2BaseResp(respDTO);
    }

    @Override
    public BaseResp queryEmployeeList(QueryEmployeeListVo queryEmployeeListVo) {
        QueryEmployeeListDTO queryEmployeeListDTO = new QueryEmployeeListDTO(queryEmployeeListVo.getPageSize(), queryEmployeeListVo.getPageNumber());
        queryEmployeeListDTO.setCertificateno(JuDataTypeUtils.getString(queryEmployeeListVo.getCertificateno()));
        queryEmployeeListDTO.setEmployeename(JuDataTypeUtils.getString(queryEmployeeListVo.getEmployeename()));
        queryEmployeeListDTO.setEmployeeno(JuDataTypeUtils.getString(queryEmployeeListVo.getEmployeeno()));
        BaseRspDTO respDTO = queryManagerService.queryEmployeeList(queryEmployeeListDTO);
        return BaseRespUtils.convertBaseRspDTO2BaseResp(respDTO);
    }

    @Override
    public BaseResp queryEmployee(QueryEmployeeVo queryEmployeeVo) {
        BaseRspDTO respDTO = queryManagerService.queryEmployee(queryEmployeeVo.getCustomerno());
        return BaseRespUtils.convertBaseRspDTO2BaseResp(respDTO);
    }

    @Override
    public BaseResp authentication(AuthenticationVO authenticationVO) {
        AuthenticationDTO authenticationDTO = new AuthenticationDTO();
        authenticationDTO.setCertificateno(authenticationVO.getCertNo());
        authenticationDTO.setCertificatetype(authenticationVO.getCertType());
        authenticationDTO.setCustid(authenticationVO.getCustId());
        authenticationDTO.setCustomerno(authenticationVO.getCustMerno());
        BaseRspDTO respDTO =  accountManagerService.authentication(authenticationDTO);
        return BaseRespUtils.convertBaseRspDTO2BaseResp(respDTO);
    }

    @Override
    public BaseResp capitalOpen(CapitalOpenVO authenticationVO) {
        CapitalOpenDTO capitalOpenDTO = new CapitalOpenDTO();
        capitalOpenDTO.setCharge(authenticationVO.getCharge());
        capitalOpenDTO.setCurrencytype(authenticationVO.getCurrencyType());
        capitalOpenDTO.setCustomerno(authenticationVO.getCustMerno());
        capitalOpenDTO.setOperid(authenticationVO.getOperId());
        capitalOpenDTO.setPaymethod(authenticationVO.getPaymethod());
        capitalOpenDTO.setPwdflag(authenticationVO.getPwdFlag());
        capitalOpenDTO.setRatetype(authenticationVO.getRateType());
        capitalOpenDTO.setTradepassword(authenticationVO.getTransPwd());
        capitalOpenDTO.setTransactorcertno(authenticationVO.getTransactorCertNo());
        capitalOpenDTO.setTransactorcerttype(authenticationVO.getTransactorCertType());
        capitalOpenDTO.setTransactormobiletel(authenticationVO.getTransactorMobile());
        capitalOpenDTO.setTransactorname(authenticationVO.getTransactorName());
        capitalOpenDTO.setTransactortelno(authenticationVO.getTransactorTel());
        capitalOpenDTO.setUseflag(authenticationVO.getUseFlag());
        capitalOpenDTO.setYsje(authenticationVO.getYsje());
        BaseRspDTO respDTO =  accountManagerService.capitalOpen(capitalOpenDTO);
        return BaseRespUtils.convertBaseRspDTO2BaseResp(respDTO);
    }

    @Override
    public BaseResp custBindingInfo(CustBindingInfoVO custBindingInfoVO) {
        BaseRspDTO respDTO =  accountManagerService.custBindingInfo(custBindingInfoVO.getCertNo(),custBindingInfoVO.getCustMerno());
        return BaseRespUtils.convertBaseRspDTO2BaseResp(respDTO,CustBindingInfoRespDTO.class);
    }
}
