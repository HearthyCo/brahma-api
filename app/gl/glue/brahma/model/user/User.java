package gl.glue.brahma.model.user;

import org.mindrot.jbcrypt.BCrypt;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
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

    @NotNull
    private String login;

    private String password;

    @NotNull
    private boolean canLogin;

    @NotNull
    private String name;

    private String surname1;

    private String surname2;

    @NotNull
    private Date birthdate;

    private String avatar;

    private String nationalId;

    @Enumerated(EnumType.STRING)
    @NotNull
    private Gender gender;

    private Date onlineLimit;

    @NotNull // Fake to prevent typing bug
    private String meta;


    public int getId() {
        return id;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public boolean canLogin() {
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


    @Override
    public String toString() {
        String fullname = name + " " + surname1;
        if (surname2 != null) {
            fullname += " " + surname2;
        }
        return fullname;
    }

    public void setPassword(String password) {
        this.password = BCrypt.hashpw(password, BCrypt.gensalt());
    }

    public boolean authenticate(String password) {
        if (!canLogin || password == null) {
            return false;
        }
        return BCrypt.checkpw(password, this.password);
    }

}
