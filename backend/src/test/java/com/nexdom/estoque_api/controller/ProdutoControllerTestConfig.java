package com.nexdom.estoque_api.controller;

import org.springframework.context.annotation.Import;
import com.nexdom.estoque.controller.ProdutoController;

/**
 * Configuração opcional para testes do ProdutoController.
 * Não use @SpringBootConfiguration aqui para evitar que outros testes (ex: HealthControllerTest)
 * carreguem este contexto.
 */
@Import(ProdutoController.class)
public class ProdutoControllerTestConfig {
}
