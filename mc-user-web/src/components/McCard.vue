<template>
  <div
    class="mc-card"
    :class="{ hoverable, enchanted }"
    @click="handleClick"
    @mouseenter="handleHover"
  >
    <div class="card-frame">
      <div class="card-content">
        <div v-if="icon" class="card-icon">
          {{ icon }}
        </div>
        <div v-if="title" class="card-title">
          {{ title }}
        </div>
        <div class="card-body">
          <slot />
        </div>
      </div>
    </div>
    <div v-if="enchanted" class="enchant-particles"></div>
  </div>
</template>

<script setup>
import { playSound } from '@/composables/useMinecraftSounds'

const props = defineProps({
  icon: {
    type: String,
    default: ''
  },
  title: {
    type: String,
    default: ''
  },
  hoverable: {
    type: Boolean,
    default: true
  },
  enchanted: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['click'])

const handleClick = (event) => {
  playSound('click')
  emit('click', event)
}

const handleHover = () => {
  if (props.hoverable) {
    playSound('pop')
  }
}
</script>

<style scoped lang="scss">
@import '@/styles/minecraft.scss';

.mc-card {
  @include item-frame;
  position: relative;
  cursor: pointer;
  transition: all 0.2s;
  background: mc-color(oak);

  .card-frame {
    background: mc-color(ui-medium);
    border: 2px solid mc-color(stone-dark);
    padding: 20px;
    min-height: 150px;
    display: flex;
    flex-direction: column;
    box-shadow: inset 2px 2px 0 rgba(0, 0, 0, 0.3);
  }

  .card-content {
    flex: 1;
    display: flex;
    flex-direction: column;
    gap: 12px;
  }

  .card-icon {
    font-size: 48px;
    text-align: center;
    filter: drop-shadow(3px 3px 0 rgba(0, 0, 0, 0.5));
  }

  .card-title {
    @include mc-font;
    font-size: 10px;
    color: white;
    @include pixel-text-stroke;
    text-align: center;
    text-transform: uppercase;
  }

  .card-body {
    @include mc-font;
    font-size: 7px;
    color: #aaa;
    line-height: 1.6;
    text-shadow:
      1px 1px 0 #000,
      -1px -1px 0 #000,
      1px -1px 0 #000,
      -1px 1px 0 #000;
  }

  &.hoverable:hover {
    transform: translateY(-8px) scale(1.02);
    box-shadow:
      inset 0 0 0 2px mc-color(oak-dark),
      0 8px 0 rgba(0, 0, 0, 0.3),
      0 12px 20px rgba(0, 0, 0, 0.4);

    .card-frame {
      border-color: mc-color(selected-glow);
      box-shadow:
        inset 2px 2px 0 rgba(0, 0, 0, 0.3),
        0 0 12px rgba(255, 255, 255, 0.2);
    }
  }

  &.enchanted {
    animation: enchant-glint 3s ease-in-out infinite;

    &::after {
      content: '✨';
      position: absolute;
      top: -10px;
      right: -10px;
      font-size: 20px;
      animation: float 2s ease-in-out infinite;
    }

    .card-frame {
      border-color: mc-color(enchant-purple);
      box-shadow:
        inset 2px 2px 0 rgba(0, 0, 0, 0.3),
        0 0 15px mc-color(enchant-purple);
    }
  }
}

@keyframes float {
  0%, 100% {
    transform: translateY(0) rotate(0deg);
  }
  50% {
    transform: translateY(-10px) rotate(180deg);
  }
}
</style>
