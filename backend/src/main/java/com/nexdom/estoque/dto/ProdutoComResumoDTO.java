package com.nexdom.estoque.dto;

import com.nexdom.estoque.model.TipoProduto;
import java.math.BigDecimal;

/**
 * DTO de produto com quantidade em estoque e soma das saídas (MovimentoEstoque tipo SAIDA).
 */
public record ProdutoComResumoDTO(
        Long id,
        String codigo,
        String descricao,
        TipoProduto tipo,
        Integer quantidadeEstoque,
        BigDecimal valorFornecedor,
        Integer totalSaidas
) {}
