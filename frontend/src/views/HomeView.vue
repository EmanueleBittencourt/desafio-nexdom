<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import type { ProdutoComResumoDTO, ResumoVendasProdutoDTO, TipoProdutoOption } from '@/types'
import api from '@/services/api'

const produtos = ref<ProdutoComResumoDTO[]>([])
const tiposProduto = ref<TipoProdutoOption[]>([])
const filtroTipo = ref<string>('')
const loading = ref(false)
const error = ref('')

const resumo = ref<ResumoVendasProdutoDTO | null>(null)
const filtroProdutoId = ref<string>('')
const produtosParaFiltro = ref<ProdutoComResumoDTO[]>([])
const loadingResumo = ref(false)
const errorResumo = ref('')

const resumoExibicao = computed(() => {
  const r = resumo.value
  return {
    quantidadeTotalSaidas: r ? Number(r.quantidadeTotalSaidas) || 0 : 0,
    valorTotalVenda: r ? Number(r.valorTotalVenda) || 0 : 0,
    lucro: r ? Number(r.lucro) || 0 : 0,
  }
})

function formatarLucro(valor: number | string | null | undefined): string {
  if (valor == null || valor === '') {
    return new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' }).format(0)
  }
  const n = typeof valor === 'number' ? valor : Number(valor)
  if (Number.isNaN(n)) {
    return new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' }).format(0)
  }
  return new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' }).format(n)
}

function normalizarResumoVendas(data: unknown): ResumoVendasProdutoDTO | null {
  if (!data || typeof data !== 'object' || Array.isArray(data)) return null
  const d = data as Record<string, unknown>
  if (!('quantidadeTotalSaidas' in d) || !('valorTotalVenda' in d) || !('lucro' in d)) return null
  return {
    quantidadeTotalSaidas: Number(d.quantidadeTotalSaidas) || 0,
    valorTotalVenda: Number(d.valorTotalVenda) || 0,
    lucro: Number(d.lucro) || 0,
  }
}

async function carregarTipos() {
  try {
    const { data } = await api.get<TipoProdutoOption[]>('/produtos/tipos')
    tiposProduto.value = data ?? []
  } catch {
    tiposProduto.value = []
  }
}

async function carregarProdutosParaFiltro() {
  try {
    const { data } = await api.get<ProdutoComResumoDTO[]>('/produtos?paraFiltro=true')
    produtosParaFiltro.value = data ?? []
  } catch {
    produtosParaFiltro.value = []
  }
}

async function carregarResumo() {
  loadingResumo.value = true
  errorResumo.value = ''
  try {
    const id = filtroProdutoId.value.trim()
    const params: Record<string, string> = { resumoVendas: 'true' }
    if (id) params.produtoId = id
    const response = await api.get<unknown>('/produtos', { params })
    const data = response?.data ?? response
    resumo.value = normalizarResumoVendas(data) ?? null
  } catch {
    errorResumo.value = 'Erro ao carregar resumo.'
    resumo.value = null
  } finally {
    loadingResumo.value = false
  }
}

async function carregarProdutos() {
  loading.value = true
  error.value = ''
  try {
    const url = filtroTipo.value
      ? `/produtos?tipo=${encodeURIComponent(filtroTipo.value)}`
      : '/produtos'
    const { data } = await api.get<ProdutoComResumoDTO[]>(url)
    produtos.value = data ?? []
  } catch (e) {
    error.value = 'Erro ao carregar produtos.'
    produtos.value = []
  } finally {
    loading.value = false
  }
}

onMounted(async () => {
  await carregarTipos()
  await carregarProdutosParaFiltro()
  await Promise.all([carregarResumo(), carregarProdutos()])
})

watch(filtroTipo, () => {
  carregarProdutos()
})

watch(
  () => filtroProdutoId.value,
  () => {
    carregarResumo()
  }
)
</script>

