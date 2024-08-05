// Query 5
// Find the oldest friend for each user who has a friend. For simplicity,
// use only year of birth to determine age, if there is a tie, use the
// one with smallest user_id. You may find query 2 and query 3 helpful.
// You can create selections if you want. Do not modify users collection.
// Return a javascript object : key is the user_id and the value is the oldest_friend id.
// You should return something like this (order does not matter):
// {user1:userx1, user2:userx2, user3:userx3,...}

function oldest_friend(dbname) {
    db = db.getSiblingDB(dbname);

    let results = {};
    db.users.aggregate( [ 
        { $unwind: "$friends" },
        { $project: { _id: 0, user_id: 1, friends: 1 } },
        { $out : "flat_users" } 
    ] );

    var newDocuments = [];

    db.flat_users.find().forEach(function(user) {
        var newDocument = { user_id: user.friends, friends: user.user_id };
        newDocuments.push(newDocument);
    });

    db.flat_users.insertMany(newDocuments);

    db.flat_users.aggregate( [
        {$group: {_id: "$user_id", friends: {$push: "$friends"}}},
        { $out : "final" }
     ]);

    db.final.find().forEach( function(user) {
        let oldest = 9999999;
        let oldest_id = 9999999;
        user.friends.forEach( function(friend) {
            let friend_user = db.users.findOne({user_id: friend});
            if (friend_user.YOB < oldest) {
                oldest = friend_user.YOB;
                oldest_id = friend_user.user_id; 
            }
            else if (friend_user.YOB == oldest) {
                if (friend_user.user_id < oldest_id) {
                    oldest_id = friend_user.user_id;
                }
            }

        })
        results[user._id] = oldest_id;
    });



    return results;
}
