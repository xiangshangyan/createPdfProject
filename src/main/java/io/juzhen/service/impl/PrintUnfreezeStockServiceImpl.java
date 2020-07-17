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
import io.juzhen.vo.business.PrintUnfreeStockVO;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * Desc:
 * Created by jinx on 2017/10/10.
 */
@Service
public class PrintUnfreezeStockServiceImpl extends AssemblePDFData {

    @Override
    protected <T> Object assembleCustinfoQueryDTO(T t) {
        PrintUnfreeStockVO printUnfreeStockVO = (PrintUnfreeStockVO) t;
        CustinfoQueryDTO custinfoQueryDTO = new CustinfoQueryDTO(10, 1);
        custinfoQueryDTO.setTaaccountid(printUnfreeStockVO.getAccount());
        return custinfoQueryDTO;
    }

    @Override
    protected Map getHeadInfo(Map map) {
        Map<String, String> headInfo = new HashMap<>();
        String className = PrintUnfreeStockVO.class.getSimpleName();
        headInfo.put("firstHeadInfo", "某某交易中心    托管公司（管理）");
        headInfo.put("secondHeadInfo", "【份额解冻】凭证");
        headInfo.put("orderId", ((PrintUnfreeStockVO) map.get(className)).getOrderId());
        return headInfo;
    }

    @Override
    public void createPdfTableData(Font fontChina12, PdfPTable table, Map resMap) {
        PrintUnfreeStockVO printUnfreeStockVO = (PrintUnfreeStockVO) resMap.get(PrintUnfreeStockVO.class.getSimpleName());
        /* 客户信息 */
        PdfUtil.addTableCell(table, "客户信息", fontChina12, false, true, 0, 4);

		/* 投资者名称 */
        PdfUtil.addTableCell(table, "投资者名称", fontChina12, false, false, 0, 0);
        PdfUtil.addTableCell(table, JuDataTypeUtils.getStringValue(resMap.get("chinesename")), fontChina12, true, false, 2, 0);
		
		/* 登记账号 */
        PdfUtil.addTableCell(table, "登记账号", fontChina12, false, false, 0, 0);
        PdfUtil.addTableCell(table, JuDataTypeUtils.getStringValue(resMap.get("taaccountid")), fontChina12, true, false, 2, 0);
		
		/* 证件类别 */
        PdfUtil.addTableCell(table, "证件类别", fontChina12, false, false, 0, 0);
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
        PdfUtil.addTableCell(table, "产品名称", fontChina12, false, false, 0, 0);
        PdfUtil.addTableCell(table, printUnfreeStockVO.getFundName(), fontChina12, true, false, 2, 0);
		
		/* 产品代码 */
        PdfUtil.addTableCell(table, "产品代码", fontChina12, false, false, 0, 0);
        PdfUtil.addTableCell(table, printUnfreeStockVO.getFundCode(), fontChina12, true, false, 2, 0);
		
		/* 份额性质 */
        PdfUtil.addTableCell(table, "份额性质", fontChina12, false, false, 0, 0);
        PdfUtil.addTableCell(table, printUnfreeStockVO.getStockNatureName(), fontChina12, true, false, 2, 0);
		
		/* 冻结份额 */
        PdfUtil.addTableCell(table, "解冻份额", fontChina12, false, false, 0, 0);
        PdfUtil.addTableCell(table, printUnfreeStockVO.getUnfreezeNum(), fontChina12, true, false, 2, 0);
		
		/* 冻结类别 */
        PdfUtil.addTableCell(table, "原冻结类别", fontChina12, false, false, 0, 0);
        PdfUtil.addTableCell(table, printUnfreeStockVO.getFrostTypeName(), fontChina12, true, false, 2, 0);
		/* 这里为空 */
        PdfUtil.addTableCell(table, " ", fontChina12, false, false, 0, 0);
        PdfUtil.addTableCell(table, " ", fontChina12, true, false, 2, 0);
		
		/* 摘要 */
        PdfUtil.addTableCell(table, "摘要", fontChina12, false, false, 0, 0);
        PdfUtil.addTableCell(table, JuDataTypeUtils.getStringValue(printUnfreeStockVO.getRemark()), fontChina12, true, false, 5, 0);
    }

    @Override
    public void createPdfEnd(Document document, BaseFont bfChinese, Font fontChina12, Paragraph blank1, Map resMap) throws DocumentException {
        PrintUnfreeStockVO printUnfreeStockVO = (PrintUnfreeStockVO) resMap.get(PrintUnfreeStockVO.class.getSimpleName());
        // 底部信息  客户签名
        Paragraph pE = new Paragraph("以上打印的客户信息与业务内容已由本人核实确认无误。", fontChina12);
        PdfUtil.addChunk( pE, fontChina12, "客户签名：", " ", 18);
        pE.setAlignment(Element.ALIGN_LEFT);
        document.add(pE);

        Paragraph pE1 = new Paragraph();
        // 操作柜员 与 业务章
        PdfUtil.addChunk( pE1, fontChina12, "操作柜员：", printUnfreeStockVO.getOperName(), 0);
        PdfUtil.addChunk( pE1, fontChina12, "业务章：", " ", 100);
        pE.setAlignment(Element.ALIGN_LEFT);
        document.add(pE1);
    }

    @Override
    protected int getTableColumnNumber() {
        return PDFConstant.TABLE_COLUMN_NUMBER_7;
    }
}
