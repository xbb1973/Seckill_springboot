package slf.xbb.validator;

import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * @author ：xbb
 * @date ：Created in 2020/4/3 4:51 下午
 * @description：简单的校验
 * @modifiedBy：
 * @version:
 */
@Data
public class ValidationResult {
    /**
     * 校验结果是否有错
     */
    private boolean hasErrors = false;

    /**
     * 存放错误信息的map
     */
    private Map<String, String> errMsgMap = new HashMap<>();

    public String getErrMsg() {
        return StringUtils.join(errMsgMap.values().toArray(), ",");
    }

}
