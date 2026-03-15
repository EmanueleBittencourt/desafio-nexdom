package com.nexdom.estoque.dto;

import java.math.BigDecimal;

/**
 * DTO com totais de quantidade de entrada, saída e lucro (opcionalmente filtrados por produto).
 */
public record ResumoEstoqueDTO(
        long quantidadeEntrada,
        long quantidadeSaida,
        BigDecimal lucro
) {}
