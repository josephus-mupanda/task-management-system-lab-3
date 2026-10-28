package models;

public class AdminMember extends User {

    /** Creates an admin user. */
    public AdminMember(String id, String name, String email) {
        super(id, name, email, "Admin");
    }

    /** Admins can do everything. */
    @Override
    public boolean canModify() {
        return true;
    }
}
