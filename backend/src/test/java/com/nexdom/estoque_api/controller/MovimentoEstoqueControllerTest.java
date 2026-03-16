package com.nexdom.estoque_api.controller;

import com.nexdom.estoque.model.MovimentoEstoque;
import com.nexdom.estoque.model.Produto;
import com.nexdom.estoque.model.TipoMovimento;
import com.nexdom.estoque.service.MovimentoEstoqueService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class MovimentoEstoqueControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    MovimentoEstoqueService movimentoEstoqueService;

    private static Produto produtoRef() {
        Produto p = new Produto();
        p.setId(1L);
        p.setCodigo("COD-001");
        p.setDescricao("Notebook");
        return p;
    }

    private static MovimentoEstoque movimento(Long id, TipoMovimento tipo) {
        MovimentoEstoque m = new MovimentoEstoque();
        m.setId(id);
        m.setProduto(produtoRef());
        m.setTipo(tipo);
        m.setQuantidadeMovimentada(2);
        m.setValorVenda(new BigDecimal("3000.00"));
        m.setDataVenda(LocalDateTime.now());
        return m;
    }

    @Test
    @DisplayName("GET /api/movimentos retorna 200 e lista de movimentos")
    void buscarTodos_retorna200() throws Exception {
        when(movimentoEstoqueService.buscarTodos()).thenReturn(List.of(movimento(1L, TipoMovimento.SAIDA)));

        mockMvc.perform(get("/api/movimentos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].tipo").value("SAIDA"));

        verify(movimentoEstoqueService).buscarTodos();
    }

    @Test
    @DisplayName("GET /api/movimentos/{id} com id existente retorna 200")
    void buscarPorId_existente_retorna200() throws Exception {
        when(movimentoEstoqueService.buscarPorId(1L)).thenReturn(Optional.of(movimento(1L, TipoMovimento.ENTRADA)));

        mockMvc.perform(get("/api/movimentos/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.tipo").value("ENTRADA"));

        verify(movimentoEstoqueService).buscarPorId(1L);
    }

    @Test
    @DisplayName("GET /api/movimentos/{id} com id inexistente retorna 404")
    void buscarPorId_inexistente_retorna404() throws Exception {
        when(movimentoEstoqueService.buscarPorId(999L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/movimentos/999"))
                .andExpect(status().isNotFound());

        verify(movimentoEstoqueService).buscarPorId(999L);
    }

    @Test
    @DisplayName("POST /api/movimentos retorna 201 e movimento criado")
    void criar_retorna201() throws Exception {
        MovimentoEstoque novo = movimento(null, TipoMovimento.ENTRADA);
        novo.setId(null);
        MovimentoEstoque salvo = movimento(1L, TipoMovimento.ENTRADA);
        when(movimentoEstoqueService.criar(any(MovimentoEstoque.class))).thenReturn(salvo);

        String body = """
                {"produto":{"id":1},"tipo":"ENTRADA","quantidadeMovimentada":5,"valorVenda":2500.00,"dataVenda":"2025-01-15T10:00:00"}
                """;

        mockMvc.perform(post("/api/movimentos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.tipo").value("ENTRADA"));

        verify(movimentoEstoqueService).criar(any(MovimentoEstoque.class));
    }

    @Test
    @DisplayName("POST /api/movimentos com produto inexistente retorna 400")
    void criar_produtoInexistente_retorna400() throws Exception {
        doThrow(new IllegalArgumentException("Produto não encontrado com id: 999"))
                .when(movimentoEstoqueService).criar(any(MovimentoEstoque.class));

        String body = """
                {"produto":{"id":999},"tipo":"ENTRADA","quantidadeMovimentada":1,"valorVenda":100.00,"dataVenda":"2025-01-15T10:00:00"}
                """;

        mockMvc.perform(post("/api/movimentos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro").value("Produto não encontrado com id: 999"));
    }

    @Test
    @DisplayName("POST /api/movimentos com estoque insuficiente retorna 400")
    void criar_estoqueInsuficiente_retorna400() throws Exception {
        doThrow(new IllegalStateException("Estoque insuficiente. Disponível: 0, solicitado: 10"))
                .when(movimentoEstoqueService).criar(any(MovimentoEstoque.class));

        String body = """
                {"produto":{"id":1},"tipo":"SAIDA","quantidadeMovimentada":10,"valorVenda":3000.00,"dataVenda":"2025-01-15T10:00:00"}
                """;

        mockMvc.perform(post("/api/movimentos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro").value("Estoque insuficiente. Disponível: 0, solicitado: 10"));
    }
}
