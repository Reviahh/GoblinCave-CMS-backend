package com.miji.cms.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.miji.cms.exception.BusinessException;
import com.miji.cms.mapper.UserMapper;
import com.miji.cms.model.domain.User;
import com.miji.cms.service.impl.UserServiceImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.util.DigestUtils;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * UserService#userRegister 方法单元测试
 */
@DisplayName("用户注册测试")
class UserService1Test {

    @Mock
    private UserMapper userMapper;   // mock 数据库层

    @Spy
    @InjectMocks
    private UserServiceImpl userService; // 真实 service + 可 spy

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    /**
     * 正常注册成功
     */
    @Test
    void testUserRegister_Success() {
        String userAccount = "testuser";
        String userPassword = "password123";
        String checkPassword = "password123";
        Integer userRole = 1;

        // 用户名不存在
        when(userMapper.selectCount(any(QueryWrapper.class))).thenReturn(0L);

        // 模拟 MyBatis-Plus insert 成功
        when(userMapper.insert(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(100L); // 模拟数据库返回主键
            return 1; // 插入成功
        });

        long userId = userService.userRegister(userAccount, userPassword, checkPassword, userRole);

        assertEquals(100L, userId); // 应返回数据库的主键
        verify(userMapper, times(1)).selectCount(any(QueryWrapper.class));
        verify(userMapper, times(1)).insert(any(User.class));
    }

    /**
     * 参数为空
     */
    @Test
    void testUserRegister_ParamIsNull() {
        assertThrows(BusinessException.class, () ->
                userService.userRegister(null, "pass1234", "pass1234", 1)
        );
    }

    /**
     * 用户名太短
     */
    @Test
    void testUserRegister_UserAccountTooShort() {
        assertThrows(BusinessException.class, () ->
                userService.userRegister("abc", "pass1234", "pass1234", 1)
        );
    }

    /**
     * 密码太短
     */
    @Test
    void testUserRegister_PasswordTooShort() {
        assertThrows(BusinessException.class, () ->
                userService.userRegister("testuser", "1234567", "1234567", 1)
        );
    }

    /**
     * 用户名包含非法字符
     */
    @Test
    void testUserRegister_InvalidCharacterInAccount() {
        assertThrows(BusinessException.class, () ->
                userService.userRegister("test@user", "pass1234", "pass1234", 1)
        );
    }

    /**
     * 密码不一致
     */
    @Test
    void testUserRegister_PasswordNotMatch() {
        assertThrows(BusinessException.class, () ->
                userService.userRegister("testuser", "pass1234", "differentPass", 1)
        );
    }

    /**
     * 用户名重复
     */
    @Test
    void testUserRegister_DuplicateUsername() {
        when(userMapper.selectCount(any(QueryWrapper.class))).thenReturn(1L);

        assertThrows(BusinessException.class, () ->
                userService.userRegister("testuser", "pass1234", "pass1234", 1)
        );

        verify(userMapper, times(1)).selectCount(any(QueryWrapper.class));
    }

    /**
     * 插入失败（insert 返回 0）
     */
    @Test
    void testUserRegister_InsertFailed() {
        when(userMapper.selectCount(any(QueryWrapper.class))).thenReturn(0L);
        when(userMapper.insert(any(User.class))).thenReturn(0); // 模拟失败

        assertThrows(BusinessException.class, () ->
                userService.userRegister("testuser", "pass1234", "pass1234", 1)
        );

        verify(userMapper, times(1)).insert(any(User.class));
    }
}
/**
 * UserService#userLogin 方法的单元测试
 */
@DisplayName("用户登录测试")
class UserService2Test {

    @Mock
    private UserMapper userMapper;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpSession session;

    @InjectMocks
    private UserServiceImpl userService;

    private final String SALT = "miji"; // ⚠ 确保与业务代码一致

    @BeforeEach
    void init() {
        MockitoAnnotations.openMocks(this);
        when(request.getSession()).thenReturn(session);
    }

