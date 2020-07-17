package io.juzhen.service.impl;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfPTable;
import io.juzhen.channel.dto.querymanager.CustinfoQueryDTO;
import io.juzhen.service.AssemblePDFData;
import io.juzhen.util.JuDataTypeUtils;
import io.juzhen.util.PDFConstant;
import io.juzhen.util.PdfUtil;
import io.juzhen.vo.business.PrintOpenAccountVO;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * Desc:
 * Created by jinx on 2017/10/10.
 */
@Service
public class PrintOpenAccountServiceImpl extends AssemblePDFData {

    @Override
    protected <T> Object assembleCustinfoQueryDTO(T t) {
        PrintOpenAccountVO printOpenAccountVO = (PrintOpenAccountVO) t;
        CustinfoQueryDTO custinfoQueryDTO = new CustinfoQueryDTO(10, 1);
        // 客户内码
        custinfoQueryDTO.setCustomerno(printOpenAccountVO.getCustMerno());
        return custinfoQueryDTO;
    }


    @Override
    protected Map getHeadInfo(Map map) {
        Map<String, String> headInfo = new HashMap<>();
        headInfo.put("firstHeadInfo", "某某交易中心    浙江托管服务有限公司");
        headInfo.put("secondHeadInfo", "开户申请表");
        // 流水号
        headInfo.put("orderId", "");
        return headInfo;
    }

    @Override
    public void createPdfTableData(Font fontChina12, PdfPTable table, Map resMap) {
/* 客户姓名 */
        PdfUtil.addTableCell(table, "客户姓名", fontChina12, false, false, 0, 0);
        PdfUtil.addTableCell(table, JuDataTypeUtils.getStringValue(resMap.get("chinesename")), fontChina12, true, false, 3, 0);

		/* 资金账户 */
        PdfUtil.addTableCell(table, "资金账户", fontChina12, false, false, 0, 0);
        PdfUtil.addTableCell(table, JuDataTypeUtils.getStringValue(resMap.get("capitalaccount")), fontChina12, true, false, 3, 0);
		
		/* 登记账户 */
        PdfUtil.addTableCell(table, "登记账户", fontChina12, false, false, 0, 0);
        PdfUtil.addTableCell(table, JuDataTypeUtils.getStringValue(resMap.get("taaccountid")), fontChina12, true, false, 3, 0);
		
		/* 证件类型 */
        PdfUtil.addTableCell(table, "证件类型", fontChina12, false, false, 0, 0);
        PdfUtil.addTableCell(table, JuDataTypeUtils.getStringValue(resMap.get("certificatetypename")), fontChina12, true, false, 3, 0);
		
		/* 证件号码 */
        PdfUtil.addTableCell(table, "证件号码", fontChina12, false, false, 0, 0);
        PdfUtil.addTableCell(table, JuDataTypeUtils.getStringValue(resMap.get("certificateno")), fontChina12, true, false, 3, 0);
		
		/* 联系电话 */
        PdfUtil.addTableCell(table, "联系电话", fontChina12, false, false, 0, 0);
        PdfUtil.addTableCell(table, JuDataTypeUtils.getStringValue(resMap.get("telno")), fontChina12, true, false, 3, 0);
		
		/* 地址 */
        PdfUtil.addTableCell(table, "地址", fontChina12, false, false, 0, 0);
        PdfUtil.addTableCell(table, JuDataTypeUtils.getStringValue(resMap.get("address")), fontChina12, true, false, 7, 0);
    }

    @Override
    public void createPdfEnd(Document document, BaseFont bfChinese, Font fontChina12, Paragraph blank1, Map resMap) throws DocumentException {
        PrintOpenAccountVO printOpenAccountVO = (PrintOpenAccountVO)resMap.get(PrintOpenAccountVO.class.getSimpleName()) ;
        // 底部信息  客户签名
        Paragraph pE = new Paragraph("以上打印的客户信息与业务内容已由本人核实确认无误。", fontChina12);
        PdfUtil.addChunk( pE, fontChina12, "客户签名：", " ", 18);
        pE.setAlignment(Element.ALIGN_LEFT);
        document.add(pE);

        Paragraph pE1 = new Paragraph();
        // 操作柜员 与 业务章
        PdfUtil.addChunk( pE1, fontChina12, "操作柜员：", printOpenAccountVO.getOperName(), 0);
        PdfUtil.addChunk( pE1, fontChina12, "业务章：", " ", 100);
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
        return PDFConstant.TABLE_COLUMN_NUMBER_8;
    }
}
