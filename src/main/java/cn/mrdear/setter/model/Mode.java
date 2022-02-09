package cn.mrdear.setter.model;

/**
 * 指定当前转换模式
 * @author quding
 * @since 2022/2/7
 */
public enum Mode {
    /**
     * 什么都不处理
     */
    NULL,
    /**
     * 方法内指定变量
     */
    METHOD_VARIABLE,
    /**
     * 空方法体内转换
     */
    METHOD;

}
