package cn.mrdear.setter.lang;

import cn.mrdear.setter.model.InputConvertContext;
import cn.mrdear.setter.model.OutputConvertResult;

/**
 * 转换处理器
 * @author quding
 * @since 2022/2/7
 */
public interface ConvertHandler {


    /**
     * 处理转换逻辑
     * @param context 转换依赖的上下文数据
     */
    OutputConvertResult handler(InputConvertContext context);

}
