package com.butorin.starter.resolver;

import com.butorin.starter.annotation.Auth;
import com.butorin.starter.dto.AuthorizationParams;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;

import jakarta.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthArgumentResolverUnitTest {

    private final AuthArgumentResolver resolver = new AuthArgumentResolver();

    @Mock
    private NativeWebRequest webRequest;

    @Mock
    private HttpServletRequest servletRequest;

    @Test
    void supportsParameter_onlyAuthAnnotatedAuthorizationParams() throws Exception {
        Method ok = Probe.class.getDeclaredMethod("withAuth", AuthorizationParams.class);
        Method bad = Probe.class.getDeclaredMethod("withoutAuth", String.class);

        assertThat(resolver.supportsParameter(new MethodParameter(ok, 0))).isTrue();
        assertThat(resolver.supportsParameter(new MethodParameter(bad, 0))).isFalse();
    }

    @Test
    void resolveArgument_buildsAuthorizationParamsFromRequest() throws Exception {
        when(webRequest.getNativeRequest()).thenReturn(servletRequest);
        when(servletRequest.getHeader("user-id")).thenReturn("uid");
        when(servletRequest.getHeader("user-roles")).thenReturn("[\"A\",\"B\"]");

        Method m = Probe.class.getDeclaredMethod("withAuth", AuthorizationParams.class);
        MethodParameter parameter = new MethodParameter(m, 0);

        Object resolved = resolver.resolveArgument(parameter, null, webRequest, (WebDataBinderFactory) null);

        assertThat(resolved).isInstanceOf(AuthorizationParams.class);
        AuthorizationParams params = (AuthorizationParams) resolved;
        assertThat(params.getUserId()).isEqualTo("uid");
        assertThat(params.getRoles()).containsExactly("A", "B");
    }

    @SuppressWarnings("unused")
    static class Probe {
        void withAuth(@Auth AuthorizationParams p) {
        }

        void withoutAuth(String x) {
        }
    }
}
