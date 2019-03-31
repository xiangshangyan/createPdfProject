package io.juzhen.service.impl;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfPTable;
import io.juzhen.service.AssemblePDFData;
import io.juzhen.util.PDFConstant;
import io.juzhen.util.PdfUtil;
import io.juzhen.utils.DateTimeUtil;
import io.juzhen.vo.business.PrintTransferStockVO;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * Desc:
 * Created by jinx on 2017/10/10.
 */
@Service
public class PrintTransferStockServiceImpl extends AssemblePDFData {
    
    @Override
    protected <T> Object assembleCustinfoQueryDTO(T t) {
        return null;
    }

    @Override
    protected Map getHeadInfo(Map map) {
        Map<String,String> headInfo = new HashMap<>();
        String className = PrintTransferStockVO.class.getSimpleName();
        headInfo.put("firstHeadInfo","浙江股权交易中心    托管公司（管理）");
        headInfo.put("secondHeadInfo","【非交易过户】凭证");
        headInfo.put("orderId",((PrintTransferStockVO)map.get(className)).getOrderId());
        return headInfo;
    }

    @Override
    public void createPdfTableData(Font fontChina12, PdfPTable table, Map resMap) {
        PrintTransferStockVO printTransferStockVO = (PrintTransferStockVO) resMap.get(PrintTransferStockVO.class.getSimpleName());
		/* 产品代码 */
        PdfUtil.addTableCell(table, "产品代码", fontChina12, false, false, 0, 0);
        PdfUtil.addTableCell(table, printTransferStockVO.getFundCode(), fontChina12, true, false, 2, 0);
		
		/* 产品名称 */
        PdfUtil.addTableCell(table, "产品名称", fontChina12, false, false, 0, 0);
        PdfUtil.addTableCell(table, printTransferStockVO.getFundName(), fontChina12, true, false, 2, 0);
		
		/* 发生日期 */
        PdfUtil.addTableCell(table, "发生日期", fontChina12, false, false, 0, 0);
        PdfUtil.addTableCell(table, PdfUtil.getDateFormatString(DateTimeUtil.STR_DATE_PATTERN_LONG), fontChina12, true, false, 2, 0);
		
		/* 发生时间 */
        PdfUtil.addTableCell(table, "发生时间", fontChina12, false, false, 0, 0);
        PdfUtil.addTableCell(table, PdfUtil.getDateFormatString(DateTimeUtil.STR_TIME_PATTERN_LONG), fontChina12, true, false, 2, 0);
		
		/* 过户数量 */
        PdfUtil.addTableCell(table, "过户数量", fontChina12, false, false, 0, 0);
        PdfUtil.addTableCell(table, printTransferStockVO.getOutNum(), fontChina12, true, false, 2, 0);
		
		/* 此处为空 */
        PdfUtil.addTableCell(table, " ", fontChina12, false, false, 0, 0);
        PdfUtil.addTableCell(table, " ", fontChina12, true, true, 2, 0);
		
		/* 客户资料 */
        PdfUtil.addTableCell(table, "出让方客户资料", fontChina12, true, false, 3, 0);
        PdfUtil.addTableCell(table, "受让方客户资料", fontChina12, true, false, 3, 0);
		
		/* 客户号，先出让方，后受让方 */
        PdfUtil.addTableCell(table, "客户号", fontChina12, false, false, 0, 0);
        PdfUtil.addTableCell(table, printTransferStockVO.getInAccount(), fontChina12, true, true, 2, 0);
        PdfUtil.addTableCell(table, "客户号", fontChina12, false, false, 0, 0);
        PdfUtil.addTableCell(table, printTransferStockVO.getOutAccount(), fontChina12, true, true, 2, 0);
		
		/* 客户姓名，先出让方，后受让方 */
        PdfUtil.addTableCell(table, "客户姓名", fontChina12, false, false, 0, 0);
        PdfUtil.addTableCell(table, printTransferStockVO.getInAccountName(), fontChina12, true, true, 2, 0);
        PdfUtil.addTableCell(table, "客户姓名", fontChina12, false, false, 0, 0);
        PdfUtil.addTableCell(table, printTransferStockVO.getOutAccountName(), fontChina12, true, true, 2, 0);
		
		/* 证件号码 ，先出让方，后受让方*/
        PdfUtil.addTableCell(table, "证件号码", fontChina12, false, false, 0, 0);
        PdfUtil.addTableCell(table, printTransferStockVO.getInCertType(), fontChina12, true, true, 2, 0);
        PdfUtil.addTableCell(table, "证件号码", fontChina12, false, false, 0, 0);
        PdfUtil.addTableCell(table, printTransferStockVO.getOutCertNo(), fontChina12, true, true, 2, 0);
		
		/* 登记账号，先出让方，后受让方 */
        PdfUtil.addTableCell(table, "登记账号", fontChina12, false, false, 0, 0);
        PdfUtil.addTableCell(table, printTransferStockVO.getInAccount(), fontChina12, true, true, 2, 0);
        PdfUtil.addTableCell(table, "登记账号", fontChina12, false, false, 0, 0);
        PdfUtil.addTableCell(table, printTransferStockVO.getOutAccount(), fontChina12, true, true, 2, 0);
		
		/* 份额性质  */
        PdfUtil.addTableCell(table, "转出份额性质", fontChina12, false, false, 0, 0);
        PdfUtil.addTableCell(table, printTransferStockVO.getInStockNatureName(), fontChina12, true, true, 2, 0);
        PdfUtil.addTableCell(table, "转入份额性质", fontChina12, false, false, 0, 0);
        PdfUtil.addTableCell(table, printTransferStockVO.getOutStockNatureName(), fontChina12, true, true, 2, 0);
    }

    @Override
    public void createPdfEnd(Document document, BaseFont bfChinese, Font fontChina12, Paragraph blank1, Map resMap) throws DocumentException {
        PrintTransferStockVO printTransferStockVO = (PrintTransferStockVO) resMap.get(PrintTransferStockVO.class.getSimpleName());
        // 底部信息  客户签名
        Paragraph pE = new Paragraph();
        // 操作柜员 与 业务章
        PdfUtil.addChunk( pE, fontChina12, "出让方姓名：", " ", 0);
        PdfUtil.addChunk( pE, fontChina12, "受让方姓名：", " ", 100);
        pE.setAlignment(Element.ALIGN_LEFT);
        document.add(pE);

        Paragraph pE1 = new Paragraph();
        // 操作柜员 与 业务章
        PdfUtil.addChunk( pE1, fontChina12, "操作柜员：", printTransferStockVO.getOperName(), 0);
        pE.setAlignment(Element.ALIGN_LEFT);
        document.add(pE1);

        document.add(blank1);
        document.add(blank1);
        document.add(blank1);
        document.add(blank1);

        // 注
        StringBuilder sb2 = new StringBuilder();
        sb2.append("第一联：托管公司联（白） ");
        sb2.append("第二联：股份公司联（红）");
        sb2.append("第三联：转出方联（绿）");
        sb2.append("第四联：转入方联（绿）");
        Paragraph pZ = new Paragraph(sb2.toString(), new Font(bfChinese, 10));
        pZ.setAlignment(Element.ALIGN_CENTER);
        document.add(pZ);
    }

    @Override
    protected int getTableColumnNumber() {
        return PDFConstant.TABLE_COLUMN_NUMBER_6;
    }
}
