package com.expd.expo.booking.graphql.resolvers;

import com.expd.expo.booking.core.domain.*;
import com.expd.expo.booking.core.manager.*;
import com.expd.expo.booking.core.security.ei.UserHelper;
import com.expd.expo.booking.core.service.FavoritesService;
import com.expd.expo.booking.core.service.FavoritesServiceImpl;
import com.expd.expo.booking.core.ui.helper.BookingHelper;
import com.expd.expo.booking.core.ui.helper.BookingViewResponseFormatHelper;
import com.expd.expo.booking.core.ui.helper.UIDropDownHelper;
import com.expd.expo.booking.core.web.forms.input.V2.CreateBookingInputForm;
import com.expd.expo.booking.exceptions.ValidationError;
import com.expd.expo.booking.models.*;
import com.expd.expo.booking.models.ui.AirBookingForm;
import com.expd.expo.booking.models.ui.OceanBookingForm;
import com.expd.expo.booking.util.BookingTemplateHelper;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;


import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static com.expd.expo.booking.core.domain.BookingType.INTERNATIONAL_AIR;
import static com.expd.expo.booking.core.domain.BookingType.INTERNATIONAL_OCEAN;
import static java.lang.Long.parseLong;


@Controller
public class BookingQueryResolver {

    protected static final String BOOKING_FORM = "bookingForm"; //enow
    private static final Logger log = LoggerFactory.getLogger(BookingQueryResolver.class);

    @Autowired
    BookingHelper bookingHelper;

    @Autowired
    protected BookingViewResponseFormatHelper bookingViewResponseFormatHelper;

    @Autowired
    protected UserHelper userHelper;

    @Autowired
    private BookingManager bookingManager;

    @Autowired
    private ExternalUserManager externalUserManager;

    @Autowired
    protected BookingUserManager bookingUserManager;

    @Autowired
    BookingTemplateHelper bookingTemplateHelper;

    @Autowired
    UIDropDownHelper uiDropDownHelper;

    @Autowired
    public BookingQueryResolver() {


    }

    public BookingQueryResolver(BookingHelper bookingHelper, BookingViewResponseFormatHelper bookingViewResponseFormatHelper, UserHelper userHelper, BookingManager bookingManager, ExternalUserManager userManager) {
        this.bookingHelper = bookingHelper;
        this.bookingViewResponseFormatHelper = bookingViewResponseFormatHelper;
        this.userHelper = userHelper;
        this.bookingManager = bookingManager;
        this.externalUserManager = userManager;
    }

    @QueryMapping
    public Booking getBooking(@Argument String searchString) {
        return bookingHelper.getBooking(searchString);
    }

    @QueryMapping
    public CreateBookingInputForm getBookingAsForm( @Argument String searchString) {
        return bookingHelper.convertToForm(getBooking(searchString));
    }

    @Autowired
    private DocumentCacheManager documentCacheManager;

    @QueryMapping
    public BookingDocument getDocumentsToDownload(@Argument String publicKey)
            throws IOException {
        // find the document requested
        publicKey = pkFormatter(publicKey);
        final BookingDocument bookingDocument = documentCacheManager.findByPublicKeyAndLoadImage(publicKey);
        return bookingDocument;
    }

    @QueryMapping
    private String pkFormatter(@Argument String pk) {
        String temp = pk;
        // if the public key ends with the /document.pdf, take it off and return
        // the pk string without the postfix
        if (pk.endsWith("/document.pdf")) {
            temp = pk.substring(0, pk.length() - 13);
        }
        return temp;
    }

    @QueryMapping
    @CircuitBreaker(name = "loadair",fallbackMethod = "fallBackAir")
    public AirBookingForm loadAirBookingData(@Argument String id) {
        return (AirBookingForm) bookingHelper.loadBookingData(id, INTERNATIONAL_AIR, "");
    }

    public AirBookingForm fallBackAir(@Argument String id,Throwable exception){
        System.out.println("Mahesh in helper class When loadingairbookingData failed");
        return null;
    }

    @QueryMapping
    @CircuitBreaker(name = "loadocean",fallbackMethod = "fallbackOcean")
    public OceanBookingForm loadOceanBookingData(@Argument String id, @Argument String loadType) {
        return (OceanBookingForm) bookingHelper.loadBookingData(id, INTERNATIONAL_OCEAN, loadType);
    }

    public OceanBookingForm fallbackOcean(@Argument String id, @Argument String loadType,Throwable exception){
        System.out.println("Mahesh in helper class When loadOceanBookingData failed");
        return null;
    }

    @QueryMapping
    @CircuitBreaker(name = "findbookings",fallbackMethod = "fallbackBookings")
    public List<Booking> bookingsForUser(@Argument @NotNull String bookingUser) {
        return bookingHelper.getBookingsForUser(bookingUser);
    }

