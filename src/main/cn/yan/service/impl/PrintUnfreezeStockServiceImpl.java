package cn.yan.service.impl;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfPTable;
import cn.yan.service.AssemblePDFData;
import cn.yan.util.JuDataTypeUtils;
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
public class PrintUnfreezeStockServiceImpl extends AssemblePDFData {

    @Override
    protected <T> Object assembleCustinfoQueryDTO(T t) {
        // 把T转换为需要的对象再进行返回
        return new Object();
    }

    @Override
    protected Map getHeadInfo(Map map) {
        Map<String, String> headInfo = new HashMap<>();
        headInfo.put("firstHeadInfo", "某某交易中心    托管公司（管理）");
        headInfo.put("secondHeadInfo", "【份额解冻】凭证");
        return headInfo;
    }

    @Override
    public void createPdfTableData(Font fontChina12, PdfPTable table, Map resMap) {
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
		
		/* 这里为空 */
        PdfUtil.addTableCell(table, " ", fontChina12, false, false, 0, 0);
        PdfUtil.addTableCell(table, " ", fontChina12, true, false, 2, 0);
    }

    @Override
    public void createPdfEnd(Document document, BaseFont bfChinese, Font fontChina12, Paragraph blank1, Map resMap) throws DocumentException {
        // 底部信息  客户签名
        Paragraph pE = new Paragraph("以上打印的客户信息与业务内容已由本人核实确认无误。", fontChina12);
        PdfUtil.addChunk( pE, fontChina12, "客户签名：", " ", 18);
        pE.setAlignment(Element.ALIGN_LEFT);
        document.add(pE);

        Paragraph pE1 = new Paragraph();
        // 操作柜员 与 业务章
        PdfUtil.addChunk( pE1, fontChina12, "业务章：", " ", 100);
        pE.setAlignment(Element.ALIGN_LEFT);
        document.add(pE1);
    }

    @Override
    protected int getTableColumnNumber() {
        return PDFConstant.TABLE_COLUMN_NUMBER_7;
    }
}