    /**
     * 登录成功
     */
    @Test
    void testUserLoginSuccess() {
        String userAccount = "mijiUser";
        String userPassword = "12345678";
        Integer userRole = 1;

        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());

        User fakeUser = new User();
        fakeUser.setId(1L);
        fakeUser.setUserAccount(userAccount);
        fakeUser.setUserPassword(encryptPassword);
        fakeUser.setUserRole(userRole);

        // stub：模拟数据库查询
        when(userMapper.selectOne(any(QueryWrapper.class))).thenReturn(fakeUser);

        User result = userService.userLogin(userAccount, userPassword, userRole, request);

        Assertions.assertNotNull(result);
        verify(session, times(1)).setAttribute(eq("userLoginState"), any());
    }

    /**
     * 用户名长度不规范
     */
    @Test
    void testUserLoginInvalidAccountLength() {
        Assertions.assertThrows(BusinessException.class, () ->
                userService.userLogin("abc", "12345678", 1, request)
        );
    }

    /**
     * 密码长度不规范
     */
    @Test
    void testUserLoginInvalidPasswordLength() {
        Assertions.assertThrows(BusinessException.class, () ->
                userService.userLogin("abcdef", "123", 1, request)
        );
    }

    /**
     * 用户名包含特殊字符
     */
    @Test
    void testUserLoginInvalidChars() {
        Assertions.assertThrows(BusinessException.class, () ->
                userService.userLogin("abc###", "12345678", 1, request)
        );
    }

    /**
     * 用户不存在或密码错误
     */
    @Test
    void testUserLoginUserNotFound() {
        when(userMapper.selectOne(any(QueryWrapper.class))).thenReturn(null);

        Assertions.assertThrows(BusinessException.class, () ->
                userService.userLogin("validName", "12345678", 1, request)
        );
    }

}
/**
 * UserService#其他 方法的单元测试
 */
@DisplayName("其他方法测试")
class UserService3Test {
    @Mock
    private UserMapper userMapper;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpSession session;

    @InjectMocks
    private UserServiceImpl userService;

    private final String USER_LOGIN_STATE = "userLoginState";
    private final int ADMIN_ROLE = 1;

    @BeforeEach
    void init() {
        MockitoAnnotations.openMocks(this);
        when(request.getSession()).thenReturn(session);
    }


    // ======================================
    // 1. getSafetyUser 测试
    // ======================================
    @Test
    void testGetSafetyUser() {
        User origin = new User();
        origin.setId(1L);
        origin.setUserName("testUser");
        origin.setUserAccount("testAccount");
        origin.setUserUrl("http://xxx.com/avatar.png");
        origin.setGender(1);
        origin.setPhone("12345678901");
        origin.setEmail("test@example.com");
        origin.setTags("['Java']");
        origin.setUserRole(1);
        origin.setCreateTime(new Date());
        origin.setUserPassword("secretPwd");

        User safe = userService.getSafetyUser(origin);

        assertNotNull(safe);
        assertNotSame(origin, safe);

        assertEquals(origin.getId(), safe.getId());
        assertEquals(origin.getUserName(), safe.getUserName());
        assertEquals(origin.getUserAccount(), safe.getUserAccount());
        assertEquals(origin.getUserUrl(), safe.getUserUrl());
        assertEquals(origin.getGender(), safe.getGender());
        assertEquals(origin.getPhone(), safe.getPhone());
        assertEquals(origin.getEmail(), safe.getEmail());
        assertEquals(origin.getTags(), safe.getTags());
        assertEquals(origin.getUserRole(), safe.getUserRole());
        assertEquals(origin.getCreateTime(), safe.getCreateTime());

        // 密码敏感字段必须被清理
        assertNull(safe.getUserPassword());
    }

    @Test
    void testGetSafetyUserNull() {
        assertNull(userService.getSafetyUser(null));
    }


