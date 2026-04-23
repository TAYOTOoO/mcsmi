<template>
  <nav class="mc-hotbar">
    <div class="hotbar-container">
      <div
        v-for="(item, index) in items"
        :key="index"
        class="hotbar-slot"
        :class="{ selected: currentPath === item.path }"
        @click="handleSlotClick(item)"
        @mouseenter="handleHover"
      >
        <div class="slot-content">
          <span class="slot-icon">{{ item.icon }}</span>
          <span class="slot-label">{{ item.label }}</span>
        </div>
        <div v-if="currentPath === item.path" class="selection-indicator"></div>
      </div>
    </div>
    <div class="hotbar-separator"></div>
    <div class="user-section">
      <div v-if="username" class="user-info-slot">
        <span class="username-label">{{ username }}</span>
      </div>
    </div>
  </nav>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { playSound } from '@/composables/useMinecraftSounds'

const props = defineProps({
  items: {
    type: Array,
    required: true
  },
  username: {
    type: String,
    default: ''
  }
})

const router = useRouter()
const route = useRoute()

const currentPath = computed(() => route.path)

const handleSlotClick = (item) => {
  if (item.path !== currentPath.value) {
    playSound('click')
    if (item.action) {
      item.action()
    } else if (item.path) {
      router.push(item.path)
    }
  }
}

const handleHover = () => {
  playSound('pop')
}
</script>

<style scoped lang="scss">
@import '@/styles/minecraft.scss';

.mc-hotbar {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  height: 80px;
  background: mc-color(ui-dark);
  border-bottom: 4px solid mc-color(stone-dark);
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.5);
  z-index: 1000;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 20px;

  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    height: 100%;
    background:
      repeating-linear-gradient(
        90deg,
        transparent,
        transparent 32px,
        rgba(0, 0, 0, 0.1) 32px,
        rgba(0, 0, 0, 0.1) 34px
      );
    pointer-events: none;
  }

  .hotbar-container {
    display: flex;
    gap: 8px;
    align-items: center;
  }

  .hotbar-slot {
    @include item-slot;
    width: 64px;
    height: 64px;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    position: relative;
    cursor: pointer;
    transition: all 0.15s;

    .slot-content {
      display: flex;
      flex-direction: column;
      align-items: center;
      gap: 4px;
    }

    .slot-icon {
      font-size: 24px;
      filter: drop-shadow(2px 2px 0 rgba(0, 0, 0, 0.5));
    }

    .slot-label {
      @include mc-font;
      font-size: 6px;
      color: white;
      @include pixel-text-stroke;
      text-align: center;
      white-space: nowrap;
      max-width: 60px;
      overflow: hidden;
      text-overflow: ellipsis;
    }

    &:hover:not(.selected) {
      transform: translateY(-4px);
      border-color: mc-color(selected-glow);
      box-shadow:
        inset 2px 2px 0 rgba(0, 0, 0, 0.5),
        inset -2px -2px 0 rgba(255, 255, 255, 0.2),
        0 0 12px rgba(255, 255, 255, 0.3);
    }

    &.selected {
      background: mc-color(hover-overlay);
      border-color: white;
      box-shadow:
        inset 2px 2px 0 rgba(0, 0, 0, 0.3),
        inset -2px -2px 0 rgba(255, 255, 255, 0.4),
        0 0 16px white,
        0 0 24px rgba(255, 255, 255, 0.5);
      transform: scale(1.05);

      .selection-indicator {
        position: absolute;
        bottom: -8px;
        left: 50%;
        transform: translateX(-50%);
        width: 0;
        height: 0;
        border-left: 6px solid transparent;
        border-right: 6px solid transparent;
        border-top: 6px solid white;
        filter: drop-shadow(0 0 4px white);
      }
    }
  }

  .hotbar-separator {
    width: 2px;
    height: 48px;
    background: mc-color(stone-dark);
    box-shadow:
      1px 0 0 rgba(255, 255, 255, 0.1),
      -1px 0 0 rgba(0, 0, 0, 0.5);
  }

  .user-section {
    display: flex;
    align-items: center;
    gap: 12px;
  }

  .user-info-slot {
    padding: 8px 16px;
    background: mc-color(ui-medium);
    border: 2px solid mc-color(stone-dark);
    border-radius: 4px;
    box-shadow:
      inset 2px 2px 0 rgba(0, 0, 0, 0.3),
      inset -2px -2px 0 rgba(255, 255, 255, 0.1);

    .username-label {
      @include mc-font;
      font-size: 8px;
      color: mc-color(gold);
      @include pixel-text-stroke;
    }
  }
}

@media (max-width: 768px) {
  .mc-hotbar {
    height: auto;
    padding: 10px;

    .hotbar-container {
      flex-wrap: wrap;
      justify-content: center;
    }

    .hotbar-slot {
      width: 48px;
      height: 48px;

      .slot-icon {
        font-size: 18px;
      }

      .slot-label {
        font-size: 5px;
      }
    }

    .hotbar-separator {
      display: none;
    }
  }
}
</style>
