package com.dataparser;

/**
 * Created by kevin on 2/22/17.
 */
public class Candidate {
    private String candidatePartyString;
    private String candidateNameString;
    private int candidateVotesString;
    private Double candidatePercentageDouble;
    private String candidateResultString;
    private String candidateDistrictString;

    Candidate(String party, String name, String votes, String percentage, String district){
        String trimmedName = name.trim();
        this.candidateNameString = trimmedName;
        String trimmedParty = party.trim();
        this.candidatePartyString = trimmedParty;
        String trimmedVotes = votes.trim();
        this.candidateVotesString = Integer.parseInt(trimmedVotes);
        this.candidatePercentageDouble = Double.parseDouble(percentage.replaceAll("%",""));
        if(candidatePercentageDouble >= 50){
            this.candidateResultString = "Won";
        }
        else {
            this.candidateResultString = "Lost";
        }
        String trimmedDistrict = district.trim();
        this.candidateDistrictString = trimmedDistrict;
    }

    public Double getCandidatePercentageDouble() {
        return candidatePercentageDouble;
    }

    public String getCandidateNameString() {
        return candidateNameString;
    }

    public String getCandidatePartyString() {
        return candidatePartyString;
    }

    public String getCandidateResultString() {
        return candidateResultString;
    }

    public int getCandidateVotesString() {
        return candidateVotesString;
    }

    public String getCandidateDistrictString() {
        return candidateDistrictString;
    }
}
