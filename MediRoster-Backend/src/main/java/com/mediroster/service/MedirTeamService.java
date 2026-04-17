package com.mediroster.service;

import com.mediroster.common.i18n.I18nPreconditions;
import com.mediroster.dto.request.TeamUpsertRequest;
import com.mediroster.dto.response.TeamResponse;
import com.mediroster.entity.MedirTeam;
import com.mediroster.mapper.MedirTeamMapper;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 班组。
 *
 * @author tongguo.li
 */
@Service
@RequiredArgsConstructor
public class MedirTeamService {

    private static final String NOT_FOUND = "NOT_FOUND";

    private final MedirTeamMapper teamMapper;

    public List<TeamResponse> listAll() {
        return teamMapper.findAll().stream().map(this::toResponse).toList();
    }

    public TeamResponse getById(Long id) {
        MedirTeam t = I18nPreconditions.checkNotNull(teamMapper.findById(id), NOT_FOUND, "error.team.notFound");
        return toResponse(t);
    }

    @Transactional
    public TeamResponse create(TeamUpsertRequest req) {
        MedirTeam t = new MedirTeam();
        t.setTeamCode(req.teamCode());
        t.setTeamName(req.teamName());
        t.setDescription(req.description());
        t.setEnabled(req.enabled() != null ? req.enabled() : 1);
        teamMapper.insert(t);
        return toResponse(teamMapper.findById(t.getId()));
    }

    @Transactional
    public TeamResponse update(Long id, TeamUpsertRequest req) {
        MedirTeam t = I18nPreconditions.checkNotNull(teamMapper.findById(id), NOT_FOUND, "error.team.notFound");
        t.setTeamCode(req.teamCode());
        t.setTeamName(req.teamName());
        t.setDescription(req.description());
        if (req.enabled() != null) {
            t.setEnabled(req.enabled());
        }
        teamMapper.updateById(t);
        return toResponse(teamMapper.findById(id));
    }

    @Transactional
    public void delete(Long id) {
        I18nPreconditions.checkNotNull(teamMapper.findById(id), NOT_FOUND, "error.team.notFound");
        teamMapper.deleteById(id);
    }

    private TeamResponse toResponse(MedirTeam t) {
        return new TeamResponse(
                t.getId(), t.getTeamCode(), t.getTeamName(), t.getDescription(),
                t.getEnabled(), t.getCreatedAt(), t.getUpdatedAt());
    }
}
