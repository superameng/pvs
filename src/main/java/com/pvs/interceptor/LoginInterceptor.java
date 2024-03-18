package com.pvs.interceptor;

import cn.hutool.core.bean.BeanUtil;
import com.pvs.dto.UserDTO;
import com.pvs.utils.RedisConstants;
import com.pvs.utils.UserHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author 高志强
 * @version 1.0
 */
@Component
@Slf4j
public class LoginInterceptor implements HandlerInterceptor {
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
//        //如果不是动态资源就不拦截
//        //从session中获取当前对象
//        HttpSession session = request.getSession();
//        //获取session中的user
//        User user = (User) session.getAttribute(UserConstant.USER);
        //获取请求头中的token
        String token = request.getHeader("authorization");
        //token为空代表未登录
        if(token == null){
            response.setStatus(404);//返回给前端错误信息
            return false;
        }
        //根据token来获取存储的user对象
        Map<Object, Object> userMap = stringRedisTemplate.opsForHash().entries(RedisConstants.LOGIN_USER_KEY + token);
        //将Map集合转为user对象
        if(userMap.isEmpty()){
            return false;
        }
        UserDTO userDTO = BeanUtil.fillBeanWithMap(userMap, new UserDTO(), false);

        //将User保存到LocalThread中
        UserHolder.saveUser(userDTO);
        //刷新用户信息有效期
        stringRedisTemplate.expire(RedisConstants.LOGIN_USER_KEY + token, 30, TimeUnit.MINUTES);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        //将threadLocal保存的数据清除
        UserHolder.removeUser();
    }
}
