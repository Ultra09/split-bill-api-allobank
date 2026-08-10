package allobank.test.split_bill_api.service;

import allobank.test.split_bill_api.dto.GroupCreateRequest;
import allobank.test.split_bill_api.dto.GroupResponse;
import allobank.test.split_bill_api.model.BillGroup;
import allobank.test.split_bill_api.model.Participant;
import allobank.test.split_bill_api.repository.BillGroupRepository;
import allobank.test.split_bill_api.repository.ParticipantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GroupService {

    private final BillGroupRepository billGroupRepository;
    private final ParticipantRepository participantRepository;
    private final allobank.test.split_bill_api.repository.AuditLogRepository auditLogRepository;

    @Transactional
    public GroupResponse createGroup(GroupCreateRequest request) {
        BillGroup group = new BillGroup();
        group.setName(request.getName());

        for (String participantName : request.getParticipants()) {
            Participant participant = new Participant();
            participant.setName(participantName);
            group.addParticipant(participant);
        }

        BillGroup savedGroup = billGroupRepository.save(group);
        for (Participant p : group.getParticipants()) {
            participantRepository.save(p);
        }

        allobank.test.split_bill_api.model.AuditLog log = new allobank.test.split_bill_api.model.AuditLog();
        log.setBillGroup(savedGroup);
        log.setAction("GROUP_CREATED");
        log.setDescription("Group '" + request.getName() + "' created with " + group.getParticipants().size() + " participants.");
        auditLogRepository.save(log);

        return mapToResponse(savedGroup);
    }

    private GroupResponse mapToResponse(BillGroup group) {
        GroupResponse response = new GroupResponse();
        response.setId(group.getId());
        response.setName(group.getName());
        response.setParticipants(group.getParticipants().stream().map(p -> {
            GroupResponse.ParticipantDto dto = new GroupResponse.ParticipantDto();
            dto.setId(p.getId());
            dto.setName(p.getName());
            return dto;
        }).collect(Collectors.toList()));
        return response;
    }
}
