package com.syncro.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;

/**
 * Interceptor simples de autorização por papel na sessão.
 * Espera que o LoginController coloque na sessão:
 *   - "papel" = "ADMIN" ou "OPERADOR"
 */
@Component
public class AdminSeguranca implements HandlerInterceptor {

    private static final AntPathMatcher MATCHER = new AntPathMatcher();

    private boolean match(String pattern, String uri) {
        return MATCHER.match(pattern, uri);
    }

    @Override
    public boolean preHandle(HttpServletRequest req, HttpServletResponse resp, Object handler) throws IOException {
        HttpSession session = req.getSession(false);
        String papel = (session == null) ? null : (String) session.getAttribute("papel");
        String uri   = req.getRequestURI();
        String method = req.getMethod();

        // Log de diagnóstico (pode remover depois)
        System.out.println("[SEGURANCA] " + method + " " + uri + " | papel=" + papel);

        boolean isAdmin = "ADMIN".equalsIgnoreCase(papel);

        // 1) Bloqueia GET de telas de cadastro que você quer deixar visíveis no menu,
        //    mas não acessáveis por OPERADOR (cadastrar veículo e cadastrar funcionário)
        // 1) Bloqueia GET de telas de cadastro/edição para OPERADOR
        if (!isAdmin && "GET".equalsIgnoreCase(method)) {
            if (match("/veiculos/registro", uri) ||
                    match("/funcionarios/novo", uri) ||
                    match("/veiculos/*/editar", uri)   // 👈 adicionada: bloqueia a tela de editar veículo
            ) {
                if (session != null) session.setAttribute("flashErro", "PERMISSÃO DE USUÁRIO NEGADA.");
                resp.sendRedirect(req.getContextPath() + "/home?perm=negada");
                return false;
            }
        }


        // 2) Bloqueia mutações (POST/PUT/DELETE) de áreas sensíveis
        if (!isAdmin && ("POST".equalsIgnoreCase(method)
                || "PUT".equalsIgnoreCase(method)
                || "DELETE".equalsIgnoreCase(method))) {

            if (match("/veiculos/**", uri)
                    || match("/pecas/**", uri)
                    || match("/funcionarios/**", uri)) {

                if (session != null) session.setAttribute("flashErro", "PERMISSÃO DE USUÁRIO NEGADA.");
                resp.sendRedirect(req.getContextPath() + "/home?perm=negada");
                return false;
            }
        }

        return true; // autorizado
    }
}
