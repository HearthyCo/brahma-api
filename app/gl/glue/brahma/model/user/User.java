package gl.glue.brahma.model.user;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;
import org.mindrot.jbcrypt.BCrypt;
import play.libs.Json;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.Date;

@NamedQueries({

        @NamedQuery(
                name = "User.findByLogin",
                query = "select x from User x where x.login = :login"
        ),
        @NamedQuery(
                name = "User.findByEmail",
                query = "select x from User x where x.email = :email"
        )

})
@Entity
@Inheritance
@DiscriminatorColumn(name = "type")
public abstract class User {

    public enum Gender {MALE, FEMALE, OTHER}


    @Id
    @SequenceGenerator(name = "user_id_seq", sequenceName = "user_id_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "user_id_seq")
    private int id;

    private String login;

    private String password;

    @NotNull
    private String email;

    @NotNull
    @JsonIgnore
    private boolean canLogin = true;

    @NotNull
    private String name;

    private String surname1;

    private String surname2;

    @NotNull
    @JsonFormat(shape=JsonFormat.Shape.STRING, pattern="yyyy-MM-dd")
    private Date birthdate;

    private String avatar;

    private String nationalId;

    @Enumerated(EnumType.STRING)
    @NotNull
    private Gender gender;

    @NotNull
    private int balance = 0;

    @JsonIgnore
    private Date onlineLimit;

    @NotNull // Fake to prevent typing bug
    private String meta = "{}";

    @Transient
    private JsonNode metaParsed; // Cache for meta parsing


    public int getId() {
        return id;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
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

    public int getBalance() { return this.balance; }

    public void setBalance(int balance) { this.balance = balance; }

    public Date getOnlineLimit() {
        return onlineLimit;
    }

    public void setOnlineLimit(Date onlineLimit) {
        this.onlineLimit = onlineLimit;
    }

    public JsonNode getMeta() {
        if (metaParsed == null) {
            metaParsed = meta == null ? Json.newObject() : Json.parse(meta);
        }
        return metaParsed;
    }

    public void setMeta(JsonNode meta) {
        this.metaParsed = meta;
        this.meta = meta == null ? "{}" : meta.toString();
    }

    @Override
    public String toString() {
        String fullname = name + " " + surname1;
        if (surname2 != null) {
            fullname += " " + surname2;
        }
        return fullname;
    }

    public void merge(User updated) {
        if (updated.getGender() != null) this.setGender(updated.getGender());
        if (updated.getName() != null) this.setName(updated.getName());
        if (updated.getBirthdate() != null) this.setBirthdate(updated.getBirthdate());
        if (updated.getSurname1() != null) this.setSurname1(updated.getSurname1());
        if (updated.getSurname2() != null) this.setSurname2(updated.getSurname2());
        if (updated.getAvatar() != null) this.setAvatar(updated.getAvatar());
        if (updated.getNationalId() != null) this.setNationalId(updated.getNationalId());
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

    public abstract String getType();

}
