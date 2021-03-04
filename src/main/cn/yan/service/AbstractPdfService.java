package cn.yan.service;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import cn.yan.util.PDFConstant;
import cn.yan.util.PdfUtil;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

/**
 * Desc:
 * Created by jinx on 2017/9/28.
 */
public abstract class AbstractPdfService implements PdfStrategy, PdfCommonService {

    private OutputStream outputStream;

    @Override
    public <T> void print(T t, OutputStream outputStream) {
        this.outputStream = outputStream;
        // 1.查询数据
        Map map = this.selectData(t);
        // 2.根据查询的数据生成pdf
        this.generatePdf(map);
    }

    protected void generatePdf(Map map) {
        // 1 open document
        Document document = new Document();
        try {
            BaseFont bfChinese = this.openDocument(outputStream, document);
            // 标题加粗
            Font fontChina18 = new Font(bfChinese, PDFConstant.FONT_SIZE_18, Font.BOLD);
            Font fontChina15 = new Font(bfChinese, PDFConstant.FONT_SIZE_15);
            Font fontChina10 = new Font(bfChinese, PDFConstant.FONT_SIZE_10);

            // 2.1 获取pdf头文件信息
            Map headInfo = this.getHeadInfo(map);
            Paragraph blank1 = this.createPdfFirst(document, bfChinese, fontChina18, fontChina15, fontChina10, headInfo);

            // 3 创建表格
            PdfPTable table = new PdfPTable(this.getTableColumnNumber());// 表格总共几列
            table.setWidthPercentage(PDFConstant.TABLE_WIDTH_PERCENTAGE);// 表格宽度为100%
            // 3.1 表格数据添加
            createPdfTableData(fontChina10, table, map);
            // 表格添加到Document中
            document.add(table);

            // 4. 生成底部信息
            createPdfEnd(document, bfChinese, fontChina10, blank1, map);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (DocumentException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (document != null) {
                // 关闭文档
                document.close();
            }
        }
    }

    @Override
    public BaseFont openDocument(OutputStream outputStream, Document document) throws DocumentException, IOException {
        // 1:建立一个PDF 写入器与document对象关联通过书写器(Writer)可以将文档写入到response
        PdfWriter.getInstance(document, outputStream);
        // 2:打开文档
        document.open();
        // 解决中文不显示问题
        BaseFont bfChinese = BaseFont.createFont("STSong-Light", "UniGB-UCS2-H", BaseFont.NOT_EMBEDDED);
        return bfChinese;
    }

    @Override
    public Paragraph createPdfFirst(Document document, BaseFont bfChinese, Font fontChina18, Font fontChina15, Font fontChina12, Map headInfo) throws DocumentException {

        // 空格
        Paragraph blank1 = new Paragraph(" ", new Font(bfChinese, PDFConstant.FONT_SIZE_5));

        // 1.firstTitle
        Paragraph firstTitle = new Paragraph(headInfo.get("firstHeadInfo").toString(), fontChina18);
        firstTitle.setAlignment(Element.ALIGN_CENTER);// 居中
        document.add(firstTitle);

        // 2.secondTitle
        Paragraph secondTitle = new Paragraph(headInfo.get("secondHeadInfo").toString(), fontChina15);
        secondTitle.setAlignment(Element.ALIGN_CENTER);// 居中
        document.add(secondTitle);

        // 添加空格
        document.add(blank1);

        // 3.打印时间  左对齐
        Paragraph paragraph = new Paragraph();
        PdfUtil.addChunk(paragraph, fontChina12, "打印时间：", PdfUtil.getDateFormatString("yyyy年MM月dd日"), 0);
        PdfUtil.addChunk(paragraph, fontChina12, "流水号：", headInfo.get("orderId").toString(), 110);
        document.add(paragraph);

        // 添加一个空格
        document.add(blank1);
        return blank1;

    }

    protected abstract <T> Map selectData(T t);

    protected abstract Map getHeadInfo(Map map);

    protected abstract int getTableColumnNumber();
}
