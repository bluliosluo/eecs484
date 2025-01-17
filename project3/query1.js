// Query 1
// Find users who live in city "city".
// Return an array of user_ids. The order does not matter.

function find_user(city, dbname) {
    db = db.getSiblingDB(dbname);

    let results = [];
    db.users.find({"hometown.city": city}).forEach( function(myDoc) { 
        results.push(myDoc.user_id); 
    } );

    return results;
}
