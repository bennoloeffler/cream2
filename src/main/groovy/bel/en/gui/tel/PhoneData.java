package bel.en.gui.tel;

public class PhoneData {
    private String historyEntry;
    private String callTodoEntry;
    private String callTime;
    private String callDate;
    private String completeHistory;

    public PhoneData() {
    }

    public String getHistoryEntry() {
        return historyEntry;
    }

    public void setHistoryEntry(final String historyEntry) {
        this.historyEntry = historyEntry;
    }

    public String getCallTodoEntry() {
        return callTodoEntry;
    }

    public void setCallTodoEntry(final String callTodoEntry) {
        this.callTodoEntry = callTodoEntry;
    }

    public String getCallTime() {
        return callTime;
    }

    public void setCallTime(final String callTime) {
        this.callTime = callTime;
    }

    public String getCallDate() {
        return callDate;
    }

    public void setCallDate(final String callDate) {
        this.callDate = callDate;
    }

    public String getCompleteHistory() {
        return completeHistory;
    }

    public void setCompleteHistory(final String completeHistory) {
        this.completeHistory = completeHistory;
    }
}