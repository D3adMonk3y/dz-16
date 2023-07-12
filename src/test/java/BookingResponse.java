import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder

@JsonIgnoreProperties(ignoreUnknown = true)
public class BookingResponse {
    private Integer bookingid;
    private BookingBody booking;

}