<template>
  <div class="home-view">
    <header class="page-header">
      <h1>Bem-vindo(a) ao controle de estoque</h1>
    </header>

    <div class="home-layout">
      <section class="card resumo-section">
        <div class="filter-row">
          <label for="filtro-produto" class="filter-label">Produtos</label>
          <select
            id="filtro-produto"
            v-model="filtroProdutoId"
            class="filter-select"
            @change="carregarResumo"
          >
            <option value="">Todos os produtos</option>
            <option
              v-for="p in produtosParaFiltro"
              :key="p.id"
              :value="String(p.id)"
            >
              {{ p.descricao }}
            </option>
          </select>
        </div>
        <div v-if="loadingResumo" class="loading">
          Carregando…
        </div>
        <div v-else-if="errorResumo" class="view-error">
          {{ errorResumo }}
        </div>
        <div v-else class="resumo-cards">
          <div class="resumo-card card-saida">
            <span class="resumo-card-label">Quantidade total de saídas</span>
            <span class="resumo-card-value">{{ resumoExibicao.quantidadeTotalSaidas }}</span>
          </div>
          <div class="resumo-card card-venda">
            <span class="resumo-card-label">Valor total de venda</span>
            <span class="resumo-card-value">{{ formatarLucro(resumoExibicao.valorTotalVenda) }}</span>
          </div>
          <div class="resumo-card card-lucro">
            <span class="resumo-card-label">Lucro</span>
            <span class="resumo-card-value">{{ formatarLucro(resumoExibicao.lucro) }}</span>
          </div>
        </div>
      </section>

      <section class="card table-card">
        <div class="filter-row">
          <label for="filtro-tipo" class="filter-label">Tipos de Produtos</label>
          <select id="filtro-tipo" v-model="filtroTipo" class="filter-select">
            <option value="">Todos os tipos</option>
            <option
              v-for="t in tiposProduto"
              :key="t.value"
              :value="t.value"
            >
              {{ t.label }}
            </option>
          </select>
        </div>

        <div v-if="loading && produtos.length === 0" class="loading">
          Carregando…
        </div>
        <div v-else-if="error" class="view-error">
          {{ error }}
        </div>
        <div v-else-if="produtos.length === 0" class="empty">
          Nenhum produto encontrado.
        </div>
        <div v-else class="table-wrap">
          <table class="table">
            <thead>
              <tr>
                <th>Descrição</th>
                <th>Quantidade em estoque</th>
                <th>Quantidade de saída</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="p in produtos" :key="p.id">
                <td data-label="Descrição">{{ p.descricao }}</td>
                <td data-label="Quantidade em estoque">{{ p.quantidadeEstoque }}</td>
                <td data-label="Quantidade de saída">{{ p.totalSaidas }}</td>
              </tr>
            </tbody>
          </table>
        </div>
      </section>
    </div>
  </div>
</template>

<style scoped>
.home-view {
  --card-bg: #e5e1e6;
  --card-border: #d4d0d5;
  --text: #1f2937;
  --primary: var(--palette-verde-escuro);
  color: var(--text);
}

.home-layout {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 24px;
  align-items: start;
}

@media (max-width: 900px) {
  .home-layout {
    grid-template-columns: 1fr;
  }
}

.resumo-section {
  max-width: 100%;
}

.resumo-title {
  margin: 0 0 16px 0;
  font-size: 18px;
}

.filter-row {
  display: flex;
  flex-direction: column;
  gap: 6px;
  margin-bottom: 16px;
}

.resumo-section .loading,
.resumo-section .view-error,
.resumo-section .resumo-cards {
  min-height: 360px;
}

.resumo-cards {
  display: flex;
  flex-direction: column;
  gap: 12px;
  max-height: 360px;
  overflow-y: auto;
}

.resumo-card {
  padding: 16px;
  border-radius: 10px;
  border: 1px solid var(--card-border);
  background: var(--card-bg);
}

.resumo-card-label {
  display: block;
  font-size: 12px;
  font-weight: 500;
  color: var(--text);
  opacity: 0.9;
  margin-bottom: 4px;
}

.resumo-card-value {
  font-size: 20px;
  font-weight: 700;
  color: var(--text);
}

.card-saida { border-left: 4px solid var(--palette-laranja); background: var(--palette-laranja-light); }
.card-venda { border-left: 4px solid var(--palette-verde-claro); background: var(--palette-verde-claro-light); }
.card-lucro { border-left: 4px solid var(--palette-verde-escuro); background: var(--palette-verde-escuro-light); }

.table-card {
  max-width: 100%;
}

.table-card .table-wrap {
  max-height: 360px;
  overflow-y: auto;
}
</style>
