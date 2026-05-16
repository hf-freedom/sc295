module.exports = {
  devServer: {
    port: 3008,
    proxy: {
      '/api': {
        target: 'http://localhost:8008',
        changeOrigin: true
      }
    }
  }
}