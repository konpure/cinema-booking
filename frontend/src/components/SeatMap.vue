<template>
  <div class="seat-map">
    <div class="screen-bar">银 幕</div>
    <div class="legend">
      <span><i class="dot available"></i>可选</span>
      <span><i class="dot selected"></i>已选</span>
      <span><i class="dot sold"></i>已售</span>
      <span><i class="dot locked"></i>他人锁定</span>
    </div>
    <div class="rows">
      <div v-for="r in rows" :key="r" class="row">
        <span class="row-label">{{ r }}排</span>
        <button
          v-for="c in cols"
          :key="`${r}-${c}`"
          class="seat"
          :class="seatClass(r, c)"
          :disabled="isDisabled(r, c)"
          @click="$emit('toggle', r, c)"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
const props = defineProps({
  rows: Number,
  cols: Number,
  sold: Array,
  locked: Array,
  myLocked: Array,
  selected: Array
})
defineEmits(['toggle'])

function key(r, c) { return `${r}-${c}` }
function isSold(r, c) { return props.sold?.includes(key(r, c)) }
function isLockedByOthers(r, c) { return props.locked?.includes(key(r, c)) }
function isMyLocked(r, c) { return props.myLocked?.includes(key(r, c)) }
function isSelected(r, c) {
  return props.selected?.some(s => s.row === r && s.col === c) || isMyLocked(r, c)
}
function isDisabled(r, c) { return isSold(r, c) || isLockedByOthers(r, c) }
function seatClass(r, c) {
  if (isSold(r, c)) return 'sold'
  if (isLockedByOthers(r, c)) return 'locked'
  if (isSelected(r, c)) return 'selected'
  return 'available'
}
</script>

<style scoped>
.seat-map { padding: 16px 0; }
.legend {
  display: flex;
  justify-content: center;
  gap: 20px;
  margin-bottom: 20px;
  font-size: 13px;
  color: var(--text-muted);
}
.legend span { display: flex; align-items: center; gap: 6px; }
.dot {
  width: 14px; height: 14px;
  border-radius: 4px 4px 2px 2px;
  display: inline-block;
}
.dot.available { background: #3a3a4a; }
.dot.selected { background: var(--gold); }
.dot.sold { background: #555; }
.dot.locked { background: #8b4513; }

.rows { display: flex; flex-direction: column; gap: 8px; align-items: center; }
.row { display: flex; align-items: center; gap: 6px; }
.row-label { width: 36px; font-size: 12px; color: var(--text-muted); text-align: right; }
.seat {
  width: 28px; height: 24px;
  border: none;
  border-radius: 6px 6px 3px 3px;
  cursor: pointer;
  transition: transform 0.15s, background 0.15s;
}
.seat.available { background: #3a3a4a; }
.seat.available:hover { background: #5a5a6a; transform: scale(1.1); }
.seat.selected { background: var(--gold); box-shadow: 0 0 12px rgba(212,168,83,0.5); }
.seat.sold { background: #444; cursor: not-allowed; opacity: 0.5; }
.seat.locked { background: #6b3a1a; cursor: not-allowed; opacity: 0.7; }
.seat:disabled { cursor: not-allowed; }
</style>
