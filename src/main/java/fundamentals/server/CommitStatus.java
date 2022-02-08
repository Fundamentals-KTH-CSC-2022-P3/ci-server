package fundamentals.server;

/**
 * The four different statuses that can be assigned to each Github commit.
 */
public enum CommitStatus {
    ERROR("error"),
    FAILURE("failure"),
    PENDING("pending"),
    SUCCESS("success");

    final String name;

    private CommitStatus(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}