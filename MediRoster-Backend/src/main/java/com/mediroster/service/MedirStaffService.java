package com.mediroster.service;

import static com.mediroster.common.exception.BusinessException.*;

import com.mediroster.common.i18n.I18nPreconditions;
import com.mediroster.dto.request.StaffUpsertRequest;
import com.mediroster.dto.response.StaffResponse;
import com.mediroster.entity.MedirStaff;
import com.mediroster.mapper.MedirStaffMapper;
import com.mediroster.mapper.MedirTeamMapper;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 人员。
 *
 * @author tongguo.li
 */
@Service
@RequiredArgsConstructor
public class MedirStaffService {


    private final MedirStaffMapper staffMapper;
    private final MedirTeamMapper teamMapper;

    public List<StaffResponse> listByTeam(Long teamId, boolean includeDeleted) {
        ensureTeam(teamId);
        return staffMapper.findByTeamId(teamId, includeDeleted).stream().map(this::toResponse).toList();
    }

    public StaffResponse getById(Long id) {
        MedirStaff s = staffMapper.findById(id);
        I18nPreconditions.checkArgument(
                s != null && s.getDeletedAt() == null, NOT_FOUND, "error.staff.notFound");
        return toResponse(s);
    }

    @Transactional
    public StaffResponse create(StaffUpsertRequest req) {
        ensureTeam(req.teamId());
        MedirStaff s = new MedirStaff();
        apply(s, req);
        staffMapper.insert(s);
        return toResponse(staffMapper.findById(s.getId()));
    }

    @Transactional
    public StaffResponse update(Long id, StaffUpsertRequest req) {
        MedirStaff s = staffMapper.findById(id);
        I18nPreconditions.checkArgument(
                s != null && s.getDeletedAt() == null, NOT_FOUND, "error.staff.notFound");
        ensureTeam(req.teamId());
        apply(s, req);
        staffMapper.updateById(s);
        return toResponse(staffMapper.findById(id));
    }

    @Transactional
    public void softDelete(Long id) {
        MedirStaff s = staffMapper.findById(id);
        I18nPreconditions.checkArgument(
                s != null && s.getDeletedAt() == null, NOT_FOUND, "error.staff.notFound");
        staffMapper.softDeleteById(id);
    }

    private void ensureTeam(Long teamId) {
        I18nPreconditions.checkNotNull(teamMapper.findById(teamId), NOT_FOUND, "error.team.notFound");
    }

    private void apply(MedirStaff s, StaffUpsertRequest req) {
        s.setTeamId(req.teamId());
        s.setName(req.name());
        s.setEmployeeNo(req.employeeNo());
        s.setPhone(req.phone());
        s.setEmail(req.email());
        s.setMemberType(req.memberType());
        s.setSortOrder(req.sortOrder());
        s.setStatus(req.status());
        s.setFixedPostId(req.fixedPostId());
        s.setRemark(req.remark());
    }

    private StaffResponse toResponse(MedirStaff s) {
        return new StaffResponse(
                s.getId(), s.getTeamId(), s.getName(), s.getEmployeeNo(), s.getPhone(), s.getEmail(),
                s.getMemberType(), s.getSortOrder(), s.getStatus(), s.getFixedPostId(), s.getRemark(),
                s.getDeletedAt(), s.getCreatedAt(), s.getUpdatedAt());
    }
}
