package com.miji.cms.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.miji.cms.common.ErrorCode;
import com.miji.cms.exception.BusinessException;
import com.miji.cms.mapper.UserMapper;
import com.miji.cms.model.domain.User;
import com.miji.cms.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.util.DigestUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import static com.miji.cms.constant.UserConstant.USER_LOGIN_STATE;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * UserServiceImpl#userRegister 方法单元测试
 */
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
 * UserServiceImpl#userLogin 方法的单元测试
 */
@DisplayName("用户登录测试")
class UserServiceImplTest {

    @InjectMocks
    @Spy
    private UserServiceImpl userService;

    @Mock
    private UserMapper userMapper;

    @Captor
    private ArgumentCaptor<QueryWrapper<User>> queryWrapperCaptor;

    private HttpServletRequest request;
    private HttpSession session;

    // 假设的盐值，需要与实际代码中的 SALT 常量一致
    private static final String SALT = "miji";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        request = new MockHttpServletRequest();
        session = new MockHttpSession();
        when(request.getSession()).thenReturn(session);
    }

    /**
     * 辅助方法：生成加盐后的 MD5 密码
     */
    private String encryptPassword(String password) {
        return DigestUtils.md5DigestAsHex((SALT + password).getBytes());
    }

    // ==================== 参数校验测试 ====================

    @Test
    @DisplayName("账号为 null 时抛出异常")
    void testUserLogin_AccountIsNull() {
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            userService.userLogin(null, "password123", 1, request);
        });
        assertEquals(ErrorCode.PARAMS_ERROR.getCode(), exception.getCode());
    }

    @Test
    @DisplayName("密码为 null 时抛出异常")
    void testUserLogin_PasswordIsNull() {
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            userService.userLogin("admin", null, 1, request);
        });
        assertEquals(ErrorCode.PARAMS_ERROR.getCode(), exception.getCode());
    }

    @Test
    @DisplayName("账号为空字符串时抛出异常")
    void testUserLogin_AccountIsEmpty() {
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            userService.userLogin("", "password123", 1, request);
        });
        assertEquals(ErrorCode.PARAMS_ERROR.getCode(), exception.getCode());
    }

    @Test
    @DisplayName("密码为空字符串时抛出异常")
    void testUserLogin_PasswordIsEmpty() {
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            userService.userLogin("admin", "", 1, request);
        });
        assertEquals(ErrorCode.PARAMS_ERROR.getCode(), exception.getCode());
    }

    @Test
    @DisplayName("账号长度小于4时抛出异常")
    void testUserLogin_AccountTooShort() {
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            userService.userLogin("abc", "password123", 1, request);
        });
        assertEquals(ErrorCode.PARAMS_ERROR.getCode(), exception.getCode());
        assertEquals("用户名不规范", exception.getMessage());
    }

    @Test
    @DisplayName("账号长度等于4时可以通过（边界测试）")
    void testUserLogin_AccountMinLength() {
        // 准备 mock 数据
        User mockUser = createMockUser(1L, "abcd", "password123", 1);
        when(userMapper.selectOne(any(QueryWrapper.class))).thenReturn(mockUser);

        // 不应该抛出异常
        assertDoesNotThrow(() -> {
            userService.userLogin("abcd", "password123", 1, request);
        });
    }

    @Test
    @DisplayName("密码长度小于8时抛出异常")
    void testUserLogin_PasswordTooShort() {
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            userService.userLogin("admin", "1234567", 1, request);
        });
        assertEquals(ErrorCode.PARAMS_ERROR.getCode(), exception.getCode());
        assertEquals("用户密码不规范", exception.getMessage());
    }

    @Test
    @DisplayName("密码长度等于8时可以通过（边界测试）")
    void testUserLogin_PasswordMinLength() {
        // 准备 mock 数据
        User mockUser = createMockUser(1L, "admin", "12345678", 1);
        when(userMapper.selectOne(any(QueryWrapper.class))).thenReturn(mockUser);

        // 不应该抛出异常
        assertDoesNotThrow(() -> {
            userService.userLogin("admin", "12345678", 1, request);
        });
    }

    @Test
    @DisplayName("账号包含标点符号时抛出异常")
    void testUserLogin_AccountWithPunctuation() {
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            userService.userLogin("admin!", "password123", 1, request);
        });
        assertEquals(ErrorCode.PARAMS_ERROR.getCode(), exception.getCode());
        assertEquals("用户名不支持特殊字符", exception.getMessage());
    }

    @Test
    @DisplayName("账号包含特殊符号时抛出异常")
    void testUserLogin_AccountWithSpecialCharacter() {
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            userService.userLogin("admin@123", "password123", 1, request);
        });
        assertEquals(ErrorCode.PARAMS_ERROR.getCode(), exception.getCode());
        assertEquals("用户名不支持特殊字符", exception.getMessage());
    }

    @Test
    @DisplayName("账号包含空格时抛出异常")
    void testUserLogin_AccountWithSpace() {
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            userService.userLogin("admin 123", "password123", 1, request);
        });
        assertEquals(ErrorCode.PARAMS_ERROR.getCode(), exception.getCode());
        assertEquals("用户名不支持特殊字符", exception.getMessage());
    }

    // ==================== 业务逻辑测试 ====================

    @Test
    @DisplayName("用户不存在时抛出异常")
    void testUserLogin_UserNotFound() {
        // 模拟数据库查询返回 null
        when(userMapper.selectOne(any(QueryWrapper.class))).thenReturn(null);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            userService.userLogin("admin", "password123", 1, request);
        });

        assertEquals(ErrorCode.PARAMS_ERROR.getCode(), exception.getCode());
        assertEquals("用户不存在或密码错误", exception.getMessage());

        // 验证数据库被调用
        verify(userMapper, times(1)).selectOne(any(QueryWrapper.class));
    }

    @Test
    @DisplayName("密码错误时抛出异常")
    void testUserLogin_WrongPassword() {
        // 模拟数据库查询返回 null（密码不匹配）
        when(userMapper.selectOne(any(QueryWrapper.class))).thenReturn(null);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            userService.userLogin("admin", "wrongpassword", 1, request);
        });

        assertEquals(ErrorCode.PARAMS_ERROR.getCode(), exception.getCode());
        assertEquals("用户不存在或密码错误", exception.getMessage());
    }

    @Test
    @DisplayName("角色不匹配时抛出异常")
    void testUserLogin_RoleMismatch() {
        // 模拟数据库查询返回 null（角色不匹配）
        when(userMapper.selectOne(any(QueryWrapper.class))).thenReturn(null);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            userService.userLogin("admin", "password123", 2, request);
        });

        assertEquals(ErrorCode.PARAMS_ERROR.getCode(), exception.getCode());
        assertEquals("用户不存在或密码错误", exception.getMessage());
    }

    // ==================== 成功登录测试 ====================

    @Test
    @DisplayName("成功登录 - 验证完整流程")
    void testUserLogin_Successful() {
        // 1. 准备测试数据
        String userAccount = "admin";
        String userPassword = "password123";
        Integer userRole = 1;

        User rawUser = createMockUser(1L, userAccount, userPassword, userRole);

        // 2. 模拟数据库查询
        when(userMapper.selectOne(queryWrapperCaptor.capture())).thenReturn(rawUser);

        // 3. 执行登录
        User result = userService.userLogin(userAccount, userPassword, userRole, request);

        // 4. 验证返回结果
        assertNotNull(result, "登录结果不应该为 null");
        assertEquals(rawUser.getId(), result.getId());
        assertEquals(rawUser.getUserAccount(), result.getUserAccount());
        assertEquals(rawUser.getUserName(), result.getUserName());
        assertEquals(rawUser.getUserRole(), result.getUserRole());
        assertNull(result.getUserPassword(), "密码应该被脱敏");

        // 5. 验证查询条件
        QueryWrapper<User> capturedQuery = queryWrapperCaptor.getValue();
        assertNotNull(capturedQuery);
        // 注意：这里无法直接验证 QueryWrapper 的具体条件，但可以验证调用次数
        verify(userMapper, times(1)).selectOne(any(QueryWrapper.class));

        // 6. 验证 Session
        Object sessionAttr = session.getAttribute(USER_LOGIN_STATE);
        assertNotNull(sessionAttr, "Session 中应该有用户登录状态");
        assertTrue(sessionAttr instanceof User, "Session 中应该是 User 对象");

        User sessionUser = (User) sessionAttr;
        assertEquals(rawUser.getId(), sessionUser.getId());
        assertEquals(rawUser.getUserAccount(), sessionUser.getUserAccount());
        assertNull(sessionUser.getUserPassword(), "Session 中的密码也应该被脱敏");

        // 7. 验证 getSession 被调用
        verify(request, times(1)).getSession();
    }

    @Test
    @DisplayName("不同角色用户成功登录")
    void testUserLogin_DifferentRoles() {
        // 测试普通用户（角色 0）
        User normalUser = createMockUser(2L, "user001", "password123", 0);
        when(userMapper.selectOne(any(QueryWrapper.class))).thenReturn(normalUser);

        User result = userService.userLogin("user001", "password123", 0, request);

        assertNotNull(result);
        assertEquals(0, result.getUserRole());

        // 测试管理员用户（角色 1）
        User adminUser = createMockUser(1L, "admin", "password123", 1);
        when(userMapper.selectOne(any(QueryWrapper.class))).thenReturn(adminUser);

        User adminResult = userService.userLogin("admin", "password123", 1, request);

        assertNotNull(adminResult);
        assertEquals(1, adminResult.getUserRole());
    }

    @Test
    @DisplayName("同一用户多次登录会更新 Session")
    void testUserLogin_MultipleLogins() {
        User user = createMockUser(1L, "admin", "password123", 1);
        when(userMapper.selectOne(any(QueryWrapper.class))).thenReturn(user);

        // 第一次登录
        User firstLogin = userService.userLogin("admin", "password123", 1, request);
        assertNotNull(firstLogin);

        // 第二次登录（模拟同一用户再次登录）
        User secondLogin = userService.userLogin("admin", "password123", 1, request);
        assertNotNull(secondLogin);

        // Session 应该被更新
        User sessionUser = (User) session.getAttribute(USER_LOGIN_STATE);
        assertNotNull(sessionUser);
        assertEquals(user.getId(), sessionUser.getId());

        // 验证数据库被调用了两次
        verify(userMapper, times(2)).selectOne(any(QueryWrapper.class));
    }

    // ==================== 辅助方法 ====================

    /**
     * 创建 Mock 用户对象
     */
    private User createMockUser(Long id, String account, String password, Integer role) {
        User user = new User();
        user.setId(id);
        user.setUserAccount(account);
        user.setUserName("测试用户" + id);
        // 使用加盐的 MD5 加密
        user.setUserPassword(encryptPassword(password));
        user.setUserRole(role);

        return user;
    }
}