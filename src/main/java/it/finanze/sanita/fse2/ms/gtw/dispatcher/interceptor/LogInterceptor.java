package it.finanze.sanita.fse2.ms.gtw.dispatcher.interceptor;

import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

/**
 * 
 *	@author vincenzoingenito
 *
 */
@Component
@ConditionalOnProperty("ms.dispatcher.audit.enabled")
public class LogInterceptor implements HandlerInterceptor {
     
     
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) { 
        request.setAttribute("START_TIME", new Date());  
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, ModelAndView modelAndView) throws Exception {
    	 //Questo metodo è lasciato intenzionalmente vuoto
    }

    @Override
    public void afterCompletion(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, Exception e) throws Exception {
    	//Questo metodo è lasciato intenzionalmente vuoto
    }
}