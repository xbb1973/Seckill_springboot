package slf.xbb.service.model;

import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

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

    @NotBlank(message = "用户名不能为空")
    private String name;

    @NotNull(message = "性别不能不填写")
    private Byte gender;

    @NotNull(message = "年龄不能不填写")
    @Min(value = 0, message = "年龄必须大于0岁")
    @Max(value = 150, message = "年龄不能超过150")
    private Integer age;

    @NotBlank(message = "手机号不能为空")
    private String telphone;

    private String regisitMode;
    private Integer thirdPartyId;

    @NotBlank(message = "密码不能为空")
    private String encrptPassword;
}
