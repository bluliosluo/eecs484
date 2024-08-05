// Query 6
// Find the average friend count per user.
// Return a decimal value as the average user friend count of all users in the users collection.

function find_average_friendcount(dbname) {
    db = db.getSiblingDB(dbname);

    db.users.aggregate( [
        { $group: { _id: '$user_id', count: { $sum: { $size: '$friends' } } } },
        { $group: { _id: '$user_id', avg: { $avg: '$count' } } },
        { $project: { _id: 0, avg: 1 } },
        { $out : "final" }
    ] );

    return db.final.findOne().avg;
}
