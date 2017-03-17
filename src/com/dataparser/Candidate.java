package com.dataparser;

//This class was originally designed to take information about the candidates and put them into a class that could be
//However, it is no longer used, as converting the data to a CSV file has proven more successful.
public class Candidate {
    private String candidatePartyString;
    private String candidateNameString;
    private int candidateVotesString;
    private Double candidatePercentageDouble;
    private String candidateResultString;
    private String candidateDistrictString;

    Candidate(String party, String name, String votes, String percentage, String district){
        this.candidateNameString = name.trim();
        this.candidatePartyString = party.trim();
        this.candidateVotesString = Integer.parseInt(votes.trim());
        this.candidatePercentageDouble = Double.parseDouble(percentage.replaceAll("%",""));
        if(candidatePercentageDouble >= 50){
            this.candidateResultString = "Won";
        }
        else {
            this.candidateResultString = "Lost";
        }
        this.candidateDistrictString = district.trim();
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
