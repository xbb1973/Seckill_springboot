package slf.xbb;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import slf.xbb.dao.UserDoMapper;
import slf.xbb.domain.UserDo;

/**
 * Hello world!
 *
 */
@SpringBootApplication(scanBasePackages = "slf.xbb")
@RestController
@MapperScan("slf.xbb.dao")
public class App
{
    @Autowired
    private UserDoMapper userDoMapper;

    @RequestMapping("/user")
    public String getUser() {
        UserDo userDo = userDoMapper.selectByPrimaryKey(40);
        if (userDo == null) {
            return "用户不存在";
        } else {
            return userDo.getName();
        }
    }

    @RequestMapping("/user/insert")
    @ResponseBody
    public String insertUser() {
        UserDo userDo = userDoMapper.selectByPrimaryKey(40);
        userDo.setId(null);
        userDo.setName("新的齐白石");
        userDo.setTelphone("12349");
        int count = userDoMapper.insertSelective(userDo);
        if (count == 0) {
            return "用户插入成功";
        } else {
            return userDo.toString();
        }
    }

    @RequestMapping("/")
    public String home(){
        return "hello world";
    }
    public static void main( String[] args )
    {
        System.out.println( "Hello World!" );
        SpringApplication.run(App.class, args);
    }
}
