package models;

public class RegularMember extends User {

    /** Creates a regular user. */
    public RegularMember(String id, String name, String email) {
        super(id, name, email, "Regular");
    }

    /** Regular users can only view and add. */
    @Override
    public boolean canModify() {
        return false;
    }
}
