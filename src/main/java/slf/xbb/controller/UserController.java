package slf.xbb.controller;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.tomcat.util.security.MD5Encoder;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.filter.CharacterEncodingFilter;
import slf.xbb.controller.view.UserVo;
import slf.xbb.error.BussinessException;
import slf.xbb.error.EmBusinessError;
import slf.xbb.response.CommonReturnType;
import slf.xbb.service.UserService;
import slf.xbb.service.impl.UserServiceImpl;
import slf.xbb.service.model.UserModel;
import sun.misc.BASE64Encoder;

import javax.servlet.http.HttpServletRequest;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * @author ：xbb
 * @date ：Created in 2020/3/31 2:24 上午
 * @description：基于springmvc的表现层代码
 * @modifiedBy：
 * @version:
 */
@Slf4j
@Controller("user")
@RequestMapping("/user")
//跨域请求中，不能做到session共享
@ConditionalOnClass(CharacterEncodingFilter.class)
@CrossOrigin(allowCredentials = "true", allowedHeaders = "*")
public class UserController extends BaseController {

    @Autowired
    private UserService userService;

    @Autowired
    private HttpServletRequest httpServletRequest;

    /**
     * 用户登陆接口
     * @param telphone
     * @param password
     * @return
     * @throws BussinessException
     */
    @RequestMapping(path = "/login", method = {RequestMethod.POST}, consumes = {CONTENT_TYPE_FORMED})
    @ResponseBody
    public CommonReturnType login(@RequestParam(name = "telphone") String telphone,
                                     @RequestParam(name = "password") String password) throws BussinessException, NoSuchAlgorithmException {
        // 入参校验，两个参数都不允许为空
        if (StringUtils.isEmpty(telphone) || StringUtils.isEmpty(password)) {
            throw new BussinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR);
        }

        // 用户登陆服务，校验用户参数是否合法
        UserModel userModel = userService.validateLogin(telphone, this.EncodePasswordByMD5(password));

        // 将登陆凭证加入到用户登陆成功的session内
        // 进阶-分布式-token实现
        this.httpServletRequest.getSession().setAttribute("IS_LOGIN", true);
        this.httpServletRequest.getSession().setAttribute("LOGIN_USER", userModel);

        return CommonReturnType.create(null);
    }

    /**
     * 用户注册接口
     * @param telphone
     * @param otpCode
     * @param name
     * @param gender
     * @param age
     * @param password
     * @return
     * @throws BussinessException
     */
    @RequestMapping(path = "/register", method = {RequestMethod.POST}, consumes = {CONTENT_TYPE_FORMED})
    @ResponseBody
    public CommonReturnType register(@RequestParam(name = "telphone") String telphone,
                                     @RequestParam(name = "otpCode") String otpCode,
                                     @RequestParam(name = "name") String name,
                                     @RequestParam(name = "gender") String gender,
                                     @RequestParam(name = "age") Integer age,
                                     @RequestParam(name = "password") String password) throws BussinessException {
        // 验证手机号和对应的otpcode相符合
        String inSessionOtpCode = (String) this.httpServletRequest.getSession().getAttribute(telphone);
        if (!com.alibaba.druid.util.StringUtils.equals(otpCode, inSessionOtpCode)) {
            throw new BussinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR, "短信验证码错误");
        }
        // 用户的注册流程
        UserModel userModel = new UserModel();
        userModel.setName(name);
        userModel.setGender(new Byte(String.valueOf(gender)));
        userModel.setAge(age);
        userModel.setTelphone(telphone);
        userModel.setRegisitMode("byPhone");
        try {
            userModel.setEncrptPassword(EncodePasswordByMD5(password));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        userService.register(userModel);
        return CommonReturnType.create(null);
    }

    public String EncodePasswordByMD5(String password) throws NoSuchAlgorithmException {
        MessageDigest md5 = MessageDigest.getInstance("MD5");
        BASE64Encoder base64Encoder = new BASE64Encoder();
        String encreptPassword = base64Encoder.encode(password.getBytes());
        return encreptPassword;
    }

    /**
     * 用户获取otp短信接口
     *
     * @param telphone
     * @return
     */
    @RequestMapping(path = "/getotp", method = {RequestMethod.POST}, consumes = {CONTENT_TYPE_FORMED})
    @ResponseBody
    public CommonReturnType getOtp(@RequestParam(name = "telphone") String telphone) {
        // 需要按照一定的规则生成OTP验证码
        Random random = new Random();
        int randomInt = random.nextInt(99999);
        randomInt += 10000;
        String otpCode = String.valueOf(randomInt);

        // 将OTP验证码同对应用户的手机号关联
        httpServletRequest.getSession().setAttribute(telphone, otpCode);

        // 将OTP验证啊吗通过短信通道发送给用户,暂时省略
        log.info("telphone = {} otpCode = {} ", telphone, otpCode);

        return CommonReturnType.create(null);
    }

    /**
     * 调用service服务获取对应id的用户对象并返回给前端
     *
     * @param id
     */
    @RequestMapping("/get")
    @ResponseBody
    public CommonReturnType getUser(@RequestParam(name = "id") Integer id) throws BussinessException {
        UserModel userModel = userService.getUserById(id);
        // 不抛自定义异常，验证unkown error
        if (userModel == null) {
            userModel.setEncrptPassword("1");
        }
        // 抛出自定义异常，验证自定义异常处理
        if (userModel == null) {
            throw new BussinessException(EmBusinessError.USER_NOT_EXIST);
        }
        UserVo userVo = convertFromModel(userModel);
        return CommonReturnType.create(userVo);
    }

    private UserVo convertFromModel(UserModel userModel) {
        if (userModel == null) {
            return null;
        }
        UserVo userVo = new UserVo();
        BeanUtils.copyProperties(userModel, userVo);
        return userVo;
    }
}
