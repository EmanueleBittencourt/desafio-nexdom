package com.nexdom.estoque.dto;

import java.math.BigDecimal;

/**
 * DTO com lucro (valor de venda - valor fornecedor) e quantidade total de saída do produto.
 */
public record LucroProdutoResponse(
        BigDecimal lucro,
        Integer quantidadeTotalSaida
) {}
