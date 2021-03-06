package edu.hm.muse.controller;

import org.springframework.stereotype.Component;
import org.springframework.web.util.WebUtils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
@Component
public class LoginHelper {

    public LoginHelper() {

    }

    public boolean isNotLoggedIn(HttpServletRequest request, HttpSession session) {
        Cookie cookie = WebUtils.getCookie(request, "loggedIn");
        if (cookie == null) {
            return true;
        }
        if (cookie.getValue().equals(session.getAttribute("usertoken"))) {
            return false;
        }
        return true;
    }
}