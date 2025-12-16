package com.miji.cms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.miji.cms.exception.BusinessException;
import com.miji.cms.mapper.UserMapper;
import com.miji.cms.model.domain.User;
import com.miji.cms.service.impl.UserServiceImpl;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.Nested;
import org.mockito.*;
import org.springframework.util.DigestUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * UserServiceImpl 单元测试（整合版）
 */
@DisplayName("UserService 单元测试")
class UserServiceImplTest {

    @Mock
    private UserMapper userMapper;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpSession session;

    @InjectMocks
    private UserServiceImpl userService;

    private static final String USER_LOGIN_STATE = "userLoginState";
    private static final String SALT = "miji";
    private static final int ADMIN_ROLE = 1;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(request.getSession()).thenReturn(session);
    }

    // =========================================================
    // userRegister 测试
    // =========================================================
    @Nested
    @DisplayName("userRegister 测试")
    class UserRegisterTest {

        @Test
        void testUserRegisterSuccess() {
            when(userMapper.selectCount(any(QueryWrapper.class))).thenReturn(0L);
            when(userMapper.insert(any(User.class))).thenAnswer(invocation -> {
                User user = invocation.getArgument(0);
                user.setId(100L);
                return 1;
            });

            long userId = userService.userRegister(
                    "testuser", "password123", "password123", 1);

            assertEquals(100L, userId);
        }

        @Test
        void testUserRegisterParamNull() {
            assertThrows(BusinessException.class, () ->
                    userService.userRegister(null, "pass1234", "pass1234", 1));
        }

        @Test
        void testUserRegisterAccountTooShort() {
            assertThrows(BusinessException.class, () ->
                    userService.userRegister("abc", "pass1234", "pass1234", 1));
        }

        @Test
        void testUserRegisterPasswordTooShort() {
            assertThrows(BusinessException.class, () ->
                    userService.userRegister("testuser", "1234567", "1234567", 1));
        }

        @Test
        void testUserRegisterInvalidAccount() {
            assertThrows(BusinessException.class, () ->
                    userService.userRegister("test@user", "pass1234", "pass1234", 1));
        }

        @Test
        void testUserRegisterPasswordNotMatch() {
            assertThrows(BusinessException.class, () ->
                    userService.userRegister("testuser", "pass1234", "diff", 1));
        }

        @Test
        void testUserRegisterDuplicateAccount() {
            when(userMapper.selectCount(any(QueryWrapper.class))).thenReturn(1L);

            assertThrows(BusinessException.class, () ->
                    userService.userRegister("testuser", "pass1234", "pass1234", 1));
        }

        @Test
        void testUserRegisterInsertFail() {
            when(userMapper.selectCount(any(QueryWrapper.class))).thenReturn(0L);
            when(userMapper.insert(any(User.class))).thenReturn(0);

            assertThrows(BusinessException.class, () ->
                    userService.userRegister("testuser", "pass1234", "pass1234", 1));
        }
    }

    // =========================================================
    // userLogin 测试
    // =========================================================
    @Nested
    @DisplayName("userLogin 测试")
    class UserLoginTest {

        @Test
        void testUserLoginSuccess() {
            String account = "mijiUser";
            String password = "12345678";

            String encryptPwd =
                    DigestUtils.md5DigestAsHex((SALT + password).getBytes());

            User user = new User();
            user.setId(1L);
            user.setUserAccount(account);
            user.setUserPassword(encryptPwd);
            user.setUserRole(1);

            when(userMapper.selectOne(any(QueryWrapper.class))).thenReturn(user);

            User result = userService.userLogin(account, password, 1, request);

            assertNotNull(result);
            verify(session).setAttribute(eq(USER_LOGIN_STATE), any());
        }

        @Test
        void testUserLoginInvalidAccountLength() {
            assertThrows(BusinessException.class, () ->
                    userService.userLogin("abc", "12345678", 1, request));
        }

        @Test
        void testUserLoginInvalidPasswordLength() {
            assertThrows(BusinessException.class, () ->
                    userService.userLogin("abcdef", "123", 1, request));
        }

        @Test
        void testUserLoginInvalidChar() {
            assertThrows(BusinessException.class, () ->
                    userService.userLogin("abc###", "12345678", 1, request));
        }

        @Test
        void testUserLoginNotFound() {
            when(userMapper.selectOne(any(QueryWrapper.class))).thenReturn(null);

            assertThrows(BusinessException.class, () ->
                    userService.userLogin("validUser", "12345678", 1, request));
        }
    }

    // =========================================================
    // 其他方法测试
    // =========================================================
    @Nested
    @DisplayName("其他方法测试")
    class OtherMethodTest {

        @Test
        void testGetSafetyUser() {
            User origin = new User();
            origin.setId(1L);
            origin.setUserAccount("test");
            origin.setUserPassword("secret");
            origin.setUserRole(1);
            origin.setCreateTime(new Date());

            User safe = userService.getSafetyUser(origin);

            assertNotNull(safe);
            assertNull(safe.getUserPassword());
        }

        @Test
        void testUserLogout() {
            int res = userService.userLogout(request);
            assertEquals(1, res);
            verify(session).removeAttribute(USER_LOGIN_STATE);
        }

        @Test
        void testIsAdminByRequest() {
            User admin = new User();
            admin.setUserRole(ADMIN_ROLE);

            when(session.getAttribute(USER_LOGIN_STATE)).thenReturn(admin);
            assertTrue(userService.isAdmin(request));
        }

        @Test
        void testGetLoginUserSuccess() {
            User loginUser = new User();
            loginUser.setId(1L);

            when(session.getAttribute(USER_LOGIN_STATE)).thenReturn(loginUser);

            User res = userService.getLoginUser(request);
            assertEquals(loginUser, res);
        }

        @Test
        void testGetLoginUserNotLogin() {
            when(session.getAttribute(USER_LOGIN_STATE)).thenReturn(null);

            assertThrows(BusinessException.class, () ->
                    userService.getLoginUser(request));
        }

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

        @Test
        void testUpdateUserByAdmin() {
            User user = new User();
            user.setId(10L);

            User admin = new User();
            admin.setUserRole(ADMIN_ROLE);

            when(userMapper.selectById(10L)).thenReturn(new User());
            when(userMapper.updateById(user)).thenReturn(1);

            int res = userService.updateUser(user, admin);
            assertEquals(1, res);
        }
    }
}
