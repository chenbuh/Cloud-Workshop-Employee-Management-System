import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import os from 'os'

function getNetworkIp() {
    const interfaces = os.networkInterfaces()
    for (const name of Object.keys(interfaces)) {
        for (const iface of interfaces[name]!) {
            if (iface.family === 'IPv4' && !iface.internal) {
                if (!name.includes('vEthernet') && !name.includes('Virtual') && !iface.address.startsWith('198.18')) {
                    return iface.address
                }
            }
        }
    }
    return '0.0.0.0'
}

const networkIp = getNetworkIp()

// https://vitejs.dev/config/
export default defineConfig({
    plugins: [vue()],
    server: {
        host: networkIp,
        port: 3000,
        strictPort: true,
        proxy: {
            '/api': {
                target: 'http://localhost:8080',
                changeOrigin: true
            }
        }
    }
})
