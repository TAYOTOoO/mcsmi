<template>
  <div class="generate-page minecraft-page">
    <div class="page-container">
      <h1 class="page-title">生成材质包</h1>
      <p class="notice">⚠️ 该页面功能正在迁移中，请稍后访问</p>
      <p class="hint">完整功能将在下一版本提供</p>
      <router-link to="/" class="mc-button">返回首页</router-link>
    </div>
  </div>
</template>

<script>
export default {
  name: 'UserGenerate'
}
</script>

<style scoped>
.generate-page {
  min-height: 100vh;
  background-image: url('~@/assets/images/minecraft/new-textures/default_cobble.png');
  background-size: 64px 64px;
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 20px;
}

.page-container {
  background: rgba(30, 30, 30, 0.9);
  border: 3px solid #000;
  padding: 40px;
  text-align: center;
  max-width: 600px;
}

.page-title {
  font-size: 32px;
  color: #FFD700;
  text-shadow: 3px 3px 0 #000;
  margin-bottom: 20px;
}

.notice, .hint {
  color: #fff;
  text-shadow: 2px 2px 0 #000;
  margin: 15px 0;
  font-size: 16px;
}

.mc-button {
  background-color: #7d7d7d;
  color: #fff;
  text-shadow: 2px 2px 0 #3a3a3a;
  border: 2px solid;
  border-color: #3a3a3a #fff #fff #3a3a3a;
  padding: 15px 30px;
  font-size: 16px;
  text-decoration: none;
  display: inline-block;
  margin-top: 20px;
}

.mc-button:hover {
  background-color: #9d9d9d;
}
</style>
