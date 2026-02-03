import request from '../utils/request'

export function updateProfile(data: any) {
    return request({
        url: '/user/updateProfile',
        method: 'post',
        data
    })
}

export function uploadFile(formData: FormData) {
    return request({
        url: '/file/upload',
        method: 'post',
        data: formData,
        headers: {
            'Content-Type': 'multipart/form-data'
        }
    })
}
