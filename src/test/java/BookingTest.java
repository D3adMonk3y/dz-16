import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;

public class BookingTest {
    private RequestSpecification updateSpec;

    @BeforeMethod
    public void setup(){
        RestAssured.baseURI = "https://restful-booker.herokuapp.com/booking";
        RestAssured.requestSpecification = new RequestSpecBuilder()
                .addHeader("Content-Type", "application/json")
                .build();

        updateSpec = new RequestSpecBuilder()
                .addHeader("Accept", "application/json")
                .addHeader("Cookie", "token=abc123")
                .addHeader("Authorization" , "Basic YWRtaW46cGFzc3dvcmQxMjM=")
                .build();
    }

    @Test
    public void createBookingTest(){

        Date date = new Date();
        SimpleDateFormat dmyFormat = new SimpleDateFormat("yyyy-MM-dd");
        String formattedDate = dmyFormat.format(date);

        BookingDates bookingdates = BookingDates.builder()
                .checkin(formattedDate)
                .checkout(formattedDate)
                .build();

        BookingBody bookingBody = new BookingBody().builder()
                .firstname("John")
                .lastname("Dou")
                .totalprice(700)
                .depositpaid(true)
                .bookingdates(bookingdates)
                .additionalneeds("lorem ipsum")
                .build();

        RequestSpecification createSpec = new RequestSpecBuilder()
                .addHeader("Accept", "application/json")
                .build();

        Response response = RestAssured
                .given()
                .spec(createSpec)
                .body(bookingBody)
                .post();

        assertThat(response.as(BookingResponse.class).getBooking(), equalTo(bookingBody));
    }

    private BookingIdsResponse[] getBookingIds(){
        return RestAssured.get().as(BookingIdsResponse[].class);
    }

    private Integer getRandomId(){
        BookingIdsResponse[] bookingIds = getBookingIds();
        return bookingIds[new Random().nextInt(bookingIds.length)].getBookingid();
    }

    @Test
    public void getBookingIdsTest() {
        BookingIdsResponse[] bookingIds = getBookingIds();
        assertThat(bookingIds.length, greaterThan(0));
    }

    @Test
    public void partialUpdateBookingTest(){
        int newPrice = 7000;

        BookingPartialUpdateBody body = new BookingPartialUpdateBody().builder()
                .totalprice(newPrice)
                .build();

        Response response = RestAssured
                .given()
                .spec(updateSpec)
                .body(body)
                .patch("/" + getRandomId());

        assertThat(response.as(BookingBody.class).getTotalprice(), equalTo(newPrice));

    }

    @Test
    public void updateBookingTest(){
        String newName = "Ben";
        String newAdditionalNeeds = "I'd like a hotel room, please, with an extra large bed, a TV, and one of those little refrigerators you have to open with a key";
        RequestSpecification getSpec = new RequestSpecBuilder()
                .addHeader("Accept", "application/json")
                .build();

        Integer randomId = getRandomId();

        Response getResponse = RestAssured
                .given()
                .spec(getSpec)
                .get("/" + randomId);

        BookingBody body = getResponse.as(BookingBody.class);
        body.setFirstname(newName);
        body.setAdditionalneeds(newAdditionalNeeds);

        Response response = RestAssured
                .given()
                .spec(updateSpec)
                .body(body)
                .put("/" + randomId);

        BookingBody updateBody = response.as(BookingBody.class);
        assertThat(updateBody.getFirstname(), equalTo(newName));
        assertThat(updateBody.getAdditionalneeds(), equalTo(newAdditionalNeeds));

    }

    @Test
    public void deleteBookingTest(){
        int randomId = getRandomId();

        RequestSpecification deleteSpec = new RequestSpecBuilder()
                .addHeader("Cookie", "token=abc123")
                .addHeader("Authorization" , "Basic YWRtaW46cGFzc3dvcmQxMjM=")
                .build();

        RestAssured
                .given()
                .spec(deleteSpec)
                .delete("/" + randomId);

        RequestSpecification getSpec = new RequestSpecBuilder()
                .addHeader("Accept", "application/json")
                .build();

        Response response = RestAssured
                .given()
                .spec(getSpec)
                .get("/" +randomId);

        assertThat(response.getStatusCode(), equalTo(404));

    }
}
