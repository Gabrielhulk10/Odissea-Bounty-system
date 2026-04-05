package me.Gabrielhulk10.odisseaBountySystem;

public class Bounty {

    private String issuer;
    private String victim;
    private int amount;
    private String hunter;

    public String getIssuer() {
        return issuer;
    }

    public String getVictim() {
        return victim;
    }

    public int getAmount() {
        return amount;
    }

    public String getHunter() {
        return hunter;
    }

    public Bounty(String issuer, String victim, int amount, String hunter) {
        this.issuer = issuer;
        this.victim = victim;
        this.amount = amount;
        this.hunter = hunter;
    }
}