package gl.glue.brahma.model.availability;

import gl.glue.brahma.model.user.Professional;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Date;

@Entity
public class Availability {

    @Id
    @SequenceGenerator(name="availability_type_id_seq", sequenceName="availability_type_id_seq", allocationSize=1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator="availability_type_id_seq")
    private int id;

    @ManyToOne
    @JoinColumn(name="user_id")
    @NotNull
    private Professional user;

    @NotNull
    private Date repeatStartDate;

    private Date repeatEndDate;

    @NotNull
    private Date scheduleStartTime;

    @NotNull
    private Date scheduleEndTime;

    private int repeat;


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

    public void setScheduleStartTime(Date scheduleStartTime) {
        this.scheduleStartTime = scheduleStartTime;
    }

    public Date getScheduleEndTime() {
        return scheduleEndTime;
    }

    public void setScheduleEndTime(Date scheduleEndTime) {
        this.scheduleEndTime = scheduleEndTime;
    }

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
