package com.nexdom.estoque.service;

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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

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
        return produtoRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProdutoComResumoDTO> buscarPorTipo(TipoProduto tipo) {
        List<Produto> produtos = produtoRepository.findByTipoAndDataExclusaoIsNull(tipo);
        List<Object[]> somas = movimentoEstoqueRepository.sumQuantidadeSaidaByProdutoId();
        Map<Long, Integer> mapaSaidas = somas.stream()
                .collect(Collectors.toMap(
                        row -> (Long) row[0],
                        row -> ((Number) row[1]).intValue()
                ));
        return produtos.stream()
                .map(p -> new ProdutoComResumoDTO(
                        p.getId(),
                        p.getCodigo(),
                        p.getDescricao(),
                        p.getTipo(),
                        p.getQuantidadeEstoque(),
                        p.getValorFornecedor(),
                        mapaSaidas.getOrDefault(p.getId(), 0)
                ))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProdutoComResumoDTO> buscarTodosComResumo() {
        List<Produto> produtos = produtoRepository.findAllByDataExclusaoIsNull();
        List<Object[]> somas = movimentoEstoqueRepository.sumQuantidadeSaidaByProdutoId();
        Map<Long, Integer> mapaSaidas = somas.stream()
                .collect(Collectors.toMap(
                        row -> (Long) row[0],
                        row -> ((Number) row[1]).intValue()
                ));
        return produtos.stream()
                .map(p -> new ProdutoComResumoDTO(
                        p.getId(),
                        p.getCodigo(),
                        p.getDescricao(),
                        p.getTipo(),
                        p.getQuantidadeEstoque(),
                        p.getValorFornecedor(),
                        mapaSaidas.getOrDefault(p.getId(), 0)
                ))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProdutoComResumoDTO> listarTodosParaFiltro() {
        List<Produto> produtos = produtoRepository.findAll();
        List<Object[]> somas = movimentoEstoqueRepository.sumQuantidadeSaidaByProdutoId();
        Map<Long, Integer> mapaSaidas = somas.stream()
                .collect(Collectors.toMap(
                        row -> (Long) row[0],
                        row -> ((Number) row[1]).intValue()
                ));
        return produtos.stream()
                .map(p -> new ProdutoComResumoDTO(
                        p.getId(),
                        p.getCodigo(),
                        p.getDescricao(),
                        p.getTipo(),
                        p.getQuantidadeEstoque(),
                        p.getValorFornecedor(),
                        mapaSaidas.getOrDefault(p.getId(), 0)
                ))
                .toList();
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
        Produto salvo = produtoRepository.saveAndFlush(produto);
        if (salvo.getQuantidadeEstoque() != null && salvo.getQuantidadeEstoque() > 0) {
            MovimentoEstoque entrada = new MovimentoEstoque();
            entrada.setProduto(salvo);
            entrada.setTipo(TipoMovimento.ENTRADA);
            entrada.setQuantidadeMovimentada(salvo.getQuantidadeEstoque());
            entrada.setValorVenda(BigDecimal.ZERO);
            entrada.setDataVenda(agora);
            movimentoEstoqueRepository.save(entrada);
        }
        return salvo;
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
        Produto produto = produtoRepository.findById(id)
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

    @Override
    @Transactional(readOnly = true)
    public ResumoEstoqueDTO getResumoEstoque(Long produtoId) {
        List<Produto> produtos = (produtoId == null)
                ? produtoRepository.findAllByDataExclusaoIsNull()
                : produtoRepository.findById(produtoId)
                        .filter(p -> p.getDataExclusao() == null)
                        .map(List::of)
                        .orElse(List.of());
        List<Long> ids = produtos.stream().map(Produto::getId).toList();
        if (ids.isEmpty()) {
            return new ResumoEstoqueDTO(0L, 0L, BigDecimal.ZERO);
        }
        Long entrada = movimentoEstoqueRepository.sumQuantidadeByTipoAndProdutoIdIn(TipoMovimento.ENTRADA, ids);
        Long saida = movimentoEstoqueRepository.sumQuantidadeByTipoAndProdutoIdIn(TipoMovimento.SAIDA, ids);
        long totalEntrada = entrada != null ? entrada : 0L;
        long totalSaida = saida != null ? saida : 0L;
        Map<Long, BigDecimal> valorFornecedorPorId = produtos.stream()
                .collect(Collectors.toMap(Produto::getId, p -> p.getValorFornecedor() != null ? p.getValorFornecedor() : BigDecimal.ZERO));
        Map<Long, BigDecimal> receitaPorId = new HashMap<>();
        Map<Long, Integer> qtdSaidaPorId = new HashMap<>();
        for (MovimentoEstoque m : movimentoEstoqueRepository.findByProdutoIdInAndTipo(ids, TipoMovimento.SAIDA)) {
            Long id = m.getProduto().getId();
            receitaPorId.merge(id, m.getValorVenda().multiply(BigDecimal.valueOf(m.getQuantidadeMovimentada())), BigDecimal::add);
            qtdSaidaPorId.merge(id, m.getQuantidadeMovimentada(), (a, b) -> a + b);
        }
        BigDecimal lucroTotal = ids.stream()
                .map(id -> {
                    BigDecimal receita = receitaPorId.getOrDefault(id, BigDecimal.ZERO);
                    int qtd = qtdSaidaPorId.getOrDefault(id, 0);
                    BigDecimal custo = valorFornecedorPorId.getOrDefault(id, BigDecimal.ZERO).multiply(BigDecimal.valueOf(qtd));
                    return receita.subtract(custo);
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return new ResumoEstoqueDTO(totalEntrada, totalSaida, lucroTotal.setScale(2, RoundingMode.HALF_UP));
    }

    @Override
    @Transactional(readOnly = true)
    public ResumoVendasProdutoDTO getResumoVendasPorProduto(Long produtoId) {
        Produto produto = produtoRepository.findById(produtoId)
                .orElseThrow(() -> new RuntimeException("Produto não encontrado com id: " + produtoId));
        BigDecimal custoAquisicao = produto.getValorFornecedor() != null ? produto.getValorFornecedor() : BigDecimal.ZERO;

        List<MovimentoEstoque> saidas = movimentoEstoqueRepository.findByProdutoIdAndTipo(produtoId, TipoMovimento.SAIDA);

        int quantidadeTotalSaidas = saidas.stream()
                .mapToInt(MovimentoEstoque::getQuantidadeMovimentada)
                .sum();

        BigDecimal valorTotalVenda = saidas.stream()
                .map(m -> m.getValorVenda().multiply(BigDecimal.valueOf(m.getQuantidadeMovimentada())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal lucro = saidas.stream()
                .map(m -> m.getValorVenda().subtract(custoAquisicao).multiply(BigDecimal.valueOf(m.getQuantidadeMovimentada())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new ResumoVendasProdutoDTO(
                quantidadeTotalSaidas,
                valorTotalVenda.setScale(2, RoundingMode.HALF_UP),
                lucro.setScale(2, RoundingMode.HALF_UP)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public ResumoVendasProdutoDTO getResumoVendas(Long produtoId) {
        if (produtoId != null) {
            return getResumoVendasPorProduto(produtoId);
        }
        
        List<Produto> produtos = produtoRepository.findAll();
        List<Long> ids = produtos.stream().map(Produto::getId).toList();
        if (ids.isEmpty()) {
            return new ResumoVendasProdutoDTO(0, BigDecimal.ZERO, BigDecimal.ZERO);
        }
        Map<Long, BigDecimal> valorFornecedorPorId = produtos.stream()
                .collect(Collectors.toMap(Produto::getId, p -> p.getValorFornecedor() != null ? p.getValorFornecedor() : BigDecimal.ZERO));

        List<MovimentoEstoque> saidas = movimentoEstoqueRepository.findByProdutoIdInAndTipo(ids, TipoMovimento.SAIDA);
        int quantidadeTotalSaidas = saidas.stream().mapToInt(MovimentoEstoque::getQuantidadeMovimentada).sum();
        BigDecimal valorTotalVenda = saidas.stream()
                .map(m -> m.getValorVenda().multiply(BigDecimal.valueOf(m.getQuantidadeMovimentada())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal lucro = saidas.stream()
                .map(m -> {
                    BigDecimal custo = valorFornecedorPorId.getOrDefault(m.getProduto().getId(), BigDecimal.ZERO);
                    return m.getValorVenda().subtract(custo).multiply(BigDecimal.valueOf(m.getQuantidadeMovimentada()));
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new ResumoVendasProdutoDTO(
                quantidadeTotalSaidas,
                valorTotalVenda.setScale(2, RoundingMode.HALF_UP),
                lucro.setScale(2, RoundingMode.HALF_UP)
        );
    }
}
