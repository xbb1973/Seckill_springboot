package slf.xbb.error;

import lombok.Data;
import org.omg.CORBA.UNKNOWN;

/**
 * @author ：xbb
 * @date ：Created in 2020/3/31 4:48 上午
 * @description：枚举业务error
 * @modifiedBy：
 * @version:
 */
public enum EmBusinessError implements CommonError {
    // 通用错误
    PARAMETER_VALIDATION_ERROR(500,"参数不合法"),
    // 用户信息错误
    USER_NOT_EXIST(501, "用户不存在"),
    // 未知错误错误
    UNKNOWN_ERROR(502, "未知错误")
    ;

    private int errCode;
    private String errMsg;

    private EmBusinessError(int errCode, String errMsg){
        this.errCode = errCode;
        this.errMsg = errMsg;
    }

    @Override
    public int getErrCode() {
        return this.errCode;
    }

    @Override
    public String getErrMsg() {
        return this.errMsg;
    }

    @Override
    public CommonError setErrMsg(String errMsg) {
        this.errMsg = errMsg;
        return this;
    }
}
