package com.nexdom.estoque_api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexdom.estoque.dto.LucroProdutoResponse;
import com.nexdom.estoque.dto.ProdutoComResumoDTO;
import com.nexdom.estoque.dto.ResumoEstoqueDTO;
import com.nexdom.estoque.dto.ResumoVendasProdutoDTO;
import com.nexdom.estoque.dto.TipoProdutoOption;
import com.nexdom.estoque.model.Produto;
import com.nexdom.estoque.model.TipoProduto;
import com.nexdom.estoque.service.ProdutoService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
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

@SpringBootTest
@AutoConfigureMockMvc
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

    private static ProdutoComResumoDTO produtoResumo(Long id) {
        return new ProdutoComResumoDTO(
                id, "COD-001", "Notebook", TipoProduto.ELETRONICO,
                10, new BigDecimal("2500.00"), 2);
    }

    // --- GET /api/produtos (listar) ---

    @Test
    @DisplayName("GET /api/produtos sem params retorna 200 e lista com resumo (buscarTodosComResumo)")
    void listar_semParams_retorna200_eListaComResumo() throws Exception {
        when(produtoService.buscarTodosComResumo()).thenReturn(List.of(produtoResumo(1L)));

        mockMvc.perform(get("/api/produtos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].codigo").value("COD-001"))
                .andExpect(jsonPath("$[0].totalSaidas").value(2));

        verify(produtoService).buscarTodosComResumo();
    }

    @Test
    @DisplayName("GET /api/produtos?tipo=ELETRONICO retorna 200 e lista por tipo")
    void listar_comTipo_retorna200() throws Exception {
        when(produtoService.buscarPorTipo(TipoProduto.ELETRONICO))
                .thenReturn(List.of(produtoResumo(1L)));

        mockMvc.perform(get("/api/produtos").param("tipo", "ELETRONICO"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));

        verify(produtoService).buscarPorTipo(TipoProduto.ELETRONICO);
    }

    @Test
    @DisplayName("GET /api/produtos?paraFiltro=true retorna 200 e listarTodosParaFiltro")
    void listar_paraFiltro_retorna200() throws Exception {
        when(produtoService.listarTodosParaFiltro()).thenReturn(List.of(produtoResumo(1L)));

        mockMvc.perform(get("/api/produtos").param("paraFiltro", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));

        verify(produtoService).listarTodosParaFiltro();
    }

    @Test
    @DisplayName("GET /api/produtos?resumo=true retorna 200 e getResumoEstoque")
    void listar_resumo_retorna200() throws Exception {
        ResumoEstoqueDTO resumo = new ResumoEstoqueDTO(100L, 30L, new BigDecimal("500.00"));
        when(produtoService.getResumoEstoque(null)).thenReturn(resumo);

        mockMvc.perform(get("/api/produtos").param("resumo", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantidadeEntrada").value(100))
                .andExpect(jsonPath("$.quantidadeSaida").value(30));

        verify(produtoService).getResumoEstoque(null);
    }

    @Test
    @DisplayName("GET /api/produtos?resumoVendas=true retorna 200 e getResumoVendas")
    void listar_resumoVendas_retorna200() throws Exception {
        ResumoVendasProdutoDTO resumo = new ResumoVendasProdutoDTO(
                10, new BigDecimal("5000.00"), new BigDecimal("500.00"));
        when(produtoService.getResumoVendas(null)).thenReturn(resumo);

        mockMvc.perform(get("/api/produtos").param("resumoVendas", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantidadeTotalSaidas").value(10));

        verify(produtoService).getResumoVendas(null);
    }

    @Test
    @DisplayName("GET /api/produtos/tipos retorna 200 e lista de tipos")
    void listarTipos_retorna200() throws Exception {
        mockMvc.perform(get("/api/produtos/tipos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].value").exists())
                .andExpect(jsonPath("$[0].label").exists());
    }

    // --- GET /api/produtos/{id} ---

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
    @DisplayName("GET /api/produtos/{id}/lucro retorna 200 e lucro")
    void buscarLucro_existente_retorna200() throws Exception {
        when(produtoService.getLucroPorProduto(1L))
                .thenReturn(new LucroProdutoResponse(new BigDecimal("500.00"), 5));

        mockMvc.perform(get("/api/produtos/1/lucro"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantidadeTotalSaida").value(5));

        verify(produtoService).getLucroPorProduto(1L);
    }

    @Test
    @DisplayName("GET /api/produtos/{id}/lucro com id inexistente retorna 404")
    void buscarLucro_inexistente_retorna404() throws Exception {
        when(produtoService.getLucroPorProduto(999L))
                .thenThrow(new RuntimeException("Produto não encontrado"));

        mockMvc.perform(get("/api/produtos/999/lucro"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/produtos/{id}/resumo-vendas retorna 200")
    void resumoVendas_existente_retorna200() throws Exception {
        ResumoVendasProdutoDTO dto = new ResumoVendasProdutoDTO(
                3, new BigDecimal("1500.00"), new BigDecimal("300.00"));
        when(produtoService.getResumoVendasPorProduto(1L)).thenReturn(dto);

        mockMvc.perform(get("/api/produtos/1/resumo-vendas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantidadeTotalSaidas").value(3));

        verify(produtoService).getResumoVendasPorProduto(1L);
    }

    @Test
    @DisplayName("GET /api/produtos/{id}/resumo-vendas com id inexistente retorna 404")
    void resumoVendas_inexistente_retorna404() throws Exception {
        when(produtoService.getResumoVendasPorProduto(999L))
                .thenThrow(new RuntimeException("Produto não encontrado"));

        mockMvc.perform(get("/api/produtos/999/resumo-vendas"))
                .andExpect(status().isNotFound());
    }

    // --- POST /api/produtos ---

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

    // --- PUT /api/produtos/{id} ---

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
                .thenThrow(new RuntimeException("Produto não encontrado com id: 999"));

        mockMvc.perform(put("/api/produtos/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(produto(999L))))
                .andExpect(status().isNotFound());
    }

    // --- DELETE /api/produtos/{id} ---

    @Test
    @DisplayName("DELETE /api/produtos/{id} com id existente e estoque zero retorna 204")
    void excluir_existente_estoqueZero_retorna204() throws Exception {
        mockMvc.perform(delete("/api/produtos/1"))
                .andExpect(status().isNoContent());

        verify(produtoService).excluir(1L);
    }

    @Test
    @DisplayName("DELETE /api/produtos/{id} com estoque > 0 retorna 400")
    void excluir_comEstoque_retorna400() throws Exception {
        doThrow(new IllegalStateException("Produto só pode ser excluído quando a quantidade em estoque for zero."))
                .when(produtoService).excluir(1L);

        mockMvc.perform(delete("/api/produtos/1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Produto só pode ser excluído quando a quantidade em estoque for zero."));
    }

    @Test
    @DisplayName("DELETE /api/produtos/{id} com id inexistente retorna 404")
    void excluir_inexistente_retorna404() throws Exception {
        doThrow(new RuntimeException("Produto não encontrado com id: 999")).when(produtoService).excluir(999L);

        mockMvc.perform(delete("/api/produtos/999"))
                .andExpect(status().isNotFound());
    }
}
