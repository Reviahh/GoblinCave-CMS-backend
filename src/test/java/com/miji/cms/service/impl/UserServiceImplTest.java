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

        @Test
        void testUpdateUserBySelf() {
            User user = new User();
            user.setId(10L);

            User loginUser = new User();
            loginUser.setId(10L);
            loginUser.setUserRole(0);

            when(userMapper.selectById(10L)).thenReturn(new User());
            when(userMapper.updateById(user)).thenReturn(1);

            int res = userService.updateUser(user, loginUser);
            assertEquals(1, res);
        }

        @Test
        void testUpdateUserNoAuth() {
            User user = new User();
            user.setId(10L);

            User otherUser = new User();
            otherUser.setId(20L);
            otherUser.setUserRole(0);

            assertThrows(BusinessException.class, () ->
                    userService.updateUser(user, otherUser));
        }

        @Test
        void testUpdateUserNotExist() {
            User user = new User();
            user.setId(999L);

            User admin = new User();
            admin.setUserRole(ADMIN_ROLE);

            when(userMapper.selectById(999L)).thenReturn(null);

            assertThrows(BusinessException.class, () ->
                    userService.updateUser(user, admin));
        }

        @Test
        void testUpdateUserInvalidId() {
            User user = new User();
            user.setId(0L);

            User admin = new User();
            admin.setUserRole(ADMIN_ROLE);

            assertThrows(BusinessException.class, () ->
                    userService.updateUser(user, admin));
        }

        @Test
        void testIsAdminWithNonAdmin() {
            User normalUser = new User();
            normalUser.setUserRole(0);

            when(session.getAttribute(USER_LOGIN_STATE)).thenReturn(normalUser);
            assertFalse(userService.isAdmin(request));
        }

        @Test
        void testIsAdminWithNullUser() {
            when(session.getAttribute(USER_LOGIN_STATE)).thenReturn(null);
            assertFalse(userService.isAdmin(request));
        }

        @Test
        void testIsAdminByUser() {
            User admin = new User();
            admin.setUserRole(ADMIN_ROLE);
            assertTrue(userService.isAdmin(admin));

            User normalUser = new User();
            normalUser.setUserRole(0);
            assertFalse(userService.isAdmin(normalUser));

            assertFalse(userService.isAdmin((User) null));
        }

        @Test
        void testGetSafetyUserWithNull() {
            User result = userService.getSafetyUser(null);
            assertNull(result);
        }

        @Test
        void testGetLoginUserWithNullRequest() {
            User result = userService.getLoginUser(null);
            assertNull(result);
        }
    }

    // =========================================================
    // userRegister 边界测试
    // =========================================================
    @Nested
    @DisplayName("userRegister 边界测试")
    class UserRegisterEdgeCaseTest {

        @Test
        void testUserRegisterEmptyPassword() {
            assertThrows(BusinessException.class, () ->
                    userService.userRegister("testuser", "", "", 1));
        }

        @Test
        void testUserRegisterEmptyAccount() {
            assertThrows(BusinessException.class, () ->
                    userService.userRegister("", "pass1234", "pass1234", 1));
        }

        @Test
        void testUserRegisterWithSpaceInAccount() {
            assertThrows(BusinessException.class, () ->
                    userService.userRegister("test user", "pass1234", "pass1234", 1));
        }

        @Test
        void testUserRegisterWithSymbolsInAccount() {
            assertThrows(BusinessException.class, () ->
                    userService.userRegister("test!user", "pass1234", "pass1234", 1));
        }

        @Test
        void testUserRegisterWithDotInAccount() {
            assertThrows(BusinessException.class, () ->
                    userService.userRegister("test.user", "pass1234", "pass1234", 1));
        }
    }

    // =========================================================
    // userLogin 边界测试
    // =========================================================
    @Nested
    @DisplayName("userLogin 边界测试")
    class UserLoginEdgeCaseTest {

        @Test
        void testUserLoginEmptyAccount() {
            assertThrows(BusinessException.class, () ->
                    userService.userLogin("", "12345678", 1, request));
        }

        @Test
        void testUserLoginEmptyPassword() {
            assertThrows(BusinessException.class, () ->
                    userService.userLogin("testuser", "", 1, request));
        }

        @Test
        void testUserLoginNullAccount() {
            assertThrows(BusinessException.class, () ->
                    userService.userLogin(null, "12345678", 1, request));
        }

        @Test
        void testUserLoginNullPassword() {
            assertThrows(BusinessException.class, () ->
                    userService.userLogin("testuser", null, 1, request));
        }

        @Test
        void testUserLoginWithSpaceInAccount() {
            assertThrows(BusinessException.class, () ->
                    userService.userLogin("test user", "12345678", 1, request));
        }
    }

    // =========================================================
    // User 实体测试
    // =========================================================
    @Nested
    @DisplayName("User 实体测试")
    class UserEntityTest {

        @Test
        void testUserAllFields() {
            User user = new User();
            Date now = new Date();

            user.setId(1L);
            user.setUserName("测试用户");
            user.setUserAccount("testuser");
            user.setUserPassword("password123");
            user.setUserUrl("http://avatar.com/user.png");
            user.setGender(1);
            user.setPhone("13800138000");
            user.setEmail("test@example.com");
            user.setTags("tag1,tag2");
            user.setUserRole(1);
            user.setCreateTime(now);
            user.setUpdateTime(now);
            user.setIsDelete(0);

            assertEquals(1L, user.getId());
            assertEquals("测试用户", user.getUserName());
            assertEquals("testuser", user.getUserAccount());
            assertEquals("password123", user.getUserPassword());
            assertEquals("http://avatar.com/user.png", user.getUserUrl());
            assertEquals(1, user.getGender());
            assertEquals("13800138000", user.getPhone());
            assertEquals("test@example.com", user.getEmail());
            assertEquals("tag1,tag2", user.getTags());
            assertEquals(1, user.getUserRole());
            assertEquals(now, user.getCreateTime());
            assertEquals(now, user.getUpdateTime());
            assertEquals(0, user.getIsDelete());
        }

        @Test
        void testGetSafetyUserAllFields() {
            User origin = new User();
            Date now = new Date();
            origin.setId(1L);
            origin.setUserName("测试用户");
            origin.setUserAccount("testaccount");
            origin.setUserPassword("secret");
            origin.setUserUrl("http://avatar.com/user.png");
            origin.setGender(1);
            origin.setPhone("13800138000");
            origin.setEmail("test@example.com");
            origin.setTags("tag1");
            origin.setUserRole(1);
            origin.setCreateTime(now);

            User safe = userService.getSafetyUser(origin);

            assertNotNull(safe);
            assertEquals(1L, safe.getId());
            assertEquals("测试用户", safe.getUserName());
            assertEquals("testaccount", safe.getUserAccount());
            assertNull(safe.getUserPassword());
            assertEquals("http://avatar.com/user.png", safe.getUserUrl());
            assertEquals(1, safe.getGender());
            assertEquals("13800138000", safe.getPhone());
            assertEquals("test@example.com", safe.getEmail());
            assertEquals("tag1", safe.getTags());
            assertEquals(1, safe.getUserRole());
            assertEquals(now, safe.getCreateTime());
        }
    }
}
