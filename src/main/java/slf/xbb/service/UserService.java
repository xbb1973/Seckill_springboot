package slf.xbb.service;

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
     */
    UserModel getUserById(Integer id);
    void register(UserModel userModel) throws BussinessException;
    UserModel validateLogin(String telphone, String encryptPassword) throws BussinessException;
}
