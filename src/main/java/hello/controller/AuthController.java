package hello.controller;

import hello.entity.User;
import hello.service.UserService;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.Map;

@Controller
public class AuthController {

    private UserService userService;
    private AuthenticationManager authenticationManager;

    @Inject
    public AuthController(UserService userService, AuthenticationManager authenticationManager) {
        this.userService = userService;
        this.authenticationManager = authenticationManager;
    }

    @GetMapping("/auth")
    @ResponseBody
    public Object auth() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User loggedInUser = userService.getUserByUsername(username);

        if (loggedInUser == null) {
            return Result.success("用户没有登录", false);
        }
        return Result.success(null, true, loggedInUser);
    }

    @PostMapping("/auth/login")
    @ResponseBody
    public Result login(@RequestBody Map<String, String> usernameAndPassword) {
        String username = usernameAndPassword.get("username");
        String password = usernameAndPassword.get("password");

        UserDetails userDetails;
        try {
            userDetails = userService.loadUserByUsername(username);
        } catch (UsernameNotFoundException e) {
            return Result.failure("用户" + username + "不存在");
        }

        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(userDetails, password);

        try {
            authenticationManager.authenticate(token);
            SecurityContextHolder.getContext().setAuthentication(token);

            return Result.success("登录成功", true, userService.getUserByUsername(username));

        } catch (BadCredentialsException e) {
            return Result.failure("密码不正确");
        }
    }

    @PostMapping("/auth/register")
    @ResponseBody
    public Result register(@RequestBody Map<String, String> usernameAndPassword) {
        String username = usernameAndPassword.get("username");
        String password = usernameAndPassword.get("password");

        if (username == null || password == null) {
            return Result.failure("username/password == null");
        }
        if (username.length() < 1 || username.length() > 15) {
            return Result.failure("invalid username");
        }
        if (password.length() < 1 || password.length() > 15) {
            return Result.failure("invalid password");
        }

        try {
            userService.save(username, password);
        } catch (DuplicateKeyException e) {
            return Result.failure("user already exist");
        }

        return Result.success("success", false);
    }

    @GetMapping("/auth/logout")
    @ResponseBody
    public Object logout() {
        String userName = SecurityContextHolder.getContext().getAuthentication().getName();
        User loggedInUser = userService.getUserByUsername(userName);

        if (loggedInUser == null) {
            return Result.failure("用户没有登录");
        }
        SecurityContextHolder.clearContext();
        return Result.success("注销成功", false);
    }


    private static class Result {
        String status;
        String msg;
        boolean isLogin;
        Object data;


        public Result(String status, String msg, boolean isLogin) {
            this(status, msg, isLogin, null);
        }

        public Result(String status, String msg, boolean isLogin, Object data) {
            this.status = status;
            this.msg = msg;
            this.isLogin = isLogin;
            this.data = data;
        }

        public static Result failure(String msg) {
            return new Result("fail", msg, false);
        }

        public static Result success(String msg, boolean isLogin) {
            return Result.success(msg, isLogin, null);
        }

        public static Result success(String msg, boolean isLogin, Object data) {
            return new Result("ok", msg, isLogin, data);
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getMsg() {
            return msg;
        }

        public void setMsg(String msg) {
            this.msg = msg;
        }

        public boolean isLogin() {
            return isLogin;
        }

        public void setLogin(boolean login) {
            isLogin = login;
        }

        public Object getData() {
            return data;
        }

        public void setData(Object data) {
            this.data = data;
        }
    }
}
