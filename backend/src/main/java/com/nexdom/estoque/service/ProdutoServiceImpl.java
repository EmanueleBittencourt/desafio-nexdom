package com.nexdom.estoque.service;

import com.nexdom.estoque.model.Produto;
import com.nexdom.estoque.model.TipoProduto;
import com.nexdom.estoque.repository.ProdutoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProdutoServiceImpl implements ProdutoService {

    private final ProdutoRepository produtoRepository;

    @Override
    @Transactional(readOnly = true)
    public List<Produto> listarTodos() {
        return produtoRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Produto> buscarPorId(Long id) {
        return produtoRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Produto> listarPorTipo(TipoProduto tipo) {
        return produtoRepository.findByTipo(tipo);
    }

    @Override
    @Transactional
    public Produto salvar(Produto produto) {
        return produtoRepository.save(produto);
    }

    @Override
    @Transactional
    public Produto atualizar(Long id, Produto produto) {
        return produtoRepository.findById(id)
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
        produtoRepository.deleteById(id);
    }
}
