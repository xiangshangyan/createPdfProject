package io.juzhen.service.impl;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;

import io.juzhen.channel.dto.querymanager.CustinfoQueryDTO;
import io.juzhen.service.AssemblePDFData;
import io.juzhen.util.JuDataTypeUtils;
import io.juzhen.util.PDFConstant;
import io.juzhen.util.PdfUtil;
import io.juzhen.vo.business.CompanyInfoVo;

import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * Desc:
 * Created by jinx on 2017/9/28.
 */
@Service
public class PrintCompanyServiceImpl extends AssemblePDFData {

    @Override
    protected <T > CustinfoQueryDTO assembleCustinfoQueryDTO(T t) {
        CompanyInfoVo companyInfoVo = (CompanyInfoVo) t;
        CustinfoQueryDTO custinfoQueryDTO = new CustinfoQueryDTO(10, 1);
        custinfoQueryDTO.setTaaccountid(companyInfoVo.getAccount());
        return custinfoQueryDTO;
    }

    @Override
    protected Map getHeadInfo(Map map) {
        Map<String,String> headInfo = new HashMap<>();
        headInfo.put("firstHeadInfo","企业基本情况登记表");
        headInfo.put("secondHeadInfo","");
        return headInfo;
    }

    @Override
    public void createPdfTableData(Font fontChina12, PdfPTable table, Map resMap) {
        CompanyInfoVo companyInfoVo = (CompanyInfoVo) resMap.get(CompanyInfoVo.class.getSimpleName());

		/* 企业全称 */
        PdfUtil.addTableCell(table, "企业全称", fontChina12, false, false, 0, 0);
        PdfUtil.addTableCell(table, companyInfoVo.getFundName(), fontChina12, true, false, 2, 0);//跨两列

		/* 企业简称 */
        PdfUtil.addTableCell(table, "企业简称", fontChina12, false, false, 0, 0);
        PdfUtil.addTableCell(table, companyInfoVo.getFundAbbr(), fontChina12, true, false, 2, 0);//跨两列

		/* 法定代表人 */
        PdfUtil.addTableCell(table, "法定代表人", fontChina12, false, false, 0, 0);
        PdfUtil.addTableCell(table, JuDataTypeUtils.getStringValue(resMap.get("instreprname")), fontChina12, true, false, 2, 0);//跨两列

		/* 联系电话 */
        PdfUtil.addTableCell(table, "联系电话", fontChina12, false, false, 0, 0);
        PdfUtil.addTableCell(table, JuDataTypeUtils.getStringValue(resMap.get("instreprofficetelno")), fontChina12, true, false, 2, 0);//跨两列

		/* 电子邮箱 */
        PdfUtil.addTableCell(table, "电子邮箱", fontChina12, false, false, 0, 0);
        PdfUtil.addTableCell(table, JuDataTypeUtils.getStringValue(resMap.get("instrepremailaddress")), fontChina12, true, false, 2, 0);//跨两列

		/* 企业网址 */
        PdfUtil.addTableCell(table, "企业网址", fontChina12, false, false, 0, 0);
        PdfUtil.addTableCell(table, JuDataTypeUtils.getStringValue(resMap.get("website")), fontChina12, true, false, 2, 0);//跨两列

		/* 营业执照号 */
        PdfUtil.addTableCell(table, "营业执照号", fontChina12, false, false, 0, 0);
        PdfUtil.addTableCell(table, JuDataTypeUtils.getStringValue(resMap.get("certificateno")), fontChina12, true, false, 2, 0);//跨两列

		/* 注册日期 */
        PdfUtil.addTableCell(table, "注册日期", fontChina12, false, false, 0, 0);
        PdfUtil.addTableCell(table, JuDataTypeUtils.getStringValue(resMap.get("registerdate")), fontChina12, true, false, 2, 0);//跨两列

		/* 企业住址 */
        PdfUtil.addTableCell(table, "企业住址", fontChina12, false, false, 0, 0);
        PdfUtil.addTableCell(table, JuDataTypeUtils.getStringValue(resMap.get("registeraddress")), fontChina12, true, false, 2, 0);//跨两列

		/* 注册地 */
        PdfUtil.addTableCell(table, "注册地", fontChina12, false, false, 0, 0);
        PdfUtil.addTableCell(table, JuDataTypeUtils.getStringValue("" + JuDataTypeUtils.getStringValue(resMap.get("countryname")) + JuDataTypeUtils.getStringValue(resMap.get("provincename")) + JuDataTypeUtils.getStringValue(resMap.get("cityname"))), fontChina12, true, false, 2, 0);//跨两列

		/* 注册资本（万元） */
        PdfUtil.addTableCell(table, "注册资本（万元）", fontChina12, false, false, 0, 0);
        PdfUtil.addTableCell(table, JuDataTypeUtils.getStringValue(resMap.get("registeredcptl")), fontChina12, true, false, 5, 0);//跨五列

		/* 经营范围 */
        PdfUtil.addTableCell(table, "经营范围", fontChina12, false, false, 0, 0);
        PdfUtil.addTableCell(table, JuDataTypeUtils.getStringValue(resMap.get("instreprmanagerange")), fontChina12, true, false, 5, 0);//跨五列

		/* 财务状况 */
        PdfUtil.addTableCell(table, "财务状况", fontChina12, false, true, 0, 4);// 跨四行

        PdfUtil.addTableCell(table, "年份\n（期限）", fontChina12, false, false, 0, 0);
        PdfUtil.addTableCell(table, "总资产\n（万元）", fontChina12, false, false, 0, 0);
        PdfUtil.addTableCell(table, "净资产\n（万元）", fontChina12, false, false, 0, 0);
        PdfUtil.addTableCell(table, "营业收入\n（万元）", fontChina12, false, false, 0, 0);
        PdfUtil.addTableCell(table, "净利润\n（万元）", fontChina12, false, false, 0, 0);
        PdfUtil.addTableCell(table, " ", fontChina12, false, false, 0, 0);
        PdfUtil.addTableCell(table, " ", fontChina12, false, false, 0, 0);
        PdfUtil.addTableCell(table, " ", fontChina12, false, false, 0, 0);
        PdfUtil.addTableCell(table, " ", fontChina12, false, false, 0, 0);
        PdfUtil.addTableCell(table, " ", fontChina12, false, false, 0, 0);
        PdfUtil.addTableCell(table, " ", fontChina12, false, false, 0, 0);
        PdfUtil.addTableCell(table, " ", fontChina12, false, false, 0, 0);
        PdfUtil.addTableCell(table, " ", fontChina12, false, false, 0, 0);
        PdfUtil.addTableCell(table, " ", fontChina12, false, false, 0, 0);
        PdfUtil.addTableCell(table, " ", fontChina12, false, false, 0, 0);
        PdfUtil.addTableCell(table, " ", fontChina12, false, false, 0, 0);
        PdfUtil.addTableCell(table, " ", fontChina12, false, false, 0, 0);
        PdfUtil.addTableCell(table, " ", fontChina12, false, false, 0, 0);
        PdfUtil.addTableCell(table, " ", fontChina12, false, false, 0, 0);
        PdfUtil.addTableCell(table, " ", fontChina12, false, false, 0, 0);
		/* ------->> 财务状况结束 <<-------- */

        PdfPCell cell46 = new PdfPCell();
        cell46.setRowspan(2);
        cell46.setPhrase(new Paragraph("备注", fontChina12));
        cell46.setVerticalAlignment(PdfPCell.ALIGN_MIDDLE);
        cell46.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell46.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell46.setMinimumHeight(100);
        table.addCell(cell46);
        PdfPCell cell47 = new PdfPCell();
        cell47.setColspan(5);
        cell47.setPhrase(new Paragraph(" ", fontChina12));
        cell47.setVerticalAlignment(PdfPCell.ALIGN_MIDDLE);
        cell47.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell47.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell47.setMinimumHeight(100);
        table.addCell(cell47);
    }

    @Override
    public void createPdfEnd(Document document, BaseFont bfChinese, Font fontChina12, Paragraph blank1, Map resMap) throws DocumentException {
    }

    @Override
    protected int getTableColumnNumber() {
        return PDFConstant.TABLE_COLUMN_NUMBER_6;
    }

    @Override
    public Paragraph createPdfFirst(Document document, BaseFont bfChinese, Font fontChina18, Font fontChina15, Font fontChina12, Map headInfo) throws DocumentException {
        // 如果是企业基本情况打印
        Paragraph firstTitle = new Paragraph(JuDataTypeUtils.getStringValue(headInfo.get("firstHeadInfo")), new Font(bfChinese, PDFConstant.FONT_SIZE_18));
        firstTitle.setAlignment(Element.ALIGN_CENTER);// 居中
        Paragraph blank1 =  new Paragraph(" ", new Font(bfChinese, PDFConstant.FONT_SIZE_5));
        // 添加文件名称
        document.add(firstTitle);
        // 添加一个空格
        document.add(blank1);
        document.add(blank1);
        document.add(blank1);
        return blank1;
    }
}
