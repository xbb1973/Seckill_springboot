package slf.xbb.error;

/**
 * @author ：xbb
 * @date ：Created in 2020/3/31 4:55 上午
 * @description：包装器业务异常类实现
 * @modifiedBy：
 * @version:
 */
public class BussinessException extends Exception implements CommonError {

    private CommonError commonError;

    /**
     * 直接接收commonError的传参用于构造业务异常
     * @param commonError
     */
    public BussinessException(CommonError commonError) {
        super();
        this.commonError = commonError;
    }

    /**
     * 自定义errMsg构造业务异常
     * @param commonError
     */
    public BussinessException(CommonError commonError, String errMsg) {
        super();
        this.commonError = commonError;
        this.commonError.setErrMsg(errMsg);
    }

    @Override
    public int getErrCode() {
        return commonError.getErrCode();
    }

    @Override
    public String getErrMsg() {
        return commonError.getErrMsg();
    }

    @Override
    public CommonError setErrMsg(String errMsg) {
        commonError.setErrMsg(errMsg);
        return this;
    }
}
