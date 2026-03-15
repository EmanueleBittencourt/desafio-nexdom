package com.nexdom.estoque.service;

import com.nexdom.estoque.dto.LucroProdutoResponse;
import com.nexdom.estoque.model.MovimentoEstoque;
import com.nexdom.estoque.model.Produto;
import com.nexdom.estoque.model.TipoMovimento;
import com.nexdom.estoque.model.TipoProduto;
import com.nexdom.estoque.repository.MovimentoEstoqueRepository;
import com.nexdom.estoque.repository.ProdutoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProdutoServiceImpl implements ProdutoService {

    private final ProdutoRepository produtoRepository;
    private final MovimentoEstoqueRepository movimentoEstoqueRepository;

    @Override
    @Transactional(readOnly = true)
    public List<Produto> buscarTodos() {
        return produtoRepository.findAllByDataExclusaoIsNull();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Produto> buscarPorId(Long id) {
        return produtoRepository.findByIdAndDataExclusaoIsNull(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Produto> buscarPorTipo(TipoProduto tipo) {
        return produtoRepository.findByTipoAndDataExclusaoIsNull(tipo);
    }

    @Override
    @Transactional
    public Produto criar(Produto produto) {
        LocalDateTime agora = LocalDateTime.now();
        produto.setDataCadastro(agora);
        produto.setDataAtualizacao(agora);
        if (produto.getValorFornecedor() == null) {
            produto.setValorFornecedor(BigDecimal.ZERO);
        }
        if (produto.getQuantidadeEstoque() == null) {
            produto.setQuantidadeEstoque(0);
        }
        return produtoRepository.saveAndFlush(produto);
    }

    @Override
    @Transactional
    public Produto atualizar(Long id, Produto produto) {
        return produtoRepository.findByIdAndDataExclusaoIsNull(id)
                .map(existente -> {
                    existente.setCodigo(produto.getCodigo());
                    existente.setDescricao(produto.getDescricao());
                    existente.setTipo(produto.getTipo());
                    existente.setValorFornecedor(produto.getValorFornecedor());
                    existente.setQuantidadeEstoque(produto.getQuantidadeEstoque());
                    return produtoRepository.save(existente);
                })
                .orElseThrow(() -> new RuntimeException("Produto não encontrado com id: " + id));
    }

    @Override
    @Transactional
    public void excluir(Long id) {
        Produto produto = produtoRepository.findByIdAndDataExclusaoIsNull(id)
                .orElseThrow(() -> new RuntimeException("Produto não encontrado com id: " + id));
        if (produto.getQuantidadeEstoque() != null && produto.getQuantidadeEstoque() != 0) {
            throw new IllegalStateException("Produto só pode ser excluído quando a quantidade em estoque for zero.");
        }
        produto.setDataExclusao(LocalDateTime.now());
        produtoRepository.save(produto);
    }

    @Override
    @Transactional(readOnly = true)
    public LucroProdutoResponse getLucroPorProduto(Long id) {
        Produto produto = produtoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Produto não encontrado com id: " + id));

        List<MovimentoEstoque> saidas = movimentoEstoqueRepository.findByProdutoIdAndTipo(id, TipoMovimento.SAIDA);

        int qtdSaida = saidas.stream().mapToInt(MovimentoEstoque::getQuantidadeMovimentada).sum();

        BigDecimal receitaTotal = saidas.stream()
                .map(m -> m.getValorVenda().multiply(BigDecimal.valueOf(m.getQuantidadeMovimentada())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal custoTotal = produto.getValorFornecedor().multiply(BigDecimal.valueOf(qtdSaida));

        BigDecimal lucro = receitaTotal.subtract(custoTotal);

        return new LucroProdutoResponse(lucro, qtdSaida);
    }
}
