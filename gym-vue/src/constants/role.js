/**
 * 用户角色常量定义。
 * ROLE.ADMIN 对应管理员，ROLE.USER 对应普通用户。
 * 与后端 SysUser.role 字段值保持一致。
 */

export const ROLE = {
  ADMIN: 'admin',
  USER: 'user'
}

export const ROLE_LABEL = {
  [ROLE.ADMIN]: '管理员',
  [ROLE.USER]: '普通用户'
}
