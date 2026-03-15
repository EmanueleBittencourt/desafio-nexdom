package com.nexdom.estoque_api.service;

import com.nexdom.estoque.dto.LucroProdutoResponse;
import com.nexdom.estoque.dto.ProdutoComResumoDTO;
import com.nexdom.estoque.dto.ResumoEstoqueDTO;
import com.nexdom.estoque.dto.ResumoVendasProdutoDTO;
import com.nexdom.estoque.model.MovimentoEstoque;
import com.nexdom.estoque.model.Produto;
import com.nexdom.estoque.model.TipoMovimento;
import com.nexdom.estoque.model.TipoProduto;
import com.nexdom.estoque.repository.MovimentoEstoqueRepository;
import com.nexdom.estoque.repository.ProdutoRepository;
import com.nexdom.estoque.service.ProdutoServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProdutoServiceImplTest {

    @Mock
    ProdutoRepository produtoRepository;

    @Mock
    MovimentoEstoqueRepository movimentoEstoqueRepository;

    @InjectMocks
    ProdutoServiceImpl produtoService;

    private Produto produto;

    @BeforeEach
    void setUp() {
        produto = new Produto();
        produto.setId(1L);
        produto.setCodigo("COD-001");
        produto.setDescricao("Notebook");
        produto.setTipo(TipoProduto.ELETRONICO);
        produto.setValorFornecedor(new BigDecimal("2500.00"));
        produto.setQuantidadeEstoque(10);
    }

    @Test
    @DisplayName("buscarTodos retorna lista sem excluídos")
    void buscarTodos_retornaListaSemExcluidos() {
        when(produtoRepository.findAllByDataExclusaoIsNull()).thenReturn(List.of(produto));

        List<Produto> result = produtoService.buscarTodos();

        assertThat(result).hasSize(1).first().satisfies(p -> {
            assertThat(p.getId()).isEqualTo(1L);
            assertThat(p.getCodigo()).isEqualTo("COD-001");
        });
        verify(produtoRepository).findAllByDataExclusaoIsNull();
    }

    @Test
    @DisplayName("buscarPorId existente retorna Optional com produto")
    void buscarPorId_existente_retornaOptional() {
        when(produtoRepository.findById(1L)).thenReturn(Optional.of(produto));

        assertThat(produtoService.buscarPorId(1L)).contains(produto);
        verify(produtoRepository).findById(1L);
    }

    @Test
    @DisplayName("buscarPorId inexistente retorna Optional vazio")
    void buscarPorId_inexistente_retornaVazio() {
        when(produtoRepository.findById(999L)).thenReturn(Optional.empty());

        assertThat(produtoService.buscarPorId(999L)).isEmpty();
    }

    @Test
    @DisplayName("buscarPorTipo retorna DTOs com totalSaidas do repositório")
    void buscarPorTipo_retornaComResumo() {
        when(produtoRepository.findByTipoAndDataExclusaoIsNull(TipoProduto.ELETRONICO))
                .thenReturn(List.of(produto));
        when(movimentoEstoqueRepository.sumQuantidadeSaidaByProdutoId())
                .thenReturn(List.<Object[]>of(new Object[]{1L, 3}));

        List<ProdutoComResumoDTO> result = produtoService.buscarPorTipo(TipoProduto.ELETRONICO);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).id()).isEqualTo(1L);
        assertThat(result.get(0).totalSaidas()).isEqualTo(3);
        verify(produtoRepository).findByTipoAndDataExclusaoIsNull(TipoProduto.ELETRONICO);
        verify(movimentoEstoqueRepository).sumQuantidadeSaidaByProdutoId();
    }

    @Test
    @DisplayName("buscarTodosComResumo retorna lista com totais de saída")
    void buscarTodosComResumo_retornaListaComResumo() {
        when(produtoRepository.findAllByDataExclusaoIsNull()).thenReturn(List.of(produto));
        when(movimentoEstoqueRepository.sumQuantidadeSaidaByProdutoId())
                .thenReturn(List.<Object[]>of(new Object[]{1L, 2}));

        List<ProdutoComResumoDTO> result = produtoService.buscarTodosComResumo();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).totalSaidas()).isEqualTo(2);
    }

    @Test
    @DisplayName("listarTodosParaFiltro retorna todos incluindo excluídos")
    void listarTodosParaFiltro_retornaTodos() {
        when(produtoRepository.findAll()).thenReturn(List.of(produto));
        when(movimentoEstoqueRepository.sumQuantidadeSaidaByProdutoId())
                .thenReturn(List.<Object[]>of(new Object[]{1L, 0}));

        List<ProdutoComResumoDTO> result = produtoService.listarTodosParaFiltro();

        assertThat(result).hasSize(1);
        verify(produtoRepository).findAll();
    }

    @Test
    @DisplayName("criar define datas e defaults e salva")
    void criar_defineDatasESalva() {
        Produto entrada = new Produto();
        entrada.setCodigo("COD-2");
        entrada.setDescricao("Mouse");
        entrada.setTipo(TipoProduto.ELETRONICO);
        when(produtoRepository.saveAndFlush(any(Produto.class))).thenAnswer(inv -> {
            Produto p = inv.getArgument(0);
            p.setId(2L);
            return p;
        });

        Produto result = produtoService.criar(entrada);

        assertThat(result.getId()).isEqualTo(2L);
        assertThat(entrada.getDataCadastro()).isNotNull();
        assertThat(entrada.getDataAtualizacao()).isNotNull();
        assertThat(entrada.getValorFornecedor()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(entrada.getQuantidadeEstoque()).isZero();
        verify(produtoRepository).saveAndFlush(any(Produto.class));
    }

    @Test
    @DisplayName("atualizar com id existente atualiza e retorna produto")
    void atualizar_existente_retornaProduto() {
        when(produtoRepository.findById(1L)).thenReturn(Optional.of(produto));
        when(produtoRepository.save(any(Produto.class))).thenReturn(produto);

        Produto atual = new Produto();
        atual.setCodigo("COD-1-UPD");
        atual.setDescricao("Notebook Atualizado");
        atual.setTipo(TipoProduto.ELETRONICO);
        atual.setValorFornecedor(new BigDecimal("2600.00"));
        atual.setQuantidadeEstoque(5);

        Produto result = produtoService.atualizar(1L, atual);

        assertThat(produto.getCodigo()).isEqualTo("COD-1-UPD");
        assertThat(produto.getQuantidadeEstoque()).isEqualTo(5);
        verify(produtoRepository).save(produto);
    }

    @Test
    @DisplayName("atualizar com id inexistente lança RuntimeException")
    void atualizar_inexistente_lancaExcecao() {
        when(produtoRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> produtoService.atualizar(999L, produto))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("999");
    }

    @Test
    @DisplayName("excluir com estoque zero soft delete")
    void excluir_estoqueZero_softDelete() {
        produto.setQuantidadeEstoque(0);
        when(produtoRepository.findById(1L)).thenReturn(Optional.of(produto));
        when(produtoRepository.save(any(Produto.class))).thenReturn(produto);

        produtoService.excluir(1L);

        verify(produtoRepository).save(produto);
        assertThat(produto.getDataExclusao()).isNotNull();
    }

    @Test
    @DisplayName("excluir com estoque > 0 lança IllegalStateException")
    void excluir_comEstoque_lancaIllegalState() {
        when(produtoRepository.findById(1L)).thenReturn(Optional.of(produto));

        assertThatThrownBy(() -> produtoService.excluir(1L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("quantidade em estoque for zero");
    }

    @Test
    @DisplayName("excluir com id inexistente lança RuntimeException")
    void excluir_inexistente_lancaExcecao() {
        when(produtoRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> produtoService.excluir(999L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("999");
    }

    @Test
    @DisplayName("getLucroPorProduto calcula lucro e quantidade de saída")
    void getLucroPorProduto_calculaLucro() {
        when(produtoRepository.findById(1L)).thenReturn(Optional.of(produto));
        MovimentoEstoque m = new MovimentoEstoque();
        m.setQuantidadeMovimentada(2);
        m.setValorVenda(new BigDecimal("3000.00"));
        when(movimentoEstoqueRepository.findByProdutoIdAndTipo(1L, TipoMovimento.SAIDA))
                .thenReturn(List.of(m));

        LucroProdutoResponse result = produtoService.getLucroPorProduto(1L);

        assertThat(result.quantidadeTotalSaida()).isEqualTo(2);
        // Receita 2 * 3000 = 6000, Custo 2 * 2500 = 5000, Lucro = 1000
        assertThat(result.lucro()).isEqualByComparingTo(new BigDecimal("1000.00"));
    }

    @Test
    @DisplayName("getLucroPorProduto id inexistente lança RuntimeException")
    void getLucroPorProduto_inexistente_lancaExcecao() {
        when(produtoRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> produtoService.getLucroPorProduto(999L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("999");
    }

    @Test
    @DisplayName("getResumoEstoque sem filtro retorna totais gerais")
    void getResumoEstoque_semFiltro_retornaTotais() {
        when(produtoRepository.findAllByDataExclusaoIsNull()).thenReturn(List.of(produto));
        when(movimentoEstoqueRepository.sumQuantidadeByTipoAndProdutoIdIn(TipoMovimento.ENTRADA, List.of(1L)))
                .thenReturn(50L);
        when(movimentoEstoqueRepository.sumQuantidadeByTipoAndProdutoIdIn(TipoMovimento.SAIDA, List.of(1L)))
                .thenReturn(20L);
        when(movimentoEstoqueRepository.findByProdutoIdInAndTipo(anyList(), eq(TipoMovimento.SAIDA)))
                .thenReturn(List.of());

        ResumoEstoqueDTO result = produtoService.getResumoEstoque(null);

        assertThat(result.quantidadeEntrada()).isEqualTo(50L);
        assertThat(result.quantidadeSaida()).isEqualTo(20L);
    }

    @Test
    @DisplayName("getResumoEstoque com produtoId inexistente retorna zeros")
    void getResumoEstoque_produtoInexistente_retornaZeros() {
        when(produtoRepository.findById(999L)).thenReturn(Optional.empty());

        ResumoEstoqueDTO result = produtoService.getResumoEstoque(999L);

        assertThat(result.quantidadeEntrada()).isZero();
        assertThat(result.quantidadeSaida()).isZero();
        assertThat(result.lucro()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("getResumoVendasPorProduto retorna DTO com totais")
    void getResumoVendasPorProduto_retornaDTO() {
        when(produtoRepository.findById(1L)).thenReturn(Optional.of(produto));
        MovimentoEstoque m = new MovimentoEstoque();
        m.setQuantidadeMovimentada(2);
        m.setValorVenda(new BigDecimal("3000.00"));
        when(movimentoEstoqueRepository.findByProdutoIdAndTipo(1L, TipoMovimento.SAIDA))
                .thenReturn(List.of(m));

        ResumoVendasProdutoDTO result = produtoService.getResumoVendasPorProduto(1L);

        assertThat(result.quantidadeTotalSaidas()).isEqualTo(2);
        assertThat(result.valorTotalVenda()).isEqualByComparingTo(new BigDecimal("6000.00"));
        assertThat(result.lucro()).isEqualByComparingTo(new BigDecimal("1000.00")); // 2*(3000-2500)
    }

    @Test
    @DisplayName("getResumoVendas com produtoId null retorna totais gerais")
    void getResumoVendas_semProdutoId_retornaTotais() {
        when(produtoRepository.findAll()).thenReturn(List.of(produto));
        when(movimentoEstoqueRepository.findByProdutoIdInAndTipo(List.of(1L), TipoMovimento.SAIDA))
                .thenReturn(List.of());

        ResumoVendasProdutoDTO result = produtoService.getResumoVendas(null);

        assertThat(result.quantidadeTotalSaidas()).isZero();
        assertThat(result.valorTotalVenda()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(result.lucro()).isEqualByComparingTo(BigDecimal.ZERO);
    }
}
