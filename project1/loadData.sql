INSERT INTO Users (user_id, first_name, last_name, year_of_birth, month_of_birth, day_of_birth, gender)
SELECT DISTINCT user_id, first_name, last_name, year_of_birth, month_of_birth, day_of_birth, gender
FROM project1.Public_User_Information; 


INSERT INTO Programs (institution, concentration, degree)
SELECT DISTINCT institution_name, program_concentration, program_degree
FROM project1.Public_User_Information
WHERE institution_name IS NOT NULL;

INSERT INTO Education (user_id, program_id, program_year)
SELECT DISTINCT P1.user_id, P2.program_id, P1.program_year
FROM project1.Public_User_Information P1, Programs P2
WHERE P1.institution_name IS NOT NULL AND P1.institution_name = P2.institution AND P1.program_concentration = P2.concentration AND P1.program_degree = P2.degree;


INSERT INTO Cities (city_name, state_name, country_name)
SELECT DISTINCT current_city, current_state, current_country
FROM project1.Public_User_Information 
UNION 
SELECT DISTINCT hometown_city, hometown_state, hometown_country
FROM project1.Public_User_Information;


INSERT INTO User_Current_Cities (user_id, current_city_id) 
SELECT DISTINCT P.user_id, C.city_id
FROM project1.Public_User_Information P, Cities C
WHERE P.current_city = C.city_name AND P.current_state = C.state_name AND P.current_country = C.country_name;


INSERT INTO User_Hometown_Cities (user_id, hometown_city_id) 
SELECT DISTINCT P.user_id, C.city_id
FROM project1.Public_User_Information P, Cities C
WHERE P.hometown_city = C.city_name AND P.hometown_state = C.state_name AND P.hometown_country = C.country_name; 


INSERT INTO Friends (user1_id, user2_id)
SELECT user1_id, user2_id
FROM project1.Public_Are_Friends;

SET AUTOCOMMIT OFF;

INSERT INTO Photos (photo_id, album_id, photo_caption, photo_created_time, photo_modified_time, photo_link)
SELECT photo_id, album_id, photo_caption, photo_created_time, photo_modified_time, photo_link
FROM project1.Public_Photo_Information;


INSERT INTO Albums (album_id, album_owner_id, album_name, album_created_time, album_modified_time, album_link, album_visibility, cover_photo_id)
SELECT DISTINCT album_id, owner_id, album_name, album_created_time, album_modified_time, album_link, album_visibility, cover_photo_id
FROM project1.Public_Photo_Information;

COMMIT;
SET AUTOCOMMIT ON;

INSERT INTO Tags (tag_photo_id, tag_subject_id, tag_created_time, tag_x, tag_y)
SELECT photo_id, tag_subject_id, tag_created_time, tag_x_coordinate, tag_y_coordinate
FROM project1.Public_Tag_Information;


INSERT INTO User_Events (event_id, event_creator_id, event_name, event_tagline, event_description, event_host, event_type, event_subtype, event_address, event_city_id, event_start_time, event_end_time)
SELECT P.event_id, P.event_creator_id, P.event_name, P.event_tagline, P.event_description, P.event_host, P.event_type, P.event_subtype, P.event_address, C.city_id, P.event_start_time, P.event_end_time
FROM project1.Public_Event_Information P, Cities C
WHERE P.event_city = C.city_name AND P.event_state = C.state_name AND P.event_country = C.country_name;
