package com.nexdom.estoque.service;

import com.nexdom.estoque.model.MovimentoEstoque;
import com.nexdom.estoque.model.Produto;
import com.nexdom.estoque.model.TipoMovimento;
import com.nexdom.estoque.repository.MovimentoEstoqueRepository;
import com.nexdom.estoque.repository.ProdutoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MovimentoEstoqueServiceImpl implements MovimentoEstoqueService {

    private final MovimentoEstoqueRepository movimentoEstoqueRepository;
    private final ProdutoRepository produtoRepository;

    @Override
    @Transactional(readOnly = true)
    public List<MovimentoEstoque> listarTodos() {
        return movimentoEstoqueRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<MovimentoEstoque> buscarPorId(Long id) {
        return movimentoEstoqueRepository.findById(id);
    }

    @Override
    @Transactional
    public MovimentoEstoque salvar(MovimentoEstoque movimento) {
        Produto produto = produtoRepository.findById(movimento.getProduto().getId())
                .orElseThrow(() -> new IllegalArgumentException("Produto não encontrado com id: " + movimento.getProduto().getId()));

        int quantidadeAtual = produto.getQuantidadeEstoque();
        int quantidadeMovimentada = movimento.getQuantidadeMovimentada();

        if (movimento.getTipo() == TipoMovimento.SAIDA && quantidadeAtual < quantidadeMovimentada) {
            throw new IllegalStateException(
                    "Estoque insuficiente. Disponível: " + quantidadeAtual + ", solicitado: " + quantidadeMovimentada);
        }

        produto.setQuantidadeEstoque(quantidadeAtual + (movimento.getTipo() == TipoMovimento.SAIDA ? -quantidadeMovimentada : quantidadeMovimentada));
        produtoRepository.save(produto);

        movimento.setProduto(produto);
        return movimentoEstoqueRepository.save(movimento);
    }
}