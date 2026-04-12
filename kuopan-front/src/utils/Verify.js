const regs = {
    // 邮箱校验：支持标准邮箱格式，允许点号、连字符
    email: /^[\w-]+(\.[\w-]+)*@[\w-]+(\.[\w-]+)+$/,

    // 数字校验：匹配 0 或者不以 0 开头的正整数
    number: /^([0]|[1-9][0-9]*)$/,

    // 密码校验：至少8位，必须包含数字和字母，允许特殊字符 ~!@#$%^&*_
    password: /^(?=.*\d)(?=.*[a-zA-Z])[\da-zA-Z~!@#$%^&*_]{8,}$/,

    // 分享码校验：由字母和数字组成的字符串
    shareCode: /^[A-Za-z0-9]+$/
};

const verify = (rule, value, reg, callback) => {
    if (value) {
        if (reg.test(value)) {
            callback()
        } else {
            callback(new Error(rule.message))
        }
    } else {
        callback()
    }
}
export default {
    email: (rule, value, callback) => {
        return verify(rule, value, regs.email, callback)
    },
    number: (rule, value, callback) => {
        return verify(rule, value, regs.number, callback)
    },
    password: (rule, value, callback) => {
        return verify(rule, value, regs.password, callback)
    },
    shareCode: (rule, value, callback) => {
        return verify(rule, value, regs.shareCode, callback)
    }
}