    // ======================================
    // 2. userLogout 测试
    // ======================================
    @Test
    void testUserLogout() {
        int result = userService.userLogout(request);

        assertEquals(1, result);
        verify(session, times(1)).removeAttribute(USER_LOGIN_STATE);
    }


    // ======================================
    // 3. isAdmin(HttpServletRequest) 测试
    // ======================================
    @Test
    void testIsAdminByRequestTrue() {
        User admin = new User();
        admin.setUserRole(ADMIN_ROLE);

        when(session.getAttribute(USER_LOGIN_STATE)).thenReturn(admin);

        assertTrue(userService.isAdmin(request));
    }

    @Test
    void testIsAdminByRequestFalse() {
        User user = new User();
        user.setUserRole(0);

        when(session.getAttribute(USER_LOGIN_STATE)).thenReturn(user);

        assertFalse(userService.isAdmin(request));
    }

    @Test
    void testIsAdminByRequestNull() {
        when(session.getAttribute(USER_LOGIN_STATE)).thenReturn(null);

        assertFalse(userService.isAdmin(request));
    }


    // ======================================
    // 4. isAdmin(User) 测试
    // ======================================
    @Test
    void testIsAdminByUser() {
        User admin = new User();
        admin.setUserRole(ADMIN_ROLE);
        assertTrue(userService.isAdmin(admin));

        User user = new User();
        user.setUserRole(0);
        assertFalse(userService.isAdmin(user));

        assertFalse(userService.isAdmin((User) null));
    }


    // ======================================
    // 5. getLoginUser 测试
    // ======================================
    @Test
    void testGetLoginUserSuccess() {
        User login = new User();
        login.setId(123L);

        when(session.getAttribute(USER_LOGIN_STATE)).thenReturn(login);

        User result = userService.getLoginUser(request);

        assertEquals(login, result);
    }

    @Test
    void testGetLoginUserNullRequest() {
        assertNull(userService.getLoginUser(null));
    }

    @Test
    void testGetLoginUserNotLoggedIn() {
        when(session.getAttribute(USER_LOGIN_STATE)).thenReturn(null);

        assertThrows(BusinessException.class, () ->
                userService.getLoginUser(request)
        );
    }


    // ======================================
    // 6. updateUser 测试
    // ======================================
    @Test
    void testUpdateUserInvalidId() {
        User user = new User();
        user.setId(0L);

        assertThrows(BusinessException.class, () ->
                userService.updateUser(user, new User())
        );
    }

    @Test
    void testUpdateUserNoAuth() {
        User user = new User();
        user.setId(5L);
        user.setUserRole(0);


        User loginUser = new User();
        loginUser.setId(10L); // 不是本人，也不是管理员
        loginUser.setUserRole(0);

        assertThrows(BusinessException.class, () ->
                userService.updateUser(user, loginUser)
        );
    }

    @Test
    void testUpdateUserNotFound() {
        User user = new User();
        user.setId(1L);
        user.setUserRole(0);

        User loginUser = new User();
        loginUser.setId(1L); // 本人
        loginUser.setUserRole(0);

        when(userMapper.selectById(1L)).thenReturn(null);

        assertThrows(BusinessException.class, () ->
                userService.updateUser(user, loginUser)
        );
    }

    @Test
    void testUpdateUserSuccess() {
        User user = new User();
        user.setId(1L);
        user.setUserRole(0);

        User loginUser = new User();
        loginUser.setId(1L); // 本人
        loginUser.setUserRole(0);

        when(userMapper.selectById(1L)).thenReturn(new User());
        when(userMapper.updateById(user)).thenReturn(1);

        int res = userService.updateUser(user, loginUser);
        assertEquals(1, res);
    }

    @Test
    void testUpdateUserByAdmin() {
        User user = new User();
        user.setId(100L);

        User admin = new User();
        admin.setUserRole(ADMIN_ROLE);

        when(userMapper.selectById(100L)).thenReturn(new User());
        when(userMapper.updateById(user)).thenReturn(1);

        int res = userService.updateUser(user, admin);

        assertEquals(1, res);
    }
}