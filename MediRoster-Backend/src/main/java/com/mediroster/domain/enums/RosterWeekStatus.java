package com.mediroster.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 周排班状态，与 medir_roster_week.status 一致。
 *
 * @author tongguo.li
 */
@Getter
@RequiredArgsConstructor
public enum RosterWeekStatus {
    DRAFT(1),
    PUBLISHED(2);

    private final int code;

    public static RosterWeekStatus fromCode(int code) {
        for (RosterWeekStatus s : values()) {
            if (s.code == code) {
                return s;
            }
        }
        throw new IllegalArgumentException("Unknown roster week status: " + code);
    }
}
