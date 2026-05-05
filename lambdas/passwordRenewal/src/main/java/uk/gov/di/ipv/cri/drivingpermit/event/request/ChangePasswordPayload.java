package uk.gov.di.ipv.cri.drivingpermit.event.request;

public record ChangePasswordPayload(String userName, String password, String newPassword) {

    public static ChangePasswordPayloadBuilder builder() {
        return new ChangePasswordPayloadBuilder();
    }

    public static class ChangePasswordPayloadBuilder {
        private String userName;
        private String password;
        private String newPassword;

        ChangePasswordPayloadBuilder() {}

        public ChangePasswordPayloadBuilder userName(String userName) {
            this.userName = userName;
            return this;
        }

        public ChangePasswordPayloadBuilder password(String password) {
            this.password = password;
            return this;
        }

        public ChangePasswordPayloadBuilder newPassword(String newPassword) {
            this.newPassword = newPassword;
            return this;
        }

        public ChangePasswordPayload build() {
            return new ChangePasswordPayload(this.userName, this.password, this.newPassword);
        }
    }
}
