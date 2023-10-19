package kernel.hackerthon.library.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

public class SessionInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        System.out.println("request.getSession : "+request.getSession().getAttribute("loginUser"));
        String[] allowedURLs = {
                "/user/login",
                "/user/loginProcess",
                "/user/join",
                "/user/logout"
        };  // 세션 체크하지 않을 url 명시

        String requestURI = request.getRequestURI();

        for (String allowedURL : allowedURLs) {
            if (requestURI.endsWith(allowedURL)) {
                return true;  // 세션 체크를 하지 않음
            }
        }

        if (request.getSession().getAttribute("loginUser") == null) {
            response.sendRedirect(request.getContextPath() + "/user/login");
            return false;  // 컨트롤러 실행을 중단
        }
        return true;  // 컨트롤러 실행 계속
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
                           ModelAndView modelAndView) throws Exception {

    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex)
            throws Exception {

    }
}
