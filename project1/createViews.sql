/*
CREATE VIEW View_User_Information (user_id, first_name, last_name, year_of_birth, month_of_birth, day_of_birth, gender, current_city, current_state, current_country, hometown_city, hometown_state, hometown_country, institution_name, program_year, program_concentration, program_degree) AS
SELECT U.user_id, U.first_name, U.last_name, U.year_of_birth, U.month_of_birth, U.day_of_birth, U.gender, C1.city_name, C1.state_name, C1.country_name, C2.city_name, C2.state_name, C2.country_name, P.institution, E.program_year, P.concentration, P.degree
FROM Users U
INNER JOIN User_Current_Cities UCC 
ON U.user_id = UCC.user_id 
INNER JOIN Cities C1 
ON UCC.current_city_id = C1.city_id
INNER JOIN User_Hometown_Cities UHC
ON U.user_id = UHC.user_id 
INNER JOIN Cities C2 
ON UHC.hometown_city_id = C2.city_id
FULL OUTER JOIN Education E
ON E.user_id = U.user_id
INNER JOIN Programs P
ON P.program_id = E.program_id; 
*/

/*
CREATE VIEW View_User_Information (user_id, first_name, last_name, year_of_birth, month_of_birth, day_of_birth, gender, current_city, current_state, current_country, hometown_city, hometown_state, hometown_country, institution_name, program_year, program_concentration, program_degree) AS
SELECT U.user_id, U.first_name, U.last_name, U.year_of_birth, U.month_of_birth, U.day_of_birth, U.gender, C1.city_name, C1.state_name, C1.country_name, C2.city_name, C2.state_name, C2.country_name, P.institution, E.program_year, P.concentration, P.degree
FROM Education E
INNER JOIN Programs P ON E.program_id = P.program_id
RIGHT OUTER JOIN Users U ON E.user_id = U.user_id
LEFT JOIN User_Current_Cities UCC ON U.user_id = UCC.user_id
LEFT JOIN Cities C1 ON UCC.current_city_id = C1.city_id
LEFT JOIN User_hometown_Cities UHC ON U.user_id = UHC.user_id
LEFT JOIN Cities C2 ON UHC.hometown_city_id = C2.city_id; 
*/

CREATE VIEW View_User_Information (user_id, first_name, last_name, year_of_birth, month_of_birth, day_of_birth, gender, current_city, current_state, current_country, hometown_city, hometown_state, hometown_country, institution_name, program_year, program_concentration, program_degree) AS
SELECT U.user_id, U.first_name, U.last_name, U.year_of_birth, U.month_of_birth, U.day_of_birth, U.gender, C1.city_name, C1.state_name, C1.country_name, C2.city_name, C2.state_name, C2.country_name, P.institution, E.program_year, P.concentration, P.degree
FROM Users U 
LEFT JOIN Education E ON U.user_id = E.user_id 
LEFT JOIN Programs P ON E.program_id = P.program_id 
LEFT JOIN User_Current_Cities UCC ON U.user_id = UCC.user_id
LEFT JOIN Cities C1 ON UCC.current_city_id = C1.city_id
LEFT JOIN User_hometown_Cities UHC ON U.user_id = UHC.user_id
LEFT JOIN Cities C2 ON UHC.hometown_city_id = C2.city_id; 



CREATE VIEW View_Are_Friends AS 
SELECT user1_id, user2_id
FROM Friends; 



CREATE VIEW View_Photo_Information (album_id, owner_id, cover_photo_id, album_name, album_created_time, album_modified_time, album_link, album_visibility, photo_id, photo_caption, photo_created_time, photo_modified_time, photo_link) AS
SELECT A.album_id, A.album_owner_id, A.cover_photo_id, A.album_name, A.album_created_time, A.album_modified_time, A.album_link, A.album_visibility, P.photo_id, P.photo_caption, P.photo_created_time, P.photo_modified_time, P.photo_link
FROM Albums A 
INNER JOIN Photos P
ON A.album_id = P.album_id; 



CREATE VIEW View_Event_Information (event_id, event_creator_id, event_name, event_tagline, event_description, event_host, event_type, event_subtype, event_address, event_city, event_state, event_country, event_start_time, event_end_time) AS
SELECT E.event_id, E.event_creator_id, E.event_name, E.event_tagline, E.event_description, E.event_host, E.event_type, E.event_subtype, E.event_address, C.city_name, C.state_name, C.country_name, E.event_start_time, E.event_end_time
FROM User_Events E
INNER JOIN Cities C
ON E.event_city_id = C.city_id;


CREATE VIEW View_Tag_Information AS 
SELECT tag_photo_id AS photo_id, tag_subject_id, tag_created_time, tag_x AS tag_x_coordinate, tag_y AS tag_y_coordinate
FROM Tags; 


