// Test data: Users already in a test session

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
        votes: 99,
        _class: "com.rgq.votedoreact.model.User"
    },{
        _id: "rg_quiet",
        sessionId: "5eec9863b6c08928fd7435a7",
        trackId: "5UWwZ5lm5PKu6eKsHAGxOk",
        username: "rg_quiet",
        email: "robinguedel@gmail.com",
        imgUrl: "https://upload.wikimedia.org/wikipedia/commons/thumb/c/c0/Nicolas_Cage_Deauville_2013.jpg/330px-Nicolas_Cage_Deauville_2013.jpg",
        friends: [],
        votes: 0,
        _class: "com.rgq.votedoreact.model.User"
    },{
        _id: "asdf",
        sessionId: "5eec9863b6c08928fd7435a7",
        trackId: "10Nmj3JCNoMeBQ87uw5j8k",
        username: "asdf",
        email: "test@gmail.com",
        imgUrl: "https://upload.wikimedia.org/wikipedia/commons/thumb/c/c0/Nicolas_Cage_Deauville_2013.jpg/330px-Nicolas_Cage_Deauville_2013.jpg",
        friends: [],
        votes: 0,
        _class: "com.rgq.votedoreact.model.User"
    }
]);

db.sessions.insertOne({
    _id: ObjectId("5eec9863b6c08928fd7435a7"),
    name: "Test",
    open: true,
    owner: {$ref: "users", $id: "vqyrcsnd84efzir5ukl2u5jy7"},
    members: [{$ref: "users", $id: "rg_quiet"}, {$ref: "users", $id: "asdf"}],
    currentTrack: {
        trackInfos: {
            _id: "1igUMb7IYNpPtAPGgDX6Hi",
            name: "Twisted Fate",
            artist: "The Basterds",
            imgUrl: "https://i.scdn.co/image/ab67616d0000b273fef6632b70a18260c6b70ab6",
            timeMs: 217153
        },
        progressMs: 0,
        timestamp: 1596698049448
    },
    votes: [],
    _class: "com.rgq.votedoreact.model.Session"
});

db.createCollection("vqyrcsnd84efzir5ukl2u5jy7", { capped: true, size: 4, max: 10 });
db.createCollection("rg_quiet", { capped: true, size: 4, max: 10 });
