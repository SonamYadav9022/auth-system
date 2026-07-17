package com.sonam.authsystem.entity;

public enum AuditEventType {
    REGISTER,
    LOGIN_SUCCESS,
    LOGIN_FAILURE,
    ACCOUNT_LOCKED,
    TOKEN_REFRESH,
    TOKEN_REUSE_DETECTED,
    LOGOUT,
    USER_DELETED
}
