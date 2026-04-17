package com.mediroster.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 人员状态，与 medir_staff.status 一致。
 *
 * @author tongguo.li
 */
@Getter
@RequiredArgsConstructor
public enum StaffStatus {
    ACTIVE(1),
    INACTIVE(2);

    private final int code;

    public static StaffStatus fromCode(int code) {
        for (StaffStatus s : values()) {
            if (s.code == code) {
                return s;
            }
        }
        throw new IllegalArgumentException("Unknown staff status: " + code);
    }
}
