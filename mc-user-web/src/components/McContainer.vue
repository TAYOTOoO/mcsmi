<template>
  <div class="mc-container" :class="containerClass">
    <div v-if="title" class="container-title">
      <span class="title-text">{{ title }}</span>
    </div>
    <div class="container-content">
      <slot />
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  title: String,
  size: {
    type: String,
    default: 'medium',
    validator: v => ['small', 'medium', 'large'].includes(v)
  }
})

const containerClass = computed(() => `container--${props.size}`)
</script>

<style scoped lang="scss">
@import '@/styles/minecraft.scss';

.mc-container {
  @include container-background;
  padding: 8px;

  &.container--small {
    max-width: 400px;
  }

  &.container--medium {
    max-width: 600px;
  }

  &.container--large {
    max-width: 800px;
  }

  .container-title {
    @include mc-font;
    @include mc-text-shadow;
    color: #404040;
    font-size: 10px;
    margin-bottom: 8px;
    text-align: center;

    .title-text {
      display: inline-block;
    }
  }

  .container-content {
    @include mc-scrollbar;
    max-height: 70vh;
    overflow-y: auto;
  }
}
</style>
