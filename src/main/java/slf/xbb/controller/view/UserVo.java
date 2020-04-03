package slf.xbb.controller.view;

import lombok.Data;

/**
 * @author ：xbb
 * @date ：Created in 2020/3/31 3:45 上午
 * @description：UserVo
 * @modifiedBy：
 * @version:
 */
@Data
public class UserVo {
    private Integer id;
    private String name;
    private Byte gender;
    private Integer age;
    private String telphone;
}
