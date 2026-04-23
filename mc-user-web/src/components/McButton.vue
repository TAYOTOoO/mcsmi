<template>
  <button
    class="mc-button"
    :class="{'mc-button--small': size === 'small'}"
    :disabled="disabled || loading"
    @click="handleClick"
  >
    <span class="button-text">
      <slot>{{ loading ? '...' : '' }}</slot>
    </span>
  </button>
</template>

<script setup>
import { playSound } from '@/composables/useMinecraftSounds'

const props = defineProps({
  size: {
    type: String,
    default: 'default',
    validator: v => ['default', 'small'].includes(v)
  },
  disabled: Boolean,
  loading: Boolean
})

const emit = defineEmits(['click'])

const handleClick = (e) => {
  if (!props.disabled && !props.loading) {
    playSound()
    emit('click', e)
  }
}
</script>

<style scoped lang="scss">
@import '@/styles/minecraft.scss';

.mc-button {
  @include mc-button;
  font-size: 10px;
  padding: 6px 12px;
  transition: none;

  &.mc-button--small {
    min-width: 100px;
    min-height: 20px;
    background-size: 100px 20px;
    font-size: 8px;
    padding: 4px 8px;
  }

  .button-text {
    position: relative;
    z-index: 1;
  }
}
</style>
