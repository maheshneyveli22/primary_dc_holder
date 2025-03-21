--SELECT  BOOKING.ID, BOOKING.ONHAND_NUMBER, BOOKING.BOOKING_NUMBER, BOOKING.STATUS, BOOKING.TYPE from Booking;
--select * from Booking_organization;
--SELECT  BOOKING.ID, BOOKING.ONHAND_NUMBER, BOOKING.BOOKING_NUMBER, BOOKING.STATUS, BOOKING.TYPE, BOOKING_ORGANIZATION.ORG_NAME from  BOOKING AS BOOKING, BOOKING_ORGANIZATION 

--SELECT  booking.ID, booking.ONHAND_NUMBER, booking.BOOKING_NUMBER, booking.STATUS, booking.TYPE, bookOrg.ORG_NAME from  BOOKING AS booking, BOOKING_ORGANIZATION  as bookOrg

--select * from booking;
-- select * from user_profile
-- select * from contact
--select * from BOOKING_REFERENCE;
--select * from BOOKING_ORGANIZATION;

--SELECT  book.ID, book.ONHAND_NUMBER, book.BOOKING_NUMBER, book.STATUS, book.TYPE from BOOKING as book;

--select * from booking_organization where id in (601,1401);
--select * from booking

--select customer_id, consignee_id,shipper_id from booking
--select * from booking_organization where id in (601)

--SELECT  book.ID, book.ONHAND_NUMBER, book.BOOKING_NUMBER, book.STATUS, book.TYPE, bookingorg.org_name as orggNAME from BOOKING as book  inner join  booking_organization as bookingorg on book.shipper_id = bookingorg.id ;

--Booking query 
/*SELECT  book.ID, book.ONHAND_NUMBER, book.BOOKING_NUMBER, book.STATUS, book.TYPE,
 bookingorg1.org_name as shipperOrgName ,  bookingorg2.org_name as consArgName,bookingorg3.org_name as custName
from BOOKING as book 
inner join  booking_organization as bookingorg1
on (book.shipper_id = bookingorg1.id )  
inner join  booking_organization as bookingorg2
on (book.CONSIGNEE_ID=bookingorg2.id)  
inner join  booking_organization as bookingorg3
on (book.CUSTOMER_ID=bookingorg3.id)  */ 

/*SELECT  book.last_modification_time,book.LAST_MODIFICATION_TZ, book.ID, book.ONHAND_NUMBER, book.BOOKING_NUMBER, book.STATUS, book.TYPE,
 bookingorg1.org_name as shipperOrgName ,  bookingorg2.org_name as consArgName,bookingorg3.org_name as custName, shipperRef.REFERENCE_NUMBER as shipperRefNo, consRef.REFERENCE_NUMBER as consRefNo
from BOOKING as book 
left outer  join  booking_organization as bookingorg1
on (book.shipper_id = bookingorg1.id )  
left outer  join  booking_organization as bookingorg2
on (book.CONSIGNEE_ID=bookingorg2.id)  
left outer  join  booking_organization as bookingorg3
on (book.CUSTOMER_ID=bookingorg3.id)  
left outer join  BOOKING_REFERENCE as shipperRef
on (book.SHIPPER_REFERENCE_ID=shipperRef.id)  
left outer join  BOOKING_REFERENCE as consRef
on (book.CONSIGNEE_REFERENCE_ID=consRef.id) */
--select * from USER_PROFILE  u where lower(u.email) ='maheswaran.elumalai@expeditors.com';
/*select custGciOrg.gci_code,custGciOrg.customer_name,custGciOrg.active from USER_PROFILE as userProfile
 inner join user_cust_organization t2 
 on userProfile.id=t2.EXTERNAL_USER_ID
inner join customer_gci_organization custGciOrg on t2.CUST_GCI_ORG=custGciOrg.id where userProfile.email
 like'%mahe%';*/
 
 
 USER_PROFILE.id
 user_cust_organization.EXTERNAL_USER_ID

 