package com.nexdom.estoque_api.controller;

import org.springframework.boot.SpringBootConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import com.nexdom.estoque.controller.ProdutoController;

@SpringBootConfiguration
@Import(ProdutoController.class)
public class ProdutoControllerTestConfig {
}
