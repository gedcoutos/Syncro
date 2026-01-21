package com.syncro.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Registra o AdminSeguranca e define rotas protegidas/excluídas.
 * Listamos apenas o que é sensível para evitar liberar algo por engano.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final AdminSeguranca adminSeguranca;

    public WebConfig(AdminSeguranca adminSeguranca) {
        this.adminSeguranca = adminSeguranca;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(adminSeguranca)
                .addPathPatterns(
                        // ===== VEÍCULOS =====
                        "/veiculos/registro",    // GET formulário de cadastro (bloqueio de GET p/ operador)
                        "/veiculos",             // POST salvar novo veículo
                        "/veiculos/*/editar",    // GET/POST editar veículo
                        "/veiculos/*/entregar",  // POST dar baixa/entregar

                        // ===== PEÇAS =====
                        "/pecas/*/editar",       // POST editar peça

                        // ===== FUNCIONÁRIOS =====
                        "/funcionarios/**"       // GET novo + POST salvar + futuras rotas
                )
                .excludePathPatterns(
                        // ===== PÚBLICO/ABERTO =====
                        "/",
                        "/home",
                        "/login",
                        "/logout",
                        "/consulta-placa",

                        // Detalhes e arquivos públicos de veículo (somente leitura)
                        // (NÃO usamos "/veiculos/*" aqui, para não excluir /veiculos/registro!)
                        "/veiculos/*/fotos",
                        "/veiculos/*/os",
                        "/veiculos/os/**",

                        // Consultas de peças liberadas / fluxo de cadastro de peças liberado
                        "/pecas/api/**",
                        "/pecas/estoque/consulta",
                        "/pecas/estoque/consulta/buscar",
                        "/pecas/cadastrar",
                        "/pecas/novo",
                        "/pecas/avulsas/**",

                        // ===== ESTÁTICOS =====
                        "/css/**",
                        "/js/**",
                        "/images/**",
                        "/webjars/**"
                );
    }
}
