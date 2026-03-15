<script setup lang="ts">
import { ref, onMounted } from 'vue';
import type { Movimentacao } from '@/types';
import api from '@/services/api';

const movimentacoes = ref<Movimentacao[]>([])
const loading = ref(false)
const error = ref('')

/** Normaliza um item da API para o formato usado na tela. */
function normalizarMovimentacao(raw: unknown): Movimentacao {
  const r = raw as Record<string, unknown>
  const p = r?.produto as { id?: number; codigo?: string; descricao?: string } | null | undefined
  const produto: Movimentacao['produto'] = p
    ? { id: Number(p.id) || 0, codigo: String(p.codigo ?? ''), descricao: String(p.descricao ?? '') }
    : null
  let dataVenda = r?.dataVenda
  if (Array.isArray(dataVenda)) {
    const arr = dataVenda as number[]
    const y = arr[0] ?? 0
    const mo = (arr[1] ?? 1) - 1
    const d = arr[2] ?? 1
    const h = arr[3] ?? 0
    const min = arr[4] ?? 0
    dataVenda = new Date(y, mo, d, h, min).toISOString()
  }
  return {
    id: Number(r?.id) || 0,
    produto,
    tipo: String(r?.tipo ?? ''),
    valorVenda: Number(r?.valorVenda) || 0,
    quantidadeMovimentada: Number(r?.quantidadeMovimentada) ?? 0,
    dataVenda: typeof dataVenda === 'string' ? dataVenda : '',
  }
}

function asMovimentacoes(data: unknown): Movimentacao[] {
  let list: unknown[] = []
  if (Array.isArray(data)) list = data
  else if (data && typeof data === 'object' && 'content' in data && Array.isArray((data as { content: unknown }).content)) {
    list = (data as { content: unknown[] }).content
  }
  return list.map(normalizarMovimentacao)
}

async function carregarMovimentacoes() {
  loading.value = true
  error.value = ''
  try {
    const { data } = await api.get<Movimentacao[]>('/movimentos')
    movimentacoes.value = asMovimentacoes(data)
  } catch (err: unknown) {
    const res = err && typeof err === 'object' && 'response' in err
      ? (err as { response?: { status?: number; data?: { message?: string } } }).response
      : null
    const msg = res?.data?.message ?? (res?.status === 404 ? 'Endpoint de movimentações não encontrado.' : 'Erro ao carregar movimentações. Verifique se o backend está rodando.')
    error.value = msg
  } finally {
    loading.value = false
  }
}

function formatarData(iso: string | undefined): string {
  if (!iso) return '—'
  try {
    const d = new Date(iso)
    return d.toLocaleString('pt-BR', { dateStyle: 'short', timeStyle: 'short' })
  } catch {
    return iso
  }
}

function formatarMoeda(valor: number | undefined): string {
  if (valor == null) return '—'
  return Number(valor).toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' })
}

onMounted(async () => {
    await carregarMovimentacoes()
})
</script>

<template>
  <div class="movimentacao-view">
    <header class="page-header">
      <h1>Movimentação de Estoque</h1>
    </header>

    <section class="card table-card">
      <p v-if="error" class="error-msg" role="alert">{{ error }}</p>
      <div v-if="loading && movimentacoes.length === 0" class="loading">
        Carregando…
      </div>
      <div v-else-if="movimentacoes.length === 0" class="empty">
        Nenhuma movimentação cadastrada.
      </div>
      <div v-else class="table-wrap">
        <table class="table">
          <thead>
            <tr>
              <th>Data Movimentação</th>
              <th>Produto</th>
              <th>Tipo</th>
              <th>Valor Venda</th>
              <th>Quantidade Movimentada</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="m in movimentacoes" :key="m.id">
              <td data-label="Data Movimentação">{{ formatarData(m.dataVenda) }}</td>
              <td data-label="Produto">{{ m.produto?.descricao ?? m.produto?.codigo ?? '—' }}</td>
              <td data-label="Tipo">{{ m.tipo }}</td>
              <td data-label="Valor Venda">{{ formatarMoeda(m.valorVenda) }}</td>
              <td data-label="Quantidade Movimentada">{{ m.quantidadeMovimentada ?? '—' }}</td>
            </tr>
          </tbody>
        </table>
      </div>
    </section>
  </div>
</template>

<style scoped>

</style>
