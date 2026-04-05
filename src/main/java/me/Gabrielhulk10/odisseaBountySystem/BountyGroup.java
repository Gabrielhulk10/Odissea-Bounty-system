package me.Gabrielhulk10.odisseaBountySystem;
import java.util.ArrayList;
import java.util.List;

public class BountyGroup {

    private String leader;
    private List<String> members = new ArrayList<>();
    private List<String> invites = new ArrayList<>();

    public BountyGroup(String leader) {
        this.leader = leader;
        members.add(leader);
    }

    public String getLeader() {
        return leader;
    }

    public List<String> getMembers() {
        return members;
    }

    public List<String> getInvites() {
        return invites;
    }
}