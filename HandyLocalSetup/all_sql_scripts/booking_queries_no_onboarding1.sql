--1-NOWDB select * from BOOKING_USER u where lower(u.email) = lower('MAHESWARAN.ELUMALAI@EXPEDITORS.COM')
--2-NOWDB select u from USER_PROFILE u where lower(u.email) = lower('MAHESWARAN.ELUMALAI@EXPEDITORS.COM')
--3-NOWDB select onboarding_status from BOOKING_USER u where lower(u.email)=lower('MAHESWARAN.ELUMALAI@EXPEDITORS.COM')

-- select * from BOOKING_USER u where lower(u.email) = lower('MAHESWARAN.ELUMALAI@EXPEDITORS.COM')
 --select * from BOOKING_USER u where lower(u.email) = lower('expoecadev01+onboard1@gmail.com')
 --select u from USER_PROFILE u where lower(u.email) = lower('expoecadev01+onboard1@gmail.com')
 
 -- call deleteapiquery('maheswaran.elum1alai@expeditors.com');
--  call deleteapiquery('mah11eswaran.elumalai@expeditors.com');
update booking_user set onboarding_status ='NOT_STARTED' where email = 'Maheswaran.Elumalai@expeditors.com'

