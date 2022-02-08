package cn.mrdear.setter.model;

import java.util.Set;

/**
 * 结果输出信息
 * @author quding
 * @since 2022/2/7
 */
public class OutputConvertResult {
    /**
     * 写入光标处的文档
     */
    private StringBuilder insertCaretText = new StringBuilder();

    /**
     * 导入的包集合
     */
    private Set<String> importList;

    /**
     * 追加写入文本
     * @param str 追加的内容
     */
    public StringBuilder appendInsert(String str) {
        this.insertCaretText.append(str);
        return this.insertCaretText;
    }

    /**
     * 获取写入内容
     */
    public String getInsertText() {
        return insertCaretText.toString();
    }

}
