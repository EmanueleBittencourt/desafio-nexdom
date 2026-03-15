package com.nexdom.estoque_api.service;

import com.nexdom.estoque.model.MovimentoEstoque;
import com.nexdom.estoque.model.Produto;
import com.nexdom.estoque.model.TipoMovimento;
import com.nexdom.estoque.repository.MovimentoEstoqueRepository;
import com.nexdom.estoque.repository.ProdutoRepository;
import com.nexdom.estoque.service.MovimentoEstoqueServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MovimentoEstoqueServiceImplTest {

    @Mock
    MovimentoEstoqueRepository movimentoEstoqueRepository;

    @Mock
    ProdutoRepository produtoRepository;

    @InjectMocks
    MovimentoEstoqueServiceImpl movimentoEstoqueService;

    private Produto produto;
    private MovimentoEstoque movimentoEntrada;
    private MovimentoEstoque movimentoSaida;

    @BeforeEach
    void setUp() {
        produto = new Produto();
        produto.setId(1L);
        produto.setCodigo("COD-001");
        produto.setDescricao("Notebook");
        produto.setQuantidadeEstoque(10);

        movimentoEntrada = new MovimentoEstoque();
        movimentoEntrada.setProduto(produto);
        movimentoEntrada.setTipo(TipoMovimento.ENTRADA);
        movimentoEntrada.setQuantidadeMovimentada(5);
        movimentoEntrada.setValorVenda(new BigDecimal("2500.00"));
        movimentoEntrada.setDataVenda(LocalDateTime.now());

        movimentoSaida = new MovimentoEstoque();
        movimentoSaida.setProduto(produto);
        movimentoSaida.setTipo(TipoMovimento.SAIDA);
        movimentoSaida.setQuantidadeMovimentada(3);
        movimentoSaida.setValorVenda(new BigDecimal("3000.00"));
        movimentoSaida.setDataVenda(LocalDateTime.now());
    }

    @Test
    @DisplayName("buscarTodos retorna lista do repositório")
    void buscarTodos_retornaLista() {
        when(movimentoEstoqueRepository.findAllWithProduto()).thenReturn(List.of(movimentoEntrada));

        List<MovimentoEstoque> result = movimentoEstoqueService.buscarTodos();

        assertThat(result).hasSize(1);
        verify(movimentoEstoqueRepository).findAllWithProduto();
    }

    @Test
    @DisplayName("buscarPorId existente retorna Optional com movimento")
    void buscarPorId_existente_retornaOptional() {
        when(movimentoEstoqueRepository.findById(1L)).thenReturn(Optional.of(movimentoEntrada));

        assertThat(movimentoEstoqueService.buscarPorId(1L)).contains(movimentoEntrada);
    }

    @Test
    @DisplayName("buscarPorId inexistente retorna Optional vazio")
    void buscarPorId_inexistente_retornaVazio() {
        when(movimentoEstoqueRepository.findById(999L)).thenReturn(Optional.empty());

        assertThat(movimentoEstoqueService.buscarPorId(999L)).isEmpty();
    }

    @Test
    @DisplayName("criar ENTRADA incrementa quantidade no produto e salva")
    void criar_entrada_incrementaEstoque() {
        when(produtoRepository.findById(1L)).thenReturn(Optional.of(produto));
        when(movimentoEstoqueRepository.save(any(MovimentoEstoque.class))).thenAnswer(inv -> {
            MovimentoEstoque m = inv.getArgument(0);
            m.setId(1L);
            return m;
        });

        MovimentoEstoque result = movimentoEstoqueService.criar(movimentoEntrada);

        assertThat(produto.getQuantidadeEstoque()).isEqualTo(15);
        verify(produtoRepository).save(produto);
        verify(movimentoEstoqueRepository).save(movimentoEntrada);
        assertThat(movimentoEntrada.getProduto()).isEqualTo(produto);
    }

    @Test
    @DisplayName("criar SAIDA decrementa quantidade no produto e salva")
    void criar_saida_decrementaEstoque() {
        when(produtoRepository.findById(1L)).thenReturn(Optional.of(produto));
        when(movimentoEstoqueRepository.save(any(MovimentoEstoque.class))).thenReturn(movimentoSaida);

        movimentoEstoqueService.criar(movimentoSaida);

        assertThat(produto.getQuantidadeEstoque()).isEqualTo(7);
        verify(produtoRepository).save(produto);
    }

    @Test
    @DisplayName("criar SAIDA com estoque insuficiente lança IllegalStateException")
    void criar_saida_estoqueInsuficiente_lancaExcecao() {
        produto.setQuantidadeEstoque(2);
        when(produtoRepository.findById(1L)).thenReturn(Optional.of(produto));

        assertThatThrownBy(() -> movimentoEstoqueService.criar(movimentoSaida))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Estoque insuficiente")
                .hasMessageContaining("2")
                .hasMessageContaining("3");
    }

    @Test
    @DisplayName("criar com produto inexistente lança IllegalArgumentException")
    void criar_produtoInexistente_lancaExcecao() {
        when(produtoRepository.findById(999L)).thenReturn(Optional.empty());
        movimentoEntrada.getProduto().setId(999L);

        assertThatThrownBy(() -> movimentoEstoqueService.criar(movimentoEntrada))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Produto não encontrado")
                .hasMessageContaining("999");
    }
}
