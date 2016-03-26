package cz.meteocar.unit.engine.storage.model;

/**
 * DTC Entity.
 */
public class DTCEntity extends AbstractEntity {

    private String tripId;
    private String dtcCode;
    private Long time;
    private boolean posted;

    /**
     * Trip hash identification.
     *
     * @return trip id
     */
    public String getTripId() {
        return tripId;
    }

    public void setTripId(String tripId) {
        this.tripId = tripId;
    }

    /**
     * DTC code.
     *
     * @return dtc code.
     */
    public String getDtcCode() {
        return dtcCode;
    }

    public void setDtcCode(String dtcCode) {
        this.dtcCode = dtcCode;
    }

    /**
     * Time when DTC was captured.
     *
     * @return time in milliseconds.
     */
    public Long getTime() {
        return time;
    }

    public void setTime(Long time) {
        this.time = time;
    }

    /**
     * @return True if messages was posted successfully on server.
     */
    public boolean isPosted() {
        return posted;
    }

    public void setPosted(boolean posted) {
        this.posted = posted;
    }
}
