<template>
  <footer class="footer-with-fade w-full bg-white dark:bg-gray-800 border-t border-gray-200 dark:border-gray-700 py-6 mt-8">
    <div class="container mx-auto px-4">
      <div class="flex flex-col items-center gap-2 text-sm text-gray-600 dark:text-gray-400">
        <div class="flex items-center gap-4 flex-wrap justify-center">
          <a 
            href="https://github.com/RainbowDashLabs/reputation-bot" 
            target="_blank" 
            rel="noopener noreferrer"
            class="hover:text-indigo-600 dark:hover:text-indigo-400 transition-colors"
          >
            GitHub
          </a>
          <span>•</span>
          <span>© 2026 RainbowDashLabs and Contributor</span>
          <span>•</span>
          <span>Licensed under AGPL-3.0</span>
        </div>
        <div>
          Made with ❤️ and 🏳️‍🌈
        </div>
      </div>
    </div>
  </footer>
</template>

<style scoped>
.footer-with-fade {
  position: relative;
}

.footer-with-fade::after {
  content: '';
  position: absolute;
  bottom: -40px;
  left: 0;
  right: 0;
  height: 40px;
  background: linear-gradient(to bottom, rgb(255, 255, 255), transparent);
  pointer-events: none;
}

.dark .footer-with-fade::after {
  background: linear-gradient(to bottom, rgb(31, 41, 55), transparent);
}
</style>
