package slf.xbb.service.model;

import lombok.Data;

/**
 * @author ：xbb
 * @date ：Created in 2020/3/31 3:09 上午
 * @description：userModel
 * @modifiedBy：
 * @version:
 */
@Data
public class UserModel {
    private Integer id;
    private String name;
    private Byte gender;
    private Integer age;
    private String telphone;
    private String regisitMode;
    private Integer thirdPartyId;
    private String encrptPassword;
}
