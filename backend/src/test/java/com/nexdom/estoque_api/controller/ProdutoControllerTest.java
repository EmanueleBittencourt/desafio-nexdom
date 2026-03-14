package com.nexdom.estoque_api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexdom.estoque.controller.ProdutoController;
import com.nexdom.estoque.model.Produto;
import com.nexdom.estoque.model.TipoProduto;
import com.nexdom.estoque.service.ProdutoService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProdutoController.class)
@ContextConfiguration(classes = ProdutoControllerTestConfig.class)
class ProdutoControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    ProdutoService produtoService;

    private static final ObjectMapper objectMapper = new ObjectMapper();

    private static Produto produto(Long id) {
        Produto p = new Produto();
        p.setId(id);
        p.setCodigo("COD-001");
        p.setDescricao("Notebook");
        p.setTipo(TipoProduto.ELETRONICO);
        p.setValorFornecedor(new BigDecimal("2500.00"));
        p.setQuantidadeEstoque(10);
        return p;
    }

    @Test
    @DisplayName("GET /api/produtos retorna 200 e lista de produtos")
    void buscarTodos_deveRetornar200() throws Exception {
        when(produtoService.buscarTodos()).thenReturn(List.of(produto(1L)));

        mockMvc.perform(get("/api/produtos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].codigo").value("COD-001"));

        verify(produtoService).buscarTodos();
    }

    @Test
    @DisplayName("GET /api/produtos/{id} com id existente retorna 200 e o produto")
    void buscarPorId_existente_retorna200() throws Exception {
        when(produtoService.buscarPorId(1L)).thenReturn(Optional.of(produto(1L)));

        mockMvc.perform(get("/api/produtos/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.codigo").value("COD-001"));

        verify(produtoService).buscarPorId(1L);
    }

    @Test
    @DisplayName("GET /api/produtos/{id} com id inexistente retorna 404")
    void buscarPorId_inexistente_retorna404() throws Exception {
        when(produtoService.buscarPorId(999L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/produtos/999"))
                .andExpect(status().isNotFound());

        verify(produtoService).buscarPorId(999L);
    }

    @Test
    @DisplayName("POST /api/produtos retorna 201 e o produto criado")
    void criar_retorna201() throws Exception {
        Produto novo = produto(null);
        novo.setId(null);
        Produto salvo = produto(1L);

        when(produtoService.criar(any(Produto.class))).thenReturn(salvo);

        mockMvc.perform(post("/api/produtos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(novo)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.codigo").value("COD-001"));

        verify(produtoService).criar(any(Produto.class));
    }

    @Test
    @DisplayName("PUT /api/produtos/{id} com id existente retorna 200")
    void atualizar_existente_retorna200() throws Exception {
        Produto atualizado = produto(1L);
        when(produtoService.atualizar(eq(1L), any(Produto.class))).thenReturn(atualizado);

        mockMvc.perform(put("/api/produtos/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(produto(1L))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));

        verify(produtoService).atualizar(eq(1L), any(Produto.class));
    }

    @Test
    @DisplayName("PUT /api/produtos/{id} com id inexistente retorna 404")
    void atualizar_inexistente_retorna404() throws Exception {
        when(produtoService.atualizar(eq(999L), any(Produto.class)))
                .thenThrow(new RuntimeException("Produto não encontrado"));

        mockMvc.perform(put("/api/produtos/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(produto(999L))))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE /api/produtos/{id} com id existente retorna 204")
    void excluir_existente_retorna204() throws Exception {
        mockMvc.perform(delete("/api/produtos/1"))
                .andExpect(status().isNoContent());

        verify(produtoService).excluir(1L);
    }

    @Test
    @DisplayName("DELETE /api/produtos/{id} com id inexistente retorna 404")
    void excluir_inexistente_retorna404() throws Exception {
        doThrow(new RuntimeException("Produto não encontrado")).when(produtoService).excluir(999L);

        mockMvc.perform(delete("/api/produtos/999"))
                .andExpect(status().isNotFound());
    }
}
