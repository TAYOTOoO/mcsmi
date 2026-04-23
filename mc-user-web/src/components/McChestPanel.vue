<template>
  <div class="mc-chest-panel">
    <div v-if="title" class="chest-title">
      {{ title }}
    </div>
    <div class="chest-content">
      <slot />
    </div>
  </div>
</template>

<script setup>
defineProps({
  title: {
    type: String,
    default: ''
  }
})
</script>

<style scoped lang="scss">
@import '@/styles/minecraft.scss';

.mc-chest-panel {
  @include chest-panel;
  position: relative;
  border-radius: 0;
  min-width: 400px;
  max-width: 800px;

  .chest-title {
    @include mc-font;
    font-size: 12px;
    color: white;
    @include pixel-text-stroke;
    text-align: center;
    margin-bottom: 20px;
    padding: 12px;
    background: mc-color(ui-medium);
    border: 2px solid mc-color(stone-dark);
    box-shadow: inset 0 2px 0 rgba(0, 0, 0, 0.3);
  }

  .chest-content {
    position: relative;
    z-index: 1;
  }

  // 箱子开启动画
  @keyframes chest-open {
    from {
      opacity: 0;
      transform: translateY(-20px);
    }
    to {
      opacity: 1;
      transform: translateY(0);
    }
  }

  animation: chest-open 0.3s ease-out;
}
</style>
