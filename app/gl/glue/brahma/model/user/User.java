package gl.glue.brahma.model.user;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import gl.glue.brahma.util.JsonUtils;
import gl.glue.brahma.util.serializers.UserMetaCleanerSerializer;
import gl.glue.brahma.util.serializers.UserPasswordSerializer;
import org.mindrot.jbcrypt.BCrypt;
import play.libs.Json;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.List;

@NamedQueries({
        @NamedQuery(
                name = "User.findByLogin",
                query = "select x from User x where x.login = :login"
        ),
        @NamedQuery(
                name = "User.findByEmail",
                query = "select x from User x where x.email = :email"
        ),
        @NamedQuery(
                name = "User.findAdmins",
                query = "select x from Admin x order by x.id"
        ),
        @NamedQuery(
                name = "User.findClients",
                query = "select x from Client x order by x.id"
        ),
        @NamedQuery(
                name = "User.findCoordinators",
                query = "select x from Coordinator x order by x.id"
        ),
        @NamedQuery(
                name = "User.findProfessionals",
                query = "select x from Professional x order by x.id"
        ),
        @NamedQuery(
                name = "User.findTutors",
                query = "select x from Tutor x order by x.id"
        )
})
@Entity
@Inheritance
@DiscriminatorColumn(name = "type")
public abstract class User {
    public enum Gender {MALE, FEMALE, OTHER}
    public enum State {UNCONFIRMED, CONFIRMED, DELEGATED, BANNED, DELETED}

    @Id
    @SequenceGenerator(name = "user_id_seq", sequenceName = "user_id_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "user_id_seq")
    private int id;

    private String login;

    @JsonSerialize(using = UserPasswordSerializer.class)
    private String password;

    @NotNull
    private String email;

    private String name;

    private String surname1;

    private String surname2;

    @JsonFormat(shape=JsonFormat.Shape.STRING, pattern="yyyy-MM-dd")
    private Date birthdate;

    private String avatar;

    private String nationalId;

    @Enumerated(EnumType.STRING)
    @NotNull // Fake to prevent typing bug
    private Gender gender;

    @Enumerated(EnumType.STRING)
    @NotNull
    private State state = State.UNCONFIRMED;

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
        this.email = email.toLowerCase();
    }

    public boolean canLogin() {
        return (this.state == State.UNCONFIRMED || this.state == State.CONFIRMED);
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

    public String getFullName() {
        StringBuilder fullname = new StringBuilder();
        if (getName() != null) fullname.append(getName() + " ");
        if (getSurname1() != null) fullname.append(getSurname1() + " ");
        if (getSurname2() != null) fullname.append(getSurname2());
        return fullname.toString().trim();
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

    @JsonIgnore
    public boolean isConfirmed() { return this.state == State.CONFIRMED; }

    @JsonIgnore
    public boolean isBanned() {
        return this.state == State.BANNED;
    }

    @JsonIgnore
    public boolean isLocked() { return (this.state == State.BANNED || this.state == State.DELETED); }

    public void setState(State state) {
        this.state = state;
    }

    public State getState() {
        return state;
    }

    public int getBalance() { return this.balance; }

    public void setBalance(int balance) { this.balance = balance; }

    public Date getOnlineLimit() {
        return onlineLimit;
    }

    public void setOnlineLimit(Date onlineLimit) {
        this.onlineLimit = onlineLimit;
    }

    @JsonSerialize(using = UserMetaCleanerSerializer.class)
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

    public void mergeMeta(JsonNode update) {
        setMeta(JsonUtils.merge(getMeta(), update));
    }

    @Override
    public String toString() {
        String fullname = name + " " + surname1;
        if (surname2 != null) {
            fullname += " " + surname2;
        }
        return fullname;
    }

    public void merge(User updated, List<String> fields) {
        if (!fields.isEmpty()) {
            for (String field : fields) {
                switch (field) {
                    case "login": this.setLogin(updated.getLogin());  break;
                    case "email": this.setEmail(updated.getEmail());  break;
                    case "password": this.password = updated.getPassword();  break;
                    case "balance": this.setBalance(updated.getBalance());  break;
                    case "state": this.setState(updated.getState());  break;
                    case "gender": this.setGender(updated.getGender()); break;
                    case "name": this.setName(updated.getName()); break;
                    case "birthdate":  this.setBirthdate(updated.getBirthdate()); break;
                    case "surname1": this.setSurname1(updated.getSurname1()); break;
                    case "surname2": this.setSurname2(updated.getSurname2()); break;
                    case "avatar": this.setAvatar(updated.getAvatar()); break;
                    case "nationalId": this.setNationalId(updated.getNationalId()); break;
                    case "meta": this.mergeMeta(updated.getMeta()); break;
                }
            }
        }
    }

    public void setPassword(String password) {
        this.password = BCrypt.hashpw(password, BCrypt.gensalt());
    }
    public String getPassword() {
        return password;
    }

    public boolean authenticate(String password) {
        if (password == null) {
            return false;
        }
        return BCrypt.checkpw(password, this.password);
    }

    public abstract String getUserType();

}
