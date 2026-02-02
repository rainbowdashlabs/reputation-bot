<script lang="ts" setup>
// Common container for settings subviews
import Header2 from "@/components/heading/Header2.vue";

defineProps(["title"])
</script>

<template>
  <div>
    <Header2>{{ title }}</Header2>
    <div class="bg-white dark:bg-gray-800 shadow rounded-lg p-6 transition-colors">
      <slot/>
    </div>
  </div>
</template>
