package slf.xbb.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import slf.xbb.dao.UserDoMapper;
import slf.xbb.dao.UserPasswordDoMapper;
import slf.xbb.domain.UserDo;
import slf.xbb.domain.UserPasswordDo;
import slf.xbb.error.BussinessException;
import slf.xbb.error.EmBusinessError;
import slf.xbb.service.UserService;
import slf.xbb.service.model.UserModel;
import slf.xbb.validator.ValidationResult;
import slf.xbb.validator.ValidatorImpl;

/**
 * @author ：xbb
 * @date ：Created in 2020/3/31 2:36 上午
 * @description：user service impl
 * @modifiedBy：
 * @version:
 */
@Slf4j
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserDoMapper userDoMapper;

    @Autowired
    private UserPasswordDoMapper userPasswordDoMapper;

    @Autowired
    private ValidatorImpl validator;

    @Override
    public UserModel validateLogin(String telphone, String encryptPassword) throws BussinessException {
        // 通过用户手机获取用户信息
        UserDo userDo = userDoMapper.selectByTelphone(telphone);
        if (userDo == null) {
            throw new BussinessException(EmBusinessError.USER_LOGIN_ERROR);
        }
        UserPasswordDo userPasswordDo = userPasswordDoMapper.selectByUserId(userDo.getId());
        UserModel userModel = convertFromDataObject(userDo, userPasswordDo);

        // 比对用户信息内加密的密码是否和传输进来的密码相匹配
        if (!StringUtils.equals(encryptPassword, userModel.getEncrptPassword())) {
            throw new BussinessException(EmBusinessError.USER_LOGIN_ERROR);
        }
        // 没有异常抛出，直接return，表示登陆成功
        return userModel;
    }

    @Override
    public UserModel getUserById(Integer id) {
        UserDo userDo = userDoMapper.selectByPrimaryKey(id);
        UserPasswordDo userPasswordDo = userPasswordDoMapper.selectByUserId(id);
        return convertFromDataObject(userDo, userPasswordDo);
    }

    @Override
    @Transactional(rollbackFor = BussinessException.class)
    public void register(UserModel userModel) throws BussinessException {
        if (userModel == null) {
            throw new BussinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR);
        }
        // if (StringUtils.isEmpty(userModel.getName())
        //         || userModel.getGender() == null
        //         || userModel.getAge() == null
        //         || StringUtils.isEmpty(userModel.getTelphone())) {
        //     throw new BussinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR);
        // }
        ValidationResult result = validator.validationResult(userModel);
        if (result.isHasErrors()) {
            throw new BussinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR, result.getErrMsg());
        }


        // 实现model->data object->persist layer
        // UserDo
        UserDo userDo = convertUserDoFromModel(userModel);
        try {
            userDoMapper.insertSelective(userDo);
        } catch (DuplicateKeyException e) {
            throw new BussinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR, "手机号重复注册");
        }
        // UserPasswordDo
        userModel.setId(userDo.getId());
        UserPasswordDo userPasswordDo = convertUserPasswordFromModel(userModel);
        userPasswordDoMapper.insertSelective(userPasswordDo);

        return;
    }

    private UserPasswordDo convertUserPasswordFromModel(UserModel userModel) {
        if (userModel == null) {
            return null;
        }
        UserPasswordDo userPasswordDo = new UserPasswordDo();
        userPasswordDo.setEncrptPassword(userModel.getEncrptPassword());
        userPasswordDo.setUserId(userModel.getId());
        return userPasswordDo;
    }


    private UserDo convertUserDoFromModel(UserModel userModel) {
        if (userModel == null) {
            return null;
        }
        UserDo userDo = new UserDo();
        BeanUtils.copyProperties(userModel, userDo);

        return userDo;
    }

    private UserModel convertFromDataObject(UserDo userDo, UserPasswordDo userPasswordDo) {
        if (userDo == null) {
            return null;
        }
        UserModel userModel = new UserModel();
        BeanUtils.copyProperties(userDo, userModel);
        if (userPasswordDo != null) {
            userModel.setEncrptPassword(userPasswordDo.getEncrptPassword());
        }
        return userModel;
    }
}
