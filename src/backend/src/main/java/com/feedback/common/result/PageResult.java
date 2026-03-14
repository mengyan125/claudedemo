package com.feedback.common.result;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

/**
 * 分页响应封装类
 *
 * @param <T> 列表元素类型
 */
@Data
@AllArgsConstructor
public class PageResult<T> {

    /** 数据列表 */
    private List<T> list;

    /** 总记录数 */
    private long total;

    /** 当前页码 */
    private int pageNum;

    /** 每页大小 */
    private int pageSize;

    /**
     * 静态工厂方法，创建分页结果
     *
     * @param list     数据列表
     * @param total    总记录数
     * @param pageNum  当前页码
     * @param pageSize 每页大小
     */
    public static <T> PageResult<T> of(List<T> list, long total,
                                       int pageNum, int pageSize) {
        return new PageResult<>(list, total, pageNum, pageSize);
    }
}
