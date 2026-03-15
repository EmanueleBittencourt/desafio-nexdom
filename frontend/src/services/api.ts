import axios, { type AxiosError } from 'axios'

const api = axios.create({
  baseURL: 'http://localhost:8080/api',
  headers: {
    'Content-Type': 'application/json',
  },
})

api.interceptors.response.use(
  (response) => response,
  (error: AxiosError) => {
    if (error.code === 'ERR_NETWORK') {
      console.error('Erro de conexão: não foi possível alcançar o servidor.')
      // Aqui você pode exibir um toast, modal ou redirecionar
    } else if (error.response) {
      const status = error.response.status
      const message =
        (error.response.data as { message?: string })?.message ?? error.message
      console.error(`Erro ${status}:`, message)
    } else {
      console.error('Erro na requisição:', error.message)
    }
    throw error
  }
)

export default api
