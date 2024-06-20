package com.quan.enumeration;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 数据报类型
 * @author Quan
 */
@AllArgsConstructor
@Getter
public enum PackageType {
    REQUEST_PACKAGE(0),
    RESPONSE_PACKAGE(1);

    private final int code;
}
