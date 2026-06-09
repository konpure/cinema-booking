import { ref, watch } from 'vue'

const STORAGE_KEY = 'selectedCinemaId'

const selectedCinemaId = ref(localStorage.getItem(STORAGE_KEY) || '')
const cinemas = ref([])

export function useCinema() {
  function setCinema(id) {
    selectedCinemaId.value = String(id)
    localStorage.setItem(STORAGE_KEY, String(id))
    window.dispatchEvent(new Event('cinema-changed'))
  }

  function syncFromStorage() {
    selectedCinemaId.value = localStorage.getItem(STORAGE_KEY) || ''
  }

  return { selectedCinemaId, cinemas, setCinema, syncFromStorage }
}

export function getCinemaIdParam() {
  const id = localStorage.getItem(STORAGE_KEY)
  return id ? Number(id) : undefined
}
