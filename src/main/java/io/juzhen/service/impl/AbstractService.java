package io.juzhen.service.impl;

import io.juzhen.base.dto.WalletFileDTO;
import io.juzhen.base.exception.ServiceException;
import io.juzhen.dao.SysUserDao;
import io.juzhen.dao.UserDao;
import io.juzhen.dto.MessageDto;
import io.juzhen.helper.MessageHelper;
import io.juzhen.po.LibInvestorUser;
import io.juzhen.po.LibUser;

import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;

public abstract class AbstractService {

    protected final Logger logger = LoggerFactory.getLogger(this.getClass().getName());

    @Autowired
    protected WalletFileDTO walletFileDTO;
    @Autowired
    private SysUserDao sysUserDao;
    @Autowired
    private UserDao userDao;
    @Autowired
    private MessageSource messageSource;

    /**
     * 发送消息
     *
     * @param userId
     * @param appKey
     * @param webKey
     * @param params
     */
    protected void sendMessage(String userId, String appKey, String webKey, int type, Object[] params) {
        MessageDto appMessageDto = null;
        MessageDto webMessageDto = null;
        if (null != appKey) {
            appMessageDto = new MessageDto(userId, appKey, type);
        }
        if (null != webKey) {
            webMessageDto = new MessageDto(userId, webKey, type);
        }
        if (params != null && params.length > 0) {
            appMessageDto.setParams(params);
            webMessageDto.setParams(params);
        }
        if (null != appKey && type == 1) {
            MessageHelper.sendAppMessage(appMessageDto);
        }
        if (null != webKey && type != 1) {
            MessageHelper.sendWebMessage(webMessageDto);
        }
    }

    protected String getMessage(String messageKey, Object... params) {
        return messageSource.getMessage(messageKey, params, LocaleContextHolder.getLocale());
    }

    protected String getMessage(String messageKey, Locale locale, Object... params) {
        return messageSource.getMessage(messageKey, params, locale);
    }

    protected LibUser getSysUser(String loginName) {
        LibUser user = sysUserDao.findByAccount(loginName);
        if (user == null){
        	throw new ServiceException("用户不存在");
        }
        return user;
    }

    public LibInvestorUser selectInvestorUserByCustmerno(String custmerNo) {
        return userDao.selectInvestorUserByCustmerno(custmerNo);
    }
    
}
