package gl.glue.brahma.model.user;

import javax.persistence.*;
import java.util.Date;

@Entity
@Inheritance
@DiscriminatorColumn(name="type")
public abstract class User {

    public enum Gender {MALE, FEMALE, OTHER}

    @Id
    @SequenceGenerator(name="user_id_seq", sequenceName="user_id_seq", allocationSize=1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator="user_id_seq")
    private int id;

    private String login;

    private String password;

    private boolean canLogin;

    private String name;

    private String surname1;

    private String surname2;

    private Date birthdate;

    private String avatar;

    private String nationalId;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    private Date onlineLimit;

    private String meta;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isCanLogin() {
        return canLogin;
    }

    public void setCanLogin(boolean canLogin) {
        this.canLogin = canLogin;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSurname1() {
        return surname1;
    }

    public void setSurname1(String surname1) {
        this.surname1 = surname1;
    }

    public String getSurname2() {
        return surname2;
    }

    public void setSurname2(String surname2) {
        this.surname2 = surname2;
    }

    public Date getBirthdate() {
        return birthdate;
    }

    public void setBirthdate(Date birthdate) {
        this.birthdate = birthdate;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getNationalId() {
        return nationalId;
    }

    public void setNationalId(String nationalId) {
        this.nationalId = nationalId;
    }

    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    public Date getOnlineLimit() {
        return onlineLimit;
    }

    public void setOnlineLimit(Date onlineLimit) {
        this.onlineLimit = onlineLimit;
    }

    public String getMeta() {
        return meta;
    }

    public void setMeta(String meta) {
        this.meta = meta;
    }

    public String toString() {
        String fullname = name + " " + surname1;
        if (surname2 != null) {
            fullname += " " + surname2;
        }
        return fullname;
    }

}