    public  List<Booking> fallbackBookings(String bookingUser,Throwable exception){
        System.out.println("Mahesh in helper class When bookingsForUser failed");
        return null;
    }

    @QueryMapping
    public Boolean findUserByEmail( @Argument String email) {
        return bookingUserManager.findUserByEmail(email);
    }

    @QueryMapping
    public List<DestinationPort> searchPort(@Argument @NotNull String portCode) {
        return uiDropDownHelper.searchPorts(portCode);
    }

    @AuthorizedRole()
    @QueryMapping
    public ExternalUserProfile bookingUserSearch(@Argument String email){
        List<ExternalUserProfile> profiles=  externalUserManager.findUserByEmail(email);
        if(profiles!=null && profiles.size()>0){
            ExternalUserProfile profile = profiles.get(0);
            return  profile;
        }
        throw new ValidationError("USER_NOT_FOUND","The User profile does not exist in the System");
    }

    @QueryMapping
    public List<BookingCustomerOrganization> getCustomerOrganizations(@Argument String email) {
        List<BookingCustomerOrganization> bookingCustomerOrganizations = new ArrayList<>();
        bookingCustomerOrganizations.addAll(bookingHelper.getCustomerOrganizations(email));
        return bookingCustomerOrganizations;
    }

    @QueryMapping
    public BookingCustomerOrganizationSummary getCustomerOrganizationSummary(@Argument String gciCode) {
        return bookingHelper.getCustomerOrganizationSummary(gciCode);
    }

    @QueryMapping
    public BookingUserStatus getBookingUserStatus(@Argument String email) {
        BookingUserStatus bookingUserStatus = bookingUserManager.getBookingUserStatusWithMeta(email);
        List<ExternalUserProfile> userProfileList =  externalUserManager.findUserByEmail(email);
        if(userProfileList.size()>0){
            ExternalUserProfile userProfile = userProfileList.get(0);
            Map<String, ExternalPermission> userProfilePermList = userProfile.getPermissions();
            ArrayList<ExternalPermissionObj> externalPermissionObjList = new ArrayList<>();
            for (Map.Entry<String, ExternalPermission> entry : userProfilePermList.entrySet()) {
                ExternalPermission userPermission= entry.getValue();
                ExternalPermissionObj obj = new ExternalPermissionObj(userPermission.getName(), userPermission.isAllowed());
                externalPermissionObjList.add(obj);
            }
            bookingUserStatus.setPermissionList(externalPermissionObjList);
        }
        return bookingUserStatus;
    }

    @AuthorizedRole()
    @QueryMapping
    public List<BookingUserStatus> getBookingUserStatusList(@Argument String email) {
        return bookingUserManager.getUserStatus(email);
    }

    @QueryMapping
    public PlatformUserStatus platformUserStatus(@Argument @NotNull String email)  {
        PlatformUserStatus status = new PlatformUserStatus();
        BookingUser user = bookingUserManager.findBookingUserByEmail(email);
        if (user != null && user.getSyncTime() == null) {
            user.setSyncTime(new Date());
        }
        try{
            if (user != null){
                bookingUserManager.merge(user);
            }
            else {
                log.info("User " + email + " is not found.");
            }
        }
        catch (Exception ex){
            log.error(ex.getMessage());
        }
        status.bookingUserStatus = this.getBookingUserStatus(email);
        return status;
    }

    @QueryMapping
    public BookingTemplate getTemplate(@Argument @NotNull String id) {
        return bookingTemplateHelper.find(parseLong(id));
    }

    @QueryMapping
    public List<BookingTemplate> searchTemplatesByName(@Argument @NotNull String searchString) {
        return bookingTemplateHelper.searchTemplatesByName(searchString);
    }

    @QueryMapping
    public CreateBookingInputForm getBookingTemplateAsForm(@Argument @NotNull String id) {
        return bookingTemplateHelper.getBookingTemplateAsForm(id);
    }

    @QueryMapping
    public List<LiteWeightBookingTemplate> getBookingTemplatesForUser(@Argument String searchString) {
        return bookingTemplateHelper.getBookingTemplatesForUser(searchString);
    }

    @QueryMapping
    public List<BookingTemplate> recentTenTemplateForUser(@Argument @NotNull String bookingUser) {
        return bookingTemplateHelper.recentTenTemplateForUser(bookingUser);
    }

    @QueryMapping
    public List<LiteWeightBookingTemplate> topTenFavoriteTemplates(@Argument String bookingUser)
    {
        return bookingTemplateHelper.topTenUsedFavoriteTemplateForUser(bookingUser);
    }

    @QueryMapping
    public List<BookingType> getUserAllowedBookingTypes()
    {
        return userHelper.getUserAllowedBookingTypes();
    }
}
