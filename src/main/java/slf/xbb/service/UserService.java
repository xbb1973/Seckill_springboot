package slf.xbb.service;

import org.apache.ibatis.annotations.Param;
import slf.xbb.error.BussinessException;
import slf.xbb.service.model.UserModel;

/**
 * @author ：xbb
 * @date ：Created in 2020/3/31 2:35 上午
 * @description：User service
 * @modifiedBy：
 * @version:
 */
public interface UserService {
    /**
     * 通过用户id获取UserDo对象
     * @param id
     * @return
     */
    UserModel getUserById(Integer id);

    /**
     * 用户注册接口
     * @param userModel
     * @throws BussinessException
     */
    void register(UserModel userModel) throws BussinessException;

    /**
     * 登陆验证接口
     * @param telphone
     * @param encryptPassword
     * @return
     * @throws BussinessException
     */
    UserModel validateLogin(String telphone, String encryptPassword) throws BussinessException;

    /**
     * 缓存模型
     * @param userId
     * @return
     */
    UserModel getUserByIdInCache(Integer userId);
}
