<template>
  <router-link :to="`/movie/${movie.id}`" class="movie-card">
    <div class="poster-wrap">
      <img :src="movie.poster" :alt="movie.title" loading="lazy" />
      <div class="rating">{{ movie.rating }} 分</div>
    </div>
    <div class="info">
      <h3>{{ movie.title }}</h3>
      <p>{{ movie.genre }} · {{ movie.duration }}分钟</p>
    </div>
  </router-link>
</template>

<script setup>
defineProps({ movie: Object })
</script>

<style scoped>
.movie-card {
  display: block;
  background: var(--bg-card);
  border: 1px solid var(--border);
  border-radius: 12px;
  overflow: hidden;
  transition: transform 0.25s, box-shadow 0.25s;
}
.movie-card:hover {
  transform: translateY(-6px);
  box-shadow: 0 12px 40px rgba(212, 168, 83, 0.15);
  border-color: var(--gold);
}
.poster-wrap { position: relative; aspect-ratio: 2/3; overflow: hidden; }
.poster-wrap img { width: 100%; height: 100%; object-fit: cover; }
.rating {
  position: absolute;
  top: 10px;
  right: 10px;
  background: rgba(212, 168, 83, 0.9);
  color: #0a0a0f;
  padding: 4px 10px;
  border-radius: 20px;
  font-size: 13px;
  font-weight: 700;
}
.info { padding: 14px 16px; }
.info h3 { font-size: 1rem; margin-bottom: 6px; color: #fff; }
.info p { font-size: 13px; color: var(--text-muted); }
</style>
