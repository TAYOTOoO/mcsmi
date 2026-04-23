// Minecraft 音效管理器（仅点击音效）
import { ref } from 'vue'

class MinecraftSounds {
  constructor() {
    this.enabled = ref(false) // 暂时禁用音效
    this.volume = ref(0.3)
    this.audioContext = null
  }

  // 初始化音频上下文
  initAudioContext() {
    if (!this.audioContext) {
      const AudioContext = window.AudioContext || window.webkitAudioContext
      if (AudioContext) {
        this.audioContext = new AudioContext()
      }
    }
    return this.audioContext
  }

  // 播放点击音效（Minecraft UI点击）
  playClick() {
    if (!this.enabled.value) return

    const ctx = this.initAudioContext()
    if (!ctx) return

    try {
      const oscillator = ctx.createOscillator()
      const gainNode = ctx.createGain()

      oscillator.connect(gainNode)
      gainNode.connect(ctx.destination)

      // Minecraft菜单点击音效：圆润、深沉的"咔哒"声
      oscillator.frequency.value = 800
      oscillator.type = 'sine'

      // 快速攻击和衰减，模拟点击声
      gainNode.gain.setValueAtTime(0, ctx.currentTime)
      gainNode.gain.linearRampToValueAtTime(this.volume.value * 0.6, ctx.currentTime + 0.01)
      gainNode.gain.exponentialRampToValueAtTime(0.01, ctx.currentTime + 0.08)

      oscillator.start(ctx.currentTime)
      oscillator.stop(ctx.currentTime + 0.08)
    } catch (error) {
      // 静默失败
    }
  }

  // 设置音量
  setVolume(volume) {
    this.volume.value = Math.max(0, Math.min(1, volume))
  }

  // 切换音效
  toggle() {
    this.enabled.value = !this.enabled.value
    return this.enabled.value
  }

  // 启用/禁用
  setEnabled(enabled) {
    this.enabled.value = enabled
  }
}

// 创建单例
export const mcSounds = new MinecraftSounds()

// 导出方法
export function useMinecraftSounds() {
  return {
    playClick: () => mcSounds.playClick(),
    setVolume: (volume) => mcSounds.setVolume(volume),
    toggle: () => mcSounds.toggle(),
    setEnabled: (enabled) => mcSounds.setEnabled(enabled),
    enabled: mcSounds.enabled,
    volume: mcSounds.volume,
  }
}

export function playSound() {
  mcSounds.playClick()
}
