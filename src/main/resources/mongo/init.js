db = db.getSiblingDB("votedo");
db.dropDatabase();

db.users.insertMany([
    {
        _id: "vqyrcsnd84efzir5ukl2u5jy7",
        sessionId: "5eec9863b6c08928fd7435a7",
        username: "RobWob",
        email: "funadresse@gmx.ch",
        imgUrl: "https://i.scdn.co/image/ab6775700000ee8558dec5dc13a2ca415586179f",
        friends: [],
        _class: "com.rgq.votedoreact.model.User"
    },{
        _id: "rg_quiet",
        sessionId: "5eec9863b6c08928fd7435a7",
        trackId: "5UWwZ5lm5PKu6eKsHAGxOk",
        username: "rg_quiet",
        email: "robinguedel@gmail.com",
        imgUrl: "https://upload.wikimedia.org/wikipedia/commons/thumb/c/c0/Nicolas_Cage_Deauville_2013.jpg/330px-Nicolas_Cage_Deauville_2013.jpg",
        friends: [],
        _class: "com.rgq.votedoreact.model.User"
    }
]);

db.sessions.insertOne({
    _id: ObjectId("5eec9863b6c08928fd7435a7"),
    name: "Test",
    open: true,
    owner: {$ref: "users", $id: "vqyrcsnd84efzir5ukl2u5jy7"},
    members: [{$ref: "users", $id: "rg_quiet"}],
    _class: "com.rgq.votedoreact.model.Session"
});

db.createCollection("vqyrcsnd84efzir5ukl2u5jy7", { capped: true, size: 4, max: 10 });
db.createCollection("rg_quiet", { capped: true, size: 4, max: 10 });
