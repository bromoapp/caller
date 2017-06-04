package com.litaal.caller.dto;

/**
 * Created by sadmin on 6/3/2017.
 */

public class CandidateSignalDTO {

    private Candidate candidate;

    public CandidateSignalDTO(String candidate, String sdpMid, int sdpMLineIndex) {
        this.candidate = new Candidate(candidate, sdpMid, sdpMLineIndex);
    }

    public Candidate getCandidate() {
        return candidate;
    }

    public void setCandidate(Candidate candidate) {
        this.candidate = candidate;
    }

    public class Candidate {
        private String candidate;
        private String sdpMid;
        private int sdpMLineIndex;

        public Candidate(String candidate, String sdpMid, int sdpMLineIndex) {
            this.candidate = candidate;
            this.sdpMid = sdpMid;
            this.sdpMLineIndex = sdpMLineIndex;
        }

        public String getCandidate() {
            return candidate;
        }

        public void setCandidate(String candidate) {
            this.candidate = candidate;
        }

        public String getSdpMid() {
            return sdpMid;
        }

        public void setSdpMid(String sdpMid) {
            this.sdpMid = sdpMid;
        }

        public int getSdpMLineIndex() {
            return sdpMLineIndex;
        }

        public void setSdpMLineIndex(int sdpMLineIndex) {
            this.sdpMLineIndex = sdpMLineIndex;
        }
    }
}
