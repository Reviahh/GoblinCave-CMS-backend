package com.miji.cms;

import com.miji.cms.model.domain.User;
import com.miji.cms.service.UserService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

@SpringBootTest
class BackendApplicationTests {
    @Resource
    private UserService userService;

    @Test
    public void testRegister(){
        User user =  new User();
        user.setUserName("jxy");
        user.setUserAccount("12345");
        user.setUserPassword("11111111");
        user.setGender(0);
        user.setPhone("1234");
        user.setEmail("12323@11.com");
        user.setUserRole(0);
        user.setIsDelete(0);


        boolean result = userService.save(user);
        System.out.println(user.getId());
        Assertions.assertTrue(result);

    }

}
