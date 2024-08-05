// Query 8
// Find the city average friend count per user using MapReduce.

let city_average_friendcount_mapper = function () {
    emit(this.hometown.city, {count : 1,friends : this.friends.length});
};

let city_average_friendcount_reducer = function (key, values) {
    reduceVal = { count: 0, friends: 0};
    for (let i = 0; i < values.length; i++) {
        reduceVal.friends += values[i].friends;
        reduceVal.count += values[i].count;
    };
    return reduceVal;
};

let city_average_friendcount_finalizer = function (key, reduceVal) {
    reduceVal.avg = reduceVal.friends / reduceVal.count;
    return reduceVal.avg;
};
