package slf.xbb.controller;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import slf.xbb.controller.view.UserVo;
import slf.xbb.error.BussinessException;
import slf.xbb.error.EmBusinessError;
import slf.xbb.response.CommonReturnType;
import slf.xbb.service.UserService;
import slf.xbb.service.model.UserModel;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * @author ：xbb
 * @date ：Created in 2020/3/31 5:50 上午
 * @description：
 * @modifiedBy：
 * @version:
 */
public class BaseController {

    final public static String CONTENT_TYPE_FORMED = "application/x-www-form-urlencoded";

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public Object handleException(Exception e, HttpServletRequest request) {
        BussinessException bussinessException = e instanceof BussinessException ? ((BussinessException) e) : null;
        Map<String, Object> respData = new HashMap<>();
        if (bussinessException != null) {
            respData.put("errCode", bussinessException.getErrCode());
            respData.put("errMsg", bussinessException.getErrMsg());
        } else {
            respData.put("errCode", EmBusinessError.UNKNOWN_ERROR.getErrCode());
            respData.put("errMsg", EmBusinessError.UNKNOWN_ERROR.getErrMsg());
        }
        return CommonReturnType.create(respData, "fail");
    }
}
