package com.butorin.starter.resolver;

import com.butorin.starter.annotation.Auth;
import com.butorin.starter.dto.AuthorizationParams;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.util.Arrays;
import java.util.List;

public class AuthArgumentResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(Auth.class)
                && parameter.getParameterType().equals(AuthorizationParams.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter,
                                  ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest,
                                  WebDataBinderFactory binderFactory) throws Exception {

        HttpServletRequest request = (HttpServletRequest) webRequest.getNativeRequest();

        String userId = request.getHeader("user-id");
        String rolesHeader = request.getHeader("user-roles");

        List<String> roles = rolesHeader != null
                ? Arrays.asList(rolesHeader.replace("[", "").replace("]", "").replace("\"", "").split(","))
                : List.of();

        return new AuthorizationParams(userId, roles);
    }
}
