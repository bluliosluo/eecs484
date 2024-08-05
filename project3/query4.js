// Query 4
// Find user pairs (A,B) that meet the following constraints:
// i) user A is male and user B is female
// ii) their Year_Of_Birth difference is less than year_diff
// iii) user A and B are not friends
// iv) user A and B are from the same hometown city
// The following is the schema for output pairs:
// [
//      [user_id1, user_id2],
//      [user_id1, user_id3],
//      [user_id4, user_id2],
//      ...
//  ]
// user_id is the field from the users collection. Do not use the _id field in users.
// Return an array of arrays.

function suggest_friends(year_diff, dbname) {
  db = db.getSiblingDB(dbname);

  let pairs = [];
  // TODO: implement suggest friends


  db.users.find({"gender": "female"}).forEach(function(myDoc) {
      db.users.find({
          "gender": "male",      
          // "YOB" : {"$gt" : {$subtract: ["$myDoc.YOB", "$year_diff"]}}, 
          // "YOB" : {"$lt" : {$add: [ "$myDoc.YOB", "$year_diff" ]}}, 
          "YOB": {"$gt": myDoc.YOB - year_diff, "$lt": myDoc.YOB + year_diff},
          "hometown.city": myDoc.hometown.city }
          ).forEach(function(myDoc1) {
              if (myDoc.user_id < myDoc1.user_id) {
                  if (myDoc.friends.indexOf(myDoc1.user_id) < 0) {
                      pairs.push([myDoc.user_id, myDoc1.user_id]); 
                  }
              } else {
                  if (myDoc1.friends.indexOf(myDoc.user_id) < 0) {
                      pairs.push([myDoc1.user_id, myDoc.user_id]); 
                  }
              }
          }   
          )
  } );

  return pairs;
}
