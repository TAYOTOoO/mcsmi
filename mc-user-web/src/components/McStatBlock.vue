<template>
  <div class="mc-stat-block">
    <div class="stat-value">{{ value }}</div>
    <div class="stat-label">{{ label }}</div>
  </div>
</template>

<script setup>
defineProps({
  value: {
    type: [String, Number],
    required: true
  },
  label: {
    type: String,
    required: true
  }
})
</script>

<style scoped lang="scss">
@import '@/styles/minecraft.scss';

.mc-stat-block {
  @include grass-block;
  width: 120px;
  height: 120px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
  position: relative;
  transition: all 0.3s;
  cursor: default;

  &:hover {
    transform: translateY(-4px) scale(1.05);
    filter: brightness(1.2);
    box-shadow:
      0 8px 0 rgba(0, 0, 0, 0.3),
      0 12px 20px rgba(0, 0, 0, 0.4);
  }

  .stat-value {
    @include mc-font;
    font-size: 20px;
    color: white;
    @include pixel-text-stroke;
    text-shadow:
      2px 2px 0 #000,
      -2px -2px 0 #000,
      2px -2px 0 #000,
      -2px 2px 0 #000,
      0 0 10px rgba(255, 255, 255, 0.5);
    animation: stat-glow 2s ease-in-out infinite;
  }

  .stat-label {
    @include mc-font;
    font-size: 7px;
    color: white;
    @include pixel-text-stroke;
    text-align: center;
    max-width: 100px;
    line-height: 1.4;
  }
}

@keyframes stat-glow {
  0%, 100% {
    filter: brightness(1);
  }
  50% {
    filter: brightness(1.3);
  }
}
</style>
