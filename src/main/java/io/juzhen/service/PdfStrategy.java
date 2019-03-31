package io.juzhen.service;

import java.io.OutputStream;

/**
 * Desc:
 * Created by jinx on 2017/9/28.
 */
public interface PdfStrategy {

    /**
     * 输出pdf格式文件
     * @param t VO对象
     * @param outputStream
     * @param <T>
     */
    <T> void print(T t, OutputStream outputStream);
}
