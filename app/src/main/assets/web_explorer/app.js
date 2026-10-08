/**
 * Linq Studio Mobile Code-Editor Side Drawer (File Explorer Panel)
 * Vanilla HTML5, CSS3 & ES6+ JavaScript
 * Features:
 * - Dynamic SVG language icons mapped from extensions & special filenames
 * - Real-time icon updating on keystroke
 * - Collapsible tree hierarchy with indent guidelines
 * - Large 68px touch target rows
 * - Rename, Duplicate, Delete overflow menu
 * - Touch swipe gestures (swipe left to close, swipe right to open)
 * - HTML preview/run trigger
 */

(function () {
  'use strict';

  // =========================================================================
  // 1. COMPREHENSIVE 'iconMapping' REGISTRY
  //    Maps file extensions, filenames (package.json, Dockerfile), and folders
  //    to icon classes, badge colors, and inline SVG representations.
  // =========================================================================

  const iconMapping = {
    // Specific Filenames (case-insensitive lookup)
    filenames: {
      'package.json': {
        name: 'npm',
        className: 'icon-file-npm',
        color: '#CB3837',
        svg: `<svg viewBox="0 0 24 24"><rect width="24" height="24" rx="3" fill="#CB3837"/><path d="M4 6h16v12H12v-8H8v8H4V6z" fill="#FFF"/></svg>`
      },
      'package-lock.json': {
        name: 'npm-lock',
        className: 'icon-file-npm-lock',
        color: '#CB3837',
        svg: `<svg viewBox="0 0 24 24"><rect width="24" height="24" rx="3" fill="#CB3837"/><path d="M4 6h16v12H12v-8H8v8H4V6z" fill="#FFF"/><circle cx="18" cy="18" r="4" fill="#000"/><path d="M18 16v1m-1 2h2" stroke="#FFF" stroke-width="1.5"/></svg>`
      },
      'yarn.lock': {
        name: 'yarn',
        className: 'icon-file-yarn',
        color: '#2C8EBB',
        svg: `<svg viewBox="0 0 24 24"><circle cx="12" cy="12" r="10" fill="#2C8EBB"/><path d="M7 8l5 5 5-5M12 13v5" stroke="#FFF" stroke-width="2" stroke-linecap="round"/></svg>`
      },
      'pnpm-lock.yaml': {
        name: 'pnpm',
        className: 'icon-file-pnpm',
        color: '#F69220',
        svg: `<svg viewBox="0 0 24 24"><rect x="3" y="3" width="7" height="7" fill="#F69220"/><rect x="14" y="3" width="7" height="7" fill="#F69220"/><rect x="8.5" y="14" width="7" height="7" fill="#F69220"/></svg>`
      },
      'dockerfile': {
        name: 'docker',
        className: 'icon-file-docker',
        color: '#2496ED',
        svg: `<svg viewBox="0 0 24 24"><path d="M22 13c-.5-3.5-3.5-5-3.5-5-.5.5-1.5.8-2 .8-1 0-1.8-.7-2.5-1.3-.8-.8-1.7-1.5-3-1.5H3c-.6 0-1 .4-1 1v9c0 3.5 3 6 8 6 6 0 9-3 10-7h2v-2z" fill="#2496ED"/><rect x="6" y="9" width="2" height="2" fill="#FFF"/><rect x="9" y="9" width="2" height="2" fill="#FFF"/><rect x="12" y="9" width="2" height="2" fill="#FFF"/><rect x="9" y="6" width="2" height="2" fill="#FFF"/></svg>`
      },
      'docker-compose.yml': {
        name: 'docker-compose',
        className: 'icon-file-docker',
        color: '#2496ED',
        svg: `<svg viewBox="0 0 24 24"><path d="M22 13c-.5-3.5-3.5-5-3.5-5-.5.5-1.5.8-2 .8-1 0-1.8-.7-2.5-1.3-.8-.8-1.7-1.5-3-1.5H3c-.6 0-1 .4-1 1v9c0 3.5 3 6 8 6 6 0 9-3 10-7h2v-2z" fill="#2496ED"/><rect x="6" y="9" width="2" height="2" fill="#FFF"/><rect x="9" y="9" width="2" height="2" fill="#FFF"/><rect x="12" y="9" width="2" height="2" fill="#FFF"/><rect x="9" y="6" width="2" height="2" fill="#FFF"/></svg>`
      },
      'docker-compose.yaml': {
        name: 'docker-compose',
        className: 'icon-file-docker',
        color: '#2496ED',
        svg: `<svg viewBox="0 0 24 24"><path d="M22 13c-.5-3.5-3.5-5-3.5-5-.5.5-1.5.8-2 .8-1 0-1.8-.7-2.5-1.3-.8-.8-1.7-1.5-3-1.5H3c-.6 0-1 .4-1 1v9c0 3.5 3 6 8 6 6 0 9-3 10-7h2v-2z" fill="#2496ED"/><rect x="6" y="9" width="2" height="2" fill="#FFF"/><rect x="9" y="9" width="2" height="2" fill="#FFF"/><rect x="12" y="9" width="2" height="2" fill="#FFF"/><rect x="9" y="6" width="2" height="2" fill="#FFF"/></svg>`
      },
      '.dockerignore': {
        name: 'docker-ignore',
        className: 'icon-file-docker',
        color: '#2496ED',
        svg: `<svg viewBox="0 0 24 24"><path d="M22 13c-.5-3.5-3.5-5-3.5-5-.5.5-1.5.8-2 .8-1 0-1.8-.7-2.5-1.3-.8-.8-1.7-1.5-3-1.5H3c-.6 0-1 .4-1 1v9c0 3.5 3 6 8 6 6 0 9-3 10-7h2v-2z" fill="#2496ED"/></svg>`
      },
      '.gitignore': {
        name: 'git',
        className: 'icon-file-git',
        color: '#F05032',
        svg: `<svg viewBox="0 0 24 24"><circle cx="6" cy="18" r="3" fill="#F05032"/><circle cx="6" cy="6" r="3" fill="#F05032"/><circle cx="18" cy="6" r="3" fill="#F05032"/><line x1="6" y1="9" x2="6" y2="15" stroke="#F05032" stroke-width="2"/><path d="M9 18h3a6 6 0 0 0 6-6V9" stroke="#F05032" stroke-width="2" fill="none"/></svg>`
      },
      '.gitattributes': {
        name: 'git',
        className: 'icon-file-git',
        color: '#F05032',
        svg: `<svg viewBox="0 0 24 24"><circle cx="6" cy="18" r="3" fill="#F05032"/><circle cx="6" cy="6" r="3" fill="#F05032"/><circle cx="18" cy="6" r="3" fill="#F05032"/><line x1="6" y1="9" x2="6" y2="15" stroke="#F05032" stroke-width="2"/><path d="M9 18h3a6 6 0 0 0 6-6V9" stroke="#F05032" stroke-width="2" fill="none"/></svg>`
      },
      '.gitmodules': {
        name: 'git',
        className: 'icon-file-git',
        color: '#F05032',
        svg: `<svg viewBox="0 0 24 24"><circle cx="6" cy="18" r="3" fill="#F05032"/><circle cx="6" cy="6" r="3" fill="#F05032"/><circle cx="18" cy="6" r="3" fill="#F05032"/><line x1="6" y1="9" x2="6" y2="15" stroke="#F05032" stroke-width="2"/><path d="M9 18h3a6 6 0 0 0 6-6V9" stroke="#F05032" stroke-width="2" fill="none"/></svg>`
      },
      'readme.md': {
        name: 'readme',
        className: 'icon-file-readme',
        color: '#3B82F6',
        svg: `<svg viewBox="0 0 24 24" fill="none" stroke="#3B82F6" stroke-width="2"><path d="M4 19.5A2.5 2.5 0 0 1 6.5 17H20"/><path d="M6.5 2H20v20H6.5A2.5 2.5 0 0 1 4 19.5v-15A2.5 2.5 0 0 1 6.5 2z"/></svg>`
      },
      'readme.txt': {
        name: 'readme',
        className: 'icon-file-readme',
        color: '#3B82F6',
        svg: `<svg viewBox="0 0 24 24" fill="none" stroke="#3B82F6" stroke-width="2"><path d="M4 19.5A2.5 2.5 0 0 1 6.5 17H20"/><path d="M6.5 2H20v20H6.5A2.5 2.5 0 0 1 4 19.5v-15A2.5 2.5 0 0 1 6.5 2z"/></svg>`
      },
      'license': {
        name: 'license',
        className: 'icon-file-license',
        color: '#EAB308',
        svg: `<svg viewBox="0 0 24 24" fill="none" stroke="#EAB308" stroke-width="2"><circle cx="12" cy="8" r="6"/><path d="M15.4 12.6L17 22l-5-3-5 3 1.6-9.4"/></svg>`
      },
      'license.md': {
        name: 'license',
        className: 'icon-file-license',
        color: '#EAB308',
        svg: `<svg viewBox="0 0 24 24" fill="none" stroke="#EAB308" stroke-width="2"><circle cx="12" cy="8" r="6"/><path d="M15.4 12.6L17 22l-5-3-5 3 1.6-9.4"/></svg>`
      },
      '.env': {
        name: 'env',
        className: 'icon-file-env',
        color: '#ECD53F',
        svg: `<svg viewBox="0 0 24 24" fill="none" stroke="#ECD53F" stroke-width="2"><rect x="3" y="11" width="18" height="11" rx="2" ry="2"/><path d="M7 11V7a5 5 0 0 1 10 0v4"/></svg>`
      },
      '.env.example': {
        name: 'env',
        className: 'icon-file-env',
        color: '#ECD53F',
        svg: `<svg viewBox="0 0 24 24" fill="none" stroke="#ECD53F" stroke-width="2"><rect x="3" y="11" width="18" height="11" rx="2" ry="2"/><path d="M7 11V7a5 5 0 0 1 10 0v4"/></svg>`
      },
      '.env.local': {
        name: 'env',
        className: 'icon-file-env',
        color: '#ECD53F',
        svg: `<svg viewBox="0 0 24 24" fill="none" stroke="#ECD53F" stroke-width="2"><rect x="3" y="11" width="18" height="11" rx="2" ry="2"/><path d="M7 11V7a5 5 0 0 1 10 0v4"/></svg>`
      },
      '.env.development': {
        name: 'env',
        className: 'icon-file-env',
        color: '#ECD53F',
        svg: `<svg viewBox="0 0 24 24" fill="none" stroke="#ECD53F" stroke-width="2"><rect x="3" y="11" width="18" height="11" rx="2" ry="2"/><path d="M7 11V7a5 5 0 0 1 10 0v4"/></svg>`
      },
      '.env.production': {
        name: 'env',
        className: 'icon-file-env',
        color: '#ECD53F',
        svg: `<svg viewBox="0 0 24 24" fill="none" stroke="#ECD53F" stroke-width="2"><rect x="3" y="11" width="18" height="11" rx="2" ry="2"/><path d="M7 11V7a5 5 0 0 1 10 0v4"/></svg>`
      },
      'tsconfig.json': {
        name: 'tsconfig',
        className: 'icon-file-tsconfig',
        color: '#3178C6',
        svg: `<svg viewBox="0 0 24 24"><rect width="24" height="24" rx="4" fill="#3178C6"/><text x="4" y="16" font-size="10" font-weight="bold" fill="#FFF" font-family="monospace">TS</text></svg>`
      },
      'jsconfig.json': {
        name: 'jsconfig',
        className: 'icon-file-jsconfig',
        color: '#F7DF1E',
        svg: `<svg viewBox="0 0 24 24"><rect width="24" height="24" rx="4" fill="#F7DF1E"/><text x="4" y="16" font-size="10" font-weight="bold" fill="#000" font-family="monospace">JS</text></svg>`
      },
      'vite.config.js': {
        name: 'vite',
        className: 'icon-file-vite',
        color: '#646CFF',
        svg: `<svg viewBox="0 0 24 24"><polygon points="3 2 13 2 12 11 18 11 8 22 9 13 3 13" fill="#646CFF"/></svg>`
      },
      'vite.config.ts': {
        name: 'vite',
        className: 'icon-file-vite',
        color: '#646CFF',
        svg: `<svg viewBox="0 0 24 24"><polygon points="3 2 13 2 12 11 18 11 8 22 9 13 3 13" fill="#646CFF"/></svg>`
      },
      'webpack.config.js': {
        name: 'webpack',
        className: 'icon-file-webpack',
        color: '#8DD6F9',
        svg: `<svg viewBox="0 0 24 24"><polygon points="12 2 21 7 21 17 12 22 3 17 3 7" fill="none" stroke="#8DD6F9" stroke-width="2"/><polygon points="12 6 17 9 17 15 12 18 7 15 7 9" fill="#8DD6F9"/></svg>`
      },
      'tailwind.config.js': {
        name: 'tailwind',
        className: 'icon-file-tailwind',
        color: '#38BDF8',
        svg: `<svg viewBox="0 0 24 24"><path d="M12 6c-3 0-4.5 1.5-4.5 4.5 1-1.5 2.25-2 3.75-1.5 1 .3 1.8 1.1 2.5 1.8C15 12 16.5 13 19 13c3 0 4.5-1.5 4.5-4.5-1 1.5-2.25 2-3.75 1.5-1-.3-1.8-1.1-2.5-1.8C16 7 14.5 6 12 6zM5 13c-3 0-4.5 1.5-4.5 4.5 1-1.5 2.25-2 3.75-1.5 1 .3 1.8 1.1 2.5 1.8C8 19 9.5 20 12 20c3 0 4.5-1.5 4.5-4.5-1 1.5-2.25 2-3.75 1.5-1-.3-1.8-1.1-2.5-1.8C9 14 7.5 13 5 13z" fill="#38BDF8"/></svg>`
      },
      'build.gradle.kts': {
        name: 'gradle',
        className: 'icon-file-gradle',
        color: '#02303A',
        svg: `<svg viewBox="0 0 24 24"><rect width="24" height="24" rx="4" fill="#02303A"/><circle cx="12" cy="12" r="6" stroke="#00C4B4" stroke-width="2" fill="none"/><circle cx="12" cy="12" r="2" fill="#00C4B4"/></svg>`
      },
      'settings.gradle.kts': {
        name: 'gradle',
        className: 'icon-file-gradle',
        color: '#02303A',
        svg: `<svg viewBox="0 0 24 24"><rect width="24" height="24" rx="4" fill="#02303A"/><circle cx="12" cy="12" r="6" stroke="#00C4B4" stroke-width="2" fill="none"/><circle cx="12" cy="12" r="2" fill="#00C4B4"/></svg>`
      },
      'cargo.toml': {
        name: 'cargo',
        className: 'icon-file-cargo',
        color: '#DEA584',
        svg: `<svg viewBox="0 0 24 24"><rect width="24" height="24" rx="4" fill="#DEA584"/><path d="M6 10h12v8H6z" fill="#3E2723"/><path d="M8 7h8v3H8z" fill="#5D4037"/></svg>`
      },
      'go.mod': {
        name: 'go',
        className: 'icon-file-go',
        color: '#00ADD8',
        svg: `<svg viewBox="0 0 24 24"><rect width="24" height="24" rx="4" fill="#00ADD8"/><text x="4" y="16" font-size="10" font-weight="bold" fill="#FFF" font-family="sans-serif">GO</text></svg>`
      }
    },

    // File Extensions (case-insensitive lookup)
    extensions: {
      // JavaScript & Web Scripting
      js: {
        name: 'javascript',
        className: 'icon-file-js',
        color: '#F7DF1E',
        svg: `<svg viewBox="0 0 24 24"><rect width="24" height="24" rx="4" fill="#F7DF1E"/><path d="M7 16.5c0 1.5 1 2.5 2.5 2.5s2.5-1 2.5-2.5v-7h-2v7c0 .3-.2.5-.5.5s-.5-.2-.5-.5v-3H7v3zm7.5-5h2.5c1.1 0 2 .9 2 2 0 .8-.5 1.5-1.2 1.8l1.7 3.7h-2.3l-1.4-3.2h-1.3v3.2h-2v-9.5h2zm0 4.5h2c.3 0 .5-.2.5-.5s-.2-.5-.5-.5h-2v1z" fill="#000"/></svg>`
      },
      mjs: {
        name: 'javascript',
        className: 'icon-file-js',
        color: '#F7DF1E',
        svg: `<svg viewBox="0 0 24 24"><rect width="24" height="24" rx="4" fill="#F7DF1E"/><path d="M7 16.5c0 1.5 1 2.5 2.5 2.5s2.5-1 2.5-2.5v-7h-2v7c0 .3-.2.5-.5.5s-.5-.2-.5-.5v-3H7v3zm7.5-5h2.5c1.1 0 2 .9 2 2 0 .8-.5 1.5-1.2 1.8l1.7 3.7h-2.3l-1.4-3.2h-1.3v3.2h-2v-9.5h2zm0 4.5h2c.3 0 .5-.2.5-.5s-.2-.5-.5-.5h-2v1z" fill="#000"/></svg>`
      },
      cjs: {
        name: 'javascript',
        className: 'icon-file-js',
        color: '#F7DF1E',
        svg: `<svg viewBox="0 0 24 24"><rect width="24" height="24" rx="4" fill="#F7DF1E"/><path d="M7 16.5c0 1.5 1 2.5 2.5 2.5s2.5-1 2.5-2.5v-7h-2v7c0 .3-.2.5-.5.5s-.5-.2-.5-.5v-3H7v3zm7.5-5h2.5c1.1 0 2 .9 2 2 0 .8-.5 1.5-1.2 1.8l1.7 3.7h-2.3l-1.4-3.2h-1.3v3.2h-2v-9.5h2zm0 4.5h2c.3 0 .5-.2.5-.5s-.2-.5-.5-.5h-2v1z" fill="#000"/></svg>`
      },
      ts: {
        name: 'typescript',
        className: 'icon-file-ts',
        color: '#3178C6',
        svg: `<svg viewBox="0 0 24 24"><rect width="24" height="24" rx="4" fill="#3178C6"/><path d="M5 9h7v2H9.5v8h-2v-8H5V9zm9 0h5v2h-3v2h2.5c1.1 0 2 .9 2 2v2c0 1.1-.9 2-2 2H14v-2h3v-2h-2.5c-1.1 0-2-.9-2-2V11c0-1.1.9-2 2-2z" fill="#FFF"/></svg>`
      },
      mts: {
        name: 'typescript',
        className: 'icon-file-ts',
        color: '#3178C6',
        svg: `<svg viewBox="0 0 24 24"><rect width="24" height="24" rx="4" fill="#3178C6"/><path d="M5 9h7v2H9.5v8h-2v-8H5V9zm9 0h5v2h-3v2h2.5c1.1 0 2 .9 2 2v2c0 1.1-.9 2-2 2H14v-2h3v-2h-2.5c-1.1 0-2-.9-2-2V11c0-1.1.9-2 2-2z" fill="#FFF"/></svg>`
      },
      jsx: {
        name: 'react',
        className: 'icon-file-jsx',
        color: '#61DAFB',
        svg: `<svg viewBox="0 0 24 24" fill="none" stroke="#61DAFB" stroke-width="1.8"><ellipse cx="12" cy="12" rx="10" ry="4"/><ellipse cx="12" cy="12" rx="10" ry="4" transform="rotate(60 12 12)"/><ellipse cx="12" cy="12" rx="10" ry="4" transform="rotate(120 12 12)"/><circle cx="12" cy="12" r="2" fill="#61DAFB"/></svg>`
      },
      tsx: {
        name: 'react-ts',
        className: 'icon-file-tsx',
        color: '#61DAFB',
        svg: `<svg viewBox="0 0 24 24" fill="none" stroke="#61DAFB" stroke-width="1.8"><ellipse cx="12" cy="12" rx="10" ry="4"/><ellipse cx="12" cy="12" rx="10" ry="4" transform="rotate(60 12 12)"/><ellipse cx="12" cy="12" rx="10" ry="4" transform="rotate(120 12 12)"/><circle cx="12" cy="12" r="2" fill="#61DAFB"/></svg>`
      },
      vue: {
        name: 'vue',
        className: 'icon-file-vue',
        color: '#42B883',
        svg: `<svg viewBox="0 0 24 24"><polygon points="2 3 12 20 22 3 17 3 12 11.5 7 3" fill="#42B883"/><polygon points="6.5 3 12 12.5 17.5 3 13.5 3 12 5.5 10.5 3" fill="#35495E"/></svg>`
      },
      svelte: {
        name: 'svelte',
        className: 'icon-file-svelte',
        color: '#FF3E00',
        svg: `<svg viewBox="0 0 24 24"><path d="M18.8 5.4c-2.3-2-6-1.7-8 .6l-4 4.6c-2.3 2.7-1.9 6.8.9 9 2.3 1.9 5.8 1.6 7.9-.7l4-4.6c2.3-2.6 1.9-6.7-.8-8.9z" fill="#FF3E00"/><circle cx="12" cy="12" r="2.5" fill="#FFF"/></svg>`
      },

      // Web Markup & Styling
      html: {
        name: 'html',
        className: 'icon-file-html',
        color: '#E34F26',
        svg: `<svg viewBox="0 0 24 24"><path d="M4 3l1.5 17 6.5 2 6.5-2L20 3H4zm13.8 4.5l-.2 2.5h-8l.2 2.5h7.6l-.7 7.5-4.7 1.4-4.7-1.4-.3-3.5h2.4l.2 1.8 2.4.7 2.4-.7.3-3.3H7.5L6.9 7.5h10.9z" fill="#E34F26"/></svg>`
      },
      htm: {
        name: 'html',
        className: 'icon-file-html',
        color: '#E34F26',
        svg: `<svg viewBox="0 0 24 24"><path d="M4 3l1.5 17 6.5 2 6.5-2L20 3H4zm13.8 4.5l-.2 2.5h-8l.2 2.5h7.6l-.7 7.5-4.7 1.4-4.7-1.4-.3-3.5h2.4l.2 1.8 2.4.7 2.4-.7.3-3.3H7.5L6.9 7.5h10.9z" fill="#E34F26"/></svg>`
      },
      css: {
        name: 'css',
        className: 'icon-file-css',
        color: '#1572B6',
        svg: `<svg viewBox="0 0 24 24"><path d="M4 3l1.5 17 6.5 2 6.5-2L20 3H4zm13.8 4.5l-.3 3.5H9.4l.2 2h7.8l-.8 8-4.6 1.3-4.6-1.3-.3-3.2h2.4l.2 1.6 2.3.6 2.3-.6.3-3.7H7.2L6.6 7.5h11.2z" fill="#1572B6"/></svg>`
      },
      scss: {
        name: 'scss',
        className: 'icon-file-scss',
        color: '#CC6699',
        svg: `<svg viewBox="0 0 24 24"><rect width="24" height="24" rx="4" fill="#CC6699"/><path d="M12 7c-2.5 0-4.5 1.5-4.5 3.5 0 2.8 5.5 2 5.5 4.5 0 1-1 1.5-2 1.5s-2.5-.7-3-1.5l-1.5 1.5c1 1.5 2.8 2 4.5 2 2.8 0 4.5-1.5 4.5-3.5 0-3-5.5-2.2-5.5-4.5 0-.8.8-1.5 1.8-1.5 1.2 0 2.2.6 2.7 1.2l1.5-1.5C14.8 7.5 13.5 7 12 7z" fill="#FFF"/></svg>`
      },
      sass: {
        name: 'sass',
        className: 'icon-file-scss',
        color: '#CC6699',
        svg: `<svg viewBox="0 0 24 24"><rect width="24" height="24" rx="4" fill="#CC6699"/><path d="M12 7c-2.5 0-4.5 1.5-4.5 3.5 0 2.8 5.5 2 5.5 4.5 0 1-1 1.5-2 1.5s-2.5-.7-3-1.5l-1.5 1.5c1 1.5 2.8 2 4.5 2 2.8 0 4.5-1.5 4.5-3.5 0-3-5.5-2.2-5.5-4.5 0-.8.8-1.5 1.8-1.5 1.2 0 2.2.6 2.7 1.2l1.5-1.5C14.8 7.5 13.5 7 12 7z" fill="#FFF"/></svg>`
      },
      less: {
        name: 'less',
        className: 'icon-file-less',
        color: '#1D365D',
        svg: `<svg viewBox="0 0 24 24"><rect width="24" height="24" rx="4" fill="#1D365D"/><text x="4" y="16" font-size="9" font-weight="bold" fill="#FFF" font-family="sans-serif">LESS</text></svg>`
      },

      // Data, Config & Serialization
      json: {
        name: 'json',
        className: 'icon-file-json',
        color: '#CBCB41',
        svg: `<svg viewBox="0 0 24 24" fill="none" stroke="#CBCB41" stroke-width="2" stroke-linecap="round"><path d="M8 4c-2 0-3 1-3 3v2c0 1.5-1 2-2 2 1 0 2 .5 2 2v2c0 2 1 3 3 3m8-14c2 0 3 1 3 3v2c0 1.5 1 2 2 2-1 0-2 .5-2 2v2c0 2-1 3-3 3"/></svg>`
      },
      yaml: {
        name: 'yaml',
        className: 'icon-file-yaml',
        color: '#CB171E',
        svg: `<svg viewBox="0 0 24 24"><rect width="24" height="24" rx="4" fill="#CB171E"/><text x="4" y="16" font-size="9" font-weight="bold" fill="#FFF" font-family="sans-serif">YML</text></svg>`
      },
      yml: {
        name: 'yaml',
        className: 'icon-file-yaml',
        color: '#CB171E',
        svg: `<svg viewBox="0 0 24 24"><rect width="24" height="24" rx="4" fill="#CB171E"/><text x="4" y="16" font-size="9" font-weight="bold" fill="#FFF" font-family="sans-serif">YML</text></svg>`
      },
      xml: {
        name: 'xml',
        className: 'icon-file-xml',
        color: '#E66E33',
        svg: `<svg viewBox="0 0 24 24" fill="none" stroke="#E66E33" stroke-width="2" stroke-linecap="round"><polyline points="16 18 22 12 16 6"/><polyline points="8 6 2 12 8 18"/></svg>`
      },
      toml: {
        name: 'toml',
        className: 'icon-file-toml',
        color: '#9C4221',
        svg: `<svg viewBox="0 0 24 24"><rect width="24" height="24" rx="4" fill="#9C4221"/><text x="3" y="16" font-size="8" font-weight="bold" fill="#FFF" font-family="sans-serif">TOML</text></svg>`
      },
      graphql: {
        name: 'graphql',
        className: 'icon-file-graphql',
        color: '#E10098',
        svg: `<svg viewBox="0 0 24 24" fill="none" stroke="#E10098" stroke-width="2"><polygon points="12 2 21 8 21 16 12 22 3 16 3 8"/></svg>`
      },
      gql: {
        name: 'graphql',
        className: 'icon-file-graphql',
        color: '#E10098',
        svg: `<svg viewBox="0 0 24 24" fill="none" stroke="#E10098" stroke-width="2"><polygon points="12 2 21 8 21 16 12 22 3 16 3 8"/></svg>`
      },

      // Backend & Systems
      py: {
        name: 'python',
        className: 'icon-file-python',
        color: '#3776AB',
        svg: `<svg viewBox="0 0 24 24"><path d="M11.8 2c-4 0-3.8 1.8-3.8 1.8l.01 1.8h3.9v.6H5.4s-2.4-.3-2.4 3.6c0 3.8 2.1 3.7 2.1 3.7h1.3v-1.8s-.1-2.1 2.1-2.1h3.6s2 .03 2-2V4s.2-2-2.3-2h-.1zm-1.2 1.1c.36 0 .6.28.6.64 0 .36-.28.64-.64.64-.36 0-.64-.28-.64-.64 0-.36.28-.64.64-.64z" fill="#3776AB"/><path d="M12.2 22c4 0 3.8-1.8 3.8-1.8l-.01-1.8h-3.9v-.6h6.5s2.4.3 2.4-3.6c0-3.8-2.1-3.7-2.1-3.7h-1.3v1.8s.1 2.1-2.1 2.1H12s-2-.03-2 2V20s-.2 2 2.3 2h-.1zm1.2-1.1c-.36 0-.6-.28-.6-.64 0-.36.28-.64.64-.64.36 0 .64.28.64.64 0 .36-.28.64-.64.64z" fill="#FFD43B"/></svg>`
      },
      pyw: {
        name: 'python',
        className: 'icon-file-python',
        color: '#3776AB',
        svg: `<svg viewBox="0 0 24 24"><path d="M11.8 2c-4 0-3.8 1.8-3.8 1.8l.01 1.8h3.9v.6H5.4s-2.4-.3-2.4 3.6c0 3.8 2.1 3.7 2.1 3.7h1.3v-1.8s-.1-2.1 2.1-2.1h3.6s2 .03 2-2V4s.2-2-2.3-2h-.1zm-1.2 1.1c.36 0 .6.28.6.64 0 .36-.28.64-.64.64-.36 0-.64-.28-.64-.64 0-.36.28-.64.64-.64z" fill="#3776AB"/></svg>`
      },
      java: {
        name: 'java',
        className: 'icon-file-java',
        color: '#EA2D2E',
        svg: `<svg viewBox="0 0 24 24" fill="none" stroke="#EA2D2E" stroke-width="2" stroke-linecap="round"><path d="M18 8h1a4 4 0 0 1 0 8h-1M2 8h16v9a4 4 0 0 1-4 4H6a4 4 0 0 1-4-4V8zM6 1v3M10 1v3M14 1v3"/></svg>`
      },
      jar: {
        name: 'java',
        className: 'icon-file-java',
        color: '#EA2D2E',
        svg: `<svg viewBox="0 0 24 24" fill="none" stroke="#EA2D2E" stroke-width="2" stroke-linecap="round"><path d="M18 8h1a4 4 0 0 1 0 8h-1M2 8h16v9a4 4 0 0 1-4 4H6a4 4 0 0 1-4-4V8zM6 1v3M10 1v3M14 1v3"/></svg>`
      },
      kt: {
        name: 'kotlin',
        className: 'icon-file-kotlin',
        color: '#7F52FF',
        svg: `<svg viewBox="0 0 24 24"><polygon points="2 2 22 2 12 12 22 22 2 22" fill="#7F52FF"/><polygon points="2 2 12 12 2 22" fill="#C711E1"/></svg>`
      },
      kts: {
        name: 'kotlin',
        className: 'icon-file-kotlin',
        color: '#7F52FF',
        svg: `<svg viewBox="0 0 24 24"><polygon points="2 2 22 2 12 12 22 22 2 22" fill="#7F52FF"/><polygon points="2 2 12 12 2 22" fill="#C711E1"/></svg>`
      },
      swift: {
        name: 'swift',
        className: 'icon-file-swift',
        color: '#F05138',
        svg: `<svg viewBox="0 0 24 24"><path d="M21.5 15.5c-.5.8-2 2.5-4.5 3.5 2.5-1.5 4-4 4.5-5.5-2 2-5 3.5-8.5 3.5 4.5-2.5 6.5-6.5 7-9.5-1.5 1.5-3.5 2.5-5.5 3 2.5-2.5 3.5-5.5 3.5-7.5-2 1.5-4.5 3-7 4 1-2 1.5-4 1.5-5.5-3.5 2.5-7.5 7-9 12.5 4-3 9-3.5 14-1.5 1.5.5 3 1.5 4 3z" fill="#F05138"/></svg>`
      },
      c: {
        name: 'c',
        className: 'icon-file-c',
        color: '#00599C',
        svg: `<svg viewBox="0 0 24 24"><rect width="24" height="24" rx="4" fill="#00599C"/><path d="M16 8.5c-.8-.6-1.8-1-3-1-2.8 0-5 2.2-5 5s2.2 5 5 5c1.2 0 2.2-.4 3-1l1 1.5c-1.1.8-2.5 1.3-4 1.3-3.9 0-7-3.1-7-7s3.1-7 7-7c1.5 0 2.9.5 4 1.3l-1 1.9z" fill="#FFF"/></svg>`
      },
      h: {
        name: 'c-header',
        className: 'icon-file-c-header',
        color: '#A8B9CC',
        svg: `<svg viewBox="0 0 24 24"><rect width="24" height="24" rx="4" fill="#A8B9CC"/><text x="6" y="17" font-size="12" font-weight="bold" fill="#000" font-family="monospace">H</text></svg>`
      },
      cpp: {
        name: 'cpp',
        className: 'icon-file-cpp',
        color: '#00599C',
        svg: `<svg viewBox="0 0 24 24"><rect width="24" height="24" rx="4" fill="#00599C"/><path d="M12 9c-2 0-3.5 1.5-3.5 3.5S10 16 12 16c1 0 1.8-.4 2.4-.9l1 1.2c-.9.8-2.1 1.2-3.4 1.2-3 0-5.5-2.2-5.5-5s2.5-5 5.5-5c1.3 0 2.5.5 3.4 1.2l-1 1.2c-.6-.5-1.4-.9-2.4-.9zm5 2.5h1v1h-1v1h-1v-1h-1v-1h1v-1h1v1zm3 0h1v1h-1v1h-1v-1h-1v-1h1v-1h1v1z" fill="#FFF"/></svg>`
      },
      hpp: {
        name: 'cpp-header',
        className: 'icon-file-cpp-header',
        color: '#00599C',
        svg: `<svg viewBox="0 0 24 24"><rect width="24" height="24" rx="4" fill="#00599C"/><text x="4" y="16" font-size="9" font-weight="bold" fill="#FFF" font-family="monospace">H++</text></svg>`
      },
      cs: {
        name: 'csharp',
        className: 'icon-file-csharp',
        color: '#68217A',
        svg: `<svg viewBox="0 0 24 24"><rect width="24" height="24" rx="4" fill="#68217A"/><path d="M11 9c-1.7 0-3 1.3-3 3s1.3 3 3 3c.9 0 1.6-.3 2.1-.8l.9 1.1c-.8.7-1.8 1.1-3 1.1-2.8 0-5-2.2-5-5s2.2-5 5-5c1.2 0 2.2.4 3 1.1l-.9 1.1c-.5-.5-1.2-.8-2.1-.8zm5 1.5l.3-1.5h1.2l-.3 1.5h1.2v1h-1.4l-.2 1.2h1.4v1h-1.6l-.3 1.5h-1.2l.3-1.5h-1.2l-.3 1.5h-1.2l.3-1.5h-1v-1h1.2l.2-1.2h-1.2v-1h1.4l.3-1.5h1.2l-.3 1.5h1.2zm-.1 2.2l.2-1.2h-1.2l-.2 1.2h1.2z" fill="#FFF"/></svg>`
      },
      go: {
        name: 'go',
        className: 'icon-file-go',
        color: '#00ADD8',
        svg: `<svg viewBox="0 0 24 24"><path d="M2 11c0-2.8 2.2-5 5-5 1.5 0 2.8.6 3.7 1.7l-1.8 1.4C8.3 8.4 7.7 8 7 8c-1.7 0-3 1.3-3 3s1.3 3 3 3c1.2 0 2.1-.7 2.6-1.7H7v-2h4.5c.1.4.1.7.1 1 0 2.8-2.2 5-5 5-2.8 0-5-2.2-5-5zm16.5-5c2.8 0 5 2.2 5 5s-2.2 5-5 5-5-2.2-5-5 2.2-5 5-5zm0 2c-1.7 0-3 1.3-3 3s1.3 3 3 3 3-1.3 3-3-1.3-3-3-3z" fill="#00ADD8"/></svg>`
      },
      rs: {
        name: 'rust',
        className: 'icon-file-rust',
        color: '#DEA584',
        svg: `<svg viewBox="0 0 24 24"><circle cx="12" cy="12" r="9" fill="none" stroke="#DEA584" stroke-width="2"/><circle cx="12" cy="12" r="3" fill="#DEA584"/><path d="M12 2v3m0 14v3M2 12h3m14 0h3M5 5l2.2 2.2m9.6 9.6L19 19M5 19l2.2-2.2m9.6-9.6L19 5" stroke="#DEA584" stroke-width="2"/></svg>`
      },
      php: {
        name: 'php',
        className: 'icon-file-php',
        color: '#777BB4',
        svg: `<svg viewBox="0 0 24 24"><ellipse cx="12" cy="12" rx="10" ry="6" fill="#777BB4"/><path d="M8 9h2c1 0 1.8.8 1.8 1.8s-.8 1.8-1.8 1.8H9.2L8.6 15H7.2L8 9zm1.3 2.6h.7c.4 0 .7-.3.7-.7s-.3-.7-.7-.7h-.7l-.2 1.4zm5.5-2.6h1.4l-.6 2.4h1.5l.6-2.4h1.4L18 15h-1.4l.6-2.6h-1.5L15 15h-1.4l1.2-6z" fill="#FFF"/></svg>`
      },
      rb: {
        name: 'ruby',
        className: 'icon-file-ruby',
        color: '#CC342D',
        svg: `<svg viewBox="0 0 24 24"><path d="M19.5 8.5L15 3H9L4.5 8.5 12 21l7.5-12.5z" fill="#CC342D"/><path d="M12 21L4.5 8.5h15L12 21z" fill="#E6534C"/><path d="M9 3l3 5.5-4.5 0L9 3zm6 0l-3 5.5h4.5L15 3z" fill="#FFA5A1"/></svg>`
      },
      dart: {
        name: 'dart',
        className: 'icon-file-dart',
        color: '#0175C2',
        svg: `<svg viewBox="0 0 24 24"><polygon points="4 4 14 4 20 10 20 20 10 20 4 14" fill="#0175C2"/><polygon points="10 4 20 14 14 20 4 10" fill="#02569B"/><polygon points="9 9 15 9 15 15 9 15" fill="#40C4FF"/></svg>`
      },
      lua: {
        name: 'lua',
        className: 'icon-file-lua',
        color: '#000080',
        svg: `<svg viewBox="0 0 24 24"><circle cx="12" cy="12" r="8" fill="#000080"/><circle cx="15" cy="9" r="2.5" fill="#FFF"/><circle cx="17.5" cy="6.5" r="1" fill="#FFD700"/></svg>`
      },
      sql: {
        name: 'sql',
        className: 'icon-file-sql',
        color: '#E38C00',
        svg: `<svg viewBox="0 0 24 24" fill="none" stroke="#E38C00" stroke-width="2"><ellipse cx="12" cy="5" rx="9" ry="3"/><path d="M21 12c0 1.66-4 3-9 3s-9-1.34-9-3"/><path d="M3 5v14c0 1.66 4 3 9 3s9-1.34 9-3V5"/></svg>`
      },
      sh: {
        name: 'shell',
        className: 'icon-file-shell',
        color: '#4EAA25',
        svg: `<svg viewBox="0 0 24 24" fill="none" stroke="#4EAA25" stroke-width="2" stroke-linecap="round"><polyline points="4 17 10 11 4 5"/><line x1="12" y1="19" x2="20" y2="19"/></svg>`
      },
      bash: {
        name: 'shell',
        className: 'icon-file-shell',
        color: '#4EAA25',
        svg: `<svg viewBox="0 0 24 24" fill="none" stroke="#4EAA25" stroke-width="2" stroke-linecap="round"><polyline points="4 17 10 11 4 5"/><line x1="12" y1="19" x2="20" y2="19"/></svg>`
      },
      zsh: {
        name: 'shell',
        className: 'icon-file-shell',
        color: '#4EAA25',
        svg: `<svg viewBox="0 0 24 24" fill="none" stroke="#4EAA25" stroke-width="2" stroke-linecap="round"><polyline points="4 17 10 11 4 5"/><line x1="12" y1="19" x2="20" y2="19"/></svg>`
      },

      // Documentation & Text
      md: {
        name: 'markdown',
        className: 'icon-file-markdown',
        color: '#42A5F5',
        svg: `<svg viewBox="0 0 24 24"><rect width="22" height="16" x="1" y="4" rx="2" fill="none" stroke="#42A5F5" stroke-width="2"/><path d="M4 15V9l3 3 3-3v6M17 9v6m-2-2l2 2 2-2" fill="none" stroke="#42A5F5" stroke-width="2" stroke-linecap="round"/></svg>`
      },
      markdown: {
        name: 'markdown',
        className: 'icon-file-markdown',
        color: '#42A5F5',
        svg: `<svg viewBox="0 0 24 24"><rect width="22" height="16" x="1" y="4" rx="2" fill="none" stroke="#42A5F5" stroke-width="2"/><path d="M4 15V9l3 3 3-3v6M17 9v6m-2-2l2 2 2-2" fill="none" stroke="#42A5F5" stroke-width="2" stroke-linecap="round"/></svg>`
      },
      txt: {
        name: 'text',
        className: 'icon-file-text',
        color: '#94A3B8',
        svg: `<svg viewBox="0 0 24 24" fill="none" stroke="#94A3B8" stroke-width="2"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/></svg>`
      },
      pdf: {
        name: 'pdf',
        className: 'icon-file-pdf',
        color: '#EF4444',
        svg: `<svg viewBox="0 0 24 24"><rect width="24" height="24" rx="4" fill="#EF4444"/><text x="3" y="16" font-size="8" font-weight="bold" fill="#FFF" font-family="sans-serif">PDF</text></svg>`
      },

      // Media & Graphics
      svg: {
        name: 'svg',
        className: 'icon-file-svg',
        color: '#FFB13B',
        svg: `<svg viewBox="0 0 24 24" fill="none" stroke="#FFB13B" stroke-width="2"><circle cx="12" cy="12" r="9"/><polygon points="12 5 15 12 9 12"/></svg>`
      },
      png: {
        name: 'image',
        className: 'icon-file-image',
        color: '#10B981',
        svg: `<svg viewBox="0 0 24 24" fill="none" stroke="#10B981" stroke-width="2"><rect x="3" y="3" width="18" height="18" rx="2"/><circle cx="8.5" cy="8.5" r="1.5"/><polyline points="21 15 16 10 5 21"/></svg>`
      },
      jpg: {
        name: 'image',
        className: 'icon-file-image',
        color: '#10B981',
        svg: `<svg viewBox="0 0 24 24" fill="none" stroke="#10B981" stroke-width="2"><rect x="3" y="3" width="18" height="18" rx="2"/><circle cx="8.5" cy="8.5" r="1.5"/><polyline points="21 15 16 10 5 21"/></svg>`
      },
      jpeg: {
        name: 'image',
        className: 'icon-file-image',
        color: '#10B981',
        svg: `<svg viewBox="0 0 24 24" fill="none" stroke="#10B981" stroke-width="2"><rect x="3" y="3" width="18" height="18" rx="2"/><circle cx="8.5" cy="8.5" r="1.5"/><polyline points="21 15 16 10 5 21"/></svg>`
      },
      webp: {
        name: 'image',
        className: 'icon-file-image',
        color: '#10B981',
        svg: `<svg viewBox="0 0 24 24" fill="none" stroke="#10B981" stroke-width="2"><rect x="3" y="3" width="18" height="18" rx="2"/><circle cx="8.5" cy="8.5" r="1.5"/><polyline points="21 15 16 10 5 21"/></svg>`
      },
      ico: {
        name: 'image',
        className: 'icon-file-image',
        color: '#10B981',
        svg: `<svg viewBox="0 0 24 24" fill="none" stroke="#10B981" stroke-width="2"><rect x="3" y="3" width="18" height="18" rx="2"/><circle cx="8.5" cy="8.5" r="1.5"/></svg>`
      },
      zip: {
        name: 'archive',
        className: 'icon-file-archive',
        color: '#EAB308',
        svg: `<svg viewBox="0 0 24 24" fill="none" stroke="#EAB308" stroke-width="2"><path d="M10 2v20M14 2v20"/><path d="M10 5h4M10 9h4M10 13h4M10 17h4"/></svg>`
      },
      wasm: {
        name: 'wasm',
        className: 'icon-file-wasm',
        color: '#654FF0',
        svg: `<svg viewBox="0 0 24 24"><rect width="24" height="24" rx="4" fill="#654FF0"/><text x="2" y="16" font-size="7.5" font-weight="bold" fill="#FFF" font-family="monospace">WASM</text></svg>`
      }
    },

    // Well-Known Folders
    folders: {
      src: {
        name: 'folder-src',
        className: 'icon-folder-src',
        color: '#38BDF8',
        svg: `<svg viewBox="0 0 24 24" fill="#38BDF8"><path d="M10 4H4c-1.1 0-2 .9-2 2v12c0 1.1.9 2 2 2h16c1.1 0 2-.9 2-2V8c0-1.1-.9-2-2-2h-8l-2-2z"/></svg>`
      },
      source: {
        name: 'folder-src',
        className: 'icon-folder-src',
        color: '#38BDF8',
        svg: `<svg viewBox="0 0 24 24" fill="#38BDF8"><path d="M10 4H4c-1.1 0-2 .9-2 2v12c0 1.1.9 2 2 2h16c1.1 0 2-.9 2-2V8c0-1.1-.9-2-2-2h-8l-2-2z"/></svg>`
      },
      components: {
        name: 'folder-components',
        className: 'icon-folder-components',
        color: '#60A5FA',
        svg: `<svg viewBox="0 0 24 24" fill="#60A5FA"><path d="M10 4H4c-1.1 0-2 .9-2 2v12c0 1.1.9 2 2 2h16c1.1 0 2-.9 2-2V8c0-1.1-.9-2-2-2h-8l-2-2z"/></svg>`
      },
      ui: {
        name: 'folder-components',
        className: 'icon-folder-components',
        color: '#60A5FA',
        svg: `<svg viewBox="0 0 24 24" fill="#60A5FA"><path d="M10 4H4c-1.1 0-2 .9-2 2v12c0 1.1.9 2 2 2h16c1.1 0 2-.9 2-2V8c0-1.1-.9-2-2-2h-8l-2-2z"/></svg>`
      },
      assets: {
        name: 'folder-assets',
        className: 'icon-folder-assets',
        color: '#F472B6',
        svg: `<svg viewBox="0 0 24 24" fill="#F472B6"><path d="M10 4H4c-1.1 0-2 .9-2 2v12c0 1.1.9 2 2 2h16c1.1 0 2-.9 2-2V8c0-1.1-.9-2-2-2h-8l-2-2z"/></svg>`
      },
      images: {
        name: 'folder-assets',
        className: 'icon-folder-assets',
        color: '#F472B6',
        svg: `<svg viewBox="0 0 24 24" fill="#F472B6"><path d="M10 4H4c-1.1 0-2 .9-2 2v12c0 1.1.9 2 2 2h16c1.1 0 2-.9 2-2V8c0-1.1-.9-2-2-2h-8l-2-2z"/></svg>`
      },
      img: {
        name: 'folder-assets',
        className: 'icon-folder-assets',
        color: '#F472B6',
        svg: `<svg viewBox="0 0 24 24" fill="#F472B6"><path d="M10 4H4c-1.1 0-2 .9-2 2v12c0 1.1.9 2 2 2h16c1.1 0 2-.9 2-2V8c0-1.1-.9-2-2-2h-8l-2-2z"/></svg>`
      },
      media: {
        name: 'folder-assets',
        className: 'icon-folder-assets',
        color: '#F472B6',
        svg: `<svg viewBox="0 0 24 24" fill="#F472B6"><path d="M10 4H4c-1.1 0-2 .9-2 2v12c0 1.1.9 2 2 2h16c1.1 0 2-.9 2-2V8c0-1.1-.9-2-2-2h-8l-2-2z"/></svg>`
      },
      node_modules: {
        name: 'folder-modules',
        className: 'icon-folder-modules',
        color: '#CB3837',
        svg: `<svg viewBox="0 0 24 24" fill="#CB3837"><path d="M10 4H4c-1.1 0-2 .9-2 2v12c0 1.1.9 2 2 2h16c1.1 0 2-.9 2-2V8c0-1.1-.9-2-2-2h-8l-2-2z"/></svg>`
      },
      public: {
        name: 'folder-public',
        className: 'icon-folder-public',
        color: '#34D399',
        svg: `<svg viewBox="0 0 24 24" fill="#34D399"><path d="M10 4H4c-1.1 0-2 .9-2 2v12c0 1.1.9 2 2 2h16c1.1 0 2-.9 2-2V8c0-1.1-.9-2-2-2h-8l-2-2z"/></svg>`
      },
      dist: {
        name: 'folder-dist',
        className: 'icon-folder-dist',
        color: '#34D399',
        svg: `<svg viewBox="0 0 24 24" fill="#34D399"><path d="M10 4H4c-1.1 0-2 .9-2 2v12c0 1.1.9 2 2 2h16c1.1 0 2-.9 2-2V8c0-1.1-.9-2-2-2h-8l-2-2z"/></svg>`
      },
      build: {
        name: 'folder-build',
        className: 'icon-folder-build',
        color: '#34D399',
        svg: `<svg viewBox="0 0 24 24" fill="#34D399"><path d="M10 4H4c-1.1 0-2 .9-2 2v12c0 1.1.9 2 2 2h16c1.1 0 2-.9 2-2V8c0-1.1-.9-2-2-2h-8l-2-2z"/></svg>`
      },
      test: {
        name: 'folder-test',
        className: 'icon-folder-test',
        color: '#FBBF24',
        svg: `<svg viewBox="0 0 24 24" fill="#FBBF24"><path d="M10 4H4c-1.1 0-2 .9-2 2v12c0 1.1.9 2 2 2h16c1.1 0 2-.9 2-2V8c0-1.1-.9-2-2-2h-8l-2-2z"/></svg>`
      },
      tests: {
        name: 'folder-test',
        className: 'icon-folder-test',
        color: '#FBBF24',
        svg: `<svg viewBox="0 0 24 24" fill="#FBBF24"><path d="M10 4H4c-1.1 0-2 .9-2 2v12c0 1.1.9 2 2 2h16c1.1 0 2-.9 2-2V8c0-1.1-.9-2-2-2h-8l-2-2z"/></svg>`
      }
    },

    // Default Fallbacks
    default: {
      file: {
        name: 'generic-file',
        className: 'icon-file-generic',
        color: '#8B95A7',
        svg: `<svg viewBox="0 0 24 24" fill="none" stroke="#8B95A7" stroke-width="1.8"><path d="M13 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V9z"/><polyline points="13 2 13 9 20 9"/></svg>`
      },
      folderClosed: {
        name: 'folder',
        className: 'icon-folder-closed',
        color: '#FBBF24',
        svg: `<svg viewBox="0 0 24 24" fill="#FBBF24"><path d="M10 4H4c-1.1 0-2 .9-2 2v12c0 1.1.9 2 2 2h16c1.1 0 2-.9 2-2V8c0-1.1-.9-2-2-2h-8l-2-2z"/></svg>`
      },
      folderOpen: {
        name: 'folder-open',
        className: 'icon-folder-open',
        color: '#FBBF24',
        svg: `<svg viewBox="0 0 24 24" fill="#FBBF24"><path d="M20 6h-8l-2-2H4c-1.1 0-2 .9-2 2v12c0 1.1.9 2 2 2h16c1.1 0 2-.9 2-2V8c0-1.1-.9-2-2-2zm0 12H4V8h16v10z"/></svg>`
      }
    },

    /**
     * Resolves the icon definition for any file or directory name.
     */
    get(name, isDirectory = false, isOpen = false) {
      if (isDirectory) {
        const lower = name.toLowerCase().trim();
        if (this.folders[lower]) {
          return this.folders[lower];
        }
        return isOpen ? this.default.folderOpen : this.default.folderClosed;
      }

      const lowerName = name.toLowerCase().trim();
      if (this.filenames[lowerName]) {
        return this.filenames[lowerName];
      }

      const dotIndex = lowerName.lastIndexOf('.');
      if (dotIndex !== -1 && dotIndex < lowerName.length - 1) {
        const ext = lowerName.substring(dotIndex + 1);
        if (this.extensions[ext]) {
          return this.extensions[ext];
        }
      }

      return this.default.file;
    },

    // Registration Methods
    registerExtension(ext, iconDef) {
      this.extensions[ext.toLowerCase().replace(/^\./, '')] = iconDef;
    },
    registerFilename(filename, iconDef) {
      this.filenames[filename.toLowerCase().trim()] = iconDef;
    },
    registerFolder(folderName, iconDef) {
      this.folders[folderName.toLowerCase().trim()] = iconDef;
    }
  };

  // Expose to window for external consumption or test suites
  if (typeof window !== 'undefined') {
    window.iconMapping = iconMapping;
  }

  /**
   * Helper wrapper for tree node icon rendering.
   */
  function getFileIcon(name, isDirectory, isOpen = false) {
    return iconMapping.get(name, isDirectory, isOpen);
  }

  // =========================================================================
  // 2. PROJECT FILE TREE DATA (Simple JSON Structure)
  // =========================================================================

  let fileTreeData = [
    {
      id: 'folder-src',
      name: 'src',
      type: 'folder',
      isOpen: true,
      children: [
        {
          id: 'folder-components',
          name: 'components',
          type: 'folder',
          isOpen: true,
          children: [
            {
              id: 'file-header',
              name: 'Header.tsx',
              type: 'file',
              content: `import React from 'react';\n\nexport const Header: React.FC = () => {\n  return (\n    <header className="navbar">\n      <h1>⚡ Linq Studio</h1>\n    </header>\n  );\n};`
            },
            {
              id: 'file-counter',
              name: 'Counter.vue',
              type: 'file',
              content: `<template>\n  <button @click="count++">Count: {{ count }}</button>\n</template>\n\n<script setup>\nimport { ref } from 'vue';\nconst count = ref(0);\n</script>`
            }
          ]
        },
        {
          id: 'file-main-py',
          name: 'main.py',
          type: 'file',
          content: `# Linq Studio Python Micro-Engine\nimport sys\n\ndef run():\n    print("Running Python 3.12...")\n    for i in range(5):\n        print(f"Step {i+1}")\n\nif __name__ == '__main__':\n    run()`
        },
        {
          id: 'file-main-java',
          name: 'Main.java',
          type: 'file',
          content: `package com.example.studio;\n\nimport java.util.List;\nimport java.util.ArrayList;\n\n/**\n * Linq Studio Java Highlighting & Runner\n */\npublic class Main {\n    private static final String APP_NAME = "Linq Studio";\n\n    public static void main(String[] args) {\n        System.out.println("⚡ Running " + APP_NAME + " on Android runtime...");\n        List<String> features = new ArrayList<>();\n        features.add("Syntax Highlighting (Prism.js)");\n        features.add("Git & GitHub VCS Sync");\n        features.add("Dual Terminal Shell");\n\n        for (int i = 0; i < features.size(); i++) {\n            System.out.printf("[%d] Active: %s%n", i + 1, features.get(i));\n        }\n    }\n}`
        },
        {
          id: 'file-server-go',
          name: 'server.go',
          type: 'file',
          content: `package main\n\nimport "fmt"\n\nfunc main() {\n    fmt.Println("Linq Studio microserver started on :8080")\n}`
        }
      ]
    },
    {
      id: 'folder-assets',
      name: 'assets',
      type: 'folder',
      isOpen: false,
      children: [
        {
          id: 'file-logo',
          name: 'logo.svg',
          type: 'file',
          content: `<svg viewBox="0 0 100 100">\n  <circle cx="50" cy="50" r="45" fill="#6366F1"/>\n</svg>`
        }
      ]
    },
    {
      id: 'file-index-html',
      name: 'index.html',
      type: 'file',
      content: `<!DOCTYPE html>\n<html lang="en">\n<head>\n  <meta charset="UTF-8">\n  <title>Linq Studio Demo</title>\n  <link rel="stylesheet" href="style.css">\n</head>\n<body>\n  <div id="root">\n    <h1>Welcome to Linq Studio</h1>\n    <p>Mobile-first development at lightning speed.</p>\n  </div>\n</body>\n</html>`
    },
    {
      id: 'file-style-css',
      name: 'style.css',
      type: 'file',
      content: `body {\n  margin: 0;\n  background: #0F1218;\n  color: #f3f4f6;\n  font-family: sans-serif;\n}`
    },
    {
      id: 'file-app-js',
      name: 'app.js',
      type: 'file',
      content: `console.log("Linq Studio workspace loaded successfully!");\n\nexport function init() {\n  return true;\n}`
    },
    {
      id: 'file-package-json',
      name: 'package.json',
      type: 'file',
      content: `{\n  "name": "linq-studio-app",\n  "version": "1.0.0",\n  "private": true,\n  "scripts": {\n    "dev": "vite",\n    "build": "vite build"\n  }\n}`
    },
    {
      id: 'file-readme',
      name: 'README.md',
      type: 'file',
      content: `# My Project\n\nBuilt with Linq Studio mobile code studio.`
    },
    {
      id: 'file-gitignore',
      name: '.gitignore',
      type: 'file',
      content: `node_modules/\ndist/\n.env.local`
    },
    {
      id: 'file-env',
      name: '.env',
      type: 'file',
      content: `PORT=8080\nAPI_KEY=prod_key_sample_secret`
    }
  ];

  // Active state tracker
  let activeFileId = 'file-index-html';
  let contextTargetNode = null;
  let creatorMode = null; // 'file' or 'folder'
  let filterQuery = '';

  // =========================================================================
  // 3. DOM ELEMENTS
  // =========================================================================

  const sideDrawer = document.getElementById('sideDrawer');
  const drawerBackdrop = document.getElementById('drawerBackdrop');
  const btnOpenDrawer = document.getElementById('btnOpenDrawer');
  const fileTree = document.getElementById('fileTree');
  const projectRootHeader = document.getElementById('projectRootHeader');
  const rootChevron = document.getElementById('rootChevron');

  const btnToggleSearch = document.getElementById('btnToggleSearch');
  const quickFilterRow = document.getElementById('quickFilterRow');
  const quickFilterInput = document.getElementById('quickFilterInput');
  const btnCloseFilter = document.getElementById('btnCloseFilter');

  const btnRevealActive = document.getElementById('btnRevealActive');
  const btnNewFile = document.getElementById('btnNewFile');
  const btnNewFolder = document.getElementById('btnNewFolder');
  const btnHeaderOverflow = document.getElementById('btnHeaderOverflow');

  const inlineCreator = document.getElementById('inlineCreator');
  const creatorIcon = document.getElementById('creatorIcon');
  const creatorInput = document.getElementById('creatorInput');
  const btnConfirmCreate = document.getElementById('btnConfirmCreate');
  const btnCancelCreate = document.getElementById('btnCancelCreate');

  const rowPopupMenu = document.getElementById('rowPopupMenu');
  const toastContainer = document.getElementById('toastContainer');

  // Main Editor View Elements
  const activeEditorTab = document.getElementById('activeEditorTab');
  const editorTabIcon = document.getElementById('editorTabIcon');
  const editorTabTitle = document.getElementById('editorTabTitle');
  const codeContent = document.getElementById('codeContent');
  const lineNumbers = document.getElementById('lineNumbers');
  const statusFile = document.getElementById('statusFile');
  const statusLang = document.getElementById('statusLang');
  const btnRunActive = document.getElementById('btnRunActive');

  // =========================================================================
  // 4. DRAWER SLIDE-IN & FLUID TOUCH GESTURES (Swipe-to-close & Drag tracking)
  // =========================================================================

  function getDrawerWidth() {
    return sideDrawer.getBoundingClientRect().width || window.innerWidth * 0.9;
  }

  function openDrawer() {
    sideDrawer.style.transform = '';
    drawerBackdrop.style.opacity = '';
    sideDrawer.classList.remove('dragging');
    drawerBackdrop.classList.remove('dragging');
    sideDrawer.classList.add('open');
    drawerBackdrop.classList.add('active');
    hidePopupMenu();
  }

  function closeDrawer() {
    sideDrawer.style.transform = '';
    drawerBackdrop.style.opacity = '';
    sideDrawer.classList.remove('dragging');
    drawerBackdrop.classList.remove('dragging');
    sideDrawer.classList.remove('open');
    drawerBackdrop.classList.remove('active');
    hidePopupMenu();
  }

  function toggleDrawer() {
    if (sideDrawer.classList.contains('open')) {
      closeDrawer();
    } else {
      openDrawer();
    }
  }

  btnOpenDrawer.addEventListener('click', toggleDrawer);
  drawerBackdrop.addEventListener('click', closeDrawer);

  // --- Real-time 1:1 Swipe-to-Close Touch Tracking ---
  let touchStartX = 0;
  let touchStartY = 0;
  let touchCurrentX = 0;
  let touchStartTime = 0;
  let isDraggingDrawer = false;
  let isTouchCandidate = false;
  let cachedDrawerWidth = 0;

  sideDrawer.addEventListener('touchstart', (e) => {
    if (e.touches.length !== 1) return;
    touchStartX = e.touches[0].clientX;
    touchStartY = e.touches[0].clientY;
    touchCurrentX = touchStartX;
    touchStartTime = performance.now();
    isDraggingDrawer = false;
    isTouchCandidate = true;
    cachedDrawerWidth = getDrawerWidth();
  }, { passive: true });

  sideDrawer.addEventListener('touchmove', (e) => {
    if (!isTouchCandidate || e.touches.length !== 1) return;

    const clientX = e.touches[0].clientX;
    const clientY = e.touches[0].clientY;
    const deltaX = clientX - touchStartX;
    const deltaY = clientY - touchStartY;

    // Detect horizontal intention vs vertical scrolling
    if (!isDraggingDrawer) {
      if (Math.abs(deltaX) > 8) {
        if (Math.abs(deltaX) > Math.abs(deltaY) && deltaX < 0) {
          isDraggingDrawer = true;
          sideDrawer.classList.add('dragging');
          drawerBackdrop.classList.add('dragging');
        } else {
          isTouchCandidate = false;
          return;
        }
      }
    }

    if (isDraggingDrawer) {
      touchCurrentX = clientX;
      // Clamp between -cachedDrawerWidth and 0
      const boundedDelta = Math.min(0, Math.max(-cachedDrawerWidth, deltaX));
      sideDrawer.style.transform = `translate3d(${boundedDelta}px, 0, 0)`;

      // Smooth proportional backdrop fade
      const progress = 1 - (Math.abs(boundedDelta) / cachedDrawerWidth);
      drawerBackdrop.style.opacity = Math.max(0, Math.min(1, progress * 0.65)).toString();
    }
  }, { passive: true });

  function finishDrawerDrag() {
    if (!isDraggingDrawer) {
      isTouchCandidate = false;
      return;
    }

    isDraggingDrawer = false;
    isTouchCandidate = false;
    sideDrawer.classList.remove('dragging');
    drawerBackdrop.classList.remove('dragging');
    sideDrawer.style.transform = '';
    drawerBackdrop.style.opacity = '';

    const deltaX = touchCurrentX - touchStartX;
    const deltaTime = Math.max(1, performance.now() - touchStartTime);
    const velocityX = deltaX / deltaTime; // px per millisecond

    // Thresholds: dragged > 28% of width OR quick left flick (velocity < -0.32 px/ms)
    const dragThreshold = -cachedDrawerWidth * 0.28;
    if (deltaX < dragThreshold || velocityX < -0.32) {
      closeDrawer();
    } else {
      // Re-snap open smoothly with CSS spring transition
      sideDrawer.classList.add('open');
      drawerBackdrop.classList.add('active');
    }
  }

  sideDrawer.addEventListener('touchend', finishDrawerDrag, { passive: true });
  sideDrawer.addEventListener('touchcancel', finishDrawerDrag, { passive: true });

  // --- Edge Swipe-to-Open from Left Border (< 28px) ---
  let isEdgeDragging = false;
  let edgeStartX = 0;
  let edgeStartTime = 0;
  let edgeCurrentX = 0;

  window.addEventListener('touchstart', (e) => {
    if (sideDrawer.classList.contains('open')) return;
    if (e.touches.length !== 1) return;
    if (e.touches[0].clientX < 28) {
      edgeStartX = e.touches[0].clientX;
      edgeCurrentX = edgeStartX;
      edgeStartTime = performance.now();
      isEdgeDragging = true;
      cachedDrawerWidth = getDrawerWidth();
      sideDrawer.classList.add('dragging');
      drawerBackdrop.classList.add('dragging');
      drawerBackdrop.classList.add('active');
    }
  }, { passive: true });

  window.addEventListener('touchmove', (e) => {
    if (!isEdgeDragging || e.touches.length !== 1) return;
    edgeCurrentX = e.touches[0].clientX;
    const deltaX = edgeCurrentX - edgeStartX;
    if (deltaX > 0) {
      const boundedDelta = Math.min(cachedDrawerWidth, deltaX);
      const translate = -cachedDrawerWidth + boundedDelta;
      sideDrawer.style.transform = `translate3d(${translate}px, 0, 0)`;

      const progress = boundedDelta / cachedDrawerWidth;
      drawerBackdrop.style.opacity = Math.max(0, Math.min(1, progress * 0.65)).toString();
    }
  }, { passive: true });

  function finishEdgeDrag() {
    if (!isEdgeDragging) return;
    isEdgeDragging = false;
    sideDrawer.classList.remove('dragging');
    drawerBackdrop.classList.remove('dragging');
    sideDrawer.style.transform = '';
    drawerBackdrop.style.opacity = '';

    const deltaX = edgeCurrentX - edgeStartX;
    const deltaTime = Math.max(1, performance.now() - edgeStartTime);
    const velocityX = deltaX / deltaTime;

    if (deltaX > cachedDrawerWidth * 0.28 || velocityX > 0.32) {
      openDrawer();
    } else {
      closeDrawer();
    }
  }

  window.addEventListener('touchend', finishEdgeDrag, { passive: true });
  window.addEventListener('touchcancel', finishEdgeDrag, { passive: true });

  // =========================================================================
  // 5. TOP TAB BAR NAVIGATION
  // =========================================================================

  const navTabs = document.querySelectorAll('.nav-tab');
  const tabPanels = document.querySelectorAll('.tab-panel');

  navTabs.forEach((tab) => {
    tab.addEventListener('click', () => {
      const target = tab.dataset.tab;
      navTabs.forEach((t) => t.classList.remove('active'));
      tab.classList.add('active');

      tabPanels.forEach((p) => p.classList.remove('active'));
      const activePanel = document.getElementById(`panel${capitalize(target)}`);
      if (activePanel) {
        activePanel.classList.add('active');
      }
    });
  });

  function capitalize(str) {
    return str.charAt(0).toUpperCase() + str.slice(1);
  }

  // =========================================================================
  // 6. TREE RENDERING ENGINE (Indented with faint line & 68px row height)
  // =========================================================================

  function renderTree() {
    fileTree.innerHTML = '';
    renderNodes(fileTreeData, fileTree, 0);
  }

  function renderNodes(nodes, container, depth) {
    nodes.forEach((node) => {
      // Filter query support
      if (filterQuery) {
        const matchesSelf = node.name.toLowerCase().includes(filterQuery);
        const matchesChildren = node.children && hasMatchingChild(node.children, filterQuery);
        if (!matchesSelf && !matchesChildren) return;
      }

      const nodeWrapper = document.createElement('div');
      nodeWrapper.className = 'tree-node';
      nodeWrapper.dataset.id = node.id;

      // Tree Row
      const row = document.createElement('div');
      row.className = `tree-row ${node.id === activeFileId ? 'active' : ''}`;
      row.dataset.id = node.id;
      row.tabIndex = 0;

      // Indent guides for depth
      for (let i = 0; i < depth; i++) {
        const spacer = document.createElement('div');
        spacer.className = 'indent-spacer';
        const guide = document.createElement('div');
        guide.className = 'indent-guide';
        spacer.appendChild(guide);
        row.appendChild(spacer);
      }

      // Chevron for folders, or empty spacer for files
      const chevron = document.createElement('span');
      chevron.className = `node-chevron ${node.type === 'folder' ? (node.isOpen ? 'open' : '') : 'placeholder'}`;
      chevron.innerHTML = `<svg viewBox="0 0 24 24" width="13" height="13" stroke="currentColor" stroke-width="2.5" fill="none"><polyline points="6 9 12 15 18 9"></polyline></svg>`;
      row.appendChild(chevron);

      // Icon (Auto-mapped)
      const iconData = getFileIcon(node.name, node.type === 'folder', node.isOpen);
      const iconSpan = document.createElement('span');
      iconSpan.className = 'file-icon';
      iconSpan.innerHTML = iconData.svg;
      row.appendChild(iconSpan);

      // Small Run Button for HTML files
      const isHtml = node.type === 'file' && (node.name.endsWith('.html') || node.name.endsWith('.htm'));
      if (isHtml) {
        const runBtn = document.createElement('button');
        runBtn.className = 'btn-run-html';
        runBtn.title = 'Run / Preview in browser';
        runBtn.innerHTML = `<svg viewBox="0 0 24 24" width="10" height="10" fill="currentColor"><polygon points="5 3 19 12 5 21 5 3"/></svg>Run`;
        runBtn.addEventListener('click', (e) => {
          e.stopPropagation();
          triggerHtmlPreview(node);
        });
        row.appendChild(runBtn);
      }

      // File Name
      const nameSpan = document.createElement('span');
      nameSpan.className = 'file-name';
      nameSpan.textContent = node.name;
      row.appendChild(nameSpan);

      // Overflow menu button
      const overflowBtn = document.createElement('button');
      overflowBtn.className = 'btn-row-overflow';
      overflowBtn.ariaLabel = `Options for ${node.name}`;
      overflowBtn.innerHTML = `<svg viewBox="0 0 24 24" width="16" height="16" fill="currentColor"><circle cx="12" cy="12" r="1.5"/><circle cx="12" cy="5" r="1.5"/><circle cx="12" cy="19" r="1.5"/></svg>`;
      overflowBtn.addEventListener('click', (e) => {
        e.stopPropagation();
        showPopupMenu(node, overflowBtn);
      });
      row.appendChild(overflowBtn);

      // Tap handling: open file or expand/collapse folder
      row.addEventListener('click', () => {
        if (node.type === 'folder') {
          node.isOpen = !node.isOpen;
          renderTree();
        } else {
          setActiveFile(node);
        }
      });

      nodeWrapper.appendChild(row);

      // Children container
      if (node.type === 'folder' && node.children) {
        const childrenContainer = document.createElement('div');
        childrenContainer.className = `tree-node-children ${node.isOpen ? '' : 'collapsed'}`;
        renderNodes(node.children, childrenContainer, depth + 1);
        nodeWrapper.appendChild(childrenContainer);
      }

      container.appendChild(nodeWrapper);
    });
  }

  function hasMatchingChild(children, query) {
    return children.some((c) => {
      if (c.name.toLowerCase().includes(query)) return true;
      if (c.children) return hasMatchingChild(c.children, query);
      return false;
    });
  }

  // =========================================================================
  // 7. SYNTAX HIGHLIGHTING ENGINE (PRISM.JS & ADVANCED FALLBACK FOR HTML, CSS, JS, JAVA)
  // =========================================================================

  const SyntaxHighlighter = {
    escapeHtml(str) {
      if (!str) return '';
      return str
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;')
        .replace(/'/g, '&#39;');
    },

    getPrismLanguage(ext) {
      if (!ext) return null;
      const lang = ext.toLowerCase();
      const prismObj = typeof window !== 'undefined' ? window.Prism : (typeof Prism !== 'undefined' ? Prism : null);
      if (!prismObj || !prismObj.languages) return null;

      switch (lang) {
        case 'html':
        case 'htm':
        case 'xml':
        case 'svg':
          return { grammar: prismObj.languages.markup, name: 'markup' };
        case 'css':
        case 'scss':
        case 'sass':
        case 'less':
          return { grammar: prismObj.languages.css, name: 'css' };
        case 'js':
        case 'javascript':
        case 'mjs':
        case 'cjs':
          return { grammar: prismObj.languages.javascript, name: 'javascript' };
        case 'ts':
        case 'mts':
        case 'cts':
        case 'jsx':
        case 'tsx':
        case 'vue':
        case 'svelte':
          return { grammar: prismObj.languages.javascript || prismObj.languages.clike, name: 'javascript' };
        case 'java':
        case 'jav':
          return { grammar: prismObj.languages.java, name: 'java' };
        case 'json':
          return { grammar: prismObj.languages.json, name: 'json' };
        case 'py':
        case 'python':
          return { grammar: prismObj.languages.python, name: 'python' };
        default:
          return prismObj.languages[lang] ? { grammar: prismObj.languages[lang], name: lang } : null;
      }
    },

    highlight(code, ext) {
      if (!code) return '';
      const prismLang = this.getPrismLanguage(ext);
      const prismObj = typeof window !== 'undefined' ? window.Prism : (typeof Prism !== 'undefined' ? Prism : null);

      // 1. Primary engine: Library syntax highlighting via Prism.js
      if (prismLang && prismLang.grammar && prismObj) {
        try {
          return prismObj.highlight(code, prismLang.grammar, prismLang.name);
        } catch (err) {
          console.warn('[SyntaxHighlighter] Prism highlighting fallback:', err);
        }
      }

      // 2. High-speed Fallback Engine: Dedicated tokenizers for HTML, CSS, JS, JAVA, etc.
      const lang = (ext || '').toLowerCase();
      switch (lang) {
        case 'html':
        case 'htm':
        case 'xml':
        case 'svg':
          return this.highlightHtml(code);
        case 'css':
        case 'scss':
        case 'sass':
        case 'less':
          return this.highlightCss(code);
        case 'js':
        case 'javascript':
        case 'mjs':
        case 'cjs':
          return this.highlightJs(code, false);
        case 'ts':
        case 'mts':
        case 'cts':
        case 'jsx':
        case 'tsx':
        case 'vue':
        case 'svelte':
          return this.highlightJs(code, true);
        case 'java':
        case 'jav':
          return this.highlightJava(code);
        case 'json':
          return this.highlightJson(code);
        case 'py':
        case 'python':
          return this.highlightPython(code);
        default:
          return this.escapeHtml(code);
      }
    },

    highlightHtml(code) {
      const self = this;
      return code.replace(/(<!--[\s\S]*?-->)|(<!DOCTYPE[^>]*>)|(<(?:\/)?)([a-zA-Z0-9\-:]+)([^>]*?)(\/?>)|([^<]+)/gi, (match, comment, doctype, tagStart, tagName, tagAttrs, tagEnd, text) => {
        if (comment) {
          return `<span class="token comment">${self.escapeHtml(comment)}</span>`;
        }
        if (doctype) {
          return `<span class="token keyword">${self.escapeHtml(doctype)}</span>`;
        }
        if (tagName) {
          let highlightedAttrs = '';
          if (tagAttrs) {
            highlightedAttrs = tagAttrs.replace(/([a-zA-Z0-9\-:]+)(?:\s*=\s*(["'][^"']*["']))?/g, (attrMatch, attrName, attrVal) => {
              if (!attrName) return attrMatch;
              let res = ` <span class="token attr-name">${self.escapeHtml(attrName)}</span>`;
              if (attrVal) {
                res += `<span class="token punctuation">=</span><span class="token attr-val">${self.escapeHtml(attrVal)}</span>`;
              }
              return res;
            });
          }
          return `<span class="token punctuation">${self.escapeHtml(tagStart)}</span><span class="token tag">${self.escapeHtml(tagName)}</span>${highlightedAttrs}<span class="token punctuation">${self.escapeHtml(tagEnd)}</span>`;
        }
        if (text) {
          return self.escapeHtml(text);
        }
        return self.escapeHtml(match);
      });
    },

    highlightCss(code) {
      const self = this;
      return code.replace(/(\/\*[\s\S]*?\*\/)|(@[a-zA-Z0-9\-]+)|([a-zA-Z0-9_\-#.:\[\]=\s>+~]+)(?=\{)|([a-zA-Z0-9\-]+)(?=\s*:)|(:\s*)([^;\}]+)|([{}();:])/g, (match, comment, atRule, selector, prop, colonVal, val, punct) => {
        if (comment) return `<span class="token comment">${self.escapeHtml(comment)}</span>`;
        if (atRule) return `<span class="token keyword">${self.escapeHtml(atRule)}</span>`;
        if (selector) return `<span class="token selector">${self.escapeHtml(selector)}</span>`;
        if (prop) return `<span class="token property">${self.escapeHtml(prop)}</span>`;
        if (colonVal) {
          const highlightedVal = val.replace(/(["'][^"']*["'])|(#(?:[0-9a-fA-F]{3,8}))|(\b\d+\.?\d*(?:px|rem|em|%|vh|vw|s|ms|deg)?\b)|(var\(--[a-zA-Z0-9\-]+\))|([a-zA-Z\-]+)/g, (vMatch, str, hex, num, vVar, kw) => {
            if (str) return `<span class="token string">${self.escapeHtml(str)}</span>`;
            if (hex || num) return `<span class="token number">${self.escapeHtml(hex || num)}</span>`;
            if (vVar) return `<span class="token variable">${self.escapeHtml(vVar)}</span>`;
            if (kw) return `<span class="token keyword">${self.escapeHtml(kw)}</span>`;
            return self.escapeHtml(vMatch);
          });
          return `<span class="token punctuation">:</span>${highlightedVal}`;
        }
        if (punct) return `<span class="token punctuation">${self.escapeHtml(punct)}</span>`;
        return self.escapeHtml(match);
      });
    },

    highlightJs(code, isTypeScript = false) {
      const self = this;
      const jsKeywords = 'break|case|catch|class|const|continue|debugger|default|delete|do|else|export|extends|finally|for|function|if|import|in|instanceof|new|return|super|switch|this|throw|try|typeof|var|void|while|with|yield|async|await|of|null|undefined|true|false|from|as';
      const tsKeywords = jsKeywords + '|interface|type|implements|enum|namespace|declare|abstract|public|private|protected|readonly|keyof|infer|never|unknown|any|boolean|number|string|symbol|bigint|void';
      const kwPattern = isTypeScript ? tsKeywords : jsKeywords;

      const regex = new RegExp(
        '(\\/\\*[\\s\\S]*?\\*\\/)' +
        '|(\\/\\/.*)' +
        '|(`(?:\\\\.|[^`\\\\])*`)' +
        '|("(?:\\\\.|[^"\\\\])*")' +
        '|(\'(?:\\\\.|[^\'\\\\])*\')' +
        '|(\\b(?:' + kwPattern + ')\\b)' +
        '|(\\b[A-Z][a-zA-Z0-9_$]*\\b)' +
        '|(\\b\\d+\\.?\\d*(?:[eE][+-]?\\d+)?\\b|0x[0-9a-fA-F]+)' +
        '|([a-zA-Z_$][a-zA-Z0-9_$]*(?=\\s*\\())' +
        '|(=>|===|!==|==|!=|<=|>=|&&|\\|\\||\\?\\?|[-+*/%=&|^~<>!?])' +
        '|([{}[\\],.;:()])',
        'g'
      );

      return code.replace(regex, (match, mComment, sComment, tStr, dStr, sStr, kw, typeName, num, fnCall, op, punct) => {
        if (mComment || sComment) return `<span class="token comment">${self.escapeHtml(mComment || sComment)}</span>`;
        if (tStr || dStr || sStr) return `<span class="token string">${self.escapeHtml(tStr || dStr || sStr)}</span>`;
        if (kw) return `<span class="token keyword">${self.escapeHtml(kw)}</span>`;
        if (typeName) return `<span class="token type">${self.escapeHtml(typeName)}</span>`;
        if (num) return `<span class="token number">${self.escapeHtml(num)}</span>`;
        if (fnCall) return `<span class="token function">${self.escapeHtml(fnCall)}</span>`;
        if (op) return `<span class="token operator">${self.escapeHtml(op)}</span>`;
        if (punct) return `<span class="token punctuation">${self.escapeHtml(punct)}</span>`;
        return self.escapeHtml(match);
      });
    },

    highlightJson(code) {
      const self = this;
      return code.replace(/("(\\u[a-zA-Z0-9]{4}|\\[^u]|[^\\"])*"(\s*:)?|\b(true|false|null)\b|-?\d+(?:\.\d*)?(?:[eE][+\-]?\d+)?|[{}\[\],:])/g, (match) => {
        if (/^"/.test(match)) {
          if (/:$/.test(match)) {
            const key = match.slice(0, -1).trim();
            return `<span class="token property">${self.escapeHtml(key)}</span><span class="token punctuation">:</span>`;
          }
          return `<span class="token string">${self.escapeHtml(match)}</span>`;
        }
        if (/true|false/.test(match)) return `<span class="token number">${self.escapeHtml(match)}</span>`;
        if (/null/.test(match)) return `<span class="token keyword">${self.escapeHtml(match)}</span>`;
        if (/^-?\d/.test(match)) return `<span class="token number">${self.escapeHtml(match)}</span>`;
        if (/[{}\[\],:]/.test(match)) return `<span class="token punctuation">${self.escapeHtml(match)}</span>`;
        return self.escapeHtml(match);
      });
    },

    highlightPython(code) {
      const self = this;
      const pyKeywords = 'def|class|import|from|return|if|elif|else|for|while|try|except|finally|with|as|in|is|not|and|or|lambda|pass|break|continue|yield|None|True|False';
      const regex = new RegExp(
        '(#.*)' +
        '|("""[\\s\\S]*?"""|\'\'\'[\\s\\S]*?\'\'\')' +
        '|("(?:\\\\.|[^"\\\\])*"|\'(?:\\\\.|[^\'\\\\])*\')' +
        '|(\\b(?:' + pyKeywords + ')\\b)' +
        '|(\\b(?:print|range|len|list|dict|set|int|str|float|sys)\\b)' +
        '|(\\b\\d+\\.?\\d*\\b)' +
        '|([a-zA-Z_][a-zA-Z0-9_]*(?=\\s*\\())' +
        '|([{}[\\],.;:()])',
        'g'
      );
      return code.replace(regex, (match, comment, triStr, str, kw, builtin, num, fnCall, punct) => {
        if (comment) return `<span class="token comment">${self.escapeHtml(comment)}</span>`;
        if (triStr || str) return `<span class="token string">${self.escapeHtml(triStr || str)}</span>`;
        if (kw) return `<span class="token keyword">${self.escapeHtml(kw)}</span>`;
        if (builtin) return `<span class="token function">${self.escapeHtml(builtin)}</span>`;
        if (num) return `<span class="token number">${self.escapeHtml(num)}</span>`;
        if (fnCall) return `<span class="token function">${self.escapeHtml(fnCall)}</span>`;
        if (punct) return `<span class="token punctuation">${self.escapeHtml(punct)}</span>`;
        return self.escapeHtml(match);
      });
    },

    highlightJava(code) {
      const self = this;
      const javaKeywords = 'abstract|assert|boolean|break|byte|case|catch|char|class|const|continue|default|do|double|else|enum|exports|extends|final|finally|float|for|goto|if|implements|import|instanceof|int|interface|long|module|native|new|non-sealed|null|open|opens|package|permits|private|protected|provides|public|record|return|sealed|short|static|strictfp|super|switch|synchronized|this|throw|throws|to|transient|transitive|try|uses|var|void|volatile|while|with|yield|true|false';
      const regex = new RegExp(
        '(\\/\\*[\\s\\S]*?\\*\\/)' +
        '|(\\/\\/.*)' +
        '|(@[a-zA-Z_]\\w*(?:\\.[a-zA-Z_]\\w*)*)' +
        '|("""[\\s\\S]*?""")' +
        '|("(?:\\\\.|[^"\\\\])*")' +
        '|(\'(?:\\\\.|[^\'\\\\])*\')' +
        '|(\\b(?:' + javaKeywords + ')\\b)' +
        '|(\\b[A-Z][a-zA-Z0-9_$]*\\b)' +
        '|(\\b(?:0x[0-9a-fA-F]+(?:_[0-9a-fA-F]+)*[lL]?|0b[01]+(?:_[01]+)*[lL]?|\\d+(?:_\\d+)*(?:\\.(?:\\d+(?:_\\d+)*)?)?(?:[eE][+-]?\\d+)?[fFdDlL]?)\\b)' +
        '|([a-zA-Z_$][a-zA-Z0-9_$]*(?=\\s*\\())' +
        '|(->|::|==|!=|<=|>=|&&|\\|\\||\\+\\+|--|[-+*/%=&|^~<>!?])' +
        '|([{}[\\],.;:])',
        'g'
      );
      return code.replace(regex, (match, mComment, sComment, annotation, tStr, dStr, sStr, kw, typeName, num, fnCall, op, punct) => {
        if (mComment || sComment) return `<span class="token comment">${self.escapeHtml(mComment || sComment)}</span>`;
        if (annotation) return `<span class="token annotation">${self.escapeHtml(annotation)}</span>`;
        if (tStr || dStr || sStr) return `<span class="token string">${self.escapeHtml(tStr || dStr || sStr)}</span>`;
        if (kw) return `<span class="token keyword">${self.escapeHtml(kw)}</span>`;
        if (typeName) return `<span class="token class-name">${self.escapeHtml(typeName)}</span>`;
        if (num) return `<span class="token number">${self.escapeHtml(num)}</span>`;
        if (fnCall) return `<span class="token function">${self.escapeHtml(fnCall)}</span>`;
        if (op) return `<span class="token operator">${self.escapeHtml(op)}</span>`;
        if (punct) return `<span class="token punctuation">${self.escapeHtml(punct)}</span>`;
        return self.escapeHtml(match);
      });
    }
  };

  // Expose SyntaxHighlighter globally
  if (typeof window !== 'undefined') {
    window.SyntaxHighlighter = SyntaxHighlighter;
  }

  // =========================================================================
  // 8. ACTIVE FILE SELECTION & MAIN EDITOR SYNC
  // =========================================================================

  function findNodeById(tree, id) {
    for (const item of tree) {
      if (item.id === id) return item;
      if (item.children) {
        const found = findNodeById(item.children, id);
        if (found) return found;
      }
    }
    return null;
  }

  function setActiveFile(node) {
    // If switching away from an unsaved file and auto-save is enabled, flush save
    if (activeFileId && activeFileId !== node.id && isAutoSaveEnabled) {
      const prevNode = findNodeById(fileTreeData, activeFileId);
      if (prevNode && tabDot && tabDot.classList.contains('dirty')) {
        saveCurrentFile(false);
      }
    }

    activeFileId = node.id;

    // Highlight row in tree
    document.querySelectorAll('.tree-row').forEach((r) => {
      r.classList.toggle('active', r.dataset.id === node.id);
    });

    // Update Editor Tab & Code
    editorTabTitle.textContent = node.name;
    const iconData = getFileIcon(node.name, false);
    editorTabIcon.innerHTML = iconData.svg;

    if (tabDot) {
      tabDot.classList.remove('dirty');
    }

    // Update Status Bar
    statusFile.textContent = node.name;
    const dotIdx = node.name.lastIndexOf('.');
    const ext = dotIdx !== -1 ? node.name.substring(dotIdx + 1).toLowerCase() : '';
    statusLang.textContent = ext ? ext.toUpperCase() : 'TEXT';

    // Populate code content with syntax highlighting & line numbers
    const sampleCode = node.content || `// ${node.name}\n// Created inside Linq Studio\n\nfunction main() {\n  return "Ready";\n}\n`;
    codeContent.className = `language-${ext || 'txt'}`;
    codeContent.innerHTML = SyntaxHighlighter.highlight(sampleCode, ext);

    updateLineNumbers(sampleCode);

    // Update Breadcrumb Navigation
    const breadcrumbFolder = document.getElementById('breadcrumbFolder');
    const breadcrumbFile = document.getElementById('breadcrumbFile');
    if (breadcrumbFolder && breadcrumbFile) {
      breadcrumbFile.textContent = node.name;
    }

    // Mobile: automatically close drawer on file selection for immediate editing
    if (window.innerWidth < 640) {
      setTimeout(closeDrawer, 180);
    }
  }

  function triggerHtmlPreview(node) {
    showToast(`🚀 Running ${node.name} on local dev server!`);
    // Optional: open preview or trigger callback
  }

  // Reveal Active File in Explorer
  btnRevealActive.addEventListener('click', () => {
    const activeEl = document.querySelector(`.tree-row[data-id="${activeFileId}"]`);
    if (activeEl) {
      activeEl.scrollIntoView({ behavior: 'smooth', block: 'center' });
      activeEl.style.transition = 'background 0.3s ease';
      activeEl.style.background = 'rgba(56, 189, 248, 0.3)';
      setTimeout(() => {
        activeEl.style.background = '';
      }, 700);
      showToast('Active file revealed in tree');
    }
  });

  // Project Root Header Toggle
  let isRootOpen = true;
  projectRootHeader.addEventListener('click', (e) => {
    if (e.target.closest('.root-overflow')) return;
    isRootOpen = !isRootOpen;
    rootChevron.classList.toggle('open', isRootOpen);
    fileTree.style.display = isRootOpen ? 'flex' : 'none';
  });

  // =========================================================================
  // 8. INLINE CREATOR (New File / New Folder with LIVE Icon Updating)
  // =========================================================================

  function showCreator(mode) {
    creatorMode = mode;
    inlineCreator.classList.remove('hidden');
    creatorInput.value = '';
    creatorInput.placeholder = mode === 'file' ? 'filename.ext (e.g. index.ts)' : 'folder name';

    updateCreatorIcon();
    creatorInput.focus();
    creatorInput.scrollIntoView({ behavior: 'smooth', block: 'nearest' });
  }

  function hideCreator() {
    inlineCreator.classList.add('hidden');
    creatorMode = null;
    creatorInput.value = '';
  }

  function updateCreatorIcon() {
    const name = creatorInput.value.trim();
    if (creatorMode === 'folder') {
      creatorIcon.innerHTML = ICON_SVGS.folderClosed.svg;
    } else {
      const icon = getFileIcon(name || 'untitled.txt', false);
      creatorIcon.innerHTML = icon.svg;
    }
  }

  creatorInput.addEventListener('input', updateCreatorIcon);

  function commitCreation() {
    const rawName = creatorInput.value.trim();
    if (!rawName) {
      hideCreator();
      return;
    }

    const newNode = {
      id: `item-${Date.now()}`,
      name: rawName,
      type: creatorMode,
      isOpen: true,
      children: creatorMode === 'folder' ? [] : undefined,
      content: creatorMode === 'file' ? `// ${rawName}\n` : undefined
    };

    // Prepend to root file tree
    fileTreeData.unshift(newNode);
    renderTree();
    hideCreator();

    showToast(`Created ${creatorMode}: ${rawName}`);

    if (creatorMode === 'file') {
      setActiveFile(newNode);
    }
  }

  btnNewFile.addEventListener('click', () => showCreator('file'));
  btnNewFolder.addEventListener('click', () => showCreator('folder'));
  btnConfirmCreate.addEventListener('click', commitCreation);
  btnCancelCreate.addEventListener('click', hideCreator);

  creatorInput.addEventListener('keydown', (e) => {
    if (e.key === 'Enter') {
      e.preventDefault();
      commitCreation();
    } else if (e.key === 'Escape') {
      hideCreator();
    }
  });

  // Quick Search Filter
  btnToggleSearch.addEventListener('click', () => {
    quickFilterRow.classList.toggle('hidden');
    if (!quickFilterRow.classList.contains('hidden')) {
      quickFilterInput.focus();
    } else {
      filterQuery = '';
      quickFilterInput.value = '';
      renderTree();
    }
  });

  quickFilterInput.addEventListener('input', (e) => {
    filterQuery = e.target.value.toLowerCase().trim();
    renderTree();
  });

  btnCloseFilter.addEventListener('click', () => {
    filterQuery = '';
    quickFilterInput.value = '';
    quickFilterRow.classList.add('hidden');
    renderTree();
  });

  // =========================================================================
  // 9. ROW OVERFLOW MENU (Rename, Duplicate, Delete)
  // =========================================================================

  function showPopupMenu(node, triggerBtn) {
    contextTargetNode = node;
    const rect = triggerBtn.getBoundingClientRect();

    rowPopupMenu.style.top = `${rect.bottom + 4}px`;
    rowPopupMenu.style.left = `${Math.min(rect.left - 100, window.innerWidth - 170)}px`;
    rowPopupMenu.classList.remove('hidden');
  }

  function hidePopupMenu() {
    rowPopupMenu.classList.add('hidden');
    contextTargetNode = null;
  }

  document.addEventListener('click', (e) => {
    if (!rowPopupMenu.contains(e.target) && !e.target.closest('.btn-row-overflow')) {
      hidePopupMenu();
    }
  });

  rowPopupMenu.querySelectorAll('.popup-item').forEach((item) => {
    item.addEventListener('click', () => {
      const action = item.dataset.action;
      if (!contextTargetNode) return;

      const target = contextTargetNode;
      hidePopupMenu();

      if (action === 'rename') {
        promptRename(target);
      } else if (action === 'duplicate') {
        duplicateNode(target);
      } else if (action === 'delete') {
        deleteNode(target);
      }
    });
  });

  function promptRename(node) {
    const newName = prompt(`Rename ${node.name} to:`, node.name);
    if (newName && newName.trim() && newName !== node.name) {
      const oldName = node.name;
      node.name = newName.trim();
      renderTree();
      if (node.id === activeFileId) {
        setActiveFile(node);
      }
      showToast(`Renamed ${oldName} → ${node.name}`);
    }
  }

  function duplicateNode(node) {
    const parts = node.name.split('.');
    let newName;
    if (parts.length > 1 && node.type === 'file') {
      const ext = parts.pop();
      newName = `${parts.join('.')}_copy.${ext}`;
    } else {
      newName = `${node.name}_copy`;
    }

    const copyNode = {
      id: `copy-${Date.now()}`,
      name: newName,
      type: node.type,
      isOpen: node.isOpen,
      content: node.content,
      children: node.children ? JSON.parse(JSON.stringify(node.children)) : undefined
    };

    insertSibling(fileTreeData, node.id, copyNode);
    renderTree();
    showToast(`Duplicated ${node.name}`);
  }

  function deleteNode(node) {
    if (confirm(`Are you sure you want to delete ${node.name}?`)) {
      removeNodeRecursive(fileTreeData, node.id);
      renderTree();
      showToast(`Deleted ${node.name}`);
    }
  }

  function insertSibling(tree, targetId, newNode) {
    for (let i = 0; i < tree.length; i++) {
      if (tree[i].id === targetId) {
        tree.splice(i + 1, 0, newNode);
        return true;
      }
      if (tree[i].children && insertSibling(tree[i].children, targetId, newNode)) {
        return true;
      }
    }
    return false;
  }

  function removeNodeRecursive(tree, targetId) {
    const idx = tree.findIndex((n) => n.id === targetId);
    if (idx !== -1) {
      tree.splice(idx, 1);
      return true;
    }
    for (const item of tree) {
      if (item.children && removeNodeRecursive(item.children, targetId)) {
        return true;
      }
    }
    return false;
  }

  // =========================================================================
  // 10. TOAST NOTIFICATIONS & RUN TRIGGER
  // =========================================================================

  function showToast(message) {
    const toast = document.createElement('div');
    toast.className = 'toast';
    toast.innerHTML = `<span style="color:#38BDF8">●</span> ${message}`;
    toastContainer.appendChild(toast);

    setTimeout(() => {
      toast.style.opacity = '0';
      toast.style.transform = 'translateY(8px)';
      toast.style.transition = 'all 0.25s ease';
      setTimeout(() => toast.remove(), 250);
    }, 2800);
  }

  btnRunActive.addEventListener('click', () => {
    showToast(`Running active script (${statusFile.textContent})...`);
  });

  btnHeaderOverflow.addEventListener('click', () => {
    showToast('Workspace: 10 files, 3 folders (Git: clean)');
  });

  // =========================================================================
  // THEME MANAGEMENT (High-Contrast Light vs Dark Studio Mode)
  // =========================================================================

  const appContainer = document.getElementById('app');
  const btnThemeToggle = document.getElementById('btnThemeToggle');
  const themeToggleIcon = document.getElementById('themeToggleIcon');
  const themeStatusDesc = document.getElementById('themeStatusDesc');
  const themePillLabel = document.getElementById('themePillLabel');

  const SUN_ICON = `<svg viewBox="0 0 24 24" width="20" height="20" stroke="currentColor" stroke-width="2" fill="none"><circle cx="12" cy="12" r="5"/><line x1="12" y1="1" x2="12" y2="3"/><line x1="12" y1="21" x2="12" y2="23"/><line x1="4.22" y1="4.22" x2="5.64" y2="5.64"/><line x1="18.36" y1="18.36" x2="19.78" y2="19.78"/><line x1="1" y1="12" x2="3" y2="12"/><line x1="21" y1="12" x2="23" y2="12"/><line x1="4.22" y1="19.78" x2="5.64" y2="18.36"/><line x1="18.36" y1="5.64" x2="19.78" y2="4.22"/></svg>`;
  const MOON_ICON = `<svg viewBox="0 0 24 24" width="20" height="20" stroke="currentColor" stroke-width="2" fill="none"><path d="M21 12.79A9 9 0 1 1 11.21 3 7 7 0 0 0 21 12.79z"/></svg>`;

  function applyTheme(isLight, notify = true) {
    if (isLight) {
      appContainer.classList.add('light-theme');
      if (themeToggleIcon) themeToggleIcon.innerHTML = MOON_ICON;
      if (themeStatusDesc) themeStatusDesc.textContent = 'High-Contrast Light';
      if (themePillLabel) themePillLabel.textContent = 'Light';
      try { 
        localStorage.setItem('linq_theme', 'light'); 
        localStorage.setItem('novacode-theme', 'light');
      } catch (_) {}
      if (notify) showToast('Switched to High-Contrast Light Mode');
    } else {
      appContainer.classList.remove('light-theme');
      if (themeToggleIcon) themeToggleIcon.innerHTML = SUN_ICON;
      if (themeStatusDesc) themeStatusDesc.textContent = 'Dark Studio';
      if (themePillLabel) themePillLabel.textContent = 'Dark';
      try { 
        localStorage.setItem('linq_theme', 'dark'); 
        localStorage.setItem('novacode-theme', 'dark');
      } catch (_) {}
      if (notify) showToast('Switched to Dark Studio Mode');
    }

    // Refresh active code view so high-contrast token styles immediately take effect
    const curFile = fileTreeData.find((n) => n.id === activeFileId);
    if (curFile) {
      const dotIdx = curFile.name.lastIndexOf('.');
      const ext = dotIdx !== -1 ? curFile.name.substring(dotIdx + 1).toLowerCase() : '';
      codeContent.innerHTML = SyntaxHighlighter.highlight(curFile.content || '', ext);
    }
  }

  if (btnThemeToggle) {
    btnThemeToggle.addEventListener('click', () => {
      const isCurrentlyLight = appContainer.classList.contains('light-theme');
      applyTheme(!isCurrentlyLight);
    });
  }

  // Restore saved theme preference if present
  try {
    const savedTheme = localStorage.getItem('linq_theme') || localStorage.getItem('novacode-theme');
    if (savedTheme === 'light') {
      applyTheme(true, false);
    }
  } catch (_) {}

  // =========================================================================
  // AUTO-SAVE ENGINE & EDITOR REAL-TIME SYNC
  // =========================================================================

  let isAutoSaveEnabled = true;
  let autoSaveTimer = null;
  const btnAutoSaveToggle = document.getElementById('btnAutoSaveToggle');
  const autoSavePillLabel = document.getElementById('autoSavePillLabel');
  const autoSaveSlider = document.getElementById('autoSaveSlider');
  const autoSaveDesc = document.getElementById('autoSaveDesc');
  const tabDot = document.querySelector('.tab-dot');

  // Load saved Auto-Save preference from localStorage (default: true)
  try {
    const savedAutoSave = localStorage.getItem('linq_autosave') || localStorage.getItem('novacode_autosave');
    if (savedAutoSave !== null) {
      isAutoSaveEnabled = savedAutoSave === 'true';
    }
  } catch (_) {}

  function updateAutoSaveUI() {
    if (isAutoSaveEnabled) {
      if (autoSavePillLabel) autoSavePillLabel.textContent = 'On';
      if (autoSaveSlider) {
        autoSaveSlider.classList.add('active');
        autoSaveSlider.style.background = 'var(--accent-green)';
      }
      if (autoSaveDesc) autoSaveDesc.textContent = 'Saves to local storage automatically';
    } else {
      if (autoSavePillLabel) autoSavePillLabel.textContent = 'Off';
      if (autoSaveSlider) {
        autoSaveSlider.classList.remove('active');
        autoSaveSlider.style.background = 'var(--text-muted)';
      }
      if (autoSaveDesc) autoSaveDesc.textContent = 'Auto-save disabled';
    }
  }

  updateAutoSaveUI();

  if (btnAutoSaveToggle) {
    btnAutoSaveToggle.addEventListener('click', () => {
      isAutoSaveEnabled = !isAutoSaveEnabled;
      try {
        localStorage.setItem('linq_autosave', isAutoSaveEnabled.toString());
      } catch (_) {}
      updateAutoSaveUI();
      showToast(isAutoSaveEnabled ? 'Auto-Save enabled' : 'Auto-Save disabled');
    });
  }

  function updateLineNumbers(code) {
    const lines = (code || '').split('\n').length;
    let lineNumHtml = '';
    for (let i = 1; i <= lines; i++) {
      lineNumHtml += `<div class="line-num-row" data-line="${i}">${i}</div>`;
    }
    lineNumbers.innerHTML = lineNumHtml;
    syncLineNumbersScroll();
  }

  function syncLineNumbersScroll() {
    const codeArea = codeContent.closest('.code-area') || codeContent;
    if (codeArea && lineNumbers) {
      lineNumbers.scrollTop = codeArea.scrollTop;
    }
  }

  // Bind scroll event to synchronize lineNumbers with codeContent scrolling
  const codeAreaContainer = codeContent.closest('.code-area');
  if (codeAreaContainer) {
    codeAreaContainer.addEventListener('scroll', syncLineNumbersScroll, { passive: true });
  }

  function saveCurrentFile(showNotification = true) {
    const activeNode = findNodeById(fileTreeData, activeFileId);
    if (!activeNode) return;

    const currentText = codeContent.innerText || codeContent.textContent || '';
    activeNode.content = currentText;

    try {
      localStorage.setItem(`linq_file_${activeNode.id}`, currentText);
      localStorage.setItem('linq_last_saved', Date.now().toString());
      if (showNotification) {
        showToast(`Auto-saved ${activeNode.name}`);
      }
      if (tabDot) {
        tabDot.classList.remove('dirty');
      }
    } catch (e) {
      console.warn('Storage quota or error:', e);
    }
  }

  // Restore saved file contents from localStorage
  function restoreSavedFiles(tree) {
    for (const node of tree) {
      if (node.type === 'file') {
        try {
          const saved = localStorage.getItem(`linq_file_${node.id}`) || localStorage.getItem(`novacode_file_${node.id}`);
          if (saved !== null) {
            node.content = saved;
          }
        } catch (_) {}
      }
      if (node.children) {
        restoreSavedFiles(node.children);
      }
    }
  }

  // Real-time Editor Input Listener
  codeContent.addEventListener('input', () => {
    const currentCode = codeContent.innerText || codeContent.textContent || '';
    updateLineNumbers(currentCode);

    const activeNode = findNodeById(fileTreeData, activeFileId);
    if (activeNode) {
      activeNode.content = currentCode;
    }

    if (tabDot) {
      tabDot.classList.add('dirty');
    }

    if (isAutoSaveEnabled) {
      clearTimeout(autoSaveTimer);
      autoSaveTimer = setTimeout(() => {
        saveCurrentFile(true);
      }, 700);
    }
  });

  // Re-highlight syntax tokens when leaving editor focus
  codeContent.addEventListener('blur', () => {
    const activeNode = findNodeById(fileTreeData, activeFileId);
    if (!activeNode) return;
    const dotIdx = activeNode.name.lastIndexOf('.');
    const ext = dotIdx !== -1 ? activeNode.name.substring(dotIdx + 1).toLowerCase() : '';
    const currentText = codeContent.innerText || codeContent.textContent || '';
    codeContent.className = `language-${ext || 'txt'}`;
    codeContent.innerHTML = SyntaxHighlighter.highlight(currentText, ext);
  });

  // =========================================================================
  // 11. DUAL TERMINAL SYSTEM (Project CLI & Termux Linux Shell)
  // =========================================================================

  // Session Switcher Elements
  const btnTermTabCli = document.getElementById('btnTermTabCli');
  const btnTermTabTermux = document.getElementById('btnTermTabTermux');
  const termSessionCli = document.getElementById('termSessionCli');
  const termSessionTermux = document.getElementById('termSessionTermux');
  const btnClearActiveTerm = document.getElementById('btnClearActiveTerm');
  const btnResetActiveTerm = document.getElementById('btnResetActiveTerm');

  let activeTerminalMode = 'cli'; // 'cli' | 'termux'

  function switchTerminalSession(mode) {
    activeTerminalMode = mode;
    if (mode === 'cli') {
      btnTermTabCli.classList.add('active');
      btnTermTabTermux.classList.remove('active');
      termSessionCli.classList.add('active');
      termSessionTermux.classList.remove('active');
      cliCommandInput.focus();
    } else {
      btnTermTabCli.classList.remove('active');
      btnTermTabTermux.classList.add('active');
      termSessionCli.classList.remove('active');
      termSessionTermux.classList.add('active');
      termuxCommandInput.focus();
    }
  }

  if (btnTermTabCli && btnTermTabTermux) {
    btnTermTabCli.addEventListener('click', () => switchTerminalSession('cli'));
    btnTermTabTermux.addEventListener('click', () => switchTerminalSession('termux'));
  }

  // Clear / Reset Active Terminal
  if (btnClearActiveTerm) {
    btnClearActiveTerm.addEventListener('click', () => {
      if (activeTerminalMode === 'cli') {
        cliOutput.innerHTML = '';
        showToast('Project CLI screen cleared');
      } else {
        termuxOutput.innerHTML = '';
        showToast('Termux screen cleared');
      }
    });
  }

  if (btnResetActiveTerm) {
    btnResetActiveTerm.addEventListener('click', () => {
      if (activeTerminalMode === 'cli') {
        initCliSession();
        showToast('Project CLI restarted');
      } else {
        initTermuxSession();
        showToast('Termux Shell restarted');
      }
    });
  }

  // -------------------------------------------------------------------------
  // TERMINAL 1: PROJECT SCRIPTS CLI
  // -------------------------------------------------------------------------

  const cliOutput = document.getElementById('cliOutput');
  const cliInputForm = document.getElementById('cliInputForm');
  const cliCommandInput = document.getElementById('cliCommandInput');
  const cliChips = document.querySelectorAll('.cli-chip');

  const cliHistory = [];
  let cliHistoryIndex = -1;

  function appendCliLine(html, className = 'term-line') {
    const line = document.createElement('div');
    line.className = className;
    line.innerHTML = html;
    cliOutput.appendChild(line);
    cliOutput.scrollTop = cliOutput.scrollHeight;
  }

  function initCliSession() {
    cliOutput.innerHTML = '';
    appendCliLine('<span class="term-cyan term-bold">Linq Studio Project CLI v2.4.0</span> (Node.js v20.14.0)', 'term-line');
    appendCliLine('<span class="term-dim">Workspace root: ~/project (Type "help" or tap chips above)</span>', 'term-line');
    appendCliLine('');
  }

  function executeCliCommand(rawCmd) {
    const cmd = rawCmd.trim();
    if (!cmd) return;

    // Echo command
    appendCliLine(`<span class="cli-prompt-label">project$</span> <span class="term-bold">${escapeHtml(cmd)}</span>`);
    cliHistory.push(cmd);
    cliHistoryIndex = cliHistory.length;

    const parts = cmd.split(/\s+/);
    const main = parts[0].toLowerCase();
    const arg = parts.slice(1).join(' ').toLowerCase();

    if (cmd === 'help') {
      appendCliLine('<span class="term-yellow term-bold">Available Project Scripts:</span>');
      appendCliLine('  <span class="term-green">npm run dev</span>     - Launch local Vite dev server with HMR');
      appendCliLine('  <span class="term-green">npm test</span>        - Run project test suites (Vitest)');
      appendCliLine('  <span class="term-green">npm run build</span>   - Compile production bundles');
      appendCliLine('  <span class="term-green">npm i [package]</span> - Install project dependencies');
      appendCliLine('  <span class="term-green">node [file]</span>     - Execute JavaScript file');
      appendCliLine('  <span class="term-green">git status</span>      - Check repository status');
      appendCliLine('  <span class="term-green">ls</span> / <span class="term-green">cat [file]</span>   - Browse project workspace files');
      appendCliLine('  <span class="term-green">clear</span>           - Clear CLI output buffer');
    } else if (cmd === 'npm run dev' || cmd === 'npm start') {
      appendCliLine('<span class="term-dim">> linq-studio-mobile@1.0.0 dev</span>');
      appendCliLine('<span class="term-dim">> vite --host 0.0.0.0 --port 5173</span>');
      appendCliLine('');
      appendCliLine('<span class="term-green term-bold">  VITE v5.3.1</span>  <span class="term-dim">ready in 184 ms</span>');
      appendCliLine('');
      appendCliLine('  <span class="term-green">➜</span>  <span class="term-bold">Local:</span>   <span class="term-cyan">http://localhost:5173/</span>');
      appendCliLine('  <span class="term-green">➜</span>  <span class="term-bold">Network:</span> <span class="term-cyan">http://192.168.1.104:5173/</span>');
      appendCliLine('  <span class="term-dim">➜  press h + enter to show help</span>');
      showToast('🚀 Dev server live at http://localhost:5173');
    } else if (cmd === 'npm test' || cmd === 'npm t') {
      appendCliLine('<span class="term-dim">> linq-studio-mobile@1.0.0 test</span>');
      appendCliLine('<span class="term-dim">> vitest run</span>');
      appendCliLine('');
      appendCliLine('<span class="term-green"> ✓</span> tests/explorer.test.ts <span class="term-dim">(4 tests) 18ms</span>');
      appendCliLine('<span class="term-green"> ✓</span> tests/syntax-highlighter.test.ts <span class="term-dim">(6 tests) 22ms</span>');
      appendCliLine('<span class="term-green"> ✓</span> tests/auto-save.test.ts <span class="term-dim">(3 tests) 14ms</span>');
      appendCliLine('');
      appendCliLine('<span class="term-bold"> Test Files</span>  <span class="term-green term-bold">3 passed</span> (3)');
      appendCliLine('<span class="term-bold">      Tests</span>  <span class="term-green term-bold">13 passed</span> (13)');
      appendCliLine('<span class="term-bold">   Duration</span>  <span class="term-dim">214ms</span>');
    } else if (cmd === 'npm run build' || cmd === 'npm build') {
      appendCliLine('<span class="term-dim">> vite build</span>');
      appendCliLine('<span class="term-cyan">vite v5.3.1 building for production...</span>');
      appendCliLine('<span class="term-dim">transforming (28) ...</span>');
      appendCliLine('<span class="term-green">✓ 28 modules transformed.</span>');
      appendCliLine('dist/index.html                   <span class="term-dim">1.48 kB │ gzip: 0.65 kB</span>');
      appendCliLine('dist/assets/style-Dv8.css         <span class="term-dim">6.24 kB │ gzip: 1.82 kB</span>');
      appendCliLine('dist/assets/app-Bg9.js           <span class="term-dim">44.12 kB │ gzip: 13.91 kB</span>');
      appendCliLine('<span class="term-green term-bold">✓ built in 294ms</span>');
      showToast('📦 Production build generated in dist/');
    } else if (main === 'npm' && (parts[1] === 'i' || parts[1] === 'install')) {
      const pkg = parts[2] || 'dependencies';
      appendCliLine(`<span class="term-dim">Fetching ${pkg}...</span>`);
      appendCliLine(`<span class="term-green">+ ${pkg}</span>`);
      appendCliLine('<span class="term-dim">added 18 packages, and audited 164 packages in 1.2s</span>');
      appendCliLine('<span class="term-green">found 0 vulnerabilities</span>');
    } else if (main === 'node') {
      const target = parts[1] || 'app.js';
      appendCliLine(`<span class="term-cyan">[Node.js]</span> Executing ${target}...`);
      appendCliLine('<span class="term-green">Process finished with exit code 0</span>');
    } else if (cmd === 'git status') {
      appendCliLine('On branch <span class="term-green">main</span>');
      appendCliLine('Your branch is up to date with \'origin/main\'.');
      appendCliLine('');
      appendCliLine('nothing to commit, working tree clean');
    } else if (cmd === 'ls') {
      const fileNames = fileTreeData.map((f) => f.type === 'folder' ? `<span class="term-cyan term-bold">${f.name}/</span>` : f.name);
      appendCliLine(fileNames.join('   '));
    } else if (main === 'cat' && parts[1]) {
      const target = findNodeByName(fileTreeData, parts[1]);
      if (target && target.type === 'file') {
        appendCliLine(escapeHtml(target.content || '// Empty file'));
      } else {
        appendCliLine(`<span class="term-red">cat: ${parts[1]}: No such file</span>`);
      }
    } else if (cmd === 'clear') {
      cliOutput.innerHTML = '';
    } else {
      appendCliLine(`<span class="term-red">linq-studio: command not found: ${escapeHtml(cmd)}</span>`);
      appendCliLine('<span class="term-dim">Type "help" to see available commands or tap the quick scripts above.</span>');
    }

    cliOutput.scrollTop = cliOutput.scrollHeight;
  }

  // Quick Chips Click
  cliChips.forEach((chip) => {
    chip.addEventListener('click', () => {
      const cmd = chip.dataset.cmd;
      cliCommandInput.value = '';
      executeCliCommand(cmd);
    });
  });

  // CLI Input Submit
  if (cliInputForm) {
    cliInputForm.addEventListener('submit', (e) => {
      e.preventDefault();
      const val = cliCommandInput.value;
      cliCommandInput.value = '';
      executeCliCommand(val);
    });
  }

  // Key navigation for CLI history
  if (cliCommandInput) {
    cliCommandInput.addEventListener('keydown', (e) => {
      if (e.key === 'ArrowUp') {
        e.preventDefault();
        if (cliHistory.length > 0 && cliHistoryIndex > 0) {
          cliHistoryIndex--;
          cliCommandInput.value = cliHistory[cliHistoryIndex];
        }
      } else if (e.key === 'ArrowDown') {
        e.preventDefault();
        if (cliHistoryIndex < cliHistory.length - 1) {
          cliHistoryIndex++;
          cliCommandInput.value = cliHistory[cliHistoryIndex];
        } else {
          cliHistoryIndex = cliHistory.length;
          cliCommandInput.value = '';
        }
      }
    });
  }

  // -------------------------------------------------------------------------
  // TERMINAL 2: TERMUX / ANDROID LINUX SHELL (BASH)
  // -------------------------------------------------------------------------

  const termuxOutput = document.getElementById('termuxOutput');
  const termuxInputForm = document.getElementById('termuxInputForm');
  const termuxCommandInput = document.getElementById('termuxCommandInput');
  const termuxPromptLabel = document.getElementById('termuxPromptLabel');
  const termuxKeys = document.querySelectorAll('.termux-key');

  let termuxCwd = '~';
  const termuxHistory = [];
  let termuxHistoryIndex = -1;

  function appendTermuxLine(html, className = 'term-line') {
    const line = document.createElement('div');
    line.className = className;
    line.innerHTML = html;
    termuxOutput.appendChild(line);
    termuxOutput.scrollTop = termuxOutput.scrollHeight;
  }

  function getTermuxPrompt() {
    return `u0_a249@localhost:${termuxCwd}$`;
  }

  function updateTermuxPrompt() {
    termuxPromptLabel.textContent = `${getTermuxPrompt()} `;
  }

  function initTermuxSession() {
    termuxOutput.innerHTML = '';
    termuxCwd = '~';
    updateTermuxPrompt();

    appendTermuxLine('<span class="term-green term-bold">Welcome to Termux!</span>');
    appendTermuxLine('<span class="term-dim">Community forum:  https://termux.dev/community</span>');
    appendTermuxLine('<span class="term-dim">Packages search:  pkg search &lt;query&gt;</span>');
    appendTermuxLine('<span class="term-dim">Package install:  pkg install &lt;package&gt;</span>');
    appendTermuxLine('<span class="term-dim">Type </span><span class="term-yellow">neofetch</span><span class="term-dim"> for device specs or </span><span class="term-yellow">help</span><span class="term-dim"> for shell commands.</span>');
    appendTermuxLine('');
  }

  function executeTermuxCommand(rawCmd) {
    const cmd = rawCmd.trim();
    if (!cmd) return;

    // Echo command
    appendTermuxLine(`<span class="termux-prompt-label">${escapeHtml(getTermuxPrompt())}</span> <span class="term-bold">${escapeHtml(cmd)}</span>`);
    termuxHistory.push(cmd);
    termuxHistoryIndex = termuxHistory.length;

    const parts = cmd.split(/\s+/);
    const main = parts[0].toLowerCase();
    const arg = parts.slice(1).join(' ');

    if (cmd === 'help') {
      appendTermuxLine('<span class="term-green term-bold">Termux Android Shell Commands:</span>');
      appendTermuxLine('  <span class="term-yellow">neofetch</span>        - Display system hardware & OS banner');
      appendTermuxLine('  <span class="term-yellow">pkg [install|help]</span> - Termux package manager');
      appendTermuxLine('  <span class="term-yellow">uname -a</span>        - Display Linux kernel version');
      appendTermuxLine('  <span class="term-yellow">whoami</span> / <span class="term-yellow">id</span>      - Display current user & UID');
      appendTermuxLine('  <span class="term-yellow">pwd</span> / <span class="term-yellow">cd [dir]</span>    - Print/change working directory');
      appendTermuxLine('  <span class="term-yellow">ls</span> / <span class="term-yellow">ls -la</span>      - List files & permissions');
      appendTermuxLine('  <span class="term-yellow">cat [file]</span>      - Display file contents');
      appendTermuxLine('  <span class="term-yellow">mkdir</span> / <span class="term-yellow">touch</span>    - Create directory or file');
      appendTermuxLine('  <span class="term-yellow">rm [file]</span>       - Remove file');
      appendTermuxLine('  <span class="term-yellow">ps</span> / <span class="term-yellow">top</span>         - Process list');
      appendTermuxLine('  <span class="term-yellow">free -m</span> / <span class="term-yellow">df -h</span>   - Memory & disk usage');
      appendTermuxLine('  <span class="term-yellow">echo [text]</span>     - Echo text to stdout');
      appendTermuxLine('  <span class="term-yellow">python -V</span> / <span class="term-yellow">node -v</span> - Runtime version checks');
      appendTermuxLine('  <span class="term-yellow">clear</span>           - Clear screen');
    } else if (cmd === 'neofetch' || cmd === 'screenfetch') {
      const neofetchArt = [
        '<span class="term-green">      .-.      </span> <span class="term-green term-bold">u0_a249@localhost</span>',
        '<span class="term-green">     (o.o)     </span> <span class="term-dim">-----------------</span>',
        '<span class="term-green">      |=|      </span> <span class="term-yellow">OS:</span> Android 14 aarch64 (Termux VM)',
        '<span class="term-green">     __|__     </span> <span class="term-yellow">Host:</span> Google Pixel 8 Pro [Mobile IDE]',
        '<span class="term-green">   //.=|=.\\\\   </span> <span class="term-yellow">Kernel:</span> 6.1.75-android14-arm64',
        '<span class="term-green">  // .=|=. \\\\  </span> <span class="term-yellow">Uptime:</span> 3 days, 14 hours, 28 mins',
        '<span class="term-green">  \\\\ .=|=. //  </span> <span class="term-yellow">Packages:</span> 342 (dpkg), 12 (pkg)',
        '<span class="term-green">   \\\\(_=_)//   </span> <span class="term-yellow">Shell:</span> bash 5.2.26-termux',
        '<span class="term-green">    (:| |:)    </span> <span class="term-yellow">Terminal:</span> linq-pty0',
        '<span class="term-green">     || ||     </span> <span class="term-yellow">CPU:</span> Google Tensor G3 (9) @ 2.91GHz',
        '<span class="term-green">     () ()     </span> <span class="term-yellow">Memory:</span> 4.12GiB / 11.58GiB (35%)',
        '',
        '<span class="term-red">███</span><span class="term-green">███</span><span class="term-yellow">███</span><span class="term-blue">███</span><span class="term-purple">███</span><span class="term-cyan">███</span>'
      ];
      neofetchArt.forEach((l) => appendTermuxLine(l));
    } else if (main === 'pkg') {
      if (parts[1] === 'install' || parts[1] === 'in') {
        const pkgName = parts[2] || 'curl';
        appendTermuxLine(`<span class="term-dim">Updating package repository...</span>`);
        appendTermuxLine(`Reading package lists... Done`);
        appendTermuxLine(`Building dependency tree... Done`);
        appendTermuxLine(`The following NEW packages will be installed:`);
        appendTermuxLine(`  <span class="term-green">${pkgName}</span>`);
        appendTermuxLine(`Get:1 https://packages.termux.dev/termux-main stable [148 kB]`);
        appendTermuxLine(`Setting up ${pkgName} (1.4.2)... Done!`);
        showToast(`📦 Installed ${pkgName} in Termux`);
      } else {
        appendTermuxLine('Usage: <span class="term-yellow">pkg install &lt;package&gt;</span> | <span class="term-yellow">pkg upgrade</span> | <span class="term-yellow">pkg search &lt;query&gt;</span>');
      }
    } else if (cmd === 'uname -a') {
      appendTermuxLine('Linux localhost 6.1.75-android-aarch64 #1 SMP PREEMPT aarch64 Android');
    } else if (cmd === 'whoami') {
      appendTermuxLine('u0_a249');
    } else if (cmd === 'id') {
      appendTermuxLine('uid=10249(u0_a249) gid=10249(u0_a249) groups=10249(u0_a249),3003(inet),9997(everybody)');
    } else if (cmd === 'pwd') {
      appendTermuxLine(termuxCwd === '~' ? '/data/data/com.termux/files/home' : `/data/data/com.termux/files/home/${termuxCwd.replace(/^~\/?/, '')}`);
    } else if (main === 'cd') {
      const targetDir = parts[1] || '~';
      if (targetDir === '~' || targetDir === '') {
        termuxCwd = '~';
      } else if (targetDir === '..') {
        if (termuxCwd !== '~') {
          const parts = termuxCwd.split('/');
          parts.pop();
          termuxCwd = parts.join('/') || '~';
        }
      } else {
        const clean = targetDir.replace(/^\//, '');
        termuxCwd = termuxCwd === '~' ? `~/${clean}` : `${termuxCwd}/${clean}`;
      }
      updateTermuxPrompt();
    } else if (main === 'ls') {
      const isLong = cmd.includes('-l');
      if (termuxCwd === '~') {
        if (isLong) {
          appendTermuxLine('<span class="term-dim">total 28</span>');
          appendTermuxLine('drwxr-xr-x 4 u0_a249 u0_a249 4096 Oct  6 03:00 <span class="term-cyan term-bold">project</span>');
          appendTermuxLine('drwxr-xr-x 2 u0_a249 u0_a249 4096 Oct  6 02:40 <span class="term-cyan term-bold">storage</span>');
          appendTermuxLine('-rw-r--r-- 1 u0_a249 u0_a249  820 Oct  6 02:15 <span class="term-green">.bashrc</span>');
          appendTermuxLine('-rw-r--r-- 1 u0_a249 u0_a249 1240 Oct  6 02:22 <span class="term-green">setup.sh</span>');
        } else {
          appendTermuxLine('<span class="term-cyan term-bold">project/</span>   <span class="term-cyan term-bold">storage/</span>   <span class="term-green">.bashrc</span>   <span class="term-green">setup.sh</span>');
        }
      } else {
        // Project files view
        if (isLong) {
          appendTermuxLine('<span class="term-dim">total 48</span>');
          fileTreeData.forEach((node) => {
            const isDir = node.type === 'folder';
            const perm = isDir ? 'drwxr-xr-x' : '-rw-r--r--';
            const size = isDir ? '4096' : (node.content ? node.content.length : '1024');
            const nameSpan = isDir ? `<span class="term-cyan term-bold">${node.name}/</span>` : `<span class="term-green">${node.name}</span>`;
            appendTermuxLine(`${perm} 1 u0_a249 u0_a249 ${size.padStart(5, ' ')} Oct  6 03:00 ${nameSpan}`);
          });
        } else {
          const names = fileTreeData.map((f) => f.type === 'folder' ? `<span class="term-cyan term-bold">${f.name}/</span>` : `<span class="term-green">${f.name}</span>`);
          appendTermuxLine(names.join('   '));
        }
      }
    } else if (main === 'echo') {
      appendTermuxLine(escapeHtml(arg));
    } else if (cmd === 'date') {
      appendTermuxLine(new Date().toUTCString());
    } else if (cmd === 'uptime') {
      appendTermuxLine(' 03:10:00 up 3 days, 14:28,  1 user,  load average: 0.18, 0.24, 0.20');
    } else if (cmd === 'python -v' || cmd === 'python -V' || cmd === 'python --version') {
      appendTermuxLine('Python 3.11.8 (main, Feb 12 2024, 14:52:10) [Clang 17.0.2] on linux');
    } else if (cmd === 'node -v' || cmd === 'node --version') {
      appendTermuxLine('v20.14.0');
    } else if (cmd === 'ps' || cmd === 'top') {
      appendTermuxLine('<span class="term-dim">  PID TTY          TIME CMD</span>');
      appendTermuxLine(' 2814 pts/0    00:00:00 bash');
      appendTermuxLine(' 3120 pts/0    00:00:01 node');
      appendTermuxLine(' 4182 pts/0    00:00:00 ps');
    } else if (cmd === 'free -m') {
      appendTermuxLine('<span class="term-dim">               total        used        free      shared  buff/cache   available</span>');
      appendTermuxLine('Mem:           11860        4210        4820         120        2830        7420');
      appendTermuxLine('Swap:           4096           0        4096');
    } else if (cmd === 'df -h') {
      appendTermuxLine('<span class="term-dim">Filesystem      Size  Used Avail Use% Mounted on</span>');
      appendTermuxLine('/dev/root       128G   42G   86G  33% /');
      appendTermuxLine('/data           128G   18G  110G  14% /data');
    } else if (main === 'mkdir' && parts[1]) {
      appendTermuxLine(`<span class="term-green">Created directory '${parts[1]}'</span>`);
    } else if (main === 'touch' && parts[1]) {
      appendTermuxLine(`<span class="term-green">Created file '${parts[1]}'</span>`);
    } else if (main === 'cat' && parts[1]) {
      const found = findNodeByName(fileTreeData, parts[1]);
      if (found && found.content) {
        appendTermuxLine(escapeHtml(found.content));
      } else {
        appendTermuxLine(`<span class="term-red">cat: ${parts[1]}: No such file or directory</span>`);
      }
    } else if (cmd === 'clear') {
      termuxOutput.innerHTML = '';
    } else {
      appendTermuxLine(`<span class="term-red">bash: ${escapeHtml(main)}: command not found</span>`);
    }

    termuxOutput.scrollTop = termuxOutput.scrollHeight;
  }

  // Termux Extra Keys Click Handlers
  termuxKeys.forEach((keyBtn) => {
    keyBtn.addEventListener('click', () => {
      const char = keyBtn.dataset.char;
      const key = keyBtn.dataset.key;

      if (char) {
        termuxCommandInput.value += char;
        termuxCommandInput.focus();
      } else if (key === 'ESC') {
        termuxCommandInput.value = '';
        termuxCommandInput.focus();
      } else if (key === 'TAB') {
        // Tab Completion
        const val = termuxCommandInput.value.trim();
        const candidates = ['neofetch', 'pkg', 'install', 'uname', 'whoami', 'pwd', 'clear', 'help', 'uptime', 'python', 'node', 'project'];
        const match = candidates.find((c) => c.startsWith(val));
        if (match) {
          termuxCommandInput.value = match + ' ';
        }
        termuxCommandInput.focus();
      } else if (key === 'UP') {
        if (termuxHistory.length > 0 && termuxHistoryIndex > 0) {
          termuxHistoryIndex--;
          termuxCommandInput.value = termuxHistory[termuxHistoryIndex];
        }
        termuxCommandInput.focus();
      } else if (key === 'DOWN') {
        if (termuxHistoryIndex < termuxHistory.length - 1) {
          termuxHistoryIndex++;
          termuxCommandInput.value = termuxHistory[termuxHistoryIndex];
        } else {
          termuxHistoryIndex = termuxHistory.length;
          termuxCommandInput.value = '';
        }
        termuxCommandInput.focus();
      } else if (key === 'CTRL' || key === 'ALT') {
        showToast(`Pressed ${key}`);
        termuxCommandInput.focus();
      }
    });
  });

  // Termux Input Submit
  if (termuxInputForm) {
    termuxInputForm.addEventListener('submit', (e) => {
      e.preventDefault();
      const val = termuxCommandInput.value;
      termuxCommandInput.value = '';
      executeTermuxCommand(val);
    });
  }

  // Termux Arrow Key Listener
  if (termuxCommandInput) {
    termuxCommandInput.addEventListener('keydown', (e) => {
      if (e.key === 'ArrowUp') {
        e.preventDefault();
        if (termuxHistory.length > 0 && termuxHistoryIndex > 0) {
          termuxHistoryIndex--;
          termuxCommandInput.value = termuxHistory[termuxHistoryIndex];
        }
      } else if (e.key === 'ArrowDown') {
        e.preventDefault();
        if (termuxHistoryIndex < termuxHistory.length - 1) {
          termuxHistoryIndex++;
          termuxCommandInput.value = termuxHistory[termuxHistoryIndex];
        } else {
          termuxHistoryIndex = termuxHistory.length;
          termuxCommandInput.value = '';
        }
      }
    });
  }

  function findNodeByName(nodes, name) {
    for (const node of nodes) {
      if (node.name.toLowerCase() === name.toLowerCase()) return node;
      if (node.children) {
        const found = findNodeByName(node.children, name);
        if (found) return found;
      }
    }
    return null;
  }

  function escapeHtml(str) {
    if (!str) return '';
    return str
      .replace(/&/g, '&amp;')
      .replace(/</g, '&lt;')
      .replace(/>/g, '&gt;')
      .replace(/"/g, '&quot;')
      .replace(/'/g, '&#39;');
  }

  // Initialize both terminal sessions
  initCliSession();
  initTermuxSession();

  // Restore any previously auto-saved file contents from localStorage
  restoreSavedFiles(fileTreeData);

  renderTree();

  // Set initial active file (index.html)
  const initialFile = fileTreeData.find((n) => n.id === activeFileId);
  if (initialFile) {
    setActiveFile(initialFile);
  }

  // Open drawer by default on initial page load so user sees the explorer immediately
  setTimeout(openDrawer, 150);

})();
