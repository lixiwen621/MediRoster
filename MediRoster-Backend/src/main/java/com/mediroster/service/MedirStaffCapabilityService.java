package com.mediroster.service;

import static com.mediroster.common.exception.BusinessException.*;

import com.mediroster.common.i18n.I18nPreconditions;
import com.mediroster.dto.request.StaffCapabilityUpsertRequest;
import com.mediroster.dto.response.StaffCapabilityResponse;
import com.mediroster.entity.MedirStaff;
import com.mediroster.entity.MedirStaffCapability;
import com.mediroster.mapper.MedirStaffCapabilityMapper;
import com.mediroster.mapper.MedirStaffMapper;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 人员能力标签。
 *
 * @author tongguo.li
 */
@Service
@RequiredArgsConstructor
public class MedirStaffCapabilityService {


    private final MedirStaffCapabilityMapper capabilityMapper;
    private final MedirStaffMapper staffMapper;

    public List<StaffCapabilityResponse> listByStaff(Long staffId) {
        ensureStaff(staffId);
        return capabilityMapper.findByStaffId(staffId).stream().map(this::toResponse).toList();
    }

    @Transactional
    public StaffCapabilityResponse add(Long staffId, StaffCapabilityUpsertRequest req) {
        ensureStaff(staffId);
        MedirStaffCapability c = new MedirStaffCapability();
        c.setStaffId(staffId);
        c.setCapabilityCode(req.capabilityCode());
        c.setEnabled(req.enabled());
        capabilityMapper.insert(c);
        MedirStaffCapability saved = capabilityMapper.findById(c.getId());
        return toResponse(saved);
    }

    @Transactional
    public void delete(Long staffId, Long capabilityId) {
        ensureStaff(staffId);
        MedirStaffCapability c = capabilityMapper.findByStaffId(staffId).stream()
                .filter(x -> x.getId().equals(capabilityId))
                .findFirst()
                .orElse(null);
        I18nPreconditions.checkNotNull(c, NOT_FOUND, "error.capability.notFound");
        capabilityMapper.deleteById(capabilityId);
    }

    private void ensureStaff(Long staffId) {
        MedirStaff s = staffMapper.findById(staffId);
        I18nPreconditions.checkArgument(
                s != null && s.getDeletedAt() == null, NOT_FOUND, "error.staff.notFound");
    }

    private StaffCapabilityResponse toResponse(MedirStaffCapability c) {
        return new StaffCapabilityResponse(
                c.getId(), c.getStaffId(), c.getCapabilityCode(), c.getEnabled(),
                c.getCreatedAt(), c.getUpdatedAt());
    }
}
