import axios from 'axios'
import { createDiscreteApi } from 'naive-ui'

const { message } = createDiscreteApi(['message'])

const service = axios.create({
    baseURL: '/api/v1',
    timeout: 5000
})

service.interceptors.request.use(
    (config) => {
        const token = localStorage.getItem('token')
        if (token) {
            config.headers['Authorization'] = 'Bearer ' + token
        }
        return config
    },
    (error) => {
        return Promise.reject(error)
    }
)

service.interceptors.response.use(
    (response) => {
        const res = response.data
        if (res.code !== 200) {
            message.error(res.msg || 'Error')
            if (res.code === 401) {
                localStorage.removeItem('token') // Clear invalid token
                // Optional: Redirect to login
            }
            return Promise.reject(new Error(res.msg || 'Error'))
        } else {
            return res.data
        }
    },
    (error) => {
        message.error(error.message || 'Network Error')
        return Promise.reject(error)
    }
)

export default service
