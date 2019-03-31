package io.juzhen.service.impl;

import io.juzhen.base.dto.BaseReq;
import io.juzhen.base.dto.BaseResp;
import io.juzhen.channel.dto.BaseRspDTO;
import io.juzhen.channel.service.FileService;
import io.juzhen.dto.business.UploadFileReqDTO;
import io.juzhen.dto.business.UploadFileRespDTO;
import io.juzhen.service.JUFileService;
import io.juzhen.util.BaseRespUtils;
import io.juzhen.utils.UUIDUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.file.Paths;
import java.util.Map;
@SuppressWarnings("all")
@Service
public class JUFileServiceImpl extends AbstractService implements JUFileService {

	@Value("${jzFileServerServiceUrl}")
	String jzFileServerServiceUrl;
	
	@Autowired
	FileService fileService;
	
	@Override
	public BaseResp uploadFile(UploadFileReqDTO uploadFileReqDTO) {
		//调用OTCP系统上传文件，返回filepath
		BaseRspDTO rspDTO = fileService.uploadFile(uploadFileReqDTO.getFile());
		if (0!=rspDTO.getCode()){
			return BaseRespUtils.convertBaseRspDTO2BaseResp(rspDTO);
		}
		BaseReq<UploadFileReqDTO> req = new BaseReq<>();
		req.setMsgDetail(uploadFileReqDTO);
		req.setAndGetSignatureRSA(String.valueOf(Paths.get(walletFileDTO.getWalletPath(), walletFileDTO.getPfxFile())));
		//调用文件代理服务
		/**String jzFileServerServiceUrlRes = SendHttpMsgUtils.postHttp(jzFileServerServiceUrl + "/uploadFile",
				JsonUtils.toJson(req));
		System.out.println("response:" + jzFileServerServiceUrlRes);
		JSONObject jsonObject = JSONObject.parseObject(jzFileServerServiceUrlRes);
		JSONObject object = (JSONObject) JSONObject.parseObject((String) jsonObject.get("msg"));
		int ret = (int) object.get("ret");
		String message = (String) object.get("message");
		String fileId = (String) object.get("fileId");
		BaseResp resp = new BaseResp(ret, message);**/
		
		/**********************挡板开始**********************/ //TODO 挡板
		BaseResp resp = new BaseResp<>(0, "成功");
		String fileId = UUIDUtils.getUUID();
		int ret = 0;
		/***********************挡板结束*************************/
		//上传成功后构建文件响应对象（fileId，filePath）
		if (ret == 0) {
			resp.setData(new UploadFileRespDTO(fileId, (String) ((Map)rspDTO.getData()).get("filePath"), (String) ((Map)rspDTO.getData()).get("localFilePath")));
		}
		return resp;
	}

}
