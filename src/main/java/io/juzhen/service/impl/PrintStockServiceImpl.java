package io.juzhen.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import io.juzhen.service.AssemblePDFData;
import io.juzhen.util.JuDataTypeUtils;
import io.juzhen.util.PDFConstant;
import io.juzhen.util.PdfUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.stereotype.Service;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfPTable;

/**
 * Desc:
 * Created by xiangshang on 2017/10/10.
 */
@Service
@SuppressWarnings({ "unchecked", "rawtypes" })
public class PrintStockServiceImpl extends AssemblePDFData {
	
    @Override
    protected <T> Object assembleCustinfoQueryDTO(T t) {
        // 此处需要根据t转换成需要的数据信息
        return new Object();
    }

    @Override
    protected Map getHeadInfo(Map map) {
        Map<String, Object> headInfo = new HashMap<>();
        headInfo.put("firstHeadInfo", "某某交易中心    托管公司（管理） 客户持股明细");
        return headInfo;
    }

    @Override
    public void createPdfTableData(Font fontChina12, PdfPTable table, Map resMap) {
    	// 从股交查询的账户信息
    	List<?> dataList = (List<?>) resMap.get(PDFConstant.PRINT_STOCK_INFO_KEY);
    	
    	// 股权账号
        PdfUtil.addTableCell(table, "股权账号", fontChina12, false, false, 0, 0);
        // 股权代码 
        PdfUtil.addTableCell(table, "股权代码", fontChina12, false, false, 0, 0);
		// 股权名称 
        PdfUtil.addTableCell(table, "股权名称", fontChina12, false, false, 0, 0);
		// 份额性质 
        PdfUtil.addTableCell(table, "份额性质", fontChina12, false, false, 0, 0);
		// 股权数量
        PdfUtil.addTableCell(table, "股权数量", fontChina12, false, false, 0, 0);
        
        if (dataList != null && dataList.size() > 0) {
        	for (int i = 0; i < dataList.size(); i++) {
				LinkedHashMap<String, Object> dataMap = (LinkedHashMap<String, Object>) dataList.get(i);
        		// 股权账号
        		PdfUtil.addTableCell(table, JuDataTypeUtils.getStringValue(dataMap.get("taaccountid")), fontChina12, false, false, 0, 0);
        		// 股权代码
        		PdfUtil.addTableCell(table, JuDataTypeUtils.getStringValue(dataMap.get("fundcode")), fontChina12, false, false, 0, 0);
        		// 股权名称
        		PdfUtil.addTableCell(table, JuDataTypeUtils.getStringValue(dataMap.get("fundname")), fontChina12, false, false, 0, 0);
        		// 份额性质
        		PdfUtil.addTableCell(table, JuDataTypeUtils.getStringValue(dataMap.get("naturecodename")), fontChina12, false, false, 0, 0);
        		// 股权数量
        		PdfUtil.addTableCell(table, JuDataTypeUtils.getStringValue(dataMap.get("lastfundbal")), fontChina12, false, false, 0, 0);
			}
		}else {
			// 没有查询出数据信息则用空进行填充
			for (int i = 0; i < 5; i++) {
				PdfUtil.addTableCell(table, " ", fontChina12, false, false, 0, 0);
			}
		}
    }

    @Override
    public void createPdfEnd(Document document, BaseFont bfChinese, Font fontChina12, Paragraph blank1, Map resMap) throws DocumentException {
        // TODO: 2017/10/10
    }

    @Override
    public Paragraph createPdfFirst(Document document, BaseFont bfChinese, Font fontChina18, Font fontChina15, Font fontChina12, Map headInfo) throws DocumentException {
        if (Objects.isNull(headInfo)) {
            return new Paragraph("某某交易中心    托管公司（管理） 客户持股明细", fontChina18);
        }

    	Paragraph titleParagraph = new Paragraph("某某交易中心    托管公司（管理） 客户持股明细", fontChina18);
        titleParagraph.setAlignment(Element.ALIGN_CENTER);// 居中
        document.add(titleParagraph);

        // 空格
        Paragraph blank1 = new Paragraph(" ", new Font(bfChinese, 5));
        document.add(blank1);
        document.add(blank1);

        // 客户号、姓名
        Paragraph snoParagraph = new Paragraph();
        PdfUtil.addChunk( snoParagraph, fontChina12, "客户号：", headInfo.get("custNo").toString(), 0);
        PdfUtil.addChunk( snoParagraph, fontChina12, "姓名：", headInfo.get("name").toString(), 110);
        document.add(snoParagraph);

        // 添加一个空格
        document.add(blank1);
        return blank1;
    }

    @Override
    protected int getTableColumnNumber() {
        return PDFConstant.TABLE_COLUMN_NUMBER_5;
    }
    
    @Override
    protected <T> Map selectData(T t) {
    	Object custinfoQueryDTO = this.assembleCustinfoQueryDTO(t);
		Map map = JSONObject.parseObject(JSON.toJSONString(custinfoQueryDTO), Map.class);
		map.put(t.getClass().getSimpleName(), t);

		// 如果是持仓信息凭证打印，多查询一步,此处是获取的数据源
		List<Map<String, String>> dataList = new ArrayList<>();
		// 客户的持仓信息
		map.put(PDFConstant.PRINT_STOCK_INFO_KEY, null);
		return map;
    }
    
}
