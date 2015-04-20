package gl.glue.brahma.model.availability;

import com.fasterxml.jackson.annotation.JsonIgnore;
import gl.glue.brahma.model.user.Professional;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Calendar;
import java.util.Date;

@NamedQueries({

        @NamedQuery(
                name = "Availability.findByUser",
                query = "select availability " +
                        "from Availability availability " +
                        "where availability.user.id = :uid " +
                        "order by availability.id"
        )

})
@Entity
public class Availability {

    @Id
    @SequenceGenerator(name = "availability_id_seq", sequenceName = "availability_id_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "availability_id_seq")
    private int id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    @NotNull
    @JsonIgnore
    private Professional user;

    @NotNull
    private Date repeatStartDate;

    private Date repeatEndDate;

    @NotNull
    private Date scheduleStartTime;

    @NotNull
    private Date scheduleEndTime;

    private int repeat;

    public Availability() {
        setRepeatStartDate(new Date());
    }

    public Availability(Professional user) {
        setRepeatStartDate(new Date());
        this.user = user;
    }

    public int getId() {
        return id;
    }

    public Professional getUser() {
        return user;
    }

    public void setUser(Professional user) {
        this.user = user;
    }

    public Date getRepeatStartDate() {
        return repeatStartDate;
    }

    public void setRepeatStartDate(Date repeatStartDate) {
        this.repeatStartDate = repeatStartDate;
    }

    public Date getRepeatEndDate() {
        return repeatEndDate;
    }

    public void setRepeatEndDate(Date repeatEndDate) {
        this.repeatEndDate = repeatEndDate;
    }

    public Date getScheduleStartTime() {
        return scheduleStartTime;
    }

    public void setScheduleStartTime(int hours, int minutes) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR, hours);
        calendar.set(Calendar.MINUTE, minutes);
        this.scheduleStartTime = calendar.getTime();
    }

    public Date getScheduleEndTime() {
        return scheduleEndTime;
    }

    public void setScheduleEndTime(int hours, int minutes) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR, hours);
        calendar.set(Calendar.MINUTE, minutes);
        this.scheduleEndTime = calendar.getTime();
    }

    // The repeat field is a bitmap in which the 7 lower bits correspond to each day of the week (starting with Mon).
    public int getRepeat() {
        return repeat;
    }

    public void setRepeat(int repeat) {
        this.repeat = repeat;
    }


    @Override
    public String toString() {
        return "Availability #" + id;
    }

}
