package io.juzhen.service.impl;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfPTable;
import io.juzhen.channel.dto.querymanager.CustinfoQueryDTO;
import io.juzhen.service.AssemblePDFData;
import io.juzhen.util.JuDataTypeUtils;
import io.juzhen.util.PDFConstant;
import io.juzhen.util.PdfUtil;
import io.juzhen.utils.DateTimeUtil;
import io.juzhen.vo.business.PrintModifuCustVO;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * Desc:
 * Created by jinx on 2017/9/28.
 */
@Service
public class PrintModifyCustServiceImpl extends AssemblePDFData {

	@Override
	protected <T> CustinfoQueryDTO assembleCustinfoQueryDTO(T t) {
		PrintModifuCustVO companyInfoVo = (PrintModifuCustVO) t;
		CustinfoQueryDTO custinfoQueryDTO = new CustinfoQueryDTO(10, 1);
		custinfoQueryDTO.setTaaccountid(companyInfoVo.getAccount());
		return custinfoQueryDTO;
	}

	@Override
	protected Map getHeadInfo(Map map) {
		Map<String,String> headInfo = new HashMap<>();
		String className = PrintModifuCustVO.class.getSimpleName();
		headInfo.put("firstHeadInfo","浙江股权交易中心    托管公司（管理）");
		headInfo.put("secondHeadInfo","【修改客户重要资料】凭证");
		headInfo.put("orderId",((PrintModifuCustVO)map.get(className)).getOrderId());
		return headInfo;
	}
	
	@SuppressWarnings("rawtypes")
	@Override
	public void createPdfTableData(Font fontChina12, PdfPTable table,Map resMap) {
		PrintModifuCustVO printModifuCustVO = (PrintModifuCustVO) resMap.get(PrintModifuCustVO.class.getSimpleName());
		/* 客户信息 */
		PdfUtil.addTableCell(table, "客户信息", fontChina12, false, true, 0, 4);// 跨四行
		
		/* 客户姓名 */
		PdfUtil.addTableCell(table, "客户姓名", fontChina12, false, false, 0, 0);
		PdfUtil.addTableCell(table, JuDataTypeUtils.getStringValue(resMap.get("chinesename")), fontChina12, true, false, 2, 0);
		
		/* 客户代码 */
		PdfUtil.addTableCell(table, "客户代码", fontChina12, false, false, 0, 0);
		PdfUtil.addTableCell(table, JuDataTypeUtils.getStringValue(resMap.get("custno")), fontChina12, true, false, 2, 0);
		
		/* 证件类别 */
		PdfUtil.addTableCell(table, "证件类型", fontChina12, false, false, 0, 0);
		PdfUtil.addTableCell(table, JuDataTypeUtils.getStringValue(resMap.get("certificatetypename")), fontChina12, true, false, 2, 0);
		
		/* 证件号码 */
		PdfUtil.addTableCell(table, "证件号码", fontChina12, false, false, 0, 0);
		PdfUtil.addTableCell(table, JuDataTypeUtils.getStringValue(resMap.get("certificateno")), fontChina12, true, false, 2, 0);
		
		/* 通讯地址 */
		PdfUtil.addTableCell(table, "通讯地址", fontChina12, false, true, 0, 2);
		PdfUtil.addTableCell(table, JuDataTypeUtils.getStringValue(resMap.get("address")), fontChina12, true, true, 2, 2);
		
		/* 联系电话 */
		PdfUtil.addTableCell(table, "联系电话", fontChina12, false, true, 0, 2);
		PdfUtil.addTableCell(table, JuDataTypeUtils.getStringValue(resMap.get("telno")), fontChina12, true, true, 2, 2);
		
		/* ------------------以下为业务内容 ---------------*/
		PdfUtil.addTableCell(table, "业务内容", fontChina12, false, true, 0, 6);
		
		/* 发生日期 */
		PdfUtil.addTableCell(table, "发生日期", fontChina12, false, false, 0, 0);
		PdfUtil.addTableCell(table, PdfUtil.getDateFormatString(DateTimeUtil.STR_DATE_PATTERN_LONG), fontChina12, true, false, 2, 0);
		
		/* 发生时间 */
		PdfUtil.addTableCell(table, "发生时间", fontChina12, false, false, 0, 0);
		PdfUtil.addTableCell(table, PdfUtil.getDateFormatString(DateTimeUtil.STR_TIME_PATTERN_LONG), fontChina12, true, false, 2, 0);
		
		/* 产品名称 */
		PdfUtil.addTableCell(table, "修改项", fontChina12, false, false, 0, 0);
		PdfUtil.addTableCell(table, printModifuCustVO.getModifyBefore(), fontChina12, true, false, 2, 0);
		
		/* 产品代码 */
		PdfUtil.addTableCell(table, "修改后值", fontChina12, false, false, 0, 0);
		PdfUtil.addTableCell(table, printModifuCustVO.getModifyAfter(), fontChina12, true, false, 2, 0);
		
		/* 份额性质 */
		PdfUtil.addTableCell(table, "客户名称", fontChina12, false, false, 0, 0);
		PdfUtil.addTableCell(table, " ", fontChina12, true, false, 5, 0); // TODO ？？？？
		
		/* 冻结份额 */
		PdfUtil.addTableCell(table, "客户简称", fontChina12, false, false, 0, 0);
		PdfUtil.addTableCell(table, " ", fontChina12, true, false, 5, 0); // TODO ？？？？
	}

	@Override
	public void createPdfEnd(Document document, BaseFont bfChinese,
			Font fontChina12, Paragraph blank1, Map resMap) throws DocumentException {
		// 底部信息  客户签名
		Paragraph pE = new Paragraph("以上打印的客户信息与业务内容已由本人核实确认无误。", fontChina12);
		PdfUtil.addChunk(pE, fontChina12, "客户签名：", " ", 18);
		pE.setAlignment(Element.ALIGN_LEFT);
		document.add(pE);

		Paragraph pE1 = new Paragraph();
		// 操作柜员 与 业务章
		PdfUtil.addChunk( pE1, fontChina12, "操作柜员：", "", 0);
		PdfUtil.addChunk( pE1, fontChina12, "复核柜员：", "", 40);
		PdfUtil.addChunk( pE1, fontChina12, "业务章：", " ", 40);
		pE.setAlignment(Element.ALIGN_LEFT);
		document.add(pE1);

		document.add(blank1);
		document.add(blank1);
		document.add(blank1);
		document.add(blank1);

		// 注
		StringBuilder sb2 = new StringBuilder();
		sb2.append("第一联：托管公司联（白） ");
		sb2.append("第二联：业务留存联（红）");
		sb2.append("第三联：客户联（蓝）");
		Paragraph pZ = new Paragraph(sb2.toString(), new Font(bfChinese, 10));
		pZ.setAlignment(Element.ALIGN_CENTER);
		document.add(pZ);				
	}

	@Override
	protected int getTableColumnNumber() {
		return PDFConstant.TABLE_COLUMN_NUMBER_7;
	}

}
