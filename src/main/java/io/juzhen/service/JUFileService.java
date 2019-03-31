package io.juzhen.service;

import io.juzhen.base.dto.BaseResp;
import io.juzhen.dto.business.UploadFileReqDTO;

/**
 * @author niujsj
 * 文件管理
 */
@SuppressWarnings("all")
public interface JUFileService {

	/**
	 * 上传文件到OTPC和文件代理
	 * @param uploadFileReqDTO
	 * @return
	 */
	BaseResp uploadFile(UploadFileReqDTO uploadFileReqDTO);
}
