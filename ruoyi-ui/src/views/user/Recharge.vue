<template>
  <div class="placeholder-page minecraft-page">
    <div class="page-container">
      <h1 class="page-title">功能开发中</h1>
      <p class="notice">该页面正在迁移到Vue2</p>
      <router-link to="/" class="mc-button">返回首页</router-link>
    </div>
  </div>
</template>

<script>
export default {
  name: 'Placeholder'
}
</script>

<style scoped>
.placeholder-page {
  min-height: 100vh;
  background-image: url('~@/assets/images/minecraft/new-textures/default_cobble.png');
  background-size: 64px 64px;
  display: flex;
  justify-content: center;
  align-items: center;
}
.page-container {
  background: rgba(30, 30, 30, 0.9);
  border: 3px solid #000;
  padding: 40px;
  text-align: center;
}
.page-title {
  font-size: 32px;
  color: #FFD700;
  text-shadow: 3px 3px 0 #000;
  margin-bottom: 20px;
}
.notice {
  color: #fff;
  text-shadow: 2px 2px 0 #000;
  margin: 15px 0;
}
.mc-button {
  background-color: #7d7d7d;
  color: #fff;
  padding: 15px 30px;
  text-decoration: none;
  display: inline-block;
  margin-top: 20px;
}
</style>
