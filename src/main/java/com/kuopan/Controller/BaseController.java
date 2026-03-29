package com.kuopan.Controller;

import com.kuopan.Entity.constants.Constants;
import com.kuopan.Entity.dto.SessionWebUserDto;

import javax.servlet.http.HttpSession;

public class BaseController {
    protected SessionWebUserDto getUserInfoFromSession(HttpSession session) {
        SessionWebUserDto sessionWebUserDto = (SessionWebUserDto) session.getAttribute(Constants.SESSION_KEY);
        return sessionWebUserDto;
    }
}
