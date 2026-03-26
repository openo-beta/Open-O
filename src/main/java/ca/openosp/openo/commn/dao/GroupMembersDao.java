//CHECKSTYLE:OFF



package ca.openosp.openo.commn.dao;

import ca.openosp.openo.commn.model.GroupMembers;
import ca.openosp.openo.messenger.data.ContactIdentifier;

import java.util.List;

public interface GroupMembersDao extends AbstractDao<GroupMembers> {

    public List<GroupMembers> findRemoteByGroupId(int groupId);

    public List<GroupMembers> findLocalByGroupId(int groupId);

    public List<GroupMembers> findByGroupId(int groupId);

    public List<Object[]> findMembersByGroupId(int groupId);

    public List<GroupMembers> findByProviderNumberAndFacilityId(String providerNo, Integer facilityId);

    public List<GroupMembers> findGroupMember(String providerNo, int groupId);

    public List<GroupMembers> findByFacilityId(Integer facilityId);

    public GroupMembers findByIdentity(ContactIdentifier contactIdentifier);
}
