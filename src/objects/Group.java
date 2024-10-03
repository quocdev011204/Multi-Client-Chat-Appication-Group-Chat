package objects;

import java.util.HashSet;

public class Group {
    private String groupName;
    private HashSet<String> members;

    public Group(String groupName) {
        this.groupName = groupName;
        this.members = new HashSet<>();
    }

    public void addMember(String member) {
        members.add(member);
    }

    public void removeMember(String member) {
        members.remove(member);
    }

    public HashSet<String> getMembers() {
        return members;
    }

    public String getGroupName() {
        return groupName;
    }
}
