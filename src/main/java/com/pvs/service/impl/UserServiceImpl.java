package com.pvs.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pvs.dto.LoginFormDTO;
import com.pvs.dto.Result;
import com.pvs.dto.UserDTO;
import com.pvs.entity.User;
import com.pvs.mapper.UserMapper;
import com.pvs.service.IUserService;
import com.pvs.utils.PasswordEncoder;
import com.pvs.utils.RedisConstants;
import com.pvs.utils.RegexUtils;
import com.pvs.utils.SystemConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public Result sendCode(String phone, HttpSession session) {
        if (RegexUtils.isPhoneInvalid(phone)) {
            return Result.fail("手机号格式错误");
        }
        //生成随机验证码，通过hutool工具类
        String code = RandomUtil.randomNumbers(6);
//        //将code保存到session
//        session.setAttribute("code", code);
        //将信息保存在redis中
        stringRedisTemplate.opsForValue().set(RedisConstants.LOGIN_CODE_KEY + phone, code, 2, TimeUnit.MINUTES);//设置过期时间
        log.info("code{}", code);
        return Result.ok("验证码发送成功");
    }

    /**
     * 登录功能
     */
    @Override
    public Result login(LoginFormDTO loginForm, HttpSession session) {
        String code = loginForm.getCode();
        String phone = loginForm.getPhone();


        //判断验证码是否正确
        String scode = stringRedisTemplate.opsForValue().get(RedisConstants.LOGIN_CODE_KEY + phone);

        if (!code.equals(scode)) {
            return Result.fail("验证码错误");
        }
        //判断用户是否存在
//            //mybatis
//            if(userMapper.getByPhone(loginForm.getPhone()) == null){
//                //不存在将用户添加到数据库
//                //设置一个默认的用户名和密码
//                User user = new User();
//                user.setPassword(PasswordEncoder.encode(RandomUtil.randomNumbers(6)));
//                user.setNickName(RandomUtil.randomString(6));
//                user.setCreateTime(LocalDateTime.now());
//                user.setPhone(loginForm.getPhone());
//                userMapper.addUser(user);
//            }
        //mp
        User user = query().eq("phone", phone).one();
        if (user == null) {
            //创建user
            user = crateUserWithPhone(phone);
            save(user);
        }
//            //保存用户到session
//            session.setAttribute(UserConstant.USER, user);
        //保存用户到Redis
        //随机生成token，作为登录令牌,并返回给前端，让其保存至sessionStorage，每次请求都会携带
        String token = UUID.randomUUID().toString();
        //将User对象转换为HashMap
        UserDTO userDTO = BeanUtil.copyProperties(user, UserDTO.class);
        //通过工具类将实体类转成Map集合
        Map<String, Object> userMap = BeanUtil.beanToMap(userDTO, new HashMap<>(), CopyOptions.create()
                .setIgnoreNullValue(true)
                .setFieldValueEditor((fieldName, fieldValue) -> fieldValue.toString()));//将每个属性的值都转换为字符串
        String tokenKey = RedisConstants.LOGIN_USER_KEY + token;
        stringRedisTemplate.opsForHash().putAll(tokenKey, userMap);
        //设置有效期
        stringRedisTemplate.expire(tokenKey, 30, TimeUnit.MINUTES);

        //将token返回
        return Result.ok(token);
    }

    @Override
    public void logout() {

    }

    private User crateUserWithPhone(String phone) {
        User user = new User();
        user.setPassword(PasswordEncoder.encode(RandomUtil.randomNumbers(6)));
        user.setNickName(SystemConstants.USER_NICK_NAME_PREFIX + RandomUtil.randomString(6));
        user.setCreateTime(LocalDateTime.now());
        user.setPhone(phone);
        return user;
    }
}
