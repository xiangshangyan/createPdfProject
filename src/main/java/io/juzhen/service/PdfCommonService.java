package io.juzhen.service;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Font;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfPTable;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

/**
 * Desc:
 * Created by jinx on 2017/9/28.
 */
public interface PdfCommonService {

    /**
     * 打开文档及支持中文输出
     *
     * @param outputStream
     * @param document
     * @return
     * @throws DocumentException
     * @throws IOException
     */
    BaseFont openDocument(OutputStream outputStream, Document document) throws DocumentException, IOException;

    /**
     * -->> 生成pdf文件头部信息 <<--
     *
     * @param document
     * @param bfChinese
     * @param fontChina18
     * @param fontChina15
     * @param fontChina12
     * @return
     * @throws DocumentException
     */
    Paragraph createPdfFirst(Document document, BaseFont bfChinese, Font fontChina18, Font fontChina15, Font fontChina12, Map headInfo) throws DocumentException;

    /**
     * 创建表格数据
     *
     * @param fontChina12
     * @param table
     */
    void createPdfTableData(Font fontChina12, PdfPTable table, Map resMap);

    /**
     * 生成Pdf文件底部信息
     *
     * @param document    pdf的document对象
     * @param bfChinese   中文支持
     * @param fontChina12 字体的大小
     * @param blank1      空格
     * @param resMap      具体的map对象，里面是操作柜员,股交查询信息等相关信息
     */
    void createPdfEnd(Document document, BaseFont bfChinese, Font fontChina12, Paragraph blank1, Map resMap) throws DocumentException;

}
