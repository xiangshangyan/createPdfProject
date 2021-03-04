package cn.yan.service.impl;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfPTable;
import cn.yan.service.AssemblePDFData;
import cn.yan.util.PDFConstant;
import cn.yan.util.PdfUtil;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * Desc:
 * Created by xiangshang on 2017/10/10.
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
        headInfo.put("firstHeadInfo","某某交易中心    托管公司（管理）");
        headInfo.put("secondHeadInfo","【非交易过户】凭证");
        return headInfo;
    }

    @Override
    public void createPdfTableData(Font fontChina12, PdfPTable table, Map resMap) {
		/* 产品代码 */
        PdfUtil.addTableCell(table, "产品代码", fontChina12, false, false, 0, 0);
        PdfUtil.addTableCell(table, "111111111", fontChina12, true, false, 2, 0);
		
		/* 产品名称 */
        PdfUtil.addTableCell(table, "产品名称", fontChina12, false, false, 0, 0);
        PdfUtil.addTableCell(table, "pdf名称信息", fontChina12, true, false, 2, 0);
		
		/* 发生日期 */
        PdfUtil.addTableCell(table, "发生日期", fontChina12, false, false, 0, 0);
        PdfUtil.addTableCell(table, "2020-12-30", fontChina12, true, false, 2, 0);
		

		/* 此处为空 */
        PdfUtil.addTableCell(table, " ", fontChina12, false, false, 0, 0);
        PdfUtil.addTableCell(table, " ", fontChina12, true, true, 2, 0);
		
		/* 客户资料 */
        PdfUtil.addTableCell(table, "出让方客户资料", fontChina12, true, false, 3, 0);
        PdfUtil.addTableCell(table, "受让方客户资料", fontChina12, true, false, 3, 0);

    }

    @Override
    public void createPdfEnd(Document document, BaseFont bfChinese, Font fontChina12, Paragraph blank1, Map resMap) throws DocumentException {
        // 底部信息  客户签名
        Paragraph pE = new Paragraph();
        // 操作柜员 与 业务章
        PdfUtil.addChunk( pE, fontChina12, "出让方姓名：", " ", 0);
        PdfUtil.addChunk( pE, fontChina12, "受让方姓名：", " ", 100);
        pE.setAlignment(Element.ALIGN_LEFT);
        document.add(pE);

        Paragraph pE1 = new Paragraph();
        // 操作柜员 与 业务章
        PdfUtil.addChunk( pE1, fontChina12, "操作柜员：", "张三", 0);
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
