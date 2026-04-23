<template>
  <input
    class="mc-input"
    :type="type"
    :placeholder="placeholder"
    :disabled="disabled"
    :value="modelValue"
    @input="$emit('update:modelValue', $event.target.value)"
    @keyup.enter="$emit('enter')"
  />
</template>

<script setup>
defineProps({
  type: {
    type: String,
    default: 'text'
  },
  placeholder: String,
  disabled: Boolean,
  modelValue: [String, Number]
})

defineEmits(['update:modelValue', 'enter'])
</script>

<style scoped lang="scss">
@import '@/styles/minecraft.scss';

.mc-input {
  @include mc-input;
  width: 100%;
}
</style>
