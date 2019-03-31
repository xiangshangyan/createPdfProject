package io.juzhen.service;

import io.juzhen.base.dto.BaseResp;
import io.juzhen.po.LibInvestorUser;
import io.juzhen.vo.user.*;
import io.juzhen.vo.user.web.*;

/**
 * @author niujsj
 *         用户接口服务类
 */
@SuppressWarnings("all")
public interface UserService {

    /**
     * 短信发送
     *
     * @param smsVo
     * @return
     */
    BaseResp sendSMS(SendSMSVo smsVo);

    /**
     * 校验手机号和登录名
     *
     * @param msgVo
     * @return
     */
    BaseResp verifyMsg(VerifyMsgVo msgVo);

    /**
     * 用户注册
     *
     * @param registerVo
     * @return
     */
    BaseResp register(RegisterVo registerVo);

    /**
     * 查询账户信息
     *
     * @param accountVo
     * @return
     */
    BaseResp queryAccount(QueryAccountVo accountVo);

    /**
     * 个人用户开户
     *
     * @param accountVo
     * @return
     */
    BaseResp openAccount(OpenAccountVo accountVo);

    /**
     * 查询对应账户信息
     *
     * @param accountVo
     * @return
     */
    BaseResp queryCustAccount(QueryCustAccountVo accountVo);

    /**
     * 修改帐户重要信息
     *
     * @param majorinfoVo
     * @return
     */
    BaseResp modifyCustMajorFlow(ModifyCustMajorFlowVo majorinfoVo);

    /**
     * 重置交易密码
     *
     * @param pwdVo
     * @return
     */
    BaseResp resetTransPwd(ResetTransPwdVo pwdVo);

    /**
     * 更改登录密码
     *
     * @param pwdVo
     * @return
     */
    BaseResp updatePwd(UpdatePwdVo pwdVo);

    /**
     * 更改交易密码
     *
     * @param pwdVo
     * @return
     */
    BaseResp updateTransPwd(UpdateTransPwdVo pwdVo);

    /**
     * 重置登录密码
     *
     * @param pwdVo
     * @return
     */
    BaseResp resetLoginPwd(ResetLoginPwdVo pwdVo);

    /**
     * 校验短信验证码
     *
     * @param smsVo
     * @return
     */
    BaseResp verifySMS(VerifySMSVo smsVo);

    /**
     * 查询个人信息
     *
     * @param custinfoVo
     * @return
     */
    BaseResp queryCustInfo(QueryCustInfoVo custinfoVo);

    /**
     * 查询非资金流水信息
     *
     * @param statementVo
     * @return
     */
    BaseResp queryNoCashStream(QueryNoCashStreamVo statementVo);

    /**
     * 查询自己账户对应流水
     *
     * @param statementVo
     * @return
     */
    BaseResp queryCS(QueryCSVo statementVo);

    /**
     * 查询自己账户对应流水详情
     *
     * @param statementDetailVo
     * @return
     */
    BaseResp queryCSDetail(QueryCSDetailVo statementDetailVo);

    /**
     * 查询自己账户对应流水审核日志
     *
     * @param statementLogVo
     * @return
     */
    BaseResp queryCSLog(QueryCSLogVo statementLogVo);

    /**
     * 供客户撤销已提交，未审核的流水数据
     *
     * @param revokeVo
     * @return
     */
    BaseResp revoke(RevokeVo revokeVo);

    /**
     * 客户查询自己账户对应股权信息
     *
     * @param stockVo
     * @return
     */
    BaseResp queryCustStock(QueryCustStockVo stockVo);

    /**
     * 修改客户一般信息
     *
     * @param custVo
     * @return
     */
    BaseResp modifyCust(ModifyCustVo custVo);

    /**
     * 根据userId查询用户的客户内码、游客id
     *
     * @param custVo
     * @return
     */
    BaseResp queryCust(QueryCustInfoVo custVo);

    /**
     * 客户受理后处理审核数据
     *
     * @param completeVo
     * @return
     */
    BaseResp complete(CompleteVo completeVo);

    /**
     * 修改用户重要信息(web)
     *
     * @param custMajorVo
     * @return
     */
    BaseResp modifyCustMajor(ModifyCustMajorVo custMajorVo);

    /**
     * 用户修改状态(web)
     *
     * @param statusVo
     * @return
     */
    BaseResp updateStatus(UpdateStatusVo statusVo);

    /**
     * 用户修改登陆手机号(web)
     *
     * @param mobileVo
     * @return
     */
    BaseResp updateMobile(UpdateMobileVo mobileVo);

    /**
     * 用户和客户账户绑定(web)
     *
     * @param bindAccountVo
     * @return
     */
    BaseResp bindAccount(BindAccountVo bindAccountVo);

    /**
     * 供中心人员查询对应游客信息(web)
     *
     * @param touristVo
     * @return
     */
    BaseResp queryTourist(QueryTouristVo touristVo);

    /**
     * 供中心人员查询对应投资者账户信息(web)
     *
     * @param investorVo
     * @return
     */
    BaseResp queryInvestor(QueryInvestorVo investorVo);

//	BaseResp login(LoginVo loginVo);

    /**
     * 对于多余信息的扩展请求
     *
     * @param extendInfoVO
     * @return
     */
    BaseResp extendInfo(ExtendInfoVO extendInfoVO);

    /**
     * 查询游客信息
     *
     * @param loginName
     * @param custId
     * @return
     */
    BaseResp queryTourist(String loginName, String custId);

    /**
     * 查询合约获取用户信息
     *
     * @param id
     * @return
     */
    LibInvestorUser selectInvestorUserById(String id);


    /**
     * 查询合约获取用户信息
     *
     * @param loginName
     * @return
     */
    public LibInvestorUser selectInvestorUserByLoginName(String loginName);
    
    /**
     * 柜员开户
     * @param addEmployee 
     * @return
     */
	BaseResp addEmployee(AddEmployeeVo addEmployeeVo);
	
	/**
	 * 查询柜员信息列表
	 * @param queryEmployeeListVo
	 * @return
	 */
	BaseResp queryEmployeeList(QueryEmployeeListVo queryEmployeeListVo);
	
	/**
	 * 查询柜员信息
	 * @param queryEmployeeVo
	 * @return
	 */
	BaseResp queryEmployee(QueryEmployeeVo queryEmployeeVo);

    /**
     * 填写客户的实名信息表
     * @param authenticationVO
     * @return
     */
    BaseResp authentication(AuthenticationVO authenticationVO);

    /**
     * 单开资金账户
     * @param custBindingInfoVO
     * @return
     */
    BaseResp capitalOpen(CapitalOpenVO authenticationVO);

    /**
     * 根据证件号码查询是否已开户，是否已存在绑定关系，是否开通资金账户，是否实名接口使用说明
     * @param custBindingInfoVO
     * @return
     */
    BaseResp custBindingInfo(CustBindingInfoVO custBindingInfoVO);
}
