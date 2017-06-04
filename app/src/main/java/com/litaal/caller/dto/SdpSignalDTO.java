package com.litaal.caller.dto;

/**
 * Created by sadmin on 6/3/2017.
 */

public class SdpSignalDTO {

    private Sdp sdp;

    public SdpSignalDTO() {}

    public SdpSignalDTO(String type, String sdp) {
        this.sdp = new Sdp(type, sdp);
    }

    public Sdp getSdp() {
        return sdp;
    }

    public void setSdp(Sdp sdp) {
        this.sdp = sdp;
    }

    public class Sdp {
        private String type;
        private String sdp;

        public Sdp(String type, String sdp) {
            this.type = type;
            this.sdp = sdp;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getSdp() {
            return sdp;
        }

        public void setSdp(String sdp) {
            this.sdp = sdp;
        }
    }
}
