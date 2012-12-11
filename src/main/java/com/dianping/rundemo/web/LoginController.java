package com.dianping.rundemo.web;

import java.io.IOException;
import java.net.URLDecoder;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.view.RedirectView;

import com.dianping.rundemo.utils.QQWeiboAPIUtils;

@Controller
public class LoginController {
    private static final Logger LOG = LoggerFactory.getLogger(LoginController.class);

    @RequestMapping(value = "/logined")
    public RedirectView logined(HttpSession session, HttpServletRequest request, HttpServletResponse response, String code)
            throws NoSuchAlgorithmException, IOException {
        //根据code访问key
        LOG.info("logined, code=" + code);
        Map<String, String> map = QQWeiboAPIUtils.getAccessToken(code);//
        String access_token = (String) map.get("access_token");
        LOG.info("logined:" + map);
        if (StringUtils.isNotBlank(access_token)) {//登录成功
            session.setAttribute("access_token", access_token);
            Object expires_in = map.get("expires_in");
            session.setAttribute("expires_in", expires_in);
            String username = (String) map.get("name");
            if (username != null) {
                session.setAttribute("username", URLDecoder.decode(username, "UTF-8"));
            }
            return new RedirectView(request.getContextPath() + "/");
        } else {//登录失败
            return new RedirectView(request.getContextPath() + "/?errorMsg=failed");
        }

    }

}
