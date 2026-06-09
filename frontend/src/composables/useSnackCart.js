const KEY = 'cinema-pending-snacks'

export function getSnackCart() {
  try {
    return JSON.parse(sessionStorage.getItem(KEY) || '{}')
  } catch {
    return {}
  }
}

export function setSnackCart(cart) {
  sessionStorage.setItem(KEY, JSON.stringify(cart))
  window.dispatchEvent(new Event('snack-cart-changed'))
}

export function addToSnackCart(snackId, delta = 1) {
  const cart = getSnackCart()
  const next = Math.min(10, Math.max(0, (cart[snackId] || 0) + delta))
  if (next === 0) delete cart[snackId]
  else cart[snackId] = next
  setSnackCart(cart)
  return next
}

export function clearSnackCart() {
  sessionStorage.removeItem(KEY)
  window.dispatchEvent(new Event('snack-cart-changed'))
}

export function snackCartCount() {
  return Object.values(getSnackCart()).reduce((sum, n) => sum + n, 0)
}
