package com.nexdom.estoque.dto;

import java.math.BigDecimal;

public record ResumoVendasProdutoDTO(
        int quantidadeTotalSaidas,
        BigDecimal valorTotalVenda,
        BigDecimal lucro
) {}